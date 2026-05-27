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
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.MockAiSqlService;

@QuarkusTest
class StandaloneSchemaResourceTest {

    private static final String SCHEMA_PATH = "/api/schema";
    private static final String UUID_PATTERN =
            "\"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";

    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() {
        MockAiSqlService.reset();
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
    }

    @Test
    void uploadDDL_validFile_returnsSchemaId() {
        given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", ddlTestFile())
                .when()
                .post(SCHEMA_PATH)
                .then()
                .statusCode(200)
                .body(matchesPattern(UUID_PATTERN));
    }

    @Test
    void uploadDDL_invalidFile_returns400() {
        given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", invalidDdlTestFile())
                .when()
                .post(SCHEMA_PATH)
                .then()
                .statusCode(400)
                .body("code", equalTo(3003));
    }

    @Test
    void getSchema_afterUpload_returnsSchema() {
        String schemaId = uploadDDL();

        given().when()
                .get(SCHEMA_PATH + "/" + schemaId)
                .then()
                .statusCode(200)
                .body("id", equalTo(schemaId))
                .body("connectionId", nullValue())
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
        String schemaId = uploadDDL();
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

    private String uploadDDL() {
        return given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", ddlTestFile())
                .when()
                .post(SCHEMA_PATH)
                .then()
                .statusCode(200)
                .extract()
                .asString()
                .replace("\"", "");
    }

    private File ddlTestFile() {
        URL resource = getClass().getClassLoader().getResource("uploadDDLTestFile.sql");
        if (resource == null) throw new IllegalStateException("uploadDDLTestFile.sql not found");
        return new File(resource.getFile());
    }

    private File invalidDdlTestFile() {
        URL resource = getClass().getClassLoader().getResource("invalidDDLTestFile.sql");
        if (resource == null) throw new IllegalStateException("invalidDDLTestFile.sql not found");
        return new File(resource.getFile());
    }
}
