package com.cleanai.modules.messaging.domain

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class EntityChangedMessageHandler(
    private val messageService: EntityChangedMessageService,
    private val messageDeliveryService: MessageDeliveryService
) {

    fun onEntityChanged(@Observes event: EntityChangedEvent) {
        val messages = messageService.createMessages(
            entityId = event.entityId,
            entityType = event.entityType,
            changedByUserId = event.changedByUserId,
            receiverUserIds = event.receiverUserIds
        )

        // Attempt real-time delivery to active connections
        messageDeliveryService.deliverMessages(messages)
    }
}
