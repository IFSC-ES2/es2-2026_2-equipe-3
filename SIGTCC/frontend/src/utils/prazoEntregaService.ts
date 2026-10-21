import { apiFetch } from "./api";
import { garantirResposta, normalizarErroGenerico } from "./apiErro";
import type {
  PrazoEntrega,
  NovoPrazoEntrega,
  AtualizaPrazoEntrega,
} from "../types/PrazoEntrega";

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

function normalizarErro(erro: unknown) {
  return normalizarErroGenerico(
    erro,
    "Erro ao processar os prazos de entrega.",
  );
}
