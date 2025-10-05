package com.cleanai.modules.messaging.domain

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import java.util.*

@ApplicationScoped
class EntityChangedEventPublisher(
    private val event: Event<EntityChangedEvent>
) {

    fun fireEntityChanged(
        entityId: UUID,
        entityType: EntityType,
        changedByUserId: String,
        receiverUserIds: List<String>
    ) {
        val entityChangedEvent = EntityChangedEvent(
            entityId = entityId,
            entityType = entityType,
            changedByUserId = changedByUserId,
            receiverUserIds = receiverUserIds
        )
        event.fire(entityChangedEvent)
    }
}
