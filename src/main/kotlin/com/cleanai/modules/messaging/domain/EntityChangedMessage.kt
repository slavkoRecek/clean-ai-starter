package com.cleanai.modules.messaging.domain

import java.time.Instant
import java.util.*

data class EntityChangedMessage(
    val id: UUID,
    val entityId: UUID,
    val entityType: EntityType,
    val changedByUserId: String,
    val receiverUserId: String,
    val status: MessageStatus,
    val createdAt: Instant,
    val acknowledgedAt: Instant?
) {
    companion object {
        fun create(
            entityId: UUID,
            entityType: EntityType,
            changedByUserId: String,
            receiverUserId: String
        ): EntityChangedMessage {
            return EntityChangedMessage(
                id = UUID.randomUUID(),
                entityId = entityId,
                entityType = entityType,
                changedByUserId = changedByUserId,
                receiverUserId = receiverUserId,
                status = MessageStatus.PENDING,
                createdAt = Instant.now(),
                acknowledgedAt = null
            )
        }
    }

    fun acknowledge(): EntityChangedMessage {
        return copy(
            status = MessageStatus.ACKNOWLEDGED,
            acknowledgedAt = Instant.now()
        )
    }
}

enum class MessageStatus {
    PENDING,
    ACKNOWLEDGED
}

enum class EntityType {
    TODO,
    NOTE,
    LOG_ENTRY,
    LOG_ENTRY_ARTIFACT,
    PROJECT,
    FOLDER
}
