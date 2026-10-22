import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import {
  atualizarPrazo,
  cadastrarPrazo,
  listarPrazosVigentes,
} from "./prazoEntregaService";
import type { PrazoEntrega } from "../types/PrazoEntrega";

describe("prazoEntregaService (US05 - Issue #141)", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  test("listarPrazosVigentes busca os prazos cadastrados em GET /api/v1/prazos", async () => {
    const mockPrazos: PrazoEntrega[] = [
      {
        id: 1,
        semestre: "2026/2",
        tipo: "PROJETO_TCC",
        descricao: "Projeto de TCC",
        dataLimite: "2026-10-30",
      },
    ];

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify(mockPrazos), { status: 200 }),
    );

    const resultado = await listarPrazosVigentes();

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/prazos"),
      expect.any(Object),
    );
    expect(resultado).toEqual(mockPrazos);
  });

  test("cadastrarPrazo envia POST /api/v1/prazos para os três tipos de entrega (PROJETO_TCC, TCC1, TCC2)", async () => {
    const tipos = [
      {
        tipo: "PROJETO_TCC" as const,
        descricao: "Projeto de TCC",
        dataLimite: "2026-10-30",
      },
      {
        tipo: "TCC1" as const,
        descricao: "TCC 1",
        dataLimite: "2026-11-20",
      },
      {
        tipo: "TCC2" as const,
        descricao: "TCC 2",
        dataLimite: "2026-12-10",
      },
    ];

    for (const [indice, item] of tipos.entries()) {
      const respostaEsperada: PrazoEntrega = {
        id: indice + 1,
        semestre: "2026/2",
        tipo: item.tipo,
        descricao: item.descricao,
        dataLimite: item.dataLimite,
      };

      vi.mocked(fetch).mockResolvedValueOnce(
        new Response(JSON.stringify(respostaEsperada), { status: 201 }),
      );

      const criado = await cadastrarPrazo({
        tipo: item.tipo,
        dataLimite: item.dataLimite,
      });

      expect(criado).toEqual(respostaEsperada);
      expect(fetch).toHaveBeenLastCalledWith(
        expect.stringContaining("/api/v1/prazos"),
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify({
            tipo: item.tipo,
            dataLimite: item.dataLimite,
          }),
        }),
      );
    }
  });

  test("cadastrarPrazo lança ApiError 403 quando o professor não é responsável pela turma", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          status: 403,
          erro: "Acesso negado",
          detalhes:
            "Apenas o professor responsável pela turma pode configurar o calendário.",
        }),
        { status: 403 },
      ),
    );

    await expect(
      cadastrarPrazo({ tipo: "PROJETO_TCC", dataLimite: "2026-11-30" }),
    ).rejects.toMatchObject({
      status: 403,
      message:
        "Apenas o professor responsável pela turma pode configurar o calendário.",
    });
  });

  test("atualizarPrazo envia PATCH /api/v1/prazos/:id e retorna o prazo atualizado", async () => {
    const prazoAtualizado: PrazoEntrega = {
      id: 10,
      semestre: "2026/2",
      tipo: "PROJETO_TCC",
      descricao: "Projeto de TCC",
      dataLimite: "2026-11-15",
    };

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify(prazoAtualizado), { status: 200 }),
    );

    const resultado = await atualizarPrazo(10, { dataLimite: "2026-11-15" });

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/prazos/10"),
      expect.objectContaining({
        method: "PATCH",
        body: JSON.stringify({ dataLimite: "2026-11-15" }),
      }),
    );
    expect(resultado).toEqual(prazoAtualizado);
  });

  test("rejeita data inválida com ApiError 422 ou 400 vindo do backend", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          status: 422,
          erro: "Erro de regra de negócio",
          detalhes: "A data limite não pode estar no passado.",
        }),
        { status: 422 },
      ),
    );

    await expect(
      cadastrarPrazo({ tipo: "PROJETO_TCC", dataLimite: "2020-01-01" }),
    ).rejects.toMatchObject({
      status: 422,
      message: "A data limite não pode estar no passado.",
    });
  });
});
