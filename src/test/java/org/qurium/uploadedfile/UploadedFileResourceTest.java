/* Qurium - 2026 */
package org.qurium.uploadedfile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UploadedFileResourceTest {

    private static final String BASE_PATH = "/api/uploaded-files";
    private static final String UUID_PATTERN =
            "\"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\"";
    private static final String NONEXISTENT_ID = "00000000-0000-0000-0000-000000000099";

    @Inject EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanUp() {
        entityManager.createNativeQuery("DELETE FROM nl_query").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM schema").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM uploaded_file").executeUpdate();
    }

    @Test
    void uploadFile_validFile_returnsUploadedFileId() {
        given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", ddlTestFile())
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(200)
                .body(matchesPattern(UUID_PATTERN));
    }

    @Test
    void uploadFile_invalidFile_returns400() {
        given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", invalidDdlTestFile())
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("code", equalTo(3003));
    }

    @Test
    void listUploadedFiles_returnsPagedResult() {
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
    void listUploadedFiles_afterUpload_returnsFileWithTableCount() {
        uploadDDL();

        given().queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("content[0].name", equalTo("uploadDDLTestFile.sql"))
                .body("content[0].tableCount", greaterThan(0));
    }

    @Test
    void listUploadedFiles_sizeTooLarge_returns400() {
        given().queryParam("page", 0)
                .queryParam("size", 101)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void getUploadedFile_afterUpload_returnsFile() {
        String id = uploadDDL();

        given().when()
                .get(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo("uploadDDLTestFile.sql"))
                .body("tableCount", greaterThan(0));
    }

    @Test
    void getUploadedFile_nonexistentId_returns404() {
        given().when()
                .get(BASE_PATH + "/" + NONEXISTENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo(5001));
    }

    @Test
    void deleteUploadedFile_existingId_returns204() {
        String id = uploadDDL();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);
    }

    @Test
    void deleteUploadedFile_softDeleted_notRetrievable() {
        String id = uploadDDL();

        given().when().delete(BASE_PATH + "/" + id).then().statusCode(204);

        given().when().get(BASE_PATH + "/" + id).then().statusCode(404).body("code", equalTo(5001));
    }

    @Test
    void deleteUploadedFile_nonexistentId_returns404() {
        given().when()
                .delete(BASE_PATH + "/" + NONEXISTENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo(5001));
    }

    private String uploadDDL() {
        return given().contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart("file", ddlTestFile())
                .when()
                .post(BASE_PATH)
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
