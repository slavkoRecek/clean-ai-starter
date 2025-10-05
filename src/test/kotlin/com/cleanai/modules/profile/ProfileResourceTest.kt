package com.cleanai.modules.profile

import com.cleanai.fixture.MockClerkUser
import com.cleanai.fixture.MockClerkUserMinimal
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class ProfileResourceTest {

    @Test
    @MockClerkUser
    fun `should return user profile with full JWT claims`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/profiles/me")
            .then()
            .statusCode(200)
            .body("oidcUserId", `is`("user_test"))
            .body("name", `is`("Test User"))
            .body("email", `is`("test@example.com"))
            .body("firstName", `is`("Test"))
            .body("lastName", `is`("User"))
            .body("imageUrl", `is`("https://example.com/avatar.jpg"))
    }

    @Test
    @MockClerkUserMinimal
    fun `should return user profile with minimal claims`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/profiles/me")
            .then()
            .statusCode(200)
            .body("oidcUserId", `is`("user_minimal"))
            .body("name", `is`("minimal"))
            .body("email", `is`("minimal@example.com"))
            .body("firstName", `is`(null as String?))
            .body("lastName", `is`(null as String?))
            .body("imageUrl", `is`(null as String?))
    }

    @Test
    @TestSecurity(user = "user_fallback", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "user_fallback"),
        Claim(key = "email", value = "fallback@example.com"),
        Claim(key = "given_name", value = "John")
    ])
    fun `should use given name when family name missing`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/profiles/me")
            .then()
            .statusCode(200)
            .body("oidcUserId", `is`("user_fallback"))
            .body("name", `is`("John"))
            .body("email", `is`("fallback@example.com"))
            .body("firstName", `is`("John"))
            .body("lastName", `is`(null as String?))
    }

    @Test
    @TestSecurity(user = "user_email_fallback", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "user_email_fallback"),
        Claim(key = "email", value = "emailfallback@example.com")
    ])
    fun `should use email prefix as name fallback`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/profiles/me")
            .then()
            .statusCode(200)
            .body("oidcUserId", `is`("user_email_fallback"))
            .body("name", `is`("emailfallback"))
            .body("email", `is`("emailfallback@example.com"))
            .body("firstName", `is`(null as String?))
            .body("lastName", `is`(null as String?))
    }

    @Test
    fun `should return 401 when unauthenticated`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/profiles/me")
            .then()
            .statusCode(401)
    }
}
