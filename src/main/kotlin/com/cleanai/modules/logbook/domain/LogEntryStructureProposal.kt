package com.cleanai.modules.logbook.domain

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LogEntryStructureProposal")
@LLMDescription("Structured proposal for log entry metadata extraction including title, summary, and categorization")
data class LogEntryStructureProposal(
    @property:LLMDescription("A descriptive title for the log entry, extracted from the transcript")
    val title: String,

    @property:LLMDescription("A concise plain text summary of the log entry content")
    val summaryText: String,

    @property:LLMDescription("Category classification for the log entry (MISSION, OPERATIONS, PERSONAL, RESEARCH, OTHER)")
    val category: LogEntryCategory,
)
