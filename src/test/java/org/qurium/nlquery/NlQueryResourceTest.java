/* Qurium - 2026 */
package org.qurium.nlquery;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.MockAiSqlService;

@QuarkusTest
class NlQueryResourceTest {

    private static final String API_PATH = "/api";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";

    @InjectMock DatabaseConnectionFactory connectionFactory;
    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() throws Exception {
        MockAiSqlService.reset();
        when(connectionFactory.open(any())).thenReturn(Mockito.mock(Connection.class));
        entityManager.createNativeQuery("DELETE FROM nl_query").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM uploaded_file").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM database_connection").executeUpdate();
    }

    @Test
    void executeQuery_withSchema_returnsSqlAndExplanation() throws Exception {
        String connectionId = createConnectionWithSchema();
        when(MockAiSqlService.instance().generateSql(any(), any()))
                .thenReturn(new AiSqlResponse("SELECT * FROM users", "Returns all users"));

        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(queryPath(connectionId))
                .then()
                .statusCode(200)
                .body("sql", equalTo("SELECT * FROM users"))
                .body("explanation", equalTo("Returns all users"))
                .body("executed", equalTo(true))
                .body("resultSnapshot", nullValue());
    }

    @Test
    void executeQuery_noSchemaImported_returns404() {
        String connectionId = createConnection();

        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(queryPath(connectionId))
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    @Test
    void executeQuery_nonexistentConnection_returns404() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(queryPath(NONEXISTENT_ID))
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    @Test
    void executeQuery_aiServiceFails_returns500() throws Exception {
        String connectionId = createConnectionWithSchema();
        when(MockAiSqlService.instance().generateSql(any(), any()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(queryPath(connectionId))
                .then()
                .statusCode(500)
                .body("code", equalTo(4001));
    }

    @Test
    void executeQuery_missingQuestion_returns400() {
        String connectionId = createConnection();

        given().contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .when()
                .post(queryPath(connectionId))
                .then()
                .statusCode(400);
    }

    // --- History endpoint tests ---

    @Test
    void getHistory_noQueriesExist_returnsEmptyPage() {
        String connectionId = createConnection();

        given().when()
                .get(historyPath(connectionId) + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content", hasSize(0))
                .body("totalElements", equalTo(0))
                .body("totalPages", equalTo(1))
                .body("empty", equalTo(true));
    }

    @Test
    void getHistory_withQueries_returnsResultsOrderedByDateDesc() {
        String connectionId = createConnection();
        insertNlQuery(UUID.fromString(connectionId), "oldest question", "2026-01-01");
        insertNlQuery(UUID.fromString(connectionId), "newest question", "2026-06-01");

        given().when()
                .get(historyPath(connectionId) + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(2))
                .body("content[0].question", equalTo("newest question"))
                .body("content[1].question", equalTo("oldest question"));
    }

    @Test
    void getHistory_withQueries_returnsExpectedFields() {
        String connectionId = createConnection();
        insertNlQuery(UUID.fromString(connectionId), "How many users?", "2026-06-01");

        given().when()
                .get(historyPath(connectionId) + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content[0].question", equalTo("How many users?"))
                .body("content[0].status", equalTo("SUCCESS"))
                .body("content[0].executedAt", notNullValue());
    }

    @Test
    void getHistory_pagination_returnsCorrectPage() {
        String connectionId = createConnection();
        for (int i = 1; i <= 3; i++) {
            insertNlQuery(UUID.fromString(connectionId), "question " + i, "2026-0" + i + "-01");
        }

        given().when()
                .get(historyPath(connectionId) + "?page=0&size=2")
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("first", equalTo(true))
                .body("last", equalTo(false));

        given().when()
                .get(historyPath(connectionId) + "?page=1&size=2")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("first", equalTo(false))
                .body("last", equalTo(true));
    }

    @Test
    void getHistory_nonexistentOwner_returnsEmptyPage() {
        given().when()
                .get(historyPath(NONEXISTENT_ID) + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content", hasSize(0))
                .body("totalElements", equalTo(0));
    }

    @Test
    void getHistory_isolatedByOwner_doesNotReturnOtherOwnersHistory() {
        String connectionIdA = createConnection(VALID_CONNECTION_BODY);
        String connectionIdB = createConnection(VALID_CONNECTION_BODY_ALT);
        insertNlQuery(UUID.fromString(connectionIdA), "question A", "2026-06-01");

        given().when()
                .get(historyPath(connectionIdB) + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    @Transactional
    void insertNlQuery(UUID connectionId, String question, String createdAt) {
        entityManager
                .createNativeQuery(
                        "INSERT INTO nl_query (id, connection_id, question, status, created_at)"
                                + " VALUES (?1, ?2, ?3, 'SUCCESS', CAST(?4 AS TIMESTAMP))")
                .setParameter(1, UUID.randomUUID())
                .setParameter(2, connectionId)
                .setParameter(3, question)
                .setParameter(4, createdAt)
                .executeUpdate();
    }

    private String historyPath(String ownerId) {
        return API_PATH + "/" + ownerId + "/query/history";
    }

    private String queryPath(String connectionId) {
        return API_PATH + "/" + connectionId + "/query";
    }

    private String createConnection() {
        return createConnection(VALID_CONNECTION_BODY);
    }

    private String createConnection(String body) {
        return given().contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when()
                .post(API_PATH + "/connections")
                .then()
                .statusCode(200)
                .extract()
                .asString()
                .replace("\"", "");
    }

    private String createConnectionWithSchema() throws Exception {
        String connectionId = createConnection();
        mockSuccessfulIntrospection();
        given().when().post("/api/" + connectionId + "/schema/introspect").then().statusCode(200);
        return connectionId;
    }

    private void mockSuccessfulIntrospection() throws Exception {
        ResultSet introspectRs = Mockito.mock(ResultSet.class);
        when(introspectRs.next()).thenReturn(false);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        when(metaData.getTables(any(), any(), any(), any())).thenReturn(introspectRs);

        ResultSetMetaData queryMeta = Mockito.mock(ResultSetMetaData.class);
        when(queryMeta.getColumnCount()).thenReturn(0);
        ResultSet queryRs = Mockito.mock(ResultSet.class);
        when(queryRs.next()).thenReturn(false);
        when(queryRs.getMetaData()).thenReturn(queryMeta);
        Statement stmt = Mockito.mock(Statement.class);
        when(stmt.executeQuery(any())).thenReturn(queryRs);

        Connection jdbc = Mockito.mock(Connection.class);
        when(jdbc.getMetaData()).thenReturn(metaData);
        when(jdbc.createStatement()).thenReturn(stmt);

        when(connectionFactory.open(any(org.qurium.connection.domain.DatabaseConnection.class)))
                .thenReturn(jdbc);
    }

    private static final String VALID_CONNECTION_BODY =
            """
            {
                "name": "Test Connection",
                "type": "POSTGRES",
                "host": "192.168.1.100",
                "port": 5432,
                "databaseName": "mydb",
                "username": "admin",
                "password": "secret"
            }
            """;

    private static final String VALID_CONNECTION_BODY_ALT =
            """
            {
                "name": "Test Connection B",
                "type": "POSTGRES",
                "host": "192.168.1.101",
                "port": 5432,
                "databaseName": "mydb",
                "username": "admin",
                "password": "secret"
            }
            """;
}
