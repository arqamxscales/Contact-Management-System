import { apiClient } from "./client.js";

export async function loginUser(payload) {
  // Keeping the helper boring helps the UI stay easy to test.
  const response = await apiClient.post("/auth/login", payload);
  return response.data;
}

export async function registerUser(payload) {
  const response = await apiClient.post("/auth/register", payload);
  return response.data;
}

export async function getProfile(userId) {
  const response = await apiClient.get(`/auth/profile/${userId}`);
  return response.data;
}

export async function changePassword(userId, payload) {
  const response = await apiClient.post(`/auth/profile/${userId}/change-password`, payload);
  return response.data;
}