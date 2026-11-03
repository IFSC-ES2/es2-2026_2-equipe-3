import { apiFetch } from "./api";
import { garantirResposta, normalizarErroGenerico } from "./apiErro";
import type { Entrega, NovaEntrega, NovoParecer } from "../types/Entrega";

export async function listarEntregasDoAluno(): Promise<Entrega[]> {
  try {
    const response = await apiFetch("/submissoes/aluno/me");
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function criarEntrega(dados: NovaEntrega): Promise<Entrega> {
  try {
    const response = await apiFetch("/submissoes", {
      method: "POST",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response, 201)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function listarEntregasDoOrientador(
  orientadorId: number,
): Promise<Entrega[]> {
  try {
    const response = await apiFetch(`/submissoes/orientador/${orientadorId}`);
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function registrarParecer(
  entregaId: number,
  dados: NovoParecer,
): Promise<Entrega> {
  try {
    const response = await apiFetch(`/submissoes/${entregaId}/parecer`, {
      method: "POST",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

function normalizarErro(erro: unknown) {
  return normalizarErroGenerico(erro, "Erro ao processar a entrega.");
}
