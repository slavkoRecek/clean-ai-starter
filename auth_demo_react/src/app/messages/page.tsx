'use client'

import { useAuth } from '@clerk/nextjs'
import { useEntityMessages } from '@/hooks/useEntityMessages'
import { ConnectionStatusIndicator } from '@/components/ConnectionStatus'
import { MessageList } from '@/components/MessageList'
import Link from 'next/link'

function UnauthenticatedView() {
  return (
    <div className="container mx-auto p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-6">Entity Changed Messages</h1>
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
          <p className="text-yellow-800 mb-4">
            Please sign in to view entity change notifications from the backend.
          </p>
          <Link
            href="/"
            className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-lg inline-block transition-colors"
          >
            Go to Home Page
          </Link>
        </div>
      </div>
    </div>
  )
}

function TestingInstructions() {
  return (
    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <h2 className="font-semibold text-blue-900 mb-2">Testing Instructions:</h2>
      <ol className="list-decimal list-inside space-y-1 text-blue-800 text-sm">
        <li>Make sure your backend is running on localhost:8080</li>
        <li>The WebSocket will automatically connect when this page loads</li>
        <li>Create, update, or delete entities (todos, notes, log entries) in your backend</li>
        <li>Watch for messages appearing in real-time below</li>
        <li>Click "Acknowledge" to mark individual messages as read</li>
        <li>Use "Acknowledge All" to mark all messages as read at once</li>
      </ol>
    </div>
  )
}

function DebugInfo({ connectionStatus, messageCount }: { connectionStatus: string; messageCount: number }) {
  const getWebSocketUrl = () => {
    if (typeof window === 'undefined') return 'N/A'

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = process.env.NODE_ENV === 'development' ? 'localhost:8080' : window.location.host
    return `${protocol}//${host}/ws/entity-changed-messages`
  }

  return (
    <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
      <h3 className="font-semibold text-gray-700 mb-2">Debug Info:</h3>
      <div className="text-xs text-gray-600 space-y-1">
        <div>Connection Status: {connectionStatus}</div>
        <div>Messages in queue: {messageCount}</div>
        <div>WebSocket URL: {getWebSocketUrl()}</div>
      </div>
    </div>
  )
}

export default function MessagesPage() {
  const { isSignedIn } = useAuth()
  const {
    messages,
    connectionStatus,
    isConnected,
    connect,
    disconnect,
    acknowledgeMessage,
    acknowledgeAllMessages,
    clearMessages
  } = useEntityMessages()

  if (!isSignedIn) {
    return <UnauthenticatedView />
  }

  return (
    <div className="container mx-auto p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Entity Changed Messages</h1>
          <Link
            href="/"
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            ← Back to Home
          </Link>
        </div>

        {/* Instructions */}
        <TestingInstructions />

        {/* Connection Status */}
        <ConnectionStatusIndicator
          status={connectionStatus}
          onConnect={connect}
          onDisconnect={disconnect}
        />

        {/* Message List */}
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-semibold">Live Messages</h2>
            {isConnected && (
              <div className="text-sm text-green-600 font-medium">
                ✓ Listening for changes...
              </div>
            )}
          </div>

          <MessageList
            messages={messages}
            onAcknowledge={acknowledgeMessage}
            onAcknowledgeAll={acknowledgeAllMessages}
            onClearAll={clearMessages}
            isConnected={isConnected}
          />
        </div>

        {/* Debug Info - only show when there are messages */}
        {messages.length > 0 && (
          <DebugInfo
            connectionStatus={connectionStatus}
            messageCount={messages.length}
          />
        )}
      </div>
    </div>
  )
}
