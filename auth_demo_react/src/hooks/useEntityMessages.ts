'use client'

import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '@clerk/nextjs'
import useWebSocket, { ReadyState } from 'react-use-websocket'
import { EntityChangedMessage, AckMessage, ConnectionStatus } from '@/types/websocket'

interface WebSocketConfig {
  url: string
  protocols: string[]
}

export function useEntityMessages() {
  const { getToken, isSignedIn } = useAuth()
  const [messages, setMessages] = useState<EntityChangedMessage[]>([])
  const [socketUrl, setSocketUrl] = useState<WebSocketConfig | null>(null)

  // Convert ReadyState to our ConnectionStatus enum
  const getConnectionStatus = (readyState: ReadyState): ConnectionStatus => {
    switch (readyState) {
      case ReadyState.CONNECTING:
        return ConnectionStatus.CONNECTING
      case ReadyState.OPEN:
        return ConnectionStatus.CONNECTED
      case ReadyState.CLOSING:
      case ReadyState.CLOSED:
        return ConnectionStatus.DISCONNECTED
      case ReadyState.UNINSTANTIATED:
      default:
        return ConnectionStatus.DISCONNECTED
    }
  }

  // Helper functions
  const isValidEntityMessage = (message: any): message is EntityChangedMessage => {
    return message?.id && message?.entityId && message?.entityType
  }

  const addMessage = useCallback((message: EntityChangedMessage) => {
    setMessages(prev => {
      // Avoid duplicates
      if (prev.some(m => m.id === message.id)) {
        return prev
      }
      // Sort by creation date (newest first)
      return [...prev, message].sort((a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
    })
  }, [])

  const { sendMessage, readyState } = useWebSocket(
    socketUrl?.url || null,
    {
      onOpen: () => {
        console.log('WebSocket connected')
      },
      onClose: (event) => {
        console.log('WebSocket disconnected:', event.code, event.reason)
      },
      onError: (error) => {
        console.error('WebSocket error:', error)
      },
      onMessage: (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('Received WebSocket message:', data)

          // Skip acknowledgment responses
          if (data.status) {
            console.log('Acknowledgment response received:', data.status)
            return
          }

          // Validate and add EntityChangedMessage
          const message: EntityChangedMessage = data
          if (!isValidEntityMessage(message)) {
            console.warn('Invalid EntityChangedMessage received:', message)
            return
          }

          addMessage(message)
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error)
        }
      },
      shouldReconnect: (closeEvent) => {
        // Reconnect unless manually disconnected
        return closeEvent.code !== 1000
      },
      reconnectAttempts: 5,
      reconnectInterval: (attemptNumber) =>
        Math.min(Math.pow(2, attemptNumber) * 1000, 10000),
      protocols: socketUrl?.protocols || []
    },
    !!socketUrl && isSignedIn
  )

  const isWebSocketConnected = readyState === ReadyState.OPEN
  const connectionStatus = getConnectionStatus(readyState)

  const setupWebSocketUrl = useCallback(async () => {
    if (!isSignedIn) {
      setSocketUrl(null)
      return
    }

    try {
      const token = await getToken({ template: 'brunotesting' })
      if (!token) {
        console.error('Authentication token not available')
        setSocketUrl(null)
        return
      }

      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsHost = process.env.NODE_ENV === 'development' ? 'localhost:8080' : window.location.host
      const wsUrl = `${wsProtocol}//${wsHost}/ws/entity-changed-messages`

      // Configure Quarkus authentication via subprotocol
      const authProtocol = encodeURIComponent(`quarkus-http-upgrade#Authorization#Bearer ${token}`)
      const protocols = ['bearer-token-carrier', authProtocol]

      setSocketUrl({ url: wsUrl, protocols })
      console.log('WebSocket configured for entity messages')
    } catch (error) {
      console.error('Failed to configure WebSocket:', error)
      setSocketUrl(null)
    }
  }, [isSignedIn, getToken])

  const connect = useCallback(() => {
    setupWebSocketUrl()
  }, [setupWebSocketUrl])

  const disconnect = useCallback(() => {
    setSocketUrl(null)
  }, [])

  const acknowledgeMessage = useCallback(async (messageId: string): Promise<boolean> => {
    if (!isWebSocketConnected) {
      console.error('Cannot acknowledge: WebSocket not connected')
      return false
    }

    try {
      const ackMessage: AckMessage = { messageId }
      sendMessage(JSON.stringify(ackMessage))

      // Remove acknowledged message from local state
      setMessages(prev => prev.filter(m => m.id !== messageId))
      return true
    } catch (error) {
      console.error(`Failed to acknowledge message ${messageId}:`, error)
      return false
    }
  }, [sendMessage, isWebSocketConnected])

  const clearMessages = useCallback(() => {
    setMessages([])
  }, [])

  const acknowledgeAllMessages = useCallback(async (): Promise<boolean> => {
    if (!isWebSocketConnected) {
      console.error('Cannot acknowledge all: WebSocket not connected')
      return false
    }

    if (messages.length === 0) {
      return true
    }

    try {
      let successCount = 0
      let errorCount = 0

      // Send acknowledgment for each message
      for (const message of messages) {
        try {
          if (message.id) {
            const ackMessage: AckMessage = { messageId: message.id }
            sendMessage(JSON.stringify(ackMessage))
            successCount++
          } else {
            console.warn('Skipping message with missing ID')
            errorCount++
          }
        } catch (error) {
          console.error(`Failed to acknowledge message ${message.id}:`, error)
          errorCount++
        }
      }

      // Clear all messages from local state
      setMessages([])

      const wasSuccessful = errorCount === 0
      console.log(`Acknowledged ${successCount}/${messages.length} messages`)
      return wasSuccessful
    } catch (error) {
      console.error('Failed to acknowledge all messages:', error)
      return false
    }
  }, [sendMessage, isWebSocketConnected, messages])

  // Auto-configure WebSocket when authentication state changes
  useEffect(() => {
    if (isSignedIn) {
      setupWebSocketUrl()
    } else {
      setSocketUrl(null)
      setMessages([])
    }
  }, [isSignedIn, setupWebSocketUrl])

  return {
    messages,
    connectionStatus,
    isConnected: connectionStatus === ConnectionStatus.CONNECTED,
    connect,
    disconnect,
    acknowledgeMessage,
    acknowledgeAllMessages,
    clearMessages
  }
}
