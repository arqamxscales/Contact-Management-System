import React, { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const rawUser = window.localStorage.getItem("cms_user");
    return rawUser ? JSON.parse(rawUser) : null;
  });

  const login = (nextUser, token) => {
    // Session storage here would be fine too, but localStorage keeps this demo simple.
    window.localStorage.setItem("cms_user", JSON.stringify(nextUser));
    window.localStorage.setItem("cms_token", token);
    setUser(nextUser);
  };

  const logout = () => {
    window.localStorage.removeItem("cms_user");
    window.localStorage.removeItem("cms_token");
    setUser(null);
  };

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user),
    login,
    logout
  }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuthContext must be used within AuthProvider");
  }
  return context;
}