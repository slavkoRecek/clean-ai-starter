package com.cleanai.modules.folders.infrastructure.web

import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.folders.domain.Folder
import com.cleanai.modules.folders.domain.FolderOrderBy
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UpsertFolderRequest(
    @field:NotBlank(message = "Folder ID is required")
    val id: String,

    @field:NotBlank(message = "Folder name is required")
    @field:Size(max = 255, message = "Folder name cannot exceed 255 characters")
    val name: String,

    val parentId: String? = null,

    val isDeleted: Boolean = false,

    val deletedAt: String? = null,

    val isArchived: Boolean = false,

    val archivedAt: String? = null,

    @field:NotBlank(message = "Created at timestamp is required")
    val createdAt: String,

    @field:NotBlank(message = "Updated at timestamp is required")
    val updatedAt: String,
) {
    private fun toUUID(): UUID = UUID.fromString(id)
    private fun createdAtInstant(): Instant = Instant.parse(createdAt)
    private fun updatedAtInstant(): Instant = Instant.parse(updatedAt)
    private fun deletedAtInstant(): Instant? = deletedAt?.let { Instant.parse(it) }
    private fun archivedAtInstant(): Instant? = archivedAt?.let { Instant.parse(it) }
    private fun parentUUID(): UUID? = parentId?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) }

    fun toDomain(userId: String): Folder {
        val trimmedName = name.trim()
        val effectiveDeletedAt = if (isDeleted) {
            deletedAtInstant() ?: updatedAtInstant()
        } else {
            null
        }
        val effectiveArchivedAt = if (isArchived) {
            archivedAtInstant() ?: updatedAtInstant()
        } else {
            null
        }

        return Folder(
            id = toUUID(),
            userId = userId,
            name = trimmedName,
            parentId = parentUUID(),
            createdAt = createdAtInstant(),
            updatedAt = updatedAtInstant(),
            isDeleted = isDeleted,
            deletedAt = effectiveDeletedAt,
            isArchived = isArchived,
            archivedAt = effectiveArchivedAt,
        )
    }
}

@Serializable
data class FolderResponse(
    val id: String,
    val userId: String,
    val name: String,
    val parentId: String?,
    val isDeleted: Boolean,
    val deletedAt: String?,
    val isArchived: Boolean,
    val archivedAt: String?,
    val createdAt: String,
    val updatedAt: String,
) {
    companion object {
        fun fromDomain(folder: Folder): FolderResponse {
            return FolderResponse(
                id = folder.id.toString(),
                userId = folder.userId,
                name = folder.name,
                parentId = folder.parentId?.toString(),
                isDeleted = folder.isDeleted,
                deletedAt = folder.deletedAt?.toString(),
                isArchived = folder.isArchived,
                archivedAt = folder.archivedAt?.toString(),
                createdAt = folder.createdAt.toString(),
                updatedAt = folder.updatedAt.toString(),
            )
        }
    }
}

@Serializable
data class FolderPreview(
    val id: String,
    val name: String,
    val parentId: String?,
    val isDeleted: Boolean,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String,
) {
    companion object {
        fun fromDomain(folder: Folder): FolderPreview {
            return FolderPreview(
                id = folder.id.toString(),
                name = folder.name,
                parentId = folder.parentId?.toString(),
                isDeleted = folder.isDeleted,
                isArchived = folder.isArchived,
                createdAt = folder.createdAt.toString(),
                updatedAt = folder.updatedAt.toString(),
            )
        }
    }
}

@Serializable
enum class FolderOrderByDto {
    UPDATED_AT,
    CREATED_AT,
    NAME;

    fun toDomain(): FolderOrderBy = when (this) {
        UPDATED_AT -> FolderOrderBy.UPDATED_AT
        CREATED_AT -> FolderOrderBy.CREATED_AT
        NAME -> FolderOrderBy.NAME
    }
}

@Serializable
enum class SortDirectionDto {
    ASC,
    DESC;

    fun toDomain(): SortDirection = when (this) {
        ASC -> SortDirection.ASC
        DESC -> SortDirection.DESC
    }
}
