import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/authApi.js";
import { FormField } from "../components/FormField.jsx";
import { InfoCard } from "../components/InfoCard.jsx";

export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      await registerUser(form);
      navigate("/login");
    } catch (registerError) {
      setError(registerError.response?.data?.message ?? "Registration failed. Please check your entries.");
    }
  };

  return (
    <InfoCard title="Register">
      <form onSubmit={handleSubmit} style={formStyle}>
        <FormField label="Full name" value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} />
        <FormField label="Email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
        <FormField label="Password" type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
        {error ? <p style={errorStyle}>{error}</p> : null}
        <button type="submit" style={buttonStyle}>Create account</button>
        <p style={{ marginBottom: 0 }}>Already registered? <Link to="/login">Login here</Link>.</p>
      </form>
    </InfoCard>
  );
}

const formStyle = { display: "grid", gap: "1rem" };
const buttonStyle = { border: "none", borderRadius: "0.8rem", padding: "0.9rem 1rem", background: "#4f46e5", color: "white", cursor: "pointer" };
const errorStyle = { margin: 0, color: "#b91c1c" };