package com.cleanai.modules.messaging.infrastructure.websocket

import kotlinx.serialization.Serializable

@Serializable
enum class WsResponseStatus {
    SUCCESS,
    ERROR
}

@Serializable
data class EntityChangedMessageDto(
    val id: String,
    val entityId: String,
    val entityType: String,
    val changedByUserId: String,
    val createdAt: String
)

@Serializable
data class AckMessageDTO(
    val messageId: String? = null
)

@Serializable
data class WsResponseDTO(
    val status: WsResponseStatus,
    val error: String? = null
)
