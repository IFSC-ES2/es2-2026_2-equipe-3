import {
  lerTokensArmazenados,
  logout,
  obterAccessTokenAtual,
  renovarToken,
} from "../auth/authService";

const HOST_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const API_BASE_URL = `${HOST_URL}/api/v1`;

function redirecionarParaLogin(): void {
  if (typeof window !== "undefined" && window.location) {
    if (window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
  }
}

async function tratarFalhaAutenticacao(): Promise<void> {
  logout();
  redirecionarParaLogin();
}

export const apiFetch = async (endpoint: string, options?: RequestInit) => {
  let token = obterAccessTokenAtual();

  const tokensArmazenados = lerTokensArmazenados();
  if (!token && tokensArmazenados?.refreshToken) {
    const sessaoRenovada = await renovarToken();
    if (sessaoRenovada) {
      token = sessaoRenovada.accessToken;
    } else {
      await tratarFalhaAutenticacao();
      return new Response(null, { status: 401 });
    }
  }

  const enviar = (tokenParaUsar: string | null) => {
    return fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        ...(tokenParaUsar ? { Authorization: `Bearer ${tokenParaUsar}` } : {}),
        ...options?.headers,
      },
    });
  };

  let response = await enviar(token);

  if (response.status === 401) {
    const sessaoRenovada = await renovarToken();
    if (sessaoRenovada) {
      response = await enviar(sessaoRenovada.accessToken);
    } else {
      await tratarFalhaAutenticacao();
    }
  }

  return response;
};
