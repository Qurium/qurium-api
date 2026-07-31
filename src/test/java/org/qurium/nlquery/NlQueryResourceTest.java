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

    private String queryPath(String connectionId) {
        return API_PATH + "/" + connectionId + "/query";
    }

    private String createConnection() {
        return given().contentType(MediaType.APPLICATION_JSON)
                .body(VALID_CONNECTION_BODY)
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
}
