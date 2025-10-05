package com.cleanai.modules.logbook

import com.cleanai.modules.logbook.infrastructure.web.LogEntryCategoryDto
import com.cleanai.modules.logbook.infrastructure.web.LogEntryUpsertStatusDto
import com.cleanai.modules.logbook.infrastructure.web.UpsertLogEntryRequest
import com.cleanai.modules.messaging.domain.EntityType
import com.cleanai.modules.messaging.infrastructure.persistence.EntityChangedMessageEntity
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class LogEntryResourceTest {

    private val baseUrl = "/api/log-entries"

    @Test
    @TestSecurity(user = "user_upsert", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_upsert"),
            Claim(key = "email", value = "upsert@example.com")
        ]
    )
    fun `should upsert log entry successfully`() {
        val logEntryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val request = UpsertLogEntryRequest(
            id = logEntryId,
            title = "Test Log Entry",
            category = LogEntryCategoryDto.OTHER,
            processingStage = LogEntryUpsertStatusDto.PENDING,
            durationSeconds = 120.5,
            archived = false,
            deleted = false,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)
            .body("id", `is`(logEntryId))
            .body("title", `is`("Test Log Entry"))
            .body("category", `is`("OTHER"))
            .body("durationSeconds", `is`(120.5f))
            .body("processingStatus", `is`("PENDING"))
            .body("archived", `is`(false))
            .body("deleted", `is`(false))

        assertEntityChangedMessageCreated(logEntryId, "user_upsert")
    }

    @Test
    @TestSecurity(user = "user_get", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_get"),
            Claim(key = "email", value = "get@example.com")
        ]
    )
    fun `should get author log entries with default parameters`() {
        given()
            .`when`()
            .get("$baseUrl/me")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("page", `is`(0))
            .body("size", `is`(20))
            .body("totalElements", notNullValue())
            .body("totalPages", notNullValue())
            .body("isFirst", `is`(true))
            .body("isLast", `is`(true))
    }

    @Test
    @TestSecurity(user = "user_get_single", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_get_single"),
            Claim(key = "email", value = "getsingle@example.com")
        ]
    )
    fun `should get single log entry by id`() {
        val logEntryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val request = UpsertLogEntryRequest(
            id = logEntryId,
            title = "Get Single Test",
            category = LogEntryCategoryDto.MISSION,
            processingStage = LogEntryUpsertStatusDto.UPLOADING,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)

        assertEntityChangedMessageCreated(logEntryId, "user_get_single")

        given()
            .`when`()
            .get("$baseUrl/$logEntryId")
            .then()
            .statusCode(200)
            .body("id", `is`(logEntryId))
            .body("title", `is`("Get Single Test"))
            .body("category", `is`("MISSION"))
            .body("processingStatus", `is`("UPLOADING"))
    }

    @Test
    @TestSecurity(user = "user_upsert_uploading", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_upsert_uploading"),
            Claim(key = "email", value = "uploading@example.com")
        ]
    )
    fun `should upsert log entry with uploading status`() {
        val logEntryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val request = UpsertLogEntryRequest(
            id = logEntryId,
            title = "Uploading Test",
            category = LogEntryCategoryDto.RESEARCH,
            processingStage = LogEntryUpsertStatusDto.UPLOADING,
            durationSeconds = 180.0,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)
            .body("id", `is`(logEntryId))
            .body("title", `is`("Uploading Test"))
            .body("processingStatus", `is`("UPLOADING"))
            .body("category", `is`("RESEARCH"))

        assertEntityChangedMessageCreated(logEntryId, "user_upsert_uploading")
    }

    @Test
    @TestSecurity(user = "user_upsert_uploaded", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_upsert_uploaded"),
            Claim(key = "email", value = "uploaded@example.com")
        ]
    )
    fun `should upsert log entry with uploaded status`() {
        val logEntryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val request = UpsertLogEntryRequest(
            id = logEntryId,
            title = "Uploaded Test",
            category = LogEntryCategoryDto.OPERATIONS,
            processingStage = LogEntryUpsertStatusDto.UPLOADED,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)
            .body("id", `is`(logEntryId))
            .body("title", `is`("Uploaded Test"))
            .body("processingStatus", `is`("UPLOADED"))
            .body("category", `is`("OPERATIONS"))

        assertEntityChangedMessageCreated(logEntryId, "user_upsert_uploaded")
    }

    @Test
    @TestSecurity(user = "user_soft_delete", roles = ["user"])
    @JwtSecurity(
        claims = [
            Claim(key = "sub", value = "user_soft_delete"),
            Claim(key = "email", value = "softdelete@example.com")
        ]
    )
    fun `should soft delete log entry successfully`() {
        val logEntryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val createRequest = UpsertLogEntryRequest(
            id = logEntryId,
            title = "To be deleted",
            category = LogEntryCategoryDto.PERSONAL,
            processingStage = LogEntryUpsertStatusDto.PENDING,
            archived = false,
            deleted = false,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)

        val deleteRequest = UpsertLogEntryRequest(
            id = logEntryId,
            title = "To be deleted",
            category = LogEntryCategoryDto.PERSONAL,
            processingStage = LogEntryUpsertStatusDto.PENDING,
            archived = false,
            deleted = true,
            createdAt = now.toString(),
            updatedAt = now.plusSeconds(5).toString(),
            deletedAt = now.plusSeconds(5).toString()
        )

        given()
            .contentType(ContentType.JSON)
            .body(deleteRequest)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)
            .body("id", `is`(logEntryId))
            .body("deleted", `is`(true))

        assertNumberOfEntityChangedMessages(logEntryId, 2)
    }

    private fun assertNumberOfEntityChangedMessages(logEntryId: String, expectedCount: Int) {
        val logEntryUuid = UUID.fromString(logEntryId)
        val messages = EntityChangedMessageEntity.find(
            "entityId = ?1 and entityType = ?2",
            logEntryUuid,
            EntityType.LOG_ENTRY.name.lowercase()
        ).list()

        assertEquals(
            expectedCount,
            messages.size,
            "Expected $expectedCount EntityChangedMessages but found ${messages.size}"
        )
    }

    private fun assertEntityChangedMessageCreated(
        logEntryId: String,
        expectedChangedByUserId: String,
        expectedReceiverUserId: String = expectedChangedByUserId,
        minimumMessages: Int = 1
    ) {
        val logEntryUuid = UUID.fromString(logEntryId)

        val messages = EntityChangedMessageEntity.find(
            "entityId = ?1 and entityType = ?2",
            logEntryUuid,
            EntityType.LOG_ENTRY.name.lowercase()
        ).list()

        require(minimumMessages >= 1) { "minimumMessages must be >= 1" }
        assertTrue(
            messages.size >= minimumMessages,
            "Expected at least $minimumMessages EntityChangedMessages but found ${messages.size}"
        )

        val domainMessages = messages.map { it.toDomain() }
        val matchingMessage = domainMessages.firstOrNull {
            it.entityId == logEntryUuid &&
                    it.entityType == EntityType.LOG_ENTRY &&
                    it.changedByUserId == expectedChangedByUserId &&
                    it.receiverUserId == expectedReceiverUserId
        }

        val message = assertNotNull(
            matchingMessage,
            "Expected to find EntityChangedMessage matching criteria for log entry $logEntryId"
        )

    }
}
