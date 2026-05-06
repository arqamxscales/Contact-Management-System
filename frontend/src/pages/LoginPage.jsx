import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { loginUser } from "../api/authApi.js";
import { useAuth } from "../hooks/useAuth.js";
import { FormField } from "../components/FormField.jsx";
import { InfoCard } from "../components/InfoCard.jsx";

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await loginUser(form);
      // Backend variants:
      // 1) { user, accessToken, refreshToken }
      // 2) { id, email, ... } (legacy)
      const nextUser = response?.user ?? response;
      const nextToken = response?.accessToken
        ? { accessToken: response.accessToken, refreshToken: response.refreshToken }
        : "demo-token";

      login(nextUser, nextToken);
      navigate("/dashboard");
    } catch (loginError) {
      setError(loginError.response?.data?.message ?? "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <InfoCard title="Login">
      <form onSubmit={handleSubmit} style={formStyle}>
        <FormField label="Email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
        <FormField label="Password" type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
        {error ? <p style={errorStyle}>{error}</p> : null}
        <button type="submit" style={buttonStyle} disabled={loading}>{loading ? "Signing in..." : "Login"}</button>
        <p style={{ marginBottom: 0 }}>Need an account? <Link to="/register">Register here</Link>.</p>
      </form>
    </InfoCard>
  );
}

const formStyle = { display: "grid", gap: "1rem" };
const buttonStyle = { border: "none", borderRadius: "0.8rem", padding: "0.9rem 1rem", background: "#4f46e5", color: "white", cursor: "pointer" };
const errorStyle = { margin: 0, color: "#b91c1c" };