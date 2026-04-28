import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { LoginPage } from "../pages/LoginPage.jsx";

vi.mock("../api/authApi.js", () => ({
  loginUser: vi.fn().mockResolvedValue({ id: 1, fullName: "John Doe", email: "john@example.com" })
}));

const loginSpy = vi.fn();
vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => ({ login: loginSpy, user: null, isAuthenticated: false })
}));

describe("LoginPage", () => {
  it("renders a login form", () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument();
    expect(screen.getByLabelText("Email")).toBeInTheDocument();
  });
});