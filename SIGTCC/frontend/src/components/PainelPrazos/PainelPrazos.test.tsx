import { render, screen, waitFor, act } from "@testing-library/react";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { PainelPrazos } from "./index.tsx";
import { listarPrazosVigentes } from "../../utils/prazoEntregaService.ts";
import type { PrazoEntrega } from "../../types/PrazoEntrega.ts";

vi.mock("../../utils/prazoEntregaService.ts", () => ({
  listarPrazosVigentes: vi.fn(),
}));

describe("PainelPrazos", () => {
  const mockPrazos: PrazoEntrega[] = [
    {
      id: 1,
      semestre: "2026/2",
      tipo: "PROJETO_TCC",
      descricao: "Projeto de TCC",
      dataLimite: "2026-10-15",
    },
    {
      id: 2,
      semestre: "2026/2",
      tipo: "TCC1",
      descricao: "TCC 1",
      dataLimite: "2026-10-20",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test("deve exibir estado de carregamento inicial", () => {
    vi.mocked(listarPrazosVigentes).mockReturnValue(new Promise(() => {}));
    render(<PainelPrazos />);
    expect(
      screen.getByText("Carregando prazos de entrega..."),
    ).toBeInTheDocument();
  });

  test("deve exibir os prazos cadastrados com a contagem regressiva e marcar os não definidos", async () => {
    vi.mocked(listarPrazosVigentes).mockResolvedValue(mockPrazos);

    render(<PainelPrazos dataReferencia={new Date("2026-10-10T10:00:00")} />);

    await waitFor(() => {
      expect(screen.getByText("15/10/2026")).toBeInTheDocument();
    });

    expect(screen.getByText("Projeto de TCC")).toBeInTheDocument();
    expect(screen.getByText("Faltam 5 dias")).toBeInTheDocument();

    expect(screen.getByText("TCC 1")).toBeInTheDocument();
    expect(screen.getByText("20/10/2026")).toBeInTheDocument();
    expect(screen.getByText("Faltam 10 dias")).toBeInTheDocument();

    expect(screen.getByText("TCC 2")).toBeInTheDocument();
    expect(screen.getByText("Prazo não definido")).toBeInTheDocument();
  });

  test("deve atualizar a contagem regressiva refletindo a mudança da data corrente", async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-10-14T10:00:00"));
    vi.mocked(listarPrazosVigentes).mockResolvedValue([mockPrazos[0]]);

    render(<PainelPrazos />);

    await act(async () => {
      await Promise.resolve();
    });

    expect(screen.getByText("Falta 1 dia")).toBeInTheDocument();

    // Avança para o dia do vencimento (15/10/2026)
    act(() => {
      vi.setSystemTime(new Date("2026-10-15T10:00:00"));
      vi.advanceTimersByTime(60_000);
    });

    expect(screen.getByText("Vence hoje")).toBeInTheDocument();

    // Avança para após o vencimento (17/10/2026)
    act(() => {
      vi.setSystemTime(new Date("2026-10-17T10:00:00"));
      vi.advanceTimersByTime(60_000);
    });

    expect(screen.getByText("Prazo encerrado há 2 dias")).toBeInTheDocument();
  });

  test("deve exibir mensagem de erro quando a busca de prazos falhar", async () => {
    vi.mocked(listarPrazosVigentes).mockRejectedValue(
      new Error("Erro ao carregar os prazos de entrega."),
    );

    render(<PainelPrazos />);

    await waitFor(() => {
      expect(
        screen.getByText("Erro ao carregar os prazos de entrega."),
      ).toBeInTheDocument();
    });
  });
});
