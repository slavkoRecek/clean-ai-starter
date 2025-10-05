package com.cleanai.modules.logbook.domain

import ai.koog.agents.testing.client.CapturingLLMClient
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.ResponseMetaInfo
import com.cleanai.libs.llm.LLMProvider
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class LogEntryEnrichmentServiceTest {

    private val llmProvider: LLMProvider = mock()

    @Test
    fun `enrichLogEntry should update log entry with structured data`() {
        val logEntry = sampleLogEntry()
        val expectedProposal = LogEntryStructureProposal(
            title = "Project Launch Briefing",
            summaryText = "Discussed launch timeline and assigned follow-up tasks.",
            category = LogEntryCategory.MISSION,
        )

        val jsonResponse = Json.encodeToString(expectedProposal)
        val mockClient = CapturingLLMClient(
            executeResponses = listOf(
                Message.Assistant(
                    content = jsonResponse,
                    metaInfo = ResponseMetaInfo(
                        timestamp = Clock.System.now(),
                        totalTokensCount = null,
                        inputTokensCount = null,
                        outputTokensCount = null,
                        additionalInfo = emptyMap()
                    ),
                    attachments = emptyList(),
                    finishReason = null
                )
            ),
            streamingChunks = emptyList(),
            choices = emptyList(),
            moderationResult = ModerationResult(
                isHarmful = false,
                categories = emptyMap()
            )
        )
        val mockExecutor = SingleLLMPromptExecutor(mockClient)

        whenever(llmProvider.getGeminiExecutor()).thenReturn(mockExecutor)

        val service = LogEntryEnrichmentService(llmProvider)

        val enrichedLogEntry = service.enrichLogEntry(logEntry)

        assertEquals(expectedProposal.title, enrichedLogEntry.title)
        assertEquals(expectedProposal.summaryText, enrichedLogEntry.summaryText)
        assertEquals(expectedProposal.category, enrichedLogEntry.category)
        assertNull(enrichedLogEntry.enrichmentError)
        assertEquals(LogEntryProcessingStatus.COMPLETED, enrichedLogEntry.processingStatus)
    }

    private fun sampleLogEntry(): LogEntry {
        val now = Instant.now()
        return LogEntry(
            id = UUID.randomUUID(),
            authorId = "author-123",
            audioFileId = null,
            audioUrl = null,
            processingStatus = LogEntryProcessingStatus.UPLOADED,
            transcript = "We agreed to send assets to marketing and review the QA checklist by Friday.",
            structuredSummary = null,
            summaryText = null,
            title = null,
            category = LogEntryCategory.OTHER,
            durationSeconds = null,
            folderId = null,
            transcriptionError = null,
            enrichmentError = null,
            archived = false,
            deleted = false,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
    }
}
