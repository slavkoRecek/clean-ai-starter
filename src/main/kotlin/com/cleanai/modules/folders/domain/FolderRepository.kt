package com.cleanai.modules.folders.domain

import com.cleanai.libs.pagination.SortDirection
import java.util.UUID

interface FolderRepository {
    fun findById(id: UUID): Folder?
    fun findByIdAndUserId(id: UUID, userId: String): Folder?
    fun findByUserId(
        userId: String,
        parentId: UUID? = null,
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        includeArchived: Boolean = false,
        includeDeleted: Boolean = false,
        orderBy: FolderOrderBy = FolderOrderBy.UPDATED_AT,
        orderDirection: SortDirection = SortDirection.DESC,
    ): List<Folder>
    fun countByUserId(
        userId: String,
        parentId: UUID? = null,
        search: String? = null,
        includeArchived: Boolean = false,
        includeDeleted: Boolean = false,
    ): Long
    fun persist(folder: Folder): Folder
}

enum class FolderOrderBy {
    UPDATED_AT,
    CREATED_AT,
    NAME,
}
