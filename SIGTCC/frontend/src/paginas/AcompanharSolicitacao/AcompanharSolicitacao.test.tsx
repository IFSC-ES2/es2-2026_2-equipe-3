import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi } from "vitest";
import { ApiError } from "../../utils/orientadorService";
import { obterSolicitacao } from "../../utils/solicitacaoService";
import { AcompanharSolicitacaoPage } from "./index";

vi.mock("../../utils/solicitacaoService", () => ({
  obterSolicitacao: vi.fn(),
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
    <MemoryRouter initialEntries={["/acompanhar/84"]}>
      <Routes>
        <Route path="/acompanhar/:id" element={<AcompanharSolicitacaoPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("AcompanharSolicitacaoPage", () => {
  beforeEach(() => vi.clearAllMocks());

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
});
