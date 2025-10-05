package com.cleanai.modules.folders.domain

import com.cleanai.libs.exception.ObjectNotFoundException
import com.cleanai.libs.exception.UnauthorizedAccessException
import com.cleanai.libs.pagination.Page
import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.messaging.domain.EntityChangedEventPublisher
import com.cleanai.modules.messaging.domain.EntityType
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.LoggerFactory
import java.util.UUID

@ApplicationScoped
class FolderService(
    private val folderRepository: FolderRepository,
    private val entityChangedEventPublisher: EntityChangedEventPublisher,
) {
    private val log = LoggerFactory.getLogger(FolderService::class.java)

    fun upsertFolder(folder: Folder, userId: String): Folder {
        val existing = folderRepository.findById(folder.id)
        existing?.let {
            if (it.userId != folder.userId) {
                throw UnauthorizedAccessException("User ${folder.userId} cannot modify folder ${folder.id}")
            }
        }

        val savedFolder = folderRepository.persist(folder)

        val receiverUserIds = listOf(userId).distinct()
        entityChangedEventPublisher.fireEntityChanged(
            entityId = savedFolder.id,
            entityType = EntityType.FOLDER,
            changedByUserId = folder.userId,
            receiverUserIds = receiverUserIds,
        )

        return savedFolder
    }

    fun getFolderById(id: UUID, userId: String): Folder {
        return folderRepository.findByIdAndUserId(id, userId)
            ?: throw ObjectNotFoundException("Folder with id $id not found for user $userId")
    }

    fun getUserFolders(
        userId: String,
        parentId: UUID? = null,
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        includeArchived: Boolean = false,
        includeDeleted: Boolean = false,
        orderBy: FolderOrderBy = FolderOrderBy.UPDATED_AT,
        orderDirection: SortDirection = SortDirection.DESC,
    ): Page<Folder> {
        log.debug("Getting folders for user $userId with limit $limit and offset $offset")

        require(limit in 1..100) { "Limit must be between 1 and 100" }
        require(offset >= 0) { "Offset must be >= 0" }

        val folders = folderRepository.findByUserId(
            userId = userId,
            parentId = parentId,
            limit = limit,
            offset = offset,
            search = search,
            includeArchived = includeArchived,
            includeDeleted = includeDeleted,
            orderBy = orderBy,
            orderDirection = orderDirection,
        )

        val totalCount = folderRepository.countByUserId(
            userId = userId,
            parentId = parentId,
            search = search,
            includeArchived = includeArchived,
            includeDeleted = includeDeleted,
        )

        val page = offset / limit

        return Page(
            content = folders,
            page = page,
            size = limit,
            totalElements = totalCount,
        )
    }
}
