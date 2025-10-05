package com.cleanai.modules.folders.domain

import java.time.Instant
import java.util.UUID

/**
 * Folder aggregate mirrors the mobile local-first model while enforcing consistency between
 * deletion/archival flags and their accompanying timestamps.
 */
data class Folder(
    val id: UUID,
    val userId: String,
    val name: String,
    val parentId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val isArchived: Boolean,
    val archivedAt: Instant?,
) {
    init {
        require(name.isNotBlank()) { "Folder name cannot be blank" }

        if (isDeleted) {
            requireNotNull(deletedAt) { "deletedAt must be provided when folder is marked deleted" }
        } else {
            require(deletedAt == null) { "deletedAt must be null when folder is not deleted" }
        }

        if (isArchived) {
            requireNotNull(archivedAt) { "archivedAt must be provided when folder is archived" }
        } else {
            require(archivedAt == null) { "archivedAt must be null when folder is not archived" }
        }
    }
}
