export interface EntityChangedMessage {
  id: string;
  entityId: string;
  entityType: string;
  changedByUserId: string;
  createdAt: string;
}

export interface AckMessage {
  messageId: string;
}

export enum WsResponseStatus {
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR'
}

export interface WsResponse {
  status: WsResponseStatus;
  error?: string;
}

export enum ConnectionStatus {
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  DISCONNECTED = 'DISCONNECTED',
  ERROR = 'ERROR'
}