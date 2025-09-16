import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { API } from "@/lib/api";
import { AuthResponse, User } from "@shared/api";

interface AuthState {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const stored = localStorage.getItem("auth");
    if (stored) {
      const { user, token } = JSON.parse(stored) as AuthResponse;
      setUser(user);
      setToken(token);
    }
  }, []);

  const persist = useCallback((auth: AuthResponse) => {
    setUser(auth.user);
    setToken(auth.token);
    localStorage.setItem("auth", JSON.stringify(auth));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const auth = await API.login({ email, password });
    persist(auth);
  }, [persist]);

  const register = useCallback(async (name: string, email: string, password: string) => {
    const auth = await API.register({ name, email, password });
    persist(auth);
  }, [persist]);

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("auth");
  }, []);

  const value = useMemo(() => ({ user, token, login, register, logout }), [user, token, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
