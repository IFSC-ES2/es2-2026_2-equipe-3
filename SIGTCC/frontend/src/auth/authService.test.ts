import { afterEach, describe, expect, test } from "vitest";
import {
  armazenarTokens,
  limparTokens,
  recuperarSessaoArmazenada,
} from "./authService";

function criarToken(claims: Record<string, unknown>): string {
  const payload = btoa(JSON.stringify(claims))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  return `cabecalho.${payload}.assinatura`;
}

function armazenar(claims: Record<string, unknown>) {
  armazenarTokens({
    accessToken: criarToken(claims),
    refreshToken: "refresh",
    expiresAt: Date.now() + 60_000,
  });
}

describe("authService - sessão", () => {
  afterEach(() => limparTokens());

  test("extrai e-mail, nome e papéis das claims do token", () => {
    armazenar({
      email: "aluno@example.com",
      name: "Aluno de Teste",
      realm_access: { roles: ["ROLE_ALUNO", "offline_access"] },
    });

    expect(recuperarSessaoArmazenada()).toMatchObject({
      email: "aluno@example.com",
      nome: "Aluno de Teste",
      papeis: ["ROLE_ALUNO"],
    });
  });

  test("usa nome vazio quando o token não possui a claim name", () => {
    armazenar({ email: "aluno@example.com" });

    expect(recuperarSessaoArmazenada()?.nome).toBe("");
  });
});
