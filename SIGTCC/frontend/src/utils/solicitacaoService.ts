import { apiFetch } from "./api";
import { garantirResposta, normalizarErroGenerico } from "./apiErro";
import type {
  Solicitacao,
  StatusSolicitacao,
  AtualizaStatusRequest,
  NovaSolicitacao,
} from "../types/Solicitacao";

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

export async function criarSolicitacao(
  dados: NovaSolicitacao,
): Promise<Solicitacao> {
  try {
    const response = await apiFetch("/solicitacoes", {
      method: "POST",
      body: JSON.stringify(dados),
    });
    const resposta = await garantirResposta(response, 201);
    return resposta.json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function obterSolicitacao(id: number): Promise<Solicitacao> {
  try {
    const response = await apiFetch(`/solicitacoes/${id}`);
    return (await garantirResposta(response, 200)).json();
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

function normalizarErro(erro: unknown) {
  return normalizarErroGenerico(
    erro,
    "Erro inesperado ao processar a solicitação.",
  );
}
