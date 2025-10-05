package com.cleanai.modules.messaging.domain

import com.cleanai.libs.exception.ObjectNotFoundException
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class EntityChangedMessageService(
    private val messageRepository: EntityChangedMessageRepository
) {

    fun createMessages(
        entityId: UUID,
        entityType: EntityType,
        changedByUserId: String,
        receiverUserIds: List<String>
    ): List<EntityChangedMessage> {
        val uniqueReceiverIds = receiverUserIds.distinct()
        val messages = uniqueReceiverIds.map { receiverUserId ->
            EntityChangedMessage.create(
                entityId = entityId,
                entityType = entityType,
                changedByUserId = changedByUserId,
                receiverUserId = receiverUserId
            )
        }
        return messageRepository.persistAll(messages)
    }

    fun acknowledgeMessage(messageId: UUID, userId: String): EntityChangedMessage {
        val message = messageRepository.findById(messageId)
            ?: throw ObjectNotFoundException("EntityChangedMessage not found with id: $messageId")

        if (message.receiverUserId != userId) {
            throw ObjectNotFoundException("EntityChangedMessage not found with id: $messageId")
        }

        val acknowledgedMessage = message.acknowledge()
        return messageRepository.persist(acknowledgedMessage)
    }

    fun getPendingMessages(receiverUserId: String): List<EntityChangedMessage> {
        return messageRepository.findPendingByReceiverUserId(receiverUserId)
    }
}
