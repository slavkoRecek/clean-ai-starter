package com.cleanai.modules.folders.infrastructure.persistence

import com.cleanai.modules.folders.domain.Folder
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "folders",
    indexes = [
        Index(name = "idx_folders_user_id", columnList = "user_id"),
        Index(name = "idx_folders_user_parent", columnList = "user_id, parent_id"),
        Index(name = "idx_folders_user_archived_deleted", columnList = "user_id, is_archived, is_deleted"),
        Index(name = "idx_folders_user_updated", columnList = "user_id, updated_at"),
        Index(name = "idx_folders_user_created", columnList = "user_id, created_at")
    ]
)
class FolderEntity : PanacheEntityBase {
    companion object : PanacheCompanionBase<FolderEntity, UUID> {
        fun from(domain: Folder): FolderEntity = FolderEntity().apply {
            id = domain.id
            userId = domain.userId
            name = domain.name
            parentId = domain.parentId
            createdAt = domain.createdAt
            updatedAt = domain.updatedAt
            isDeleted = domain.isDeleted
            deletedAt = domain.deletedAt
            isArchived = domain.isArchived
            archivedAt = domain.archivedAt
        }
    }

    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID? = null

    @Column(name = "user_id", nullable = false, length = 255)
    var userId: String? = null

    @Column(name = "name", nullable = false, length = 255)
    var name: String? = null

    @Column(name = "parent_id", columnDefinition = "uuid")
    var parentId: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

    @Column(name = "is_archived", nullable = false)
    var isArchived: Boolean = false

    @Column(name = "archived_at")
    var archivedAt: Instant? = null

    fun toDomain(): Folder {
        return Folder(
            id = id!!,
            userId = userId!!,
            name = name!!,
            parentId = parentId,
            createdAt = createdAt!!,
            updatedAt = updatedAt!!,
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            isArchived = isArchived,
            archivedAt = archivedAt,
        )
    }
}
