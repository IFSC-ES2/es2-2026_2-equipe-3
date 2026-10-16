import { obterAccessTokenAtual } from "../auth/authService";

const HOST_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const API_BASE_URL = `${HOST_URL}/api/v1`;

export const apiFetch = async (endpoint: string, options?: RequestInit) => {
  const token = obterAccessTokenAtual();

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options?.headers,
    },
    ...options,
  });

  return response;
};
