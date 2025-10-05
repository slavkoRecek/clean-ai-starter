'use client'

import { EntityChangedMessage } from '@/types/websocket'
import { MessageItem } from './MessageItem'
import { useState } from 'react'

interface MessageListProps {
  messages: EntityChangedMessage[]
  onAcknowledge: (messageId: string) => Promise<boolean>
  onAcknowledgeAll: () => Promise<boolean>
  onClearAll: () => void
  isConnected: boolean
}

export function MessageList({ messages, onAcknowledge, onAcknowledgeAll, onClearAll, isConnected }: MessageListProps) {
  const [isAcknowledgingAll, setIsAcknowledgingAll] = useState(false)

  const handleAcknowledgeAll = async () => {
    setIsAcknowledgingAll(true)
    try {
      await onAcknowledgeAll()
    } finally {
      setIsAcknowledgingAll(false)
    }
  }
  if (messages.length === 0) {
    return (
      <div className="bg-gray-50 border-2 border-dashed border-gray-200 rounded-lg p-8 text-center">
        <div className="text-gray-500 text-lg mb-2">No messages</div>
        <div className="text-gray-400 text-sm">
          EntityChangedMessages will appear here when entities are modified in the backend
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900">
          {messages.length} Pending Message{messages.length !== 1 ? 's' : ''}
        </h3>
        <div className="flex items-center gap-3">
          {isConnected && (
            <button
              onClick={handleAcknowledgeAll}
              disabled={isAcknowledgingAll}
              className="text-sm text-green-600 hover:text-green-800 disabled:text-green-400 font-medium"
            >
              {isAcknowledgingAll ? 'Acknowledging All...' : 'Acknowledge All'}
            </button>
          )}
          <button
            onClick={onClearAll}
            className="text-sm text-red-600 hover:text-red-800 font-medium"
          >
            Clear All
          </button>
        </div>
      </div>

      <div className="space-y-3">
        {messages.map((message) => (
          <MessageItem
            key={`${message.id}-${message.createdAt}`}
            message={message}
            onAcknowledge={onAcknowledge}
          />
        ))}
      </div>
    </div>
  )
}