import { apiFetch } from "./api";
import { ApiError } from "./orientadorService";
import type {
  Solicitacao,
  StatusSolicitacao,
  AtualizaStatusRequest,
} from "../types/Solicitacao";
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

export async function listarSolicitacoesPorOrientador(
  orientadorId: number,
  status?: StatusSolicitacao,
): Promise<Solicitacao[]> {
  try {
    const query = status ? `?status=${encodeURIComponent(status)}` : "";
    const response = await apiFetch(
      `/solicitacoes/orientador/${orientadorId}${query}`,
    );
    const respostaValida = await garantirResposta(response, 200);
    return respostaValida.json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function atualizarStatusSolicitacao(
  solicitacaoId: number,
  dados: AtualizaStatusRequest,
): Promise<Solicitacao> {
  try {
    const response = await apiFetch(`/solicitacoes/${solicitacaoId}/status`, {
      method: "PATCH",
      body: JSON.stringify(dados),
    });
    const respostaValida = await garantirResposta(response, 200);
    return respostaValida.json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

function normalizarErro(erro: unknown): ApiError | Error {
  if (erro instanceof ApiError) {
    return erro;
  }

  if (erro instanceof TypeError) {
    return new Error(
      "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
    );
  }

  return erro instanceof Error
    ? erro
    : new Error("Erro inesperado ao processar a solicitação.");
}
