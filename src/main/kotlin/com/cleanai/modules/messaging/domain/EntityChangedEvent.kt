package com.cleanai.modules.messaging.domain

import java.util.*

data class EntityChangedEvent(
    val entityId: UUID,
    val entityType: EntityType,
    val changedByUserId: String,
    val receiverUserIds: List<String>
)
