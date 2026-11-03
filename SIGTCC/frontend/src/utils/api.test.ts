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
    vi.spyOn(authService, "lerTokensArmazenados").mockReturnValue(null);

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

  test("inclui cabeçalho Authorization quando há token de acesso ativo (token válido)", async () => {
    vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(
      "token-valido",
    );
    const renovarSpy = vi.spyOn(authService, "renovarToken");

    await apiFetch("/orientadores");

    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: "Bearer token-valido",
        },
      }),
    );
    expect(renovarSpy).not.toHaveBeenCalled();
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
    vi.spyOn(authService, "lerTokensArmazenados").mockReturnValue(null);

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

  describe("renovação automática de token e repetição de chamada", () => {
    test("renova o token antes da requisição quando estiver vencido e repete a chamada com o novo token", async () => {
      vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(null);
      vi.spyOn(authService, "lerTokensArmazenados").mockReturnValue({
        accessToken: "expirado",
        refreshToken: "refresh-valido",
        expiresAt: Date.now() - 1000,
      });

      const renovarSpy = vi
        .spyOn(authService, "renovarToken")
        .mockResolvedValue({
          accessToken: "novo-token-renovado",
          email: "usuario@ifsc.edu.br",
          nome: "Usuário Teste",
          papeis: ["ROLE_ALUNO"],
        });
      const logoutSpy = vi.spyOn(authService, "logout");

      const response = await apiFetch("/orientadores");

      expect(renovarSpy).toHaveBeenCalledTimes(1);
      expect(logoutSpy).not.toHaveBeenCalled();
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining("/api/v1/orientadores"),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer novo-token-renovado",
          }),
        }),
      );
      expect(response.status).toBe(200);
    });

    test("renova o token após receber 401 e repete a requisição uma única vez", async () => {
      vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(
        "token-antigo",
      );

      const mockFetch = vi
        .fn()
        .mockResolvedValueOnce(new Response(null, { status: 401 }))
        .mockResolvedValueOnce(
          new Response(JSON.stringify({ ok: true }), { status: 200 }),
        );
      vi.stubGlobal("fetch", mockFetch);

      const renovarSpy = vi
        .spyOn(authService, "renovarToken")
        .mockResolvedValue({
          accessToken: "token-renovado-apos-401",
          email: "usuario@ifsc.edu.br",
          nome: "Usuário Teste",
          papeis: ["ROLE_PROFESSOR"],
        });
      const logoutSpy = vi.spyOn(authService, "logout");

      const response = await apiFetch("/solicitacoes/1/status", {
        method: "PATCH",
        body: JSON.stringify({ status: "ACEITA" }),
      });

      expect(mockFetch).toHaveBeenCalledTimes(2);
      expect(renovarSpy).toHaveBeenCalledTimes(1);
      expect(logoutSpy).not.toHaveBeenCalled();

      // Primeira chamada com token antigo
      expect(mockFetch).toHaveBeenNthCalledWith(
        1,
        expect.stringContaining("/api/v1/solicitacoes/1/status"),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer token-antigo",
          }),
        }),
      );

      // Segunda chamada com token renovado
      expect(mockFetch).toHaveBeenNthCalledWith(
        2,
        expect.stringContaining("/api/v1/solicitacoes/1/status"),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer token-renovado-apos-401",
          }),
        }),
      );

      expect(response.status).toBe(200);
    });

    test("não repete a chamada mais de uma vez se a segunda chamada também retornar 401", async () => {
      vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(
        "token-antigo",
      );

      const mockFetch = vi
        .fn()
        .mockResolvedValue(new Response(null, { status: 401 }));
      vi.stubGlobal("fetch", mockFetch);

      const renovarSpy = vi
        .spyOn(authService, "renovarToken")
        .mockResolvedValue({
          accessToken: "token-renovado",
          email: "usuario@ifsc.edu.br",
          nome: "Usuário Teste",
          papeis: ["ROLE_PROFESSOR"],
        });

      const response = await apiFetch("/solicitacoes");

      // Deve chamar exatamente 2 vezes: a inicial e a repetição
      expect(mockFetch).toHaveBeenCalledTimes(2);
      expect(renovarSpy).toHaveBeenCalledTimes(1);
      expect(response.status).toBe(401);
    });
  });

  describe("falha de renovação com logout e redirecionamento", () => {
    test("executa logout e redireciona para /login quando a renovação após 401 falhar", async () => {
      vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(
        "token-invalido",
      );
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(new Response(null, { status: 401 })),
      );

      vi.spyOn(authService, "renovarToken").mockResolvedValue(null);
      const logoutSpy = vi.spyOn(authService, "logout");

      const mockLocation = {
        pathname: "/orientadores",
        href: "http://localhost:5173/orientadores",
      };
      const originalLocation = window.location;
      Object.defineProperty(window, "location", {
        value: mockLocation,
        writable: true,
        configurable: true,
      });

      try {
        const response = await apiFetch("/orientadores");

        expect(logoutSpy).toHaveBeenCalledTimes(1);
        expect(mockLocation.href).toBe("/login");
        expect(response.status).toBe(401);
      } finally {
        Object.defineProperty(window, "location", {
          value: originalLocation,
          writable: true,
          configurable: true,
        });
      }
    });

    test("executa logout e redireciona para /login quando o token vencido falhar na renovação prévia", async () => {
      vi.spyOn(authService, "obterAccessTokenAtual").mockReturnValue(null);
      vi.spyOn(authService, "lerTokensArmazenados").mockReturnValue({
        accessToken: "expirado",
        refreshToken: "refresh-invalido",
        expiresAt: Date.now() - 1000,
      });

      vi.spyOn(authService, "renovarToken").mockResolvedValue(null);
      const logoutSpy = vi.spyOn(authService, "logout");

      const mockLocation = {
        pathname: "/orientadores",
        href: "http://localhost:5173/orientadores",
      };
      const originalLocation = window.location;
      Object.defineProperty(window, "location", {
        value: mockLocation,
        writable: true,
        configurable: true,
      });

      try {
        const response = await apiFetch("/orientadores");

        expect(logoutSpy).toHaveBeenCalledTimes(1);
        expect(mockLocation.href).toBe("/login");
        expect(response.status).toBe(401);
      } finally {
        Object.defineProperty(window, "location", {
          value: originalLocation,
          writable: true,
          configurable: true,
        });
      }
    });
  });
});
