'use client'

import { ConnectionStatus } from '@/types/websocket'

interface ConnectionStatusProps {
  status: ConnectionStatus
  onConnect: () => void
  onDisconnect: () => void
}

export function ConnectionStatusIndicator({ status, onConnect, onDisconnect }: ConnectionStatusProps) {
  const getStatusConfig = (status: ConnectionStatus) => {
    switch (status) {
      case ConnectionStatus.CONNECTED:
        return {
          color: 'bg-green-500',
          text: 'Connected',
          textColor: 'text-green-700',
          bgColor: 'bg-green-50 border-green-200'
        }
      case ConnectionStatus.CONNECTING:
        return {
          color: 'bg-yellow-500',
          text: 'Connecting...',
          textColor: 'text-yellow-700',
          bgColor: 'bg-yellow-50 border-yellow-200'
        }
      case ConnectionStatus.DISCONNECTED:
        return {
          color: 'bg-gray-500',
          text: 'Disconnected',
          textColor: 'text-gray-700',
          bgColor: 'bg-gray-50 border-gray-200'
        }
      case ConnectionStatus.ERROR:
        return {
          color: 'bg-red-500',
          text: 'Connection Error',
          textColor: 'text-red-700',
          bgColor: 'bg-red-50 border-red-200'
        }
      default:
        return {
          color: 'bg-gray-500',
          text: 'Unknown',
          textColor: 'text-gray-700',
          bgColor: 'bg-gray-50 border-gray-200'
        }
    }
  }

  const config = getStatusConfig(status)

  return (
    <div className={`border rounded-lg p-4 ${config.bgColor}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <div className={`w-3 h-3 rounded-full ${config.color}`}>
              {status === ConnectionStatus.CONNECTING && (
                <div className={`w-3 h-3 rounded-full ${config.color} animate-pulse`}></div>
              )}
            </div>
            <span className={`font-medium ${config.textColor}`}>
              WebSocket: {config.text}
            </span>
          </div>
        </div>

        <div className="flex gap-2">
          {status === ConnectionStatus.CONNECTED ? (
            <button
              onClick={onDisconnect}
              className="text-sm bg-red-100 hover:bg-red-200 text-red-700 font-medium py-1 px-3 rounded transition-colors"
            >
              Disconnect
            </button>
          ) : (
            <button
              onClick={onConnect}
              disabled={status === ConnectionStatus.CONNECTING}
              className="text-sm bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white font-medium py-1 px-3 rounded transition-colors"
            >
              {status === ConnectionStatus.CONNECTING ? 'Connecting...' : 'Connect'}
            </button>
          )}
        </div>
      </div>

      {status === ConnectionStatus.ERROR && (
        <div className="mt-2 text-sm text-red-600">
          Failed to connect to WebSocket. Check if the backend is running and try reconnecting.
        </div>
      )}
    </div>
  )
}