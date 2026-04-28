import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => ({ user: null, isAuthenticated: false, login: vi.fn(), logout: vi.fn() })
}));

import { App } from "../App.jsx";

describe("App", () => {
  it("shows login page route when unauthenticated", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <App />
      </MemoryRouter>
    );

    expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument();
  });
});