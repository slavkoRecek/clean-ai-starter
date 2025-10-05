package com.cleanai.modules.messaging.domain

import java.util.*

interface EntityChangedMessageRepository {
    fun findById(id: UUID): EntityChangedMessage?
    fun findPendingByReceiverUserId(receiverUserId: String): List<EntityChangedMessage>
    fun persist(message: EntityChangedMessage): EntityChangedMessage
    fun persistAll(messages: List<EntityChangedMessage>): List<EntityChangedMessage>
}
