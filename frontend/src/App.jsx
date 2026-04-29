import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell.jsx";
import { useAuth } from "./hooks/useAuth.js";
import { LoginPage } from "./pages/LoginPage.jsx";
import { RegisterPage } from "./pages/RegisterPage.jsx";
import { DashboardPage } from "./pages/DashboardPage.jsx";
import { ContactsPage } from "./pages/ContactsPage.jsx";
import { UserProfilePage } from "./pages/UserProfilePage.jsx";

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/contacts"
          element={
            <ProtectedRoute>
              <ContactsPage />
                    <Route
                      path="/profile"
                      element={
                        <ProtectedRoute>
                          <UserProfilePage />
                        </ProtectedRoute>
                      }
                    />
            </ProtectedRoute>
          }
        />
      </Routes>
    </AppShell>
  );
}