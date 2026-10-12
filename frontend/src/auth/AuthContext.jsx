import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import { getToken, parseJwtPayload, setToken } from "../api/client.js";
import * as authApi from "../api/auth.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => getToken());

  const user = useMemo(() => {
    const payload = parseJwtPayload(token);
    if (!payload?.sub) {
      return null;
    }
    return { email: payload.sub, role: payload.role };
  }, [token]);

  const login = useCallback(async (email, password) => {
    const jwt = await authApi.login(email, password);
    setToken(jwt);
    setTokenState(jwt);
    return jwt;
  }, []);

  const register = useCallback(async (email, password) => {
    await authApi.register(email, password);
    return authApi.login(email, password).then((jwt) => {
      setToken(jwt);
      setTokenState(jwt);
      return jwt;
    });
  }, []);

  const logout = useCallback(() => {
    authApi.logout();
    setTokenState(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, user, login, register, logout]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
