import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { DashboardPage } from "../pages/DashboardPage.jsx";

vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => ({ user: { fullName: "Jane Doe" } })
}));

describe("DashboardPage", () => {
  it("renders quick actions for contacts and profile", () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText("JD")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /open contacts/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /open profile/i })).toBeInTheDocument();
  });
});