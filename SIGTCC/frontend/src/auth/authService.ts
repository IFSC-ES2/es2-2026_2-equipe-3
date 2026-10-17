const KEYCLOAK_URL =
  import.meta.env.VITE_KEYCLOAK_URL || "http://localhost:8081";
const KEYCLOAK_REALM = import.meta.env.VITE_KEYCLOAK_REALM || "sigtcc";
const KEYCLOAK_CLIENT_ID =
  import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "sigtcc-frontend";

const TOKEN_ENDPOINT = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`;

export const STORAGE_KEY = "sigtcc.auth.tokens";

export type Papel = "ROLE_ALUNO" | "ROLE_PROFESSOR";

export interface TokensArmazenados {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface SessaoAutenticada {
  accessToken: string;
  email: string;
  papeis: Papel[];
}

export class CredenciaisInvalidasError extends Error {
  constructor() {
    super("Credenciais inválidas");
    this.name = "CredenciaisInvalidasError";
  }
}

interface TokenClaims {
  email?: string;
  realm_access?: { roles?: string[] };
  exp?: number;
}

function decodificarPayload(token: string): TokenClaims {
  const payloadBase64Url = token.split(".")[1];
  if (!payloadBase64Url) {
    throw new Error("Token JWT inválido.");
  }
  const payloadBase64 = payloadBase64Url.replace(/-/g, "+").replace(/_/g, "/");
  const payloadJson = atob(
    payloadBase64.padEnd(
      payloadBase64.length + ((4 - (payloadBase64.length % 4)) % 4),
      "=",
    ),
  );
  return JSON.parse(payloadJson) as TokenClaims;
}

function extrairPapeis(claims: TokenClaims): Papel[] {
  const roles = claims.realm_access?.roles ?? [];
  return roles.filter(
    (role): role is Papel => role === "ROLE_ALUNO" || role === "ROLE_PROFESSOR",
  );
}

function montarSessao(accessToken: string): SessaoAutenticada {
  const claims = decodificarPayload(accessToken);
  return {
    accessToken,
    email: claims.email ?? "",
    papeis: extrairPapeis(claims),
  };
}

async function solicitarToken(
  body: Record<string, string>,
): Promise<TokensArmazenados> {
  let response: Response;
  try {
    response = await fetch(TOKEN_ENDPOINT, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams(body).toString(),
    });
  } catch {
    throw new Error(
      "Não foi possível conectar ao serviço de autenticação. Verifique se o Keycloak está rodando.",
    );
  }

  if (!response.ok) {
    const corpo = await response
      .clone()
      .json()
      .catch(() => null as { error?: string } | null);
    const codigoErro = corpo?.error;

    if (codigoErro === "invalid_grant") {
      // Usuário/senha incorretos, ou usuário inexistente no realm.
      throw new CredenciaisInvalidasError();
    }

    if (
      codigoErro === "unauthorized_client" ||
      codigoErro === "invalid_client" ||
      codigoErro === "unsupported_grant_type"
    ) {
      throw new Error(
        "O client do Keycloak não está autorizado a autenticar por senha " +
          `(erro: ${codigoErro}). Verifique se "directAccessGrantsEnabled" ` +
          'está "true" para o client "sigtcc-frontend" no realm em execução ' +
          "(pode ser necessário recriar o volume de dados do Keycloak).",
      );
    }

    if (response.status === 401 || response.status === 400) {
      throw new CredenciaisInvalidasError();
    }
    throw new Error("Erro ao autenticar. Tente novamente mais tarde.");
  }

  const dados = (await response.json()) as {
    access_token: string;
    refresh_token: string;
    expires_in: number;
  };

  return {
    accessToken: dados.access_token,
    refreshToken: dados.refresh_token,
    expiresAt: Date.now() + dados.expires_in * 1000,
  };
}

export async function login(
  email: string,
  senha: string,
): Promise<SessaoAutenticada> {
  const tokens = await solicitarToken({
    grant_type: "password",
    client_id: KEYCLOAK_CLIENT_ID,
    username: email,
    password: senha,
  });

  armazenarTokens(tokens);
  return montarSessao(tokens.accessToken);
}

export async function renovarToken(): Promise<SessaoAutenticada | null> {
  const armazenado = lerTokensArmazenados();
  if (!armazenado) return null;

  try {
    const tokens = await solicitarToken({
      grant_type: "refresh_token",
      client_id: KEYCLOAK_CLIENT_ID,
      refresh_token: armazenado.refreshToken,
    });
    armazenarTokens(tokens);
    return montarSessao(tokens.accessToken);
  } catch {
    limparTokens();
    return null;
  }
}

export function logout(): void {
  limparTokens();
}

export function armazenarTokens(tokens: TokensArmazenados): void {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(tokens));
}

export function lerTokensArmazenados(): TokensArmazenados | null {
  const bruto = sessionStorage.getItem(STORAGE_KEY);
  if (!bruto) return null;
  try {
    return JSON.parse(bruto) as TokensArmazenados;
  } catch {
    return null;
  }
}

export function limparTokens(): void {
  sessionStorage.removeItem(STORAGE_KEY);
}

export function recuperarSessaoArmazenada(): SessaoAutenticada | null {
  const tokens = lerTokensArmazenados();
  if (!tokens) return null;
  if (Date.now() >= tokens.expiresAt) return null;
  try {
    return montarSessao(tokens.accessToken);
  } catch {
    return null;
  }
}

export function obterAccessTokenAtual(): string | null {
  const tokens = lerTokensArmazenados();
  if (!tokens) return null;
  if (Date.now() >= tokens.expiresAt) return null;
  return tokens.accessToken;
}
