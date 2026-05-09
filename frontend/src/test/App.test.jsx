import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

const authState = {
  user: null,
  isAuthenticated: false,
  login: vi.fn(),
  logout: vi.fn()
};

vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => authState
}));

import { App } from "../App.jsx";

describe("App", () => {
  beforeEach(() => {
    authState.user = null;
    authState.isAuthenticated = false;
  });

  it("shows login page route when unauthenticated", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <App />
      </MemoryRouter>
    );

    expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument();
  });

  it("routes profile separately from contacts", () => {
    authState.user = { id: 1, fullName: "John Doe" };
    authState.isAuthenticated = true;

    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <App />
      </MemoryRouter>
    );

    expect(screen.getByRole("heading", { name: "User Profile" })).toBeInTheDocument();
  });
});