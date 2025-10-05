package com.cleanai.modules.messaging.infrastructure.websocket

import io.quarkus.websockets.next.WebSocketConnection
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class WebSocketConnectionRegistry {

    private val connections = ConcurrentHashMap<String, WebSocketConnection>()

    fun addConnection(userId: String, connection: WebSocketConnection) {
        // Remove any existing connection for this user first
        removeConnectionForUser(userId)
        connections[userId] = connection
    }

    fun removeConnectionForUser(userId: String) {
        connections.remove(userId)
    }

    fun getConnectionForUser(userId: String): WebSocketConnection? {
        return connections[userId]?.takeIf { it.isOpen }
    }

    fun hasActiveConnection(userId: String): Boolean {
        return getConnectionForUser(userId) != null
    }

    fun removeClosedConnections() {
        val closedConnections = connections.entries
            .filter { !it.value.isOpen }
            .map { it.key }

        closedConnections.forEach { userId ->
            connections.remove(userId)
        }
    }
}
