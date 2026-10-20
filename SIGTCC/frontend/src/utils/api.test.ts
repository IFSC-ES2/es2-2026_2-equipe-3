import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { apiFetch } from "./api";
import * as authService from "../auth/authService";

describe("apiFetch", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("{}", { status: 200 })),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  test("inclui Content-Type e Accept por padrão quando nenhum header extra é passado", async () => {
    vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(null);

    await apiFetch("/orientadores");

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/orientadores"),
      expect.objectContaining({
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      }),
    );
  });

  test("inclui cabeçalho Authorization quando há token de acesso ativo", async () => {
    vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue("token-123");

    await apiFetch("/orientadores");

    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: "Bearer token-123",
        },
      }),
    );
  });

  test("preserva Content-Type, Accept e Authorization ao mesclar options.headers adicionais", async () => {
    vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue("token-123");

    await apiFetch("/orientadores", {
      method: "POST",
      headers: { "X-Custom-Header": "valor" },
    });

    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: "Bearer token-123",
          "X-Custom-Header": "valor",
        },
      }),
    );
  });

  test("permite sobrescrever cabeçalhos padrão intencionalmente via options.headers", async () => {
    vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(null);

    await apiFetch("/orientadores", {
      headers: { "Content-Type": "text/plain" },
    });

    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: {
          "Content-Type": "text/plain",
          Accept: "application/json",
        },
      }),
    );
  });
});