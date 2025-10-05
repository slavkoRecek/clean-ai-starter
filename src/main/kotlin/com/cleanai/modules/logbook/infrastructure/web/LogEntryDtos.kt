package com.cleanai.modules.logbook.infrastructure.web

import com.cleanai.libs.pagination.SortDirection
import com.cleanai.modules.logbook.domain.LogEntry
import com.cleanai.modules.logbook.domain.LogEntryCategory
import com.cleanai.modules.logbook.domain.LogEntryOrderBy
import com.cleanai.modules.logbook.domain.LogEntryProcessingStatus
import com.cleanai.modules.logbook.domain.LogEntryUpsertStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UpsertLogEntryRequest(
    @field:NotBlank(message = "Log entry ID is required")
    val id: String,

    val audioFileId: String? = null,

    val audioUrl: String? = null,

    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String? = null,

    val category: LogEntryCategoryDto = LogEntryCategoryDto.OTHER,

    val processingStage: LogEntryUpsertStatusDto = LogEntryUpsertStatusDto.PENDING,

    val durationSeconds: Double? = null,

    val transcript: String? = null,

    val structuredSummary: String? = null,

    val summaryText: String? = null,

    val folderId: String? = null,

    val archived: Boolean = false,

    val deleted: Boolean = false,

    @field:NotBlank(message = "Created at timestamp is required")
    val createdAt: String,

    @field:NotBlank(message = "Updated at timestamp is required")
    val updatedAt: String,

    val deletedAt: String? = null
) {
    private fun toUUID(): UUID = UUID.fromString(id)
    private fun toAudioFileUUID(): UUID? = audioFileId?.let { UUID.fromString(it) }
    private fun toFolderUUID(): UUID? = folderId?.let { UUID.fromString(it) }
    private fun toCreatedAtInstant(): Instant = Instant.parse(createdAt)
    private fun toUpdatedAtInstant(): Instant = Instant.parse(updatedAt)
    private fun toDeletedAtInstant(): Instant? = deletedAt?.let { Instant.parse(it) }

    fun toDomain(authorId: String): LogEntry {
        return LogEntry(
            id = toUUID(),
            authorId = authorId,
            audioFileId = toAudioFileUUID(),
            audioUrl = audioUrl,
            processingStatus = processingStage.toDomain().toProcessingStatus(),
            transcript = transcript,
            structuredSummary = structuredSummary,
            summaryText = summaryText,
            title = title,
            category = category.toDomain(),
            durationSeconds = durationSeconds,
            folderId = toFolderUUID(),
            transcriptionError = null,
            enrichmentError = null,
            archived = archived,
            deleted = deleted,
            createdAt = toCreatedAtInstant(),
            updatedAt = toUpdatedAtInstant(),
            deletedAt = toDeletedAtInstant()
        )
    }
}

@Serializable
data class LogEntryResponse(
    val id: String,
    val authorId: String,
    val audioFileId: String?,
    val audioUrl: String?,
    val processingStatus: LogEntryProcessingStatusDto,
    val transcript: String?,
    val structuredSummary: String?,
    val summaryText: String?,
    val title: String?,
    val category: LogEntryCategoryDto,
    val durationSeconds: Double?,
    val folderId: String?,
    val transcriptionError: String?,
    val enrichmentError: String?,
    val archived: Boolean,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
) {
    companion object {
        fun fromDomain(logEntry: LogEntry): LogEntryResponse {
            return LogEntryResponse(
                id = logEntry.id.toString(),
                authorId = logEntry.authorId,
                audioFileId = logEntry.audioFileId?.toString(),
                audioUrl = logEntry.audioUrl,
                processingStatus = LogEntryProcessingStatusDto.fromDomain(logEntry.processingStatus),
                transcript = logEntry.transcript,
                structuredSummary = logEntry.structuredSummary,
                summaryText = logEntry.summaryText,
                title = logEntry.title,
                category = LogEntryCategoryDto.fromDomain(logEntry.category),
                durationSeconds = logEntry.durationSeconds,
                folderId = logEntry.folderId?.toString(),
                transcriptionError = logEntry.transcriptionError,
                enrichmentError = logEntry.enrichmentError,
                archived = logEntry.archived,
                deleted = logEntry.deleted,
                createdAt = logEntry.createdAt.toString(),
                updatedAt = logEntry.updatedAt.toString(),
                deletedAt = logEntry.deletedAt?.toString()
            )
        }
    }
}

@Serializable
data class LogEntryPreview(
    val id: String,
    val title: String?,
    val category: LogEntryCategoryDto,
    val processingStatus: LogEntryProcessingStatusDto,
    val durationSeconds: Double?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromDomain(logEntry: LogEntry): LogEntryPreview {
            return LogEntryPreview(
                id = logEntry.id.toString(),
                title = logEntry.title,
                category = LogEntryCategoryDto.fromDomain(logEntry.category),
                processingStatus = LogEntryProcessingStatusDto.fromDomain(logEntry.processingStatus),
                durationSeconds = logEntry.durationSeconds,
                createdAt = logEntry.createdAt.toString(),
                updatedAt = logEntry.updatedAt.toString()
            )
        }
    }
}

@Serializable
enum class LogEntryProcessingStatusDto {
    PENDING,
    UPLOADING,
    UPLOADED,
    TRANSCRIBING,
    TRANSCRIBED,
    ENRICHING,
    COMPLETED,
    FAILED;

    companion object {
        fun fromDomain(status: LogEntryProcessingStatus): LogEntryProcessingStatusDto = when (status) {
            LogEntryProcessingStatus.PENDING -> PENDING
            LogEntryProcessingStatus.UPLOADING -> UPLOADING
            LogEntryProcessingStatus.UPLOADED -> UPLOADED
            LogEntryProcessingStatus.TRANSCRIBING -> TRANSCRIBING
            LogEntryProcessingStatus.TRANSCRIBED -> TRANSCRIBED
            LogEntryProcessingStatus.ENRICHING -> ENRICHING
            LogEntryProcessingStatus.COMPLETED -> COMPLETED
            LogEntryProcessingStatus.FAILED -> FAILED
        }
    }

    fun toDomain(): LogEntryProcessingStatus = when (this) {
        PENDING -> LogEntryProcessingStatus.PENDING
        UPLOADING -> LogEntryProcessingStatus.UPLOADING
        UPLOADED -> LogEntryProcessingStatus.UPLOADED
        TRANSCRIBING -> LogEntryProcessingStatus.TRANSCRIBING
        TRANSCRIBED -> LogEntryProcessingStatus.TRANSCRIBED
        ENRICHING -> LogEntryProcessingStatus.ENRICHING
        COMPLETED -> LogEntryProcessingStatus.COMPLETED
        FAILED -> LogEntryProcessingStatus.FAILED
    }
}

@Serializable
enum class LogEntryCategoryDto {
    MISSION,
    OPERATIONS,
    PERSONAL,
    RESEARCH,
    OTHER;

    companion object {
        fun fromDomain(category: LogEntryCategory): LogEntryCategoryDto = when (category) {
            LogEntryCategory.MISSION -> MISSION
            LogEntryCategory.OPERATIONS -> OPERATIONS
            LogEntryCategory.PERSONAL -> PERSONAL
            LogEntryCategory.RESEARCH -> RESEARCH
            LogEntryCategory.OTHER -> OTHER
        }
    }

    fun toDomain(): LogEntryCategory = when (this) {
        MISSION -> LogEntryCategory.MISSION
        OPERATIONS -> LogEntryCategory.OPERATIONS
        PERSONAL -> LogEntryCategory.PERSONAL
        RESEARCH -> LogEntryCategory.RESEARCH
        OTHER -> LogEntryCategory.OTHER
    }
}

@Serializable
enum class LogEntryOrderByDto {
    CREATED_AT,
    UPDATED_AT,
    TITLE;

    fun toDomain(): LogEntryOrderBy = when (this) {
        CREATED_AT -> LogEntryOrderBy.CREATED_AT
        UPDATED_AT -> LogEntryOrderBy.UPDATED_AT
        TITLE -> LogEntryOrderBy.TITLE
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

@Serializable
enum class LogEntryUpsertStatusDto {
    PENDING,
    UPLOADING,
    UPLOADED;

    fun toDomain(): LogEntryUpsertStatus = when (this) {
        PENDING -> LogEntryUpsertStatus.PENDING
        UPLOADING -> LogEntryUpsertStatus.UPLOADING
        UPLOADED -> LogEntryUpsertStatus.UPLOADED
    }
}
