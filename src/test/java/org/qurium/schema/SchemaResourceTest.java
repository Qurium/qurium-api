/* Qurium - 2026 */
package org.qurium.schema;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;
import org.qurium.connection.DatabaseConnectionFactory;

@QuarkusTest
class SchemaResourceTest {

    private static final String CONNECTIONS_PATH = "/api/connections";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";
    private static final String UUID_PATTERN =
            "\"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"";

    @InjectMock DatabaseConnectionFactory connectionFactory;
    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() {
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM database_connection").executeUpdate();
    }

    @Test
    void introspect_validConnection_returnsSchemaId() throws Exception {
        String connectionId = createConnection();
        mockSuccessfulIntrospection();

        given().when()
                .post(schemaPath(connectionId) + "/introspect")
                .then()
                .statusCode(200)
                .body(matchesPattern(UUID_PATTERN));
    }

    @Test
    void introspect_nonexistentConnection_returns404() {
        given().when()
                .post(schemaPath(NONEXISTENT_ID) + "/introspect")
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
    }

    @Test
    void introspect_connectionUnreachable_returns400() {
        String connectionId = createConnection();

        when(connectionFactory.open(any(org.qurium.connection.domain.DatabaseConnection.class)))
                .thenThrow(
                        new QuriumException(QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE));

        given().when()
                .post(schemaPath(connectionId) + "/introspect")
                .then()
                .statusCode(400)
                .body("code", equalTo(3002));
    }

    @Test
    void introspect_replacesExistingSchema() throws Exception {
        String connectionId = createConnection();
        mockSuccessfulIntrospection();

        given().when().post(schemaPath(connectionId) + "/introspect").then().statusCode(200);
        given().when().post(schemaPath(connectionId) + "/introspect").then().statusCode(200);

        given().when()
                .get(schemaPath(connectionId))
                .then()
                .statusCode(200)
                .body("source", equalTo("CONNECTED"));
    }

    @Test
    void getSchema_afterIntrospect_returnsSchema() throws Exception {
        String connectionId = createConnection();
        mockSuccessfulIntrospection();

        given().when().post(schemaPath(connectionId) + "/introspect").then().statusCode(200);

        given().when()
                .get(schemaPath(connectionId))
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("connectionId", equalTo(connectionId))
                .body("schemaJson", notNullValue())
                .body("source", equalTo("CONNECTED"))
                .body("createdAt", notNullValue());
    }

    @Test
    void getSchema_noSchemaImported_returns404() {
        String connectionId = createConnection();

        given().when()
                .get(schemaPath(connectionId))
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    @Test
    void getSchema_nonexistentConnection_returns404() {
        given().when()
                .get(schemaPath(NONEXISTENT_ID))
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String schemaPath(String connectionId) {
        return CONNECTIONS_PATH + "/" + connectionId + "/schema";
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

    private void mockSuccessfulIntrospection() throws Exception {
        ResultSet tablesRs = mockEmptyResultSet();
        DatabaseMetaData metaData = org.mockito.Mockito.mock(DatabaseMetaData.class);
        when(metaData.getTables(any(), any(), any(), any())).thenReturn(tablesRs);

        Connection jdbc = org.mockito.Mockito.mock(Connection.class);
        when(jdbc.getMetaData()).thenReturn(metaData);

        when(connectionFactory.open(any(org.qurium.connection.domain.DatabaseConnection.class)))
                .thenReturn(jdbc);
    }

    private ResultSet mockEmptyResultSet() throws Exception {
        ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        return rs;
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
