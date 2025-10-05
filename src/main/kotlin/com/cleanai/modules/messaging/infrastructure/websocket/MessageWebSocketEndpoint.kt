package com.cleanai.modules.messaging.infrastructure.websocket

import com.cleanai.modules.messaging.domain.EntityChangedMessageService
import io.quarkus.websockets.next.*
import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import java.util.*
import org.jboss.logging.Logger
import kotlinx.serialization.json.Json

@Authenticated
@WebSocket(path = "/ws/entity-changed-messages")
@ApplicationScoped
class MessageWebSocketEndpoint(
    private val messageService: EntityChangedMessageService,
    private val connectionRegistry: WebSocketConnectionRegistry,
    private val securityIdentity: SecurityIdentity
) {


    private val logger = Logger.getLogger(MessageWebSocketEndpoint::class.java)

    @OnOpen
    fun onOpen(connection: WebSocketConnection) {
        try {
            // Get user ID from authenticated security identity (standard Quarkus approach)
            val userId = securityIdentity.principal.name
            logger.debug("WebSocket connection opened for user: $userId")

            // Register the connection
            connectionRegistry.addConnection(userId, connection)

            // Send all pending messages for this user
            val pendingMessages = messageService.getPendingMessages(userId)
            pendingMessages.forEach { message ->
                val messageJson = createMessageJson(message)
                connection.sendTextAndAwait(messageJson)
            }
        } catch (e: Exception) {
            val userId = try { securityIdentity.principal.name } catch (ex: Exception) { "unknown" }
            logger.error("Failed to open WebSocket connection for user $userId: ${e.message}", e)
            connection.close()
        }
    }

    @OnClose
    fun onClose() {
        try {
            val userId = securityIdentity.principal.name
            connectionRegistry.removeConnectionForUser(userId)
            logger.debug("WebSocket connection closed for user: $userId")
        } catch (e: Exception) {
            logger.warn("Error during WebSocket close", e)
        }
    }

    @OnTextMessage
    fun onMessage(message: String): String {
        try {
            val userId = securityIdentity.principal.name

            // Parse acknowledgment message
            val messageData = parseAckMessage(message)
            if (messageData == null) {
                logger.warn("Invalid message format: $message")
                return createAckResponse(WsResponseStatus.ERROR, "Could not parse message")
            }

            if (messageData.messageId != null) {
                val messageId = UUID.fromString(messageData.messageId)
                messageService.acknowledgeMessage(messageId, userId)
                logger.debug("Message $messageId acknowledged by user $userId")
                return createAckResponse(WsResponseStatus.SUCCESS)
            }

            return createAckResponse(WsResponseStatus.ERROR, "Invalid message format")
        } catch (e: Exception) {
            logger.error("Failed to process acknowledgment", e)
            return createAckResponse(WsResponseStatus.ERROR, "Failed to process acknowledgment: ${e.message}")
        }
    }

    @OnError
    fun onError(throwable: Throwable) {
        try {
            val userId = securityIdentity.principal.name
            connectionRegistry.removeConnectionForUser(userId)
            logger.warn("WebSocket error for user $userId", throwable)
        } catch (e: Exception) {
            logger.error("Error during WebSocket error handling", e)
        }
    }

    private fun createMessageJson(message: com.cleanai.modules.messaging.domain.EntityChangedMessage): String {
        val messageJson = EntityChangedMessageDto(
            id = message.id.toString(),
            entityId = message.entityId.toString(),
            entityType = message.entityType.name.lowercase(),
            changedByUserId = message.changedByUserId,
            createdAt = message.createdAt.toString()
        )
        return Json.encodeToString(messageJson)
    }

    private fun parseAckMessage(message: String): AckMessageDTO? {
        return try {
            Json.decodeFromString<AckMessageDTO>(message)
        } catch (e: Exception) {
            logger.warn("Failed to parse acknowledgment message: $message", e)
            null
        }
    }

    private fun createAckResponse(status: WsResponseStatus, error: String? = null): String {
        val response = WsResponseDTO(
            status = status,
            error = error
        )
        return Json.encodeToString(response)
    }
}
