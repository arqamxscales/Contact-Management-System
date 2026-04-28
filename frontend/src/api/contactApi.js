import { apiClient } from "./client.js";

export async function listContacts(search = "") {
  const response = await apiClient.get("/contacts", {
    params: search ? { search } : {}
  });
  return response.data;
}

export async function listContactsPaged(params) {
  const response = await apiClient.get("/contacts/paged", { params });
  return response.data;
}

export async function createContact(payload) {
  const response = await apiClient.post("/contacts", payload);
  return response.data;
}

export async function updateContact(id, payload) {
  const response = await apiClient.put(`/contacts/${id}`, payload);
  return response.data;
}

export async function deleteContact(id) {
  await apiClient.delete(`/contacts/${id}`);
}