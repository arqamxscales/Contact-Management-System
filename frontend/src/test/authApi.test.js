import { describe, expect, it, vi } from "vitest";
import { loginUser, logoutAllSessions, refreshAccessToken, registerUser } from "../api/authApi.js";

vi.mock("../api/client.js", () => ({
  apiClient: {
    post: vi.fn()
  }
}));

import { apiClient } from "../api/client.js";

describe("authApi helpers", () => {
  it("returns login payload", async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 1, email: "john@example.com" } });

    const result = await loginUser({ email: "john@example.com", password: "secret" });

    expect(result.email).toBe("john@example.com");
    expect(apiClient.post).toHaveBeenCalledWith("/auth/login", { email: "john@example.com", password: "secret" });
  });

  it("returns register payload", async () => {
    apiClient.post.mockResolvedValueOnce({ data: { id: 2, email: "jane@example.com" } });

    const result = await registerUser({ fullName: "Jane", email: "jane@example.com", password: "secret" });

    expect(result.id).toBe(2);
  });

  it("returns refreshed token payload", async () => {
    apiClient.post.mockResolvedValueOnce({ data: { accessToken: "new-access", refreshToken: "refresh-1" } });

    const result = await refreshAccessToken({ accessToken: "old-access", refreshToken: "refresh-1" });

    expect(result.accessToken).toBe("new-access");
    expect(apiClient.post).toHaveBeenCalledWith("/auth/refresh", {
      accessToken: "old-access",
      refreshToken: "refresh-1"
    });
  });

  it("calls logout-all endpoint", async () => {
    apiClient.post.mockResolvedValueOnce({ data: { ok: true } });

    const result = await logoutAllSessions({ refreshToken: "refresh-1" });

    expect(result.ok).toBe(true);
    expect(apiClient.post).toHaveBeenCalledWith("/auth/logout-all", { refreshToken: "refresh-1" });
  });
});