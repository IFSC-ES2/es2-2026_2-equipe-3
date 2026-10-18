import { apiFetch } from "./api";
import { ApiError } from "./orientadorService";
import type {
  PrazoEntrega,
  NovoPrazoEntrega,
  AtualizaPrazoEntrega,
} from "../types/PrazoEntrega";
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

async function garantirResposta(
  response: Response,
  statusEsperado?: number,
): Promise<Response> {
  if (
    statusEsperado !== undefined
      ? response.status !== statusEsperado
      : !response.ok
  ) {
    const erro = await lerErro(response);
    const mensagemPrincipal =
      typeof erro.detalhes === "string"
        ? erro.detalhes
        : erro.erro || "Erro ao processar a requisição";
    throw new ApiError(response.status, mensagemPrincipal, erro.detalhes);
  }
  return response;
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
    : new Error("Erro ao processar os prazos de entrega.");
}

export async function listarPrazosVigentes(): Promise<PrazoEntrega[]> {
  try {
    const response = await apiFetch("/prazos");
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function cadastrarPrazo(
  dados: NovoPrazoEntrega,
): Promise<PrazoEntrega> {
  try {
    const response = await apiFetch("/prazos", {
      method: "POST",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response, 201)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function atualizarPrazo(
  id: number,
  dados: AtualizaPrazoEntrega,
): Promise<PrazoEntrega> {
  try {
    const response = await apiFetch(`/prazos/${id}`, {
      method: "PATCH",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}