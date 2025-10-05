package com.cleanai.modules.files

import com.cleanai.fixture.mocks.TestFileStorageRepository
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.UUID

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileResourceApiTest {

    @Inject
    lateinit var fileStorage: TestFileStorageRepository

    @AfterAll
    fun cleanup() {
        fileStorage.cleanup()
    }

    @Test
    @TestSecurity(user = "user_upload", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "user_upload"),
        Claim(key = "email", value = "upload@example.com")
    ])
    fun `should upload file and return correct response`(@TempDir tempDir: Path) {
        // Create a test file
        val testFile = File(tempDir.toFile(), "test.txt")
        testFile.writeText("Hello, World!")

        // Upload file
        RestAssured.given()
            .multiPart("file", testFile)
            .`when`()
            .post("/api/files")
            .then()
            .log().all()
            .statusCode(200)
            .body("id", CoreMatchers.notNullValue())
            .body("fileName", CoreMatchers.equalTo("test.txt"))
            .body("mimeType", CoreMatchers.equalTo("application/octet-stream"))
            .body("sizeBytes", CoreMatchers.equalTo(13.0f))
            .body("downloadUrl", CoreMatchers.notNullValue())
    }

    @Test
    fun `should return 401 when user is not authenticated`(@TempDir tempDir: Path) {
        // Create a test file
        val testFile = File(tempDir.toFile(), "test.txt")
        testFile.writeText("Hello, World!")

        RestAssured.given()
            .multiPart("file", testFile)
            .`when`()
            .post("/api/files")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "user_get_file", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "user_get_file"),
        Claim(key = "email", value = "getfile@example.com")
    ])
    fun `should upload and then get file by id`(@TempDir tempDir: Path) {
        // Create and upload test file
        val fileId = uploadTextFile(tempDir)

        // Get file by ID
        RestAssured.given()
            .`when`()
            .get("/api/files/$fileId")
            .then()
            .statusCode(200)
            .body("id", CoreMatchers.equalTo(fileId))
            .body("fileName", CoreMatchers.equalTo("test.txt"))
            .body("mimeType", CoreMatchers.equalTo("application/octet-stream"))
            .body("sizeBytes", CoreMatchers.equalTo(13.0f))
    }


    private fun uploadTextFile(tempDir: Path): String? {
        val testFile = File(tempDir.toFile(), "test.txt")
        testFile.writeText("Hello, World!")

        // Upload file and extract ID
        val fileId = RestAssured.given()
            .multiPart("file", testFile)
            .`when`()
            .post("/api/files")
            .then()
            .statusCode(200)
            .extract()
            .path<String>("id")
        return fileId
    }

    @Test
    fun `should return 401 when getting file without authentication`() {
        RestAssured.given()
            .`when`()
            .get("/api/files/${UUID.randomUUID()}")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(user = "user_not_found", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "user_not_found"),
        Claim(key = "email", value = "notfound@example.com")
    ])
    fun `should return 404 when file does not exist`() {
        RestAssured.given()
            .`when`()
            .get("/api/files/${UUID.randomUUID()}")
            .then()
            .statusCode(404)
    }

}
