package com.cleanai.modules.folders

import com.cleanai.modules.folders.infrastructure.persistence.FolderEntity
import com.cleanai.modules.folders.infrastructure.web.UpsertFolderRequest
import com.cleanai.modules.messaging.domain.EntityType
import com.cleanai.modules.messaging.infrastructure.persistence.EntityChangedMessageEntity
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import jakarta.transaction.Transactional
import java.time.Instant
import java.util.UUID

@QuarkusTest
class FolderResourceTest {

    private val baseUrl = "/api/folders"

    @BeforeEach
    @Transactional
    fun cleanup() {
        EntityChangedMessageEntity.deleteAll()
        FolderEntity.deleteAll()
    }

    @Test
    @TestSecurity(user = "folder_user", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "folder_user"),
        Claim(key = "email", value = "folder@example.com")
    ])
    fun `should upsert folder successfully`() {
        val folderId = UUID.randomUUID().toString()
        val now = Instant.now()

        val request = UpsertFolderRequest(
            id = folderId,
            name = "Inbox",
            parentId = null,
            createdAt = now.toString(),
            updatedAt = now.toString(),
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(200)
            .body("id", equalTo(folderId))
            .body("userId", equalTo("folder_user"))
            .body("name", equalTo("Inbox"))
            .body("parentId", nullValue())
            .body("isDeleted", equalTo(false))
            .body("isArchived", equalTo(false))
            .body("deletedAt", nullValue())
            .body("archivedAt", nullValue())

        val persisted = FolderEntity.findById(UUID.fromString(folderId))
        assertNotNull(persisted, "Folder should exist in database after upsert")
        val persistedFolder = persisted!!.toDomain()
        assertEquals("folder_user", persistedFolder.userId)
        assertEquals("Inbox", persistedFolder.name)

        val messages = EntityChangedMessageEntity.find(
            "entityId = ?1 and entityType = ?2",
            UUID.fromString(folderId),
            EntityType.FOLDER.name.lowercase()
        ).list()
        assertEquals(1, messages.size)
        val message = messages.first().toDomain()
        assertEquals(EntityType.FOLDER, message.entityType)
        assertEquals("folder_user", message.changedByUserId)
        assertEquals("folder_user", message.receiverUserId)
    }

    @Test
    @TestSecurity(user = "intruder", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "intruder"),
        Claim(key = "email", value = "intruder@example.com")
    ])
    fun `should reject upsert when user does not own folder`() {
        val folderId = UUID.randomUUID()
        val ownerId = "owner_user"
        val createdAt = Instant.now()

        persistFolder(
            id = folderId,
            userId = ownerId,
            name = "Owner Folder",
            createdAt = createdAt,
            updatedAt = createdAt,
        )

        val request = UpsertFolderRequest(
            id = folderId.toString(),
            name = "Illegal Update",
            parentId = null,
            createdAt = createdAt.toString(),
            updatedAt = createdAt.plusSeconds(30).toString(),
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .put(baseUrl)
            .then()
            .statusCode(403)
            .body("message", containsString("cannot modify folder"))
    }

    @Test
    @TestSecurity(user = "folder_get_user", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "folder_get_user"),
        Claim(key = "email", value = "get_folder@example.com")
    ])
    fun `should get folder by id`() {
        val folderId = UUID.randomUUID()
        val createdAt = Instant.now()

        persistFolder(
            id = folderId,
            userId = "folder_get_user",
            name = "Projects",
            createdAt = createdAt,
            updatedAt = createdAt,
        )

        given()
            .`when`()
            .get("$baseUrl/${folderId}")
            .then()
            .statusCode(200)
            .body("id", equalTo(folderId.toString()))
            .body("name", equalTo("Projects"))
            .body("userId", equalTo("folder_get_user"))
    }

    @Test
    @TestSecurity(user = "missing_folder_user", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "missing_folder_user"),
        Claim(key = "email", value = "missing@example.com")
    ])
    fun `should return 404 when folder does not exist`() {
        val nonExistentId = UUID.randomUUID().toString()

        given()
            .`when`()
            .get("$baseUrl/$nonExistentId")
            .then()
            .statusCode(404)
            .body("message", containsString("not found"))
    }

    @Test
    @TestSecurity(user = "forbidden_folder_user", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "forbidden_folder_user"),
        Claim(key = "email", value = "forbidden@example.com")
    ])
    fun `should return 404 when folder belongs to another user`() {
        val folderId = UUID.randomUUID()
        val createdAt = Instant.now()

        persistFolder(
            id = folderId,
            userId = "real_owner",
            name = "Private Folder",
            createdAt = createdAt,
            updatedAt = createdAt,
        )

        given()
            .`when`()
            .get("$baseUrl/${folderId}")
            .then()
            .statusCode(404)
            .body("message", containsString("not found"))
    }

    @Test
    @TestSecurity(user = "folder_list_user", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "folder_list_user"),
        Claim(key = "email", value = "list_folder@example.com")
    ])
    fun `should list active folders with pagination metadata`() {
        val userId = "folder_list_user"
        val now = Instant.now()

        val parentId = UUID.randomUUID()
        persistFolder(
            id = parentId,
            userId = userId,
            name = "Root",
            createdAt = now.minusSeconds(300),
            updatedAt = now.minusSeconds(200),
        )

        val olderChildId = UUID.randomUUID()
        val newerChildId = UUID.randomUUID()
        val archivedId = UUID.randomUUID()
        val deletedId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()

        persistFolder(
            id = olderChildId,
            userId = userId,
            name = "Archive",
            parentId = parentId,
            createdAt = now.minusSeconds(120),
            updatedAt = now.minusSeconds(60),
        )

        persistFolder(
            id = newerChildId,
            userId = userId,
            name = "Inbox",
            parentId = parentId,
            createdAt = now.minusSeconds(90),
            updatedAt = now,
        )

        persistFolder(
            id = archivedId,
            userId = userId,
            name = "Archived",
            parentId = parentId,
            isArchived = true,
            archivedAt = now.minusSeconds(80),
            createdAt = now.minusSeconds(180),
            updatedAt = now.minusSeconds(100),
        )

        persistFolder(
            id = deletedId,
            userId = userId,
            name = "Trash",
            parentId = parentId,
            isDeleted = true,
            deletedAt = now.minusSeconds(70),
            createdAt = now.minusSeconds(200),
            updatedAt = now.minusSeconds(90),
        )

        persistFolder(
            id = otherUserId,
            userId = "someone_else",
            name = "Other",
            parentId = parentId,
            createdAt = now.minusSeconds(50),
            updatedAt = now.minusSeconds(40),
        )

        given()
            .queryParam("parentId", parentId.toString())
            .queryParam("limit", 2)
            .queryParam("offset", 0)
            .queryParam("orderBy", "UPDATED_AT")
            .queryParam("orderDirection", "DESC")
            .`when`()
            .get("$baseUrl/me")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("content[0].id", equalTo(newerChildId.toString()))
            .body("content[1].id", equalTo(olderChildId.toString()))
            .body("content.id", not(hasItem(archivedId.toString())))
            .body("content.id", not(hasItem(deletedId.toString())))
            .body("content.id", not(hasItem(otherUserId.toString())))
            .body("page", equalTo(0))
            .body("size", equalTo(2))
            .body("totalElements", equalTo(2))
            .body("totalPages", equalTo(1))
            .body("isFirst", equalTo(true))
            .body("isLast", equalTo(true))
    }

    @Test
    @TestSecurity(user = "folder_list_flags", roles = ["user"])
    @JwtSecurity(claims = [
        Claim(key = "sub", value = "folder_list_flags"),
        Claim(key = "email", value = "flags_folder@example.com")
    ])
    fun `should include archived and deleted folders when requested`() {
        val userId = "folder_list_flags"
        val now = Instant.now()

        val activeId = UUID.randomUUID()
        val archivedId = UUID.randomUUID()
        val deletedId = UUID.randomUUID()

        persistFolder(
            id = activeId,
            userId = userId,
            name = "Active",
            createdAt = now.minusSeconds(200),
            updatedAt = now.minusSeconds(50),
        )

        persistFolder(
            id = archivedId,
            userId = userId,
            name = "Archived",
            isArchived = true,
            archivedAt = now.minusSeconds(40),
            createdAt = now.minusSeconds(220),
            updatedAt = now.minusSeconds(40),
        )

        persistFolder(
            id = deletedId,
            userId = userId,
            name = "Deleted",
            isDeleted = true,
            deletedAt = now.minusSeconds(30),
            createdAt = now.minusSeconds(250),
            updatedAt = now.minusSeconds(30),
        )

        given()
            .queryParam("includeArchived", true)
            .queryParam("includeDeleted", true)
            .queryParam("orderBy", "UPDATED_AT")
            .queryParam("orderDirection", "DESC")
            .`when`()
            .get("$baseUrl/me")
            .then()
            .statusCode(200)
            .body("content.id", hasItem(activeId.toString()))
            .body("content.id", hasItem(archivedId.toString()))
            .body("content.id", hasItem(deletedId.toString()))
            .body("content.find { it.id == '${archivedId}' }.isArchived", equalTo(true))
            .body("content.find { it.id == '${deletedId}' }.isDeleted", equalTo(true))
            .body("totalElements", equalTo(3))
            .body("page", equalTo(0))
            .body("isFirst", equalTo(true))
            .body("isLast", equalTo(true))
    }

    @Transactional
    fun persistFolder(
        id: UUID,
        userId: String,
        name: String,
        parentId: UUID? = null,
        isDeleted: Boolean = false,
        deletedAt: Instant? = null,
        isArchived: Boolean = false,
        archivedAt: Instant? = null,
        createdAt: Instant,
        updatedAt: Instant,
    ) {
        val entity = FolderEntity().apply {
            this.id = id
            this.userId = userId
            this.name = name
            this.parentId = parentId
            this.isDeleted = isDeleted
            this.deletedAt = deletedAt
            this.isArchived = isArchived
            this.archivedAt = archivedAt
            this.createdAt = createdAt
            this.updatedAt = updatedAt
        }
        entity.persist()
    }
}
