package com.cleanai.modules.messaging.infrastructure.websocket

import com.cleanai.modules.messaging.domain.EntityChangedMessage
import com.cleanai.modules.messaging.domain.MessageDeliveryService
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import kotlinx.serialization.json.Json

@ApplicationScoped
class WebSocketMessageDeliveryService(
    private val connectionRegistry: WebSocketConnectionRegistry
) : MessageDeliveryService {

    private val logger = Logger.getLogger(WebSocketMessageDeliveryService::class.java)

    override fun deliverMessages(messages: List<EntityChangedMessage>) {
        messages.forEach { message ->
            deliverMessage(message)
        }
    }

    fun deliverMessage(message: EntityChangedMessage): Boolean {
        val connection = connectionRegistry.getConnectionForUser(message.receiverUserId)

        return if (connection != null) {
            try {
                val messageJson = createMessageJson(message)
                connection.sendTextAndAwait(messageJson)
                logger.debug("Message ${message.id} delivered to user ${message.receiverUserId}")
                true
            } catch (e: Exception) {
                logger.warn("Failed to deliver message ${message.id} to user ${message.receiverUserId}: ${e.message}")
                false
            }
        } else {
            logger.debug("No active connection for user ${message.receiverUserId}, message ${message.id} remains pending")
            false
        }
    }


    private fun createMessageJson(message: EntityChangedMessage): String {
        val messageJson = EntityChangedMessageDto(
            id = message.id.toString(),
            entityId = message.entityId.toString(),
            entityType = message.entityType.name.lowercase(),
            changedByUserId = message.changedByUserId,
            createdAt = message.createdAt.toString()
        )
        return Json.encodeToString(messageJson)
    }

}
