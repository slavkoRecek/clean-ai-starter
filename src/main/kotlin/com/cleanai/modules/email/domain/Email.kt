package com.cleanai.modules.email.domain

data class EmailAddress(val address: String)

enum class EmailTemplate(val templateName: String, val variableNames: List<String>) {
    MAGIC_LINK("Magic-Link-Email", listOf("magicLink"))
}

data class TemplatedEmail(
    val templateName: EmailTemplate,
    val to: EmailAddress,
    val templateData: Map<String, String>,
    val from: EmailAddress? = null
) {
    init {
        require(templateData.keys.containsAll(templateName.variableNames)) {
            val missingVariables = templateName.variableNames - templateData.keys
            "Template data must contain all required variables for template ${templateName.templateName}. Missing variables: $missingVariables"
        }
    }
}

data class EmailResult(
    val messageId: String,
    val success: Boolean,
    val errorMessage: String? = null
)

