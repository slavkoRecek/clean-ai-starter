package com.cleanai.modules.logbook.infrastructure.persistance

import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.logbook.domain.LogEntry
import com.cleanai.modules.logbook.domain.LogEntryOrderBy
import com.cleanai.modules.logbook.domain.LogEntryRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class PgLogEntryRepository : LogEntryRepository {

    override fun findById(id: UUID): LogEntry? {
        return LogEntryEntity.findById(id)?.toDomain()
    }

    override fun findByIdAndAuthorId(id: UUID, authorId: String): LogEntry? {
        return LogEntryEntity.find("id = ?1 and authorId = ?2", id, authorId)
            .firstResult()?.toDomain()
    }

    override fun findByAuthorId(
        authorId: String,
        limit: Int,
        offset: Int,
        search: String?,
        orderBy: LogEntryOrderBy,
        orderDirection: SortDirection
    ): List<LogEntry> {
        val sortField = when (orderBy) {
            LogEntryOrderBy.CREATED_AT -> "createdAt"
            LogEntryOrderBy.UPDATED_AT -> "updatedAt"
            LogEntryOrderBy.TITLE -> "title"
        }

        val sortDirection = when (orderDirection) {
            SortDirection.ASC -> Sort.Direction.Ascending
            SortDirection.DESC -> Sort.Direction.Descending
        }

        val sort = Sort.by(sortField, sortDirection)

        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        conditions.add("authorId = ?${parameters.size + 1}")
        parameters.add(authorId)

        conditions.add("deleted = false")

        if (!search.isNullOrBlank()) {
            conditions.add("(lower(title) like lower(?${parameters.size + 1}) or lower(transcript) like lower(?${parameters.size + 2}))")
            parameters.add("%$search%")
            parameters.add("%$search%")
        }

        val query = LogEntryEntity.find(conditions.joinToString(" and "), sort, *parameters.toTypedArray())

        return query.page(offset / limit, limit)
            .list()
            .map { it.toDomain() }
    }

    override fun countByAuthorId(
        authorId: String,
        search: String?
    ): Long {
        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()

        conditions.add("authorId = ?${parameters.size + 1}")
        parameters.add(authorId)

        conditions.add("deleted = false")

        if (!search.isNullOrBlank()) {
            conditions.add("(lower(title) like lower(?${parameters.size + 1}) or lower(transcript) like lower(?${parameters.size + 2}))")
            parameters.add("%$search%")
            parameters.add("%$search%")
        }

        return LogEntryEntity.count(conditions.joinToString(" and "), *parameters.toTypedArray())
    }

    @Transactional
    override fun persist(logEntry: LogEntry): LogEntry {
        val existingEntity = LogEntryEntity.findById(logEntry.id)

        val entity = if (existingEntity != null) {
            existingEntity.apply {
                authorId = logEntry.authorId
                audioFileId = logEntry.audioFileId
                audioUrl = logEntry.audioUrl
                processingStatus = logEntry.processingStatus.name.lowercase()
                transcript = logEntry.transcript
                structuredSummary = logEntry.structuredSummary
                summaryText = logEntry.summaryText
                title = logEntry.title
                category = logEntry.category.name.lowercase()
                durationSeconds = logEntry.durationSeconds?.let { java.math.BigDecimal.valueOf(it) }
                folderId = logEntry.folderId
                transcriptionError = logEntry.transcriptionError
                enrichmentError = logEntry.enrichmentError
                archived = logEntry.archived
                deleted = logEntry.deleted
                createdAt = logEntry.createdAt
                updatedAt = logEntry.updatedAt
                deletedAt = logEntry.deletedAt
            }
        } else {
            LogEntryEntity.from(logEntry)
        }

        entity.persistAndFlush()
        return entity.toDomain()
    }

    @Transactional
    override fun deleteById(id: UUID) {
        LogEntryEntity.deleteById(id)
    }

    override fun existsByIdAndAuthorId(id: UUID, authorId: String): Boolean {
        return LogEntryEntity.count("id = ?1 and authorId = ?2", id, authorId) > 0
    }
}
