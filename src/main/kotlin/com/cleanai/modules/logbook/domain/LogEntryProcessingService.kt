package com.cleanai.modules.logbook.domain

import com.cleanai.modules.messaging.domain.EntityChangedEventPublisher
import com.cleanai.modules.messaging.domain.EntityType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class LogEntryProcessingService(
    private val logEntryRepository: LogEntryRepository,
    private val logEntryTranscriptionService: LogEntryTranscriptionService,
    private val logEntryEnrichmentService: LogEntryEnrichmentService,
    private val entityChangedEventPublisher: EntityChangedEventPublisher
) {
    private val log = LoggerFactory.getLogger(LogEntryProcessingService::class.java)

    @Transactional
    @ActivateRequestContext
    fun processLogEntryAsync(logEntryId: UUID) {
        try {
            processLogEntry(logEntryId)
        } catch (error: Exception) {
            log.error("Processing failed for log entry: $logEntryId", error)
            handleProcessingError(logEntryId, error)
        }
    }

    private fun processLogEntry(logEntryId: UUID) {
        log.info("Starting processing for log entry: $logEntryId")

        val logEntry = logEntryRepository.findById(logEntryId)
            ?: error("Log entry not found: $logEntryId")

        if (logEntry.processingStatus != LogEntryProcessingStatus.UPLOADED) {
            log.warn("Log entry $logEntryId not in UPLOADED status (${logEntry.processingStatus}), skipping")
            return
        }

        updateLogEntryStatus(logEntryId, LogEntryProcessingStatus.TRANSCRIBING)
        log.info("Updated log entry $logEntryId status to TRANSCRIBING")

        val transcribedLogEntry = logEntryTranscriptionService.transcribeLogEntry(logEntry)
        val updatedLogEntry = logEntryRepository.persist(transcribedLogEntry)
        log.info("Successfully transcribed log entry $logEntryId")
        emitChangeEvent(transcribedLogEntry)

        if (updatedLogEntry.transcriptionError != null) {
            log.error("Transcription failed for log entry $logEntryId: ${updatedLogEntry.transcriptionError}")
            updateLogEntryStatus(logEntryId, LogEntryProcessingStatus.FAILED)
            emitChangeEvent(transcribedLogEntry)
            return
        }

        updateLogEntryStatus(logEntryId, LogEntryProcessingStatus.ENRICHING)
        log.info("Starting enrichment for log entry $logEntryId")

        val enrichedLogEntry = logEntryEnrichmentService.enrichLogEntry(updatedLogEntry)
        val finalLogEntry = logEntryRepository.persist(enrichedLogEntry)
        emitChangeEvent(enrichedLogEntry)

        if (finalLogEntry.enrichmentError != null) {
            log.error("Enrichment failed for log entry $logEntryId: ${finalLogEntry.enrichmentError}")
            updateLogEntryStatus(logEntryId, LogEntryProcessingStatus.FAILED)
            emitChangeEvent(finalLogEntry)
            return
        }
    }

    private fun emitChangeEvent(logEntry: LogEntry) {
        val receiverUserIds = listOf(logEntry.authorId).distinct()
        entityChangedEventPublisher.fireEntityChanged(
            entityId = logEntry.id,
            entityType = EntityType.LOG_ENTRY,
            changedByUserId = logEntry.authorId,
            receiverUserIds = receiverUserIds
        )
    }

    private fun updateLogEntryStatus(logEntryId: UUID, status: LogEntryProcessingStatus) {
        val logEntry = logEntryRepository.findById(logEntryId) ?: return
        val updatedLogEntry = logEntry.copy(
            processingStatus = status,
            updatedAt = Instant.now()
        )
        logEntryRepository.persist(updatedLogEntry)
        log.debug("Updated log entry $logEntryId status to $status")
    }

    private fun handleProcessingError(logEntryId: UUID, error: Throwable) {
        log.error("Handling processing error for log entry $logEntryId: ${error.message}", error)

        val logEntry = logEntryRepository.findById(logEntryId) ?: return
        val failedLogEntry = logEntry.copy(
            processingStatus = LogEntryProcessingStatus.FAILED,
            enrichmentError = error.message,
            updatedAt = Instant.now()
        )
        logEntryRepository.persist(failedLogEntry)
        log.info("Marked log entry $logEntryId as FAILED due to error: ${error.message}")
    }
}
