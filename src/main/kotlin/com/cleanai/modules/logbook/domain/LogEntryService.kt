package com.cleanai.modules.logbook.domain

import com.cleanai.libs.exception.ObjectNotFoundException
import com.cleanai.libs.exception.UnauthorizedAccessException
import com.cleanai.libs.pagination.Page
import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.files.domain.FileDbRepository
import com.cleanai.modules.messaging.domain.EntityChangedEventPublisher
import com.cleanai.modules.messaging.domain.EntityType
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.context.ManagedExecutor
import org.slf4j.LoggerFactory
import java.util.UUID

@ApplicationScoped
class LogEntryService(
    private val logEntryRepository: LogEntryRepository,
    private val fileDbRepository: FileDbRepository,
    private val logEntryProcessingService: LogEntryProcessingService,
    private val managedExecutor: ManagedExecutor,
    private val entityChangedEventPublisher: EntityChangedEventPublisher
) {
    private val log = LoggerFactory.getLogger(LogEntryService::class.java)

    fun upsertLogEntry(logEntry: LogEntry, requesterId: String): LogEntry {
        val existingEntry = logEntryRepository.findById(logEntry.id)

        existingEntry?.let { existing ->
            if (existing.authorId != logEntry.authorId) {
                throw UnauthorizedAccessException("User ${logEntry.authorId} cannot modify log entry ${logEntry.id}")
            }
        }

        logEntry.audioFileId?.let { fileId ->
            val file = fileDbRepository.findById(fileId)
                ?: throw ObjectNotFoundException("File with id $fileId not found")

            if (file.userId != logEntry.authorId) {
                throw UnauthorizedAccessException("User ${logEntry.authorId} cannot access file $fileId")
            }
        }

        val savedEntry = logEntryRepository.persist(logEntry)

        val receiverUserIds = listOf(logEntry.authorId, requesterId).distinct()
        entityChangedEventPublisher.fireEntityChanged(
            entityId = savedEntry.id,
            entityType = EntityType.LOG_ENTRY,
            changedByUserId = logEntry.authorId,
            receiverUserIds = receiverUserIds
        )

        if (savedEntry.processingStatus == LogEntryProcessingStatus.UPLOADED) {
            log.info("Triggering background processing for log entry ${savedEntry.id}")
            managedExecutor.submit {
                logEntryProcessingService.processLogEntryAsync(savedEntry.id)
            }
        }
        return savedEntry
    }

    fun getLogEntryById(id: UUID, authorId: String): LogEntry {
        return logEntryRepository.findByIdAndAuthorId(id, authorId)
            ?: throw ObjectNotFoundException("Log entry with id $id not found for user $authorId")
    }

    fun getLogEntriesForAuthor(
        authorId: String,
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        orderBy: LogEntryOrderBy = LogEntryOrderBy.UPDATED_AT,
        orderDirection: SortDirection = SortDirection.DESC
    ): Page<LogEntry> {
        log.debug("Getting log entries for author: $authorId with limit: $limit, offset: $offset")

        require(limit in 1..100) { "Limit must be between 1 and 100" }
        require(offset >= 0) { "Offset must be >= 0" }

        val logEntries = logEntryRepository.findByAuthorId(
            authorId = authorId,
            limit = limit,
            offset = offset,
            search = search,
            orderBy = orderBy,
            orderDirection = orderDirection
        )

        val totalCount = logEntryRepository.countByAuthorId(authorId, search)
        val page = if (limit > 0) offset / limit else 0

        return Page(
            content = logEntries,
            page = page,
            size = limit,
            totalElements = totalCount
        )
    }
}
