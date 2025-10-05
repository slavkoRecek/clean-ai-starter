package com.cleanai.fixture

import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@TestSecurity(user = "user_test", roles = ["user"])
@JwtSecurity(claims = [
    Claim(key = "sub", value = "user_test"),
    Claim(key = "email", value = "test@example.com"),
    Claim(key = "given_name", value = "Test"),
    Claim(key = "family_name", value = "User"),
    Claim(key = "picture", value = "https://example.com/avatar.jpg")
])
annotation class MockClerkUser(
    val subject: String = "user_test",
    val email: String = "test@example.com",
    val givenName: String = "Test",
    val familyName: String = "User",
    val picture: String = "https://example.com/avatar.jpg"
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@TestSecurity(user = "user_minimal", roles = ["user"])
@JwtSecurity(claims = [
    Claim(key = "sub", value = "user_minimal"),
    Claim(key = "email", value = "minimal@example.com")
])
annotation class MockClerkUserMinimal(
    val subject: String = "user_minimal",
    val email: String = "minimal@example.com"
)
