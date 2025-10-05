package com.cleanai.modules.folders.infrastructure.persistence

import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.folders.domain.Folder
import com.cleanai.modules.folders.domain.FolderOrderBy
import com.cleanai.modules.folders.domain.FolderRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class PgFolderRepository : FolderRepository {

    override fun findById(id: UUID): Folder? {
        return FolderEntity.findById(id)?.toDomain()
    }

    override fun findByIdAndUserId(id: UUID, userId: String): Folder? {
        return FolderEntity.find("id = ?1 and userId = ?2", id, userId)
            .firstResult()?.toDomain()
    }

    override fun findByUserId(
        userId: String,
        parentId: UUID?,
        limit: Int,
        offset: Int,
        search: String?,
        includeArchived: Boolean,
        includeDeleted: Boolean,
        orderBy: FolderOrderBy,
        orderDirection: SortDirection,
    ): List<Folder> {
        val sortField = when (orderBy) {
            FolderOrderBy.UPDATED_AT -> "updatedAt"
            FolderOrderBy.CREATED_AT -> "createdAt"
            FolderOrderBy.NAME -> "name"
        }

        val sortDirection = when (orderDirection) {
            SortDirection.ASC -> Sort.Direction.Ascending
            SortDirection.DESC -> Sort.Direction.Descending
        }

        val sort = Sort.by(sortField, sortDirection)

        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        conditions.add("userId = ?${parameters.size + 1}")
        parameters.add(userId)

        if (parentId != null) {
            conditions.add("parentId = ?${parameters.size + 1}")
            parameters.add(parentId)
        }

        if (!includeArchived) {
            conditions.add("isArchived = false")
        }

        if (!includeDeleted) {
            conditions.add("isDeleted = false")
        }

        if (!search.isNullOrBlank()) {
            conditions.add("lower(name) like lower(?${parameters.size + 1})")
            val pattern = "%${search.trim()}%"
            parameters.add(pattern)
        }

        val query = if (conditions.isEmpty()) {
            FolderEntity.findAll(sort)
        } else {
            FolderEntity.find(conditions.joinToString(" and "), sort, *parameters.toTypedArray())
        }

        return query.page(offset / limit, limit)
            .list()
            .map { it.toDomain() }
    }

    override fun countByUserId(
        userId: String,
        parentId: UUID?,
        search: String?,
        includeArchived: Boolean,
        includeDeleted: Boolean,
    ): Long {
        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        conditions.add("userId = ?${parameters.size + 1}")
        parameters.add(userId)

        if (parentId != null) {
            conditions.add("parentId = ?${parameters.size + 1}")
            parameters.add(parentId)
        }

        if (!includeArchived) {
            conditions.add("isArchived = false")
        }

        if (!includeDeleted) {
            conditions.add("isDeleted = false")
        }

        if (!search.isNullOrBlank()) {
            conditions.add("lower(name) like lower(?${parameters.size + 1})")
            val pattern = "%${search.trim()}%"
            parameters.add(pattern)
        }

        return FolderEntity.count(conditions.joinToString(" and "), *parameters.toTypedArray())
    }

    @Transactional
    override fun persist(folder: Folder): Folder {
        val existingEntity = FolderEntity.findById(folder.id)

        val entity = if (existingEntity != null) {
            existingEntity.apply {
                userId = folder.userId
                name = folder.name
                parentId = folder.parentId
                createdAt = folder.createdAt
                updatedAt = folder.updatedAt
                isDeleted = folder.isDeleted
                deletedAt = folder.deletedAt
                isArchived = folder.isArchived
                archivedAt = folder.archivedAt
            }
        } else {
            FolderEntity.from(folder)
        }

        entity.persistAndFlush()
        return entity.toDomain()
    }
}
