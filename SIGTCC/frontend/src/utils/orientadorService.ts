import { apiFetch } from "./api";
import { ApiError, garantirResposta, normalizarErroGenerico } from "./apiErro";
import type { Orientador, OrientadorInput } from "../types/Orientador";

export async function cadastrarOrientador(
  dados: OrientadorInput,
): Promise<Orientador> {
  try {
    const response = await apiFetch("/orientadores", {
      method: "POST",
      body: JSON.stringify(dados),
    });
    const resposta = await garantirResposta(response, 201);
    const location = resposta.headers.get("Location");
    const id = location
      ? Number(new URL(location, resposta.url).pathname.split("/").pop())
      : NaN;
    if (!Number.isInteger(id)) {
      throw new Error("O servidor não informou o orientador criado.");
    }
    return obterOrientador(id);
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export interface FiltroOrientadores {
  area?: string;
  temVagas?: boolean;
}

export async function listarOrientadores(
  filtro?: FiltroOrientadores,
): Promise<Orientador[]> {
  try {
    const params = new URLSearchParams();
    if (filtro?.area) params.set("area", filtro.area);
    if (filtro?.temVagas) params.set("temVagas", "true");
    const query = params.toString() ? `?${params.toString()}` : "";
    const response = await apiFetch(`/orientadores${query}`);
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function obterOrientador(id: number): Promise<Orientador> {
  try {
    const response = await apiFetch(`/orientadores/${id}`);
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function atualizarOrientador(
  id: number,
  dados: Partial<OrientadorInput>,
): Promise<Orientador> {
  try {
    const response = await apiFetch(`/orientadores/${id}`, {
      method: "PATCH",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

// Política de mensagens específica de orientadores: substitui o texto de
// 409 (e-mail duplicado) e 404 (orientador inexistente) por mensagens
// amigáveis, mantendo o comportamento que já existia antes da REF-1.
function normalizarErro(erro: unknown): ApiError | Error {
  const normalizado = normalizarErroGenerico(
    erro,
    "Erro ao processar a requisição",
  );

  if (normalizado instanceof ApiError) {
    if (normalizado.status === 409) {
      return new ApiError(
        normalizado.status,
        "Este e-mail já está cadastrado no sistema",
        normalizado.detalhes,
      );
    }
    if (normalizado.status === 404) {
      return new ApiError(
        normalizado.status,
        "Orientador não encontrado",
        normalizado.detalhes,
      );
    }
  }

  return normalizado;
}
