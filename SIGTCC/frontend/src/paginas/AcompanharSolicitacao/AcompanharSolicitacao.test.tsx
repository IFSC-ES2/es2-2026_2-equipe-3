import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi, describe, test, expect, beforeEach, afterEach } from "vitest";
import { ApiError } from "../../utils/apiErro";
import { obterSolicitacao } from "../../utils/solicitacaoService";
import { listarPrazosVigentes } from "../../utils/prazoEntregaService";
import { AcompanharSolicitacaoPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";

vi.mock("../../utils/solicitacaoService", () => ({
  obterSolicitacao: vi.fn(),
}));

vi.mock("../../utils/prazoEntregaService", () => ({
  listarPrazosVigentes: vi.fn().mockResolvedValue([]),
}));

const solicitacao = {
  id: 84,
  status: "RECUSADA" as const,
  tema: "Tema de teste",
  mensagem: "Mensagem de teste suficientemente longa",
  justificativa: "No momento não há disponibilidade para esse tema.",
  criadoEm: "2026-09-28T12:00:00Z",
  aluno: { id: 2, nome: "Ana Souza", email: "ana@example.com", curso: "ADS" },
  orientador: {
    id: 7,
    nome: "Prof. Teste",
    email: "prof@example.com",
    vagasDisponiveis: 1,
  },
};

function renderRota() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={["/acompanhar/84"]}>
        <Routes>
          <Route
            path="/acompanhar/:id"
            element={<AcompanharSolicitacaoPage />}
          />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  );
}

describe("AcompanharSolicitacaoPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test("exibe status e justificativa quando a solicitação foi recusada", async () => {
    vi.mocked(obterSolicitacao).mockResolvedValue(solicitacao);
    renderRota();
    expect(await screen.findByRole("status")).toHaveTextContent(
      "Solicitação recusada",
    );
    expect(screen.getByText(solicitacao.justificativa)).toBeInTheDocument();
  });

  test("exibe mensagem amigável quando a API responde 404", async () => {
    vi.mocked(obterSolicitacao).mockRejectedValue(
      new ApiError(404, "Não encontrado"),
    );
    renderRota();
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Não encontramos uma solicitação",
    );
  });

  test("exibe os prazos vigentes com contagem regressiva no acompanhamento do aluno", async () => {
    vi.setSystemTime(new Date("2026-10-10T10:00:00"));
    vi.mocked(obterSolicitacao).mockResolvedValue(solicitacao);
    vi.mocked(listarPrazosVigentes).mockResolvedValue([
      {
        id: 1,
        semestre: "2026/2",
        tipo: "TCC1",
        descricao: "TCC 1",
        dataLimite: "2026-10-18",
      },
    ]);

    renderRota();

    await waitFor(() => {
      expect(screen.getByText("18/10/2026")).toBeInTheDocument();
    });
    expect(screen.getByText("Faltam 8 dias")).toBeInTheDocument();
  });
});