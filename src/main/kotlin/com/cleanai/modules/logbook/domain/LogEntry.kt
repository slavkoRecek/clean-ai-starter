package com.cleanai.modules.logbook.domain

import java.time.Instant
import java.util.UUID

data class LogEntry(
    val id: UUID,
    val authorId: String,
    val audioFileId: UUID?,
    val audioUrl: String?,
    val processingStatus: LogEntryProcessingStatus,
    val transcript: String?,
    val structuredSummary: String?,
    val summaryText: String?,
    val title: String?,
    val category: LogEntryCategory,
    val durationSeconds: Double?,
    val folderId: UUID?,
    val transcriptionError: String?,
    val enrichmentError: String?,
    val archived: Boolean,
    val deleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null
)

enum class LogEntryProcessingStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    TRANSCRIBING,
    TRANSCRIBED,
    ENRICHING,
    COMPLETED,
    FAILED
}

enum class LogEntryCategory {
    MISSION,
    OPERATIONS,
    PERSONAL,
    RESEARCH,
    OTHER
}

enum class LogEntryOrderBy {
    CREATED_AT,
    UPDATED_AT,
    TITLE
}

enum class LogEntryUpsertStatus {
    PENDING,
    UPLOADING,
    UPLOADED;

    fun toProcessingStatus(): LogEntryProcessingStatus = when (this) {
        PENDING -> LogEntryProcessingStatus.PENDING
        UPLOADING -> LogEntryProcessingStatus.UPLOADING
        UPLOADED -> LogEntryProcessingStatus.UPLOADED
    }
}
