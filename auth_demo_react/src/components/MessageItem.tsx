'use client'

import { EntityChangedMessage } from '@/types/websocket'
import { useState } from 'react'

interface MessageItemProps {
  message: EntityChangedMessage
  onAcknowledge: (messageId: string) => Promise<boolean>
}

export function MessageItem({ message, onAcknowledge }: MessageItemProps) {
  const [isAcknowledging, setIsAcknowledging] = useState(false)

  const handleAcknowledge = async () => {
    setIsAcknowledging(true)
    try {
      await onAcknowledge(message.id)
    } finally {
      setIsAcknowledging(false)
    }
  }

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) {
      return 'Unknown Date'
    }
    try {
      const date = new Date(dateString)
      if (isNaN(date.getTime())) {
        return 'Invalid Date'
      }
      return date.toLocaleString()
    } catch {
      return dateString
    }
  }

  const getEntityTypeColor = (entityType: string | undefined) => {
    if (!entityType) {
      return 'bg-gray-100 text-gray-800'
    }
    switch (entityType.toLowerCase()) {
      case 'todo':
        return 'bg-blue-100 text-blue-800'
      case 'note':
        return 'bg-green-100 text-green-800'
      case 'log_entry':
        return 'bg-purple-100 text-purple-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1 space-y-2">
          <div className="flex items-center gap-2">
            <span
              className={`px-2 py-1 rounded-full text-xs font-medium ${getEntityTypeColor(message.entityType)}`}
            >
              {message.entityType?.toUpperCase() || 'UNKNOWN'}
            </span>
            <span className="text-xs text-gray-500">
              {formatDate(message.createdAt)}
            </span>
          </div>

          <div className="space-y-1">
            <div className="text-sm">
              <span className="font-medium text-gray-700">Entity ID:</span>
              <span className="ml-2 font-mono text-gray-900">{message.entityId || 'N/A'}</span>
            </div>
            <div className="text-sm">
              <span className="font-medium text-gray-700">Changed by:</span>
              <span className="ml-2 text-gray-900">{message.changedByUserId || 'N/A'}</span>
            </div>
            <div className="text-sm">
              <span className="font-medium text-gray-700">Message ID:</span>
              <span className="ml-2 font-mono text-xs text-gray-500">{message.id || 'N/A'}</span>
            </div>
          </div>
        </div>

        <button
          onClick={handleAcknowledge}
          disabled={isAcknowledging}
          className="ml-4 bg-green-500 hover:bg-green-600 disabled:bg-green-300 text-white font-medium py-2 px-4 rounded-lg transition-colors"
        >
          {isAcknowledging ? 'Acknowledging...' : 'Acknowledge'}
        </button>
      </div>
    </div>
  )
}
