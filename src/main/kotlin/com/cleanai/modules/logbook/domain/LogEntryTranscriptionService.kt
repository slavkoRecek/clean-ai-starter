package com.cleanai.modules.logbook.domain

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.message.Attachment
import ai.koog.prompt.message.AttachmentContent
import com.cleanai.libs.llm.LLMProvider
import com.cleanai.modules.files.domain.FileService
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@ApplicationScoped
class LogEntryTranscriptionService(
    private val fileService: FileService,
    private val llmProvider: LLMProvider
) {
    private val log = LoggerFactory.getLogger(LogEntryTranscriptionService::class.java)

    fun transcribeLogEntry(logEntry: LogEntry): LogEntry {
        log.info("Starting transcription for log entry: ${logEntry.id}")

        try {
            val audioFileId = logEntry.audioFileId
                ?: return logEntry.copy(transcriptionError = "No audio file associated with this log entry")

            log.debug("Retrieving audio content for file: $audioFileId")
            val audioContent = fileService.getFileContent(logEntry.authorId, audioFileId)

            if (audioContent.isEmpty()) {
                return logEntry.copy(transcriptionError = "Audio file is empty or corrupted")
            }

            val transcriptionPrompt = prompt("log-entry-transcription") {
                system(
                    """
                    You are a professional transcription service. Your task is to transcribe the provided audio log accurately.

                    Requirements:
                    - Provide a clean, readable transcript of the spoken content
                    - Use proper punctuation and formatting
                    - Maintain natural paragraph or bullet breaks for readability
                    - Focus on accuracy over speed
                    - Remove filler words and background noise indicators.

                    Return only the transcript text without any additional commentary or metadata.
                    """.trimIndent()
                )

                user {
                    text("Please transcribe this audio log accurately.")
                    attachments {
                        audio(
                            Attachment.Audio(
                                content = AttachmentContent.Binary.Bytes(audioContent),
                                format = "aac"
                            )
                        )
                    }
                }
            }

            val executor = llmProvider.getGeminiExecutor()
            val transcript = runBlocking {
                executor.execute(transcriptionPrompt, GoogleModels.Gemini2_5Flash).single().content
            }

            if (transcript.isBlank()) {
                return logEntry.copy(transcriptionError = "Transcription resulted in empty text")
            }

            log.info("Successfully transcribed log entry: ${logEntry.id}")

            return logEntry.copy(
                transcript = transcript,
                transcriptionError = null,
                processingStatus = LogEntryProcessingStatus.TRANSCRIBED
            )
        } catch (exception: Exception) {
            val errorMessage = "Transcription failed: ${exception.message}"
            log.error("Failed to transcribe log entry: ${logEntry.id}", exception)

            return logEntry.copy(
                transcriptionError = errorMessage
            )
        }
    }
}
