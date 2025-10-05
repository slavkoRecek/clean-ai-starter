package com.cleanai.modules.logbook.infrastructure.persistance

import com.cleanai.modules.logbook.domain.LogEntry
import com.cleanai.modules.logbook.domain.LogEntryCategory
import com.cleanai.modules.logbook.domain.LogEntryProcessingStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "log_entries",
    indexes = [
        Index(name = "idx_log_entries_author_id", columnList = "author_id"),
        Index(name = "idx_log_entries_author_created", columnList = "author_id, created_at"),
        Index(name = "idx_log_entries_author_updated", columnList = "author_id, updated_at"),
        Index(name = "idx_log_entries_author_processing_status", columnList = "author_id, processing_status"),
        Index(name = "idx_log_entries_author_category", columnList = "author_id, category"),
        Index(name = "idx_log_entries_author_deleted", columnList = "author_id, deleted_at")
    ]
)
class LogEntryEntity : PanacheEntityBase {
    companion object : PanacheCompanionBase<LogEntryEntity, UUID> {
        fun from(domain: LogEntry) = LogEntryEntity().apply {
            id = domain.id
            authorId = domain.authorId
            audioFileId = domain.audioFileId
            audioUrl = domain.audioUrl
            processingStatus = domain.processingStatus.name.lowercase()
            transcript = domain.transcript
            structuredSummary = domain.structuredSummary
            summaryText = domain.summaryText
            title = domain.title
            category = domain.category.name.lowercase()
            durationSeconds = domain.durationSeconds?.let { BigDecimal.valueOf(it) }
            folderId = domain.folderId
            transcriptionError = domain.transcriptionError
            enrichmentError = domain.enrichmentError
            archived = domain.archived
            deleted = domain.deleted
            createdAt = domain.createdAt
            updatedAt = domain.updatedAt
            deletedAt = domain.deletedAt
        }
    }

    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID? = null

    @Column(name = "author_id", nullable = false)
    var authorId: String? = null

    @Column(name = "audio_file_id", columnDefinition = "uuid")
    var audioFileId: UUID? = null

    @Column(name = "audio_url", length = 500)
    var audioUrl: String? = null

    @Column(name = "processing_status", nullable = false, length = 50)
    var processingStatus: String = "pending"

    @Column(name = "transcript", columnDefinition = "TEXT")
    var transcript: String? = null

    @Column(name = "structured_summary", columnDefinition = "JSONB")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    var structuredSummary: String? = null

    @Column(name = "summary_text", columnDefinition = "TEXT")
    var summaryText: String? = null

    @Column(name = "title", length = 255)
    var title: String? = null

    @Column(name = "category", nullable = false, length = 50)
    var category: String = LogEntryCategory.OTHER.name.lowercase()

    @Column(name = "duration_seconds", precision = 10, scale = 3)
    var durationSeconds: BigDecimal? = null

    @Column(name = "folder_id", columnDefinition = "uuid")
    var folderId: UUID? = null

    @Column(name = "transcription_error", columnDefinition = "TEXT")
    var transcriptionError: String? = null

    @Column(name = "enrichment_error", columnDefinition = "TEXT")
    var enrichmentError: String? = null

    @Column(name = "archived", nullable = false)
    var archived: Boolean = false

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

    fun toDomain(): LogEntry {
        return LogEntry(
            id = id!!,
            authorId = authorId!!,
            audioFileId = audioFileId,
            audioUrl = audioUrl,
            processingStatus = LogEntryProcessingStatus.valueOf(processingStatus.uppercase()),
            transcript = transcript,
            structuredSummary = structuredSummary,
            summaryText = summaryText,
            title = title,
            category = LogEntryCategory.valueOf(category.uppercase()),
            durationSeconds = durationSeconds?.toDouble(),
            folderId = folderId,
            transcriptionError = transcriptionError,
            enrichmentError = enrichmentError,
            archived = archived,
            deleted = deleted,
            createdAt = createdAt!!,
            updatedAt = updatedAt!!,
            deletedAt = deletedAt
        )
    }
}
