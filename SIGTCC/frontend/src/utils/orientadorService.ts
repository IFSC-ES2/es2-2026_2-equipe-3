import { apiFetch } from "./api";
import type { Orientador, OrientadorInput } from "../types/Orientador";
import type { RespostaErroPadrao } from "../types/Erro";

export class ApiError extends Error {
  public status: number;
  public detalhes: RespostaErroPadrao["detalhes"];

  constructor(
    status: number,
    message: string,
    detalhes: RespostaErroPadrao["detalhes"] = undefined,
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.detalhes = detalhes;
  }
}

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
    throw new ApiError(response.status, erro.erro, erro.detalhes);
  }
  return response;
}

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

export async function listarOrientadores(area?: string): Promise<Orientador[]> {
  try {
    const query = area ? `?area=${encodeURIComponent(area)}` : "";
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

function normalizarErro(erro: unknown): ApiError | Error {
  if (erro instanceof ApiError) {
    if (erro.status === 409) {
      return new ApiError(
        erro.status,
        "Este e-mail já está cadastrado no sistema",
        erro.detalhes,
      );
    }
    if (erro.status === 404) {
      return new ApiError(
        erro.status,
        "Orientador não encontrado",
        erro.detalhes,
      );
    }
    return erro;
  }

  if (erro instanceof TypeError) {
    return new Error(
      "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
    );
  }

  return erro instanceof Error
    ? erro
    : new Error("Erro ao processar a requisição");
}
