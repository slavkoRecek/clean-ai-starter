package com.cleanai.modules.messaging.infrastructure.persistence

import com.cleanai.modules.messaging.domain.EntityChangedMessage
import com.cleanai.modules.messaging.domain.EntityChangedMessageRepository
import com.cleanai.modules.messaging.domain.MessageStatus
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class PgEntityChangedMessageRepository : EntityChangedMessageRepository {

    override fun findById(id: UUID): EntityChangedMessage? {
        return EntityChangedMessageEntity.findById(id)?.toDomain()
    }

    override fun findPendingByReceiverUserId(receiverUserId: String): List<EntityChangedMessage> {
        return EntityChangedMessageEntity.find(
            "receiverUserId = ?1 and status = ?2",
            Sort.by("createdAt", Sort.Direction.Ascending),
            receiverUserId,
            MessageStatus.PENDING.name.uppercase()
        ).list().map { it.toDomain() }
    }

    @Transactional
    override fun persist(message: EntityChangedMessage): EntityChangedMessage {
        val existingEntity = EntityChangedMessageEntity.findById(message.id)

        val entity = if (existingEntity != null) {
            // Update existing entity
            existingEntity.apply {
                entityId = message.entityId
                entityType = message.entityType.name.lowercase()
                changedByUserId = message.changedByUserId
                receiverUserId = message.receiverUserId
                status = message.status.name.uppercase()
                createdAt = message.createdAt
                acknowledgedAt = message.acknowledgedAt
            }
        } else {
            // Create new entity
            EntityChangedMessageEntity.from(message)
        }

        entity.persistAndFlush()
        return entity.toDomain()
    }

    @Transactional
    override fun persistAll(messages: List<EntityChangedMessage>): List<EntityChangedMessage> {
        return messages.map { persist(it) }
    }
}
