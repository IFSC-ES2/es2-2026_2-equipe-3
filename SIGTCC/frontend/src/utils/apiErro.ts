import type { RespostaErroPadrao } from "../types/Erro";

/**
 * Erro lançado por qualquer chamada à API (SIGTCC/frontend/src/utils/*Service.ts).
 *
 * Antes da REF-1, esta classe e as funções auxiliares abaixo existiam
 * duplicadas (e já divergentes) em orientadorService.ts, solicitacaoService.ts
 * e professorService.ts. Ver docs/REENGENHARIA.md para o diagnóstico completo.
 */
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

export async function lerErro(response: Response): Promise<RespostaErroPadrao> {
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

/**
 * Garante que a resposta HTTP tem o status esperado (ou, se nenhum for
 * informado, que é um status de sucesso). Caso contrário, lê o corpo do
 * erro e lança um ApiError.
 *
 * A mensagem principal prefere `erro.detalhes` quando ele é uma string
 * (caso das exceções de regra de negócio, acesso negado e conflito de
 * estado do backend, que carregam uma mensagem específica em `detalhes`),
 * caindo para o título genérico `erro.erro` quando não há. Esta é a
 * política que já estava em uso em solicitacaoService.ts; a REF-1 unifica
 * o comportamento, que antes divergia em orientadorService.ts (ver seção
 * 1.2 de docs/REENGENHARIA.md).
 */
export async function garantirResposta(
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

/**
 * Tratamento de erro genérico, comum a todos os serviços: repassa um
 * ApiError como está, traduz falhas de rede (fetch rejeitado) para uma
 * mensagem amigável e cai para `mensagemPadrao` em qualquer outro caso.
 *
 * Serviços com uma política de mensagens específica (ex.: substituir o
 * texto de um 409 ou 404) devem chamar esta função primeiro e aplicar
 * suas próprias regras sobre o resultado — ver normalizarErro em
 * orientadorService.ts para um exemplo.
 */
export function normalizarErroGenerico(
  erro: unknown,
  mensagemPadrao: string,
): ApiError | Error {
  if (erro instanceof ApiError) return erro;

  if (erro instanceof TypeError) {
    return new Error(
      "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
    );
  }

  return erro instanceof Error ? erro : new Error(mensagemPadrao);
}
