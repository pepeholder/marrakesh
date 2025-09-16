/**
 * Shared code between client and server
 * Useful to share types between client and server
 * and/or small pure JS functions that can be used on both client and server
 */

/** Example response type for /api/demo */
export interface DemoResponse {
  message: string;
}

// Auth
export interface User {
  id: string;
  name: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// Sessions
export type SessionStatus = "waiting" | "active" | "finished";

export interface Session {
  id: string;
  name: string;
  players: User[];
  status: SessionStatus;
  turnOrder: string[]; // user ids in order
  activePlayerId?: string;
  createdAt: string;
}

export interface CreateSessionPayload {
  name: string;
}

// Game
export type GameStatus = "active" | "finished";

export interface GameState {
  id: string;
  sessionId: string;
  status: GameStatus;
  activePlayerId?: string;
  winnerId?: string;
  disconnectedPlayerIds?: string[];
  pieces?: Array<{ id: string; x: number; y: number }>;
}
