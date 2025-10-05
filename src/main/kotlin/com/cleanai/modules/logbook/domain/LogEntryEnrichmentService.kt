package com.cleanai.modules.logbook.domain

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.structure.StructureFixingParser
import ai.koog.prompt.structure.StructuredOutput
import ai.koog.prompt.structure.StructuredOutputConfig
import ai.koog.prompt.structure.executeStructured
import ai.koog.prompt.structure.json.JsonStructuredData
import ai.koog.prompt.structure.json.generator.BasicJsonSchemaGenerator
import com.cleanai.libs.llm.LLMProvider
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@ApplicationScoped
class LogEntryEnrichmentService(
    private val llmProvider: LLMProvider
) {
    private val log = LoggerFactory.getLogger(LogEntryEnrichmentService::class.java)

    fun enrichLogEntry(logEntry: LogEntry): LogEntry {
        log.info("Starting enrichment for log entry: ${logEntry.id}")

        try {
            val transcript = logEntry.transcript
            if (transcript.isNullOrBlank()) {
                return logEntry.copy(enrichmentError = "No transcript available for enrichment")
            }

            val structureProposalSchema = JsonStructuredData.createJsonStructure<LogEntryStructureProposal>(
                schemaGenerator = BasicJsonSchemaGenerator.Default,
            )

            val enrichmentPrompt = prompt("log-entry-enrichment") {
                system(
                    """
                    You are an AI assistant specialized in summarizing starship log entries.

                    Analyze the transcript and provide:
                    1. A descriptive title that captures the central theme (max 50 words)
                    2. A concise plain-text summary of key events (max 200 words)
                    3. A category that best fits the content

                    Available categories:
                    - MISSION: mission briefings, after-action reports, away team summaries
                    - OPERATIONS: ship systems, engineering updates, bridge operations
                    - PERSONAL: reflections, personal notes, crew wellbeing updates
                    - RESEARCH: scientific findings, anomalies, experiments
                    - OTHER: anything that does not fit the categories above

                    Focus on clarity, accuracy, and actionable insight.
                    Provide only the structured data specified in the schema; no additional commentary.
                    """.trimIndent()
                )

                user("Transcript: $transcript")
            }

            val executor = llmProvider.getGeminiExecutor()
            val structureProposalResult = runBlocking {
                executor.executeStructured(
                    enrichmentPrompt,
                    GoogleModels.Gemini2_5Flash,
                    config = StructuredOutputConfig(
                        default = StructuredOutput.Manual(structureProposalSchema),
                        fixingParser = StructureFixingParser(
                            fixingModel = GoogleModels.Gemini2_5Flash,
                            retries = 2
                        )
                    )
                )
            }

            val structureProposalWrapper = structureProposalResult.getOrNull()
                ?: return logEntry.copy(
                    enrichmentError = "Failed to enrich log entry: ${structureProposalResult.exceptionOrNull()?.message}"
                )

            log.info("Successfully enriched log entry: ${logEntry.id}")

            val structureProposal = structureProposalWrapper.structure
            return logEntry.copy(
                title = structureProposal.title,
                summaryText = structureProposal.summaryText,
                category = structureProposal.category,
                enrichmentError = null,
                processingStatus = LogEntryProcessingStatus.COMPLETED
            )
        } catch (exception: Exception) {
            val errorMessage = "Enrichment failed: ${exception.message}"
            log.error("Failed to enrich log entry: ${logEntry.id}", exception)

            return logEntry.copy(
                enrichmentError = errorMessage
            )
        }
    }
}
