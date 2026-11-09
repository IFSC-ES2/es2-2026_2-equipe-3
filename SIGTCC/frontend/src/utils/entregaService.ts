import { apiFetch } from "./api";
import { garantirResposta, normalizarErroGenerico } from "./apiErro";
import type {
  Entrega,
  NovaEntrega,
  NovoParecer,
  StatusEntrega,
} from "../types/Entrega";

export async function listarEntregasDoAluno(): Promise<Entrega[]> {
  try {
    const response = await apiFetch("/entregas/minhas");
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function criarEntrega(dados: NovaEntrega): Promise<Entrega> {
  try {
    const response = await apiFetch("/entregas", {
      method: "POST",
      body: JSON.stringify(dados),
    });
    return (await garantirResposta(response)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

export async function listarEntregasDoOrientador(
  status?: StatusEntrega,
): Promise<Entrega[]> {
  try {
    const query = status ? `?status=${encodeURIComponent(status)}` : "";
    const response = await apiFetch(`/entregas/orientador${query}`);
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
    const response = await apiFetch(`/entregas/${entregaId}/parecer`, {
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
