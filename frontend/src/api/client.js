import axios from "axios";

// This client stays tiny on purpose; it just centralizes the base URL and auth header.
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api"
});

let isRefreshing = false;
let pendingRequests = [];

function flushPendingRequests(error, token = null) {
  pendingRequests.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  pendingRequests = [];
}

apiClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("cms_access_token") ?? window.localStorage.getItem("cms_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const status = error?.response?.status;

    // If token expired, attempt one refresh and replay the request.
    if (status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingRequests.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      isRefreshing = true;
      try {
        const refreshToken = window.localStorage.getItem("cms_refresh_token");
        const accessToken = window.localStorage.getItem("cms_access_token") ?? window.localStorage.getItem("cms_token");

        if (!refreshToken || !accessToken) {
          throw error;
        }

        // Use raw axios here on purpose to avoid interceptor recursion.
        const refreshResponse = await axios.post(
          `${apiClient.defaults.baseURL}/auth/refresh`,
          { refreshToken, accessToken }
        );

        const newAccessToken = refreshResponse.data?.accessToken;
        const newRefreshToken = refreshResponse.data?.refreshToken ?? refreshToken;

        if (!newAccessToken) {
          throw error;
        }

        window.localStorage.setItem("cms_access_token", newAccessToken);
        window.localStorage.setItem("cms_refresh_token", newRefreshToken);
        window.localStorage.setItem("cms_token", newAccessToken); // Backward compatibility for old screens

        flushPendingRequests(null, newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        flushPendingRequests(refreshError, null);
        // Hard reset on auth failure so the app can route back to login.
        window.localStorage.removeItem("cms_user");
        window.localStorage.removeItem("cms_access_token");
        window.localStorage.removeItem("cms_refresh_token");
        window.localStorage.removeItem("cms_token");
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);