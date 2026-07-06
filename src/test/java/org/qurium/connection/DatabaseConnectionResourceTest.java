/* Qurium - 2026 */
package org.qurium.connection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;

@QuarkusTest
class DatabaseConnectionResourceTest {

    private static final String BASE_PATH = "/api/connections";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";
    private static final String UUID_PATTERN =
            "\"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"";

    @InjectMock DatabaseConnectionFactory connectionFactory;
    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() {
        entityManager.createNativeQuery("DELETE FROM database_connection").executeUpdate();
    }

    @Test
    void listConnections_returnsPagedResult() {
        given().queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("page", equalTo(0))
                .body("size", equalTo(10));
    }

    @Test
    void listConnections_reachableConnection_returnsIsConnectedTrueAndNullTableCount() {
        createConnection();

        given().queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("content[0].isConnected", equalTo(true))
                .body("content[0].tableCount", nullValue());
    }

    @Test
    void listConnections_unreachableConnection_returnsIsConnectedFalse() {
        createConnection();

        when(connectionFactory.open(any()))
                .thenThrow(new QuriumException(QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE));

        given().queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("content[0].isConnected", equalTo(false));
    }

    @Test
    void listConnections_doesNotReturnDeletedConnections() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().queryParam("page", 0)
                .queryParam("size", 100)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("content.id", not(hasItem(id)));
    }

    @Test
    void listConnections_sizeTooLarge_returns400() {
        given().queryParam("page", 0)
                .queryParam("size", 101)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void listConnections_sizeZero_returns400() {
        given().queryParam("page", 0)
                .queryParam("size", 0)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void getConnection_existingId_returnsConnection() {
        String id = createConnection();

        given().when()
                .get(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo("Test Connection"))
                .body("type", equalTo("POSTGRES"))
                .body("host", equalTo("192.168.1.100"))
                .body("port", equalTo(5432))
                .body("databaseName", equalTo("mydb"))
                .body("isConnected", equalTo(true))
                .body("tableCount", nullValue());
    }

    @Test
    void getConnection_unreachable_returnsIsConnectedFalse() {
        String id = createConnection();

        when(connectionFactory.open(org.mockito.ArgumentMatchers.<org.qurium.connection.domain.DatabaseConnection>any()))
                .thenThrow(new QuriumException(QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE));

        given().when()
                .get(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body("isConnected", equalTo(false));
    }

    @Test
    void getConnection_deletedId_returns404() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().when().get(BASE_PATH + "/" + id).then().statusCode(404).body("code", equalTo(2001));
    }

    @Test
    void getConnection_nonexistentId_returns404() {
        given().when()
                .get(BASE_PATH + "/" + NONEXISTENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
    }

    // POST /api/connections

    @Test
    void createConnection_validRequest_returnsUuid() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(VALID_CONNECTION_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(200)
                .body(matchesPattern(UUID_PATTERN));
    }

    @Test
    void createConnection_duplicateTarget_returns409() {
        createConnection();

        given().contentType(MediaType.APPLICATION_JSON)
                .body(VALID_CONNECTION_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("code", equalTo(2003));
    }

    @Test
    void createConnection_sameTargetDifferentName_returns409() {
        createConnection();

        String differentName = VALID_CONNECTION_BODY.replace("Test Connection", "Other Name");

        given().contentType(MediaType.APPLICATION_JSON)
                .body(differentName)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("code", equalTo(2003));
    }

    @Test
    void createConnection_unreachableHost_returns400() {
        when(connectionFactory.open(any(), any(), any(), any(), any(), any()))
                .thenThrow(
                        new QuriumException(QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE));

        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "name": "Unreachable Connection",
                            "type": "POSTGRES",
                            "host": "192.168.1.200",
                            "port": 5432,
                            "databaseName": "ghost",
                            "username": "user",
                            "password": "pass"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("code", equalTo(2002));
    }

    @Test
    void createConnection_missingName_returns400() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "type": "POSTGRES",
                            "host": "localhost",
                            "port": 5432,
                            "databaseName": "newdb"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void createConnection_invalidHost_returns400() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "name": "Bad Host Connection",
                            "type": "POSTGRES",
                            "host": "invalid-hostname.example.com",
                            "port": 5432,
                            "databaseName": "newdb"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void createConnection_onlyUsernameWithoutPassword_returns400() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "name": "Partial Credentials",
                            "type": "POSTGRES",
                            "host": "localhost",
                            "port": 5432,
                            "databaseName": "newdb",
                            "username": "admin"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void createConnection_onlyPasswordWithoutUsername_returns400() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "name": "Partial Credentials",
                            "type": "POSTGRES",
                            "host": "localhost",
                            "port": 5432,
                            "databaseName": "newdb",
                            "password": "secret"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void deleteConnection_existingId_returns204() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);
    }

    @Test
    void deleteConnection_softDeleted_connectionNoLongerRetrievable() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().when().get(BASE_PATH + "/" + id).then().statusCode(404).body("code", equalTo(2001));
    }

    @Test
    void deleteConnection_nonexistentId_returns404() {
        given().when()
                .delete(BASE_PATH + "/" + NONEXISTENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
    }

    @Test
    void deleteConnection_alreadyDeletedId_returns404() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().when()
                .delete(BASE_PATH + "/" + id)
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
    }

    @Test
    void testConnection_success_returns200() {
        String id = createConnection();

        given().when()
                .post(BASE_PATH + "/" + id + "/test")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Connection established"))
                .body("latencyMs", greaterThanOrEqualTo(0));
    }

    @Test
    void testConnection_unreachable_returns200WithFailure() {
        String id = createConnection();

        when(connectionFactory.open(org.mockito.ArgumentMatchers.<org.qurium.connection.domain.DatabaseConnection>any()))
                .thenThrow(
                        new QuriumException(QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE));

        given().when()
                .post(BASE_PATH + "/" + id + "/test")
                .then()
                .statusCode(200)
                .body("success", equalTo(false))
                .body("message", notNullValue())
                .body("latencyMs", greaterThanOrEqualTo(0));
    }

    @Test
    void testConnection_nonexistentId_returns404() {
        given().when()
                .post(BASE_PATH + "/" + NONEXISTENT_ID + "/test")
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
    }

    @Test
    void testConnection_deletedId_returns404() {
        String id = createConnection();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().when()
                .post(BASE_PATH + "/" + id + "/test")
                .then()
                .statusCode(404)
                .body("code", equalTo(2001));
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

    private String createConnection() {
        return given().contentType(MediaType.APPLICATION_JSON)
                .body(VALID_CONNECTION_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(200)
                .extract()
                .asString()
                .replace("\"", "");
    }
}
