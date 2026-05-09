import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

const logoutMock = vi.hoisted(() => vi.fn());
const mockLogoutAllSessions = vi.hoisted(() => vi.fn().mockResolvedValue({}));

vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => ({
    user: { id: 1, fullName: "John Doe" },
    isAuthenticated: true,
    login: vi.fn(),
    logout: logoutMock
  })
}));

vi.mock("../api/authApi.js", () => ({
  logoutAllSessions: mockLogoutAllSessions
}));

import { AppShell } from "../components/AppShell.jsx";

describe("AppShell", () => {
  it("renders navigation and signs out cleanly", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <AppShell>
          <div>content</div>
        </AppShell>
      </MemoryRouter>
    );

    expect(screen.getByRole("link", { name: /Contacts/ })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /Logout/ }));

    await waitFor(() => expect(mockLogoutAllSessions).toHaveBeenCalledWith({ userId: 1 }));
    expect(logoutMock).toHaveBeenCalled();
  });
});