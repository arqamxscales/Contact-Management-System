import { describe, expect, it, vi } from "vitest";
import { loginUser, registerUser } from "../api/authApi.js";

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
});