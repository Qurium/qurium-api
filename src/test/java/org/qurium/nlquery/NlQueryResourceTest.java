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
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.MockAiSqlService;

@QuarkusTest
class NlQueryResourceTest {

    private static final String CONNECTIONS_PATH = "/api/connections";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";

    @InjectMock DatabaseConnectionFactory connectionFactory;
    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() throws Exception {
        MockAiSqlService.reset();
        when(connectionFactory.open(any())).thenReturn(Mockito.mock(Connection.class));
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM database_connection").executeUpdate();
    }

    @Test
    void executeQuery_withSchema_returnsSqlAndExplanation() {
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
                .body("explanation", equalTo("Returns all users"));
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
    void executeQuery_aiServiceFails_returns500() {
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
                .statusCode(500);
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
        return CONNECTIONS_PATH + "/" + connectionId + "/query";
    }

    private String createConnection() {
        return given().contentType(MediaType.APPLICATION_JSON)
                .body(VALID_CONNECTION_BODY)
                .when()
                .post(CONNECTIONS_PATH)
                .then()
                .statusCode(200)
                .extract()
                .asString()
                .replace("\"", "");
    }

    private String createConnectionWithSchema() {
        String connectionId = createConnection();
        given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", ddlTestFile())
                .when()
                .post(CONNECTIONS_PATH + "/" + connectionId + "/schema")
                .then()
                .statusCode(200);
        return connectionId;
    }

    private File ddlTestFile() {
        URL resource = getClass().getClassLoader().getResource("uploadDDLTestFile.sql");
        if (resource == null) throw new IllegalStateException("uploadDDLTestFile.sql not found");
        return new File(resource.getFile());
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
