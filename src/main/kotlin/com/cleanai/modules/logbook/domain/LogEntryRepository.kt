package com.cleanai.modules.logbook.domain

import com.cleanai.libs.pagination.SortDirection
import java.util.UUID

interface LogEntryRepository {
    fun findById(id: UUID): LogEntry?
    fun findByIdAndAuthorId(id: UUID, authorId: String): LogEntry?
    fun findByAuthorId(
        authorId: String,
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        orderBy: LogEntryOrderBy = LogEntryOrderBy.UPDATED_AT,
        orderDirection: SortDirection = SortDirection.DESC
    ): List<LogEntry>

    fun countByAuthorId(
        authorId: String,
        search: String? = null
    ): Long

    fun persist(logEntry: LogEntry): LogEntry
    fun deleteById(id: UUID)
    fun existsByIdAndAuthorId(id: UUID, authorId: String): Boolean
}
