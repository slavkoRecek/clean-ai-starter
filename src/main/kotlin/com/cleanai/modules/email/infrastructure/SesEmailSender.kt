package com.cleanai.modules.email.infrastructure

import com.cleanai.modules.email.domain.EmailResult
import com.cleanai.modules.email.domain.EmailSender
import com.cleanai.modules.email.domain.TemplatedEmail
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest

@ApplicationScoped
class SesEmailSender(
    private val sesClient: SesClient,
    @ConfigProperty(name = "app.email.default-sender") private val defaultSender: String
) : EmailSender {
    private val log = LoggerFactory.getLogger(SesEmailSender::class.java)

    override fun sendTemplatedEmail(email: TemplatedEmail): EmailResult {
        try {
            val senderAddress = email.from?.address ?: defaultSender

            val request = SendTemplatedEmailRequest.builder()
                .source(senderAddress)
                .destination(
                    Destination.builder()
                        .toAddresses(email.to.address)
                        .build()
                )
                .template(email.templateName.templateName)
                .templateData(buildJsonTemplateData(email.templateData))
                .build()

            val response = sesClient.sendTemplatedEmail(request)

            return EmailResult(
                messageId = response.messageId(),
                success = true
            )
        } catch (e: Exception) {
            log.warn("Failed to send templated email:", e.message)
            return EmailResult(
                messageId = "",
                success = false,
                errorMessage = "Failed to send templated email: ${e.message}"
            )
        }
    }

    private fun buildJsonTemplateData(data: Map<String, String>): String {
        // SES expects template data as a JSON string
        return buildString {
            append("{")
            append(data.entries.joinToString(",") { (key, value) ->
                "\"$key\":\"$value\""
            })
            append("}")
        }
    }
}
