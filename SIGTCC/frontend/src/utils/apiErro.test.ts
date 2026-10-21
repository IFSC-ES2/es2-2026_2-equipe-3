import { describe, expect, test } from "vitest";
import {
  ApiError,
  garantirResposta,
  lerErro,
  normalizarErroGenerico,
} from "./apiErro";

function criarResponse(
  status: number,
  corpo: unknown,
  statusText = "",
): Response {
  return new Response(JSON.stringify(corpo), { status, statusText });
}

describe("lerErro", () => {
  test("lê status, título e detalhes de um corpo de erro padrão", async () => {
    const response = criarResponse(400, {
      erro: "Erro de validação",
      detalhes: [{ campo: "nome", mensagem: "obrigatório" }],
    });

    const resultado = await lerErro(response);

    expect(resultado).toEqual({
      status: 400,
      erro: "Erro de validação",
      detalhes: [{ campo: "nome", mensagem: "obrigatório" }],
    });
  });

  test("usa mensagem genérica quando o corpo não traz 'erro'", async () => {
    const response = criarResponse(500, {});

    const resultado = await lerErro(response);

    expect(resultado.erro).toBe("Erro ao processar a requisição");
  });

  test("usa o statusText quando o corpo não é um JSON válido", async () => {
    const response = new Response("não é json", {
      status: 502,
      statusText: "Bad Gateway",
    });

    const resultado = await lerErro(response);

    expect(resultado).toEqual({ status: 502, erro: "Bad Gateway" });
  });
});

describe("garantirResposta", () => {
  test("retorna a resposta quando o status bate com o esperado", async () => {
    const response = criarResponse(201, { id: 1 });

    await expect(garantirResposta(response, 201)).resolves.toBe(response);
  });

  test("retorna a resposta quando nenhum status esperado é informado e a resposta é ok", async () => {
    const response = criarResponse(200, {});

    await expect(garantirResposta(response)).resolves.toBe(response);
  });

  test("lança ApiError quando o status não bate com o esperado", async () => {
    const response = criarResponse(404, { erro: "Não encontrado" });

    await expect(garantirResposta(response, 200)).rejects.toMatchObject({
      status: 404,
      message: "Não encontrado",
    });
  });

  test("prefere a mensagem de 'detalhes' quando ela é uma string", async () => {
    const response = criarResponse(422, {
      erro: "Erro de regra de negócio",
      detalhes: "Prazo deve ser posterior ao do Projeto de TCC.",
    });

    await expect(garantirResposta(response, 200)).rejects.toMatchObject({
      status: 422,
      message: "Prazo deve ser posterior ao do Projeto de TCC.",
    });
  });

  test("usa o título genérico quando 'detalhes' não é uma string (ex.: array de erros de campo)", async () => {
    const response = criarResponse(400, {
      erro: "Erro de validação",
      detalhes: [{ campo: "dataLimite", mensagem: "obrigatório" }],
    });

    await expect(garantirResposta(response, 200)).rejects.toMatchObject({
      status: 400,
      message: "Erro de validação",
      detalhes: [{ campo: "dataLimite", mensagem: "obrigatório" }],
    });
  });
});

describe("normalizarErroGenerico", () => {
  test("repassa um ApiError sem alterações", () => {
    const erroOriginal = new ApiError(403, "Acesso negado");

    expect(normalizarErroGenerico(erroOriginal, "Erro padrão")).toBe(
      erroOriginal,
    );
  });

  test("traduz um TypeError (falha de rede) para mensagem amigável", () => {
    const resultado = normalizarErroGenerico(
      new TypeError("Failed to fetch"),
      "Erro padrão",
    );

    expect(resultado.message).toBe(
      "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
    );
  });

  test("repassa qualquer outro Error sem alterações", () => {
    const erroOriginal = new Error("Algo específico aconteceu");

    expect(normalizarErroGenerico(erroOriginal, "Erro padrão")).toBe(
      erroOriginal,
    );
  });

  test("cai para a mensagem padrão quando o valor lançado não é um Error", () => {
    const resultado = normalizarErroGenerico("string qualquer", "Erro padrão");

    expect(resultado.message).toBe("Erro padrão");
  });
});
