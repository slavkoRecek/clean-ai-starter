package com.cleanai.modules.messaging.domain

interface MessageDeliveryService {
    fun deliverMessages(messages: List<EntityChangedMessage>)
}
