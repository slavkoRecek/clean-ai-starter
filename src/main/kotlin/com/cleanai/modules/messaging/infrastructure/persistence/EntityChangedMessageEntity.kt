package com.cleanai.modules.messaging.infrastructure.persistence

import com.cleanai.modules.messaging.domain.EntityChangedMessage
import com.cleanai.modules.messaging.domain.EntityType
import com.cleanai.modules.messaging.domain.MessageStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "entity_changed_messages",
    indexes = [
        Index(name = "idx_entity_changed_messages_receiver_user_id", columnList = "receiver_user_id"),
        Index(name = "idx_entity_changed_messages_receiver_status", columnList = "receiver_user_id, status"),
        Index(name = "idx_entity_changed_messages_entity", columnList = "entity_id, entity_type"),
        Index(name = "idx_entity_changed_messages_created_at", columnList = "created_at"),
        Index(name = "idx_entity_changed_messages_changed_by", columnList = "changed_by_user_id")
    ]
)
class EntityChangedMessageEntity : PanacheEntityBase {
    companion object : PanacheCompanionBase<EntityChangedMessageEntity, UUID> {

        fun from(domain: EntityChangedMessage) = EntityChangedMessageEntity().apply {
            id = domain.id
            entityId = domain.entityId
            entityType = domain.entityType.name.lowercase()
            changedByUserId = domain.changedByUserId
            receiverUserId = domain.receiverUserId
            status = domain.status.name.uppercase()
            createdAt = domain.createdAt
            acknowledgedAt = domain.acknowledgedAt
        }
    }

    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID? = null

    @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
    var entityId: UUID? = null

    @Column(name = "entity_type", nullable = false, length = 50)
    var entityType: String? = null

    @Column(name = "changed_by_user_id", nullable = false, length = 255)
    var changedByUserId: String? = null

    @Column(name = "receiver_user_id", nullable = false, length = 255)
    var receiverUserId: String? = null

    @Column(name = "status", nullable = false, length = 50)
    var status: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "acknowledged_at")
    var acknowledgedAt: Instant? = null

    fun toDomain(): EntityChangedMessage {
        return EntityChangedMessage(
            id = id!!,
            entityId = entityId!!,
            entityType = EntityType.valueOf(entityType!!.uppercase()),
            changedByUserId = changedByUserId!!,
            receiverUserId = receiverUserId!!,
            status = MessageStatus.valueOf(status!!.uppercase()),
            createdAt = createdAt!!,
            acknowledgedAt = acknowledgedAt
        )
    }
}
