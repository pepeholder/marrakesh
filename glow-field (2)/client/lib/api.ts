import { AuthResponse, CreateSessionPayload, Session } from "@shared/api";

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return (await res.json()) as T;
}

function headers(token?: string) {
  return token
    ? { "Content-Type": "application/json", Authorization: `Bearer ${token}` }
    : { "Content-Type": "application/json" };
}

export const API = {
  // Auth
  async register(body: { name: string; email: string; password: string }): Promise<AuthResponse> {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: headers(),
      body: JSON.stringify(body),
    });
    return json<AuthResponse>(res);
  },
  async login(body: { email: string; password: string }): Promise<AuthResponse> {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: headers(),
      body: JSON.stringify(body),
    });
    return json<AuthResponse>(res);
  },

  // Sessions
  async listSessions(token?: string): Promise<Session[]> {
    const res = await fetch("/api/sessions", { headers: headers(token) });
    return json<Session[]>(res);
  },
  async createSession(payload: CreateSessionPayload, token?: string): Promise<Session> {
    const res = await fetch("/api/sessions", {
      method: "POST",
      headers: headers(token),
      body: JSON.stringify(payload),
    });
    return json<Session>(res);
  },
  async autoOrder(sessionId: string, token?: string): Promise<Session> {
    const res = await fetch(`/api/sessions/${sessionId}/auto-order`, {
      method: "POST",
      headers: headers(token),
    });
    return json<Session>(res);
  },
  async getSession(sessionId: string, token?: string): Promise<Session> {
    const res = await fetch(`/api/sessions/${sessionId}`, { headers: headers(token) });
    return json<Session>(res);
  },
  async getGameState(sessionId: string, token?: string) {
    const res = await fetch(`/api/sessions/${sessionId}/state`, { headers: headers(token) });
    return json<any>(res);
  },
  async makeMove(sessionId: string, body: { direction: "left" | "right" | "forward" }, token?: string) {
    const res = await fetch(`/api/sessions/${sessionId}/move`, { method: "POST", headers: headers(token), body: JSON.stringify(body) });
    return json<any>(res);
  },
  async rollDice(sessionId: string, token?: string) {
    const res = await fetch(`/api/sessions/${sessionId}/roll`, { method: "POST", headers: headers(token) });
    return json<any>(res);
  },
};
