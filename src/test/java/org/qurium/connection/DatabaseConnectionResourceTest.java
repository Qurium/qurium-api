/* Qurium - 2026 */
package org.qurium.connection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DatabaseConnectionResourceTest {

    private static final String BASE_PATH = "/api/connections";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";
    private static final String UUID_PATTERN =
            "\"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"";

    private static final String VALID_CONNECTION_BODY =
            """
            {
                "name": "Test Connection",
                "type": "POSTGRES",
                "host": "localhost",
                "port": 5432,
                "databaseName": "testdb"
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

    // GET /api/connections

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

    // GET /api/connections/{id}

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
                .body("host", equalTo("localhost"))
                .body("port", equalTo(5432))
                .body("databaseName", equalTo("testdb"));
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
    void createConnection_withCredentials_returnsUuid() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        {
                            "name": "Connection With Credentials",
                            "type": "MYSQL",
                            "host": "localhost",
                            "port": 3306,
                            "databaseName": "mydb",
                            "username": "admin",
                            "password": "secret"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(200)
                .body(matchesPattern(UUID_PATTERN));
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

    // DELETE /api/connections/{id}

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
}
