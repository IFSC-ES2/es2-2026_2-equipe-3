import { apiFetch } from "./api";
import { garantirResposta, normalizarErroGenerico } from "./apiErro";
import type { PerfilProfessor } from "../types/Professor";

export async function obterPerfilProfessor(): Promise<PerfilProfessor> {
  try {
    const response = await apiFetch("/professores/me");
    return (await garantirResposta(response, 200)).json();
  } catch (erro) {
    throw normalizarErro(erro);
  }
}

function normalizarErro(erro: unknown) {
  return normalizarErroGenerico(erro, "Erro ao buscar o perfil do professor.");
}
