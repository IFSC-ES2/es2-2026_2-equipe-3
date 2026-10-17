import { apiFetch } from "./api";
import { ApiError } from "./orientadorService";
import type { PerfilProfessor } from "../types/Professor";
import type { RespostaErroPadrao } from "../types/Erro";

async function lerErro(response: Response): Promise<RespostaErroPadrao> {
  try {
    const erro = (await response.json()) as Partial<RespostaErroPadrao>;
    return {
      status: response.status,
      erro: erro.erro || "Erro ao processar a requisição",
      detalhes: erro.detalhes,
    };
  } catch {
    return {
      status: response.status,
      erro: response.statusText || "Erro ao processar a requisição",
    };
  }
}

export async function obterPerfilProfessor(): Promise<PerfilProfessor> {
  try {
    const response = await apiFetch("/professores/me");
    if (!response.ok) {
      const erro = await lerErro(response);
      throw new ApiError(response.status, erro.erro, erro.detalhes);
    }
    return response.json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

function normalizarErro(erro: unknown): ApiError | Error {
  if (erro instanceof ApiError) return erro;
  if (erro instanceof TypeError) {
    return new Error(
      "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
    );
  }
  return erro instanceof Error
    ? erro
    : new Error("Erro ao buscar o perfil do professor.");
}
