/* Qurium - 2026 */
package org.qurium.schema;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.MockAiSqlService;

@QuarkusTest
class StandaloneSchemaResourceTest {

    private static final String SCHEMA_PATH = "/api/schema";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";
    private static final String SAMPLE_SCHEMA_JSON =
            "[{\"table\":\"users\",\"columns\":[{\"name\":\"id\",\"type\":\"UUID\"}]}]";

    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() {
        MockAiSqlService.reset();
        entityManager.createNativeQuery("DELETE FROM nl_query").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM uploaded_file").executeUpdate();
    }

    @Test
    void getSchema_existingStandaloneSchema_returnsSchema() {
        UUID schemaId = insertStandaloneSchema();

        given().when()
                .get(SCHEMA_PATH + "/" + schemaId)
                .then()
                .statusCode(200)
                .body("id", equalTo(schemaId.toString()))
                .body("connectionId", nullValue())
                .body("uploadedFileId", notNullValue())
                .body("source", equalTo("UPLOADED_DDL"))
                .body("schemaJson", notNullValue());
    }

    @Test
    void getSchema_nonexistentId_returns404() {
        given().when()
                .get(SCHEMA_PATH + "/" + NONEXISTENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    @Test
    void querySchema_withValidSchema_returnsSqlOnly() {
        UUID schemaId = insertStandaloneSchema();
        when(MockAiSqlService.instance().generateSql(any(), any()))
                .thenReturn(new AiSqlResponse("SELECT * FROM users", "Returns all users", null));

        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(SCHEMA_PATH + "/" + schemaId + "/query")
                .then()
                .statusCode(200)
                .body("sql", equalTo("SELECT * FROM users"))
                .body("explanation", equalTo("Returns all users"))
                .body("executed", equalTo(false))
                .body("resultSnapshot", nullValue());
    }

    @Test
    void querySchema_nonexistentSchema_returns404() {
        given().contentType(MediaType.APPLICATION_JSON)
                .body(
                        """
                        { "question": "Show me all users" }
                        """)
                .when()
                .post(SCHEMA_PATH + "/" + NONEXISTENT_ID + "/query")
                .then()
                .statusCode(404)
                .body("code", equalTo(3001));
    }

    @Transactional
    UUID insertStandaloneSchema() {
        UUID uploadedFileId = UUID.randomUUID();
        UUID schemaId = UUID.randomUUID();
        entityManager
                .createNativeQuery(
                        "INSERT INTO uploaded_file (id, name, created_at)"
                                + " VALUES (?1, ?2, NOW())")
                .setParameter(1, uploadedFileId)
                .setParameter(2, "test.sql")
                .executeUpdate();
        entityManager
                .createNativeQuery(
                        "INSERT INTO schema (id, uploaded_file_id, schema_json, source, created_at)"
                                + " VALUES (?1, ?2, ?3, 'UPLOADED_DDL', NOW())")
                .setParameter(1, schemaId)
                .setParameter(2, uploadedFileId)
                .setParameter(3, SAMPLE_SCHEMA_JSON)
                .executeUpdate();
        return schemaId;
    }
}
