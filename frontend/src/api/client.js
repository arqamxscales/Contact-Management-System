import axios from "axios";

// This client stays tiny on purpose; it just centralizes the base URL and auth header.
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api"
});

apiClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("cms_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});