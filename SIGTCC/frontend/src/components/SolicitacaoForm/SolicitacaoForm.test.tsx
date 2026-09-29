import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { SolicitacaoForm } from "./index";
import { criarSolicitacao } from "../../utils/solicitacaoService";

vi.mock("../../utils/solicitacaoService", () => ({
  criarSolicitacao: vi.fn(),
}));

describe("SolicitacaoForm", () => {
  beforeEach(() => vi.clearAllMocks());

  test("exibe erros para todos os campos obrigatórios sem chamar a API", () => {
    render(<MemoryRouter><SolicitacaoForm orientadorId={7} /></MemoryRouter>);

    fireEvent.click(screen.getByRole("button", { name: "Enviar solicitação" }));

    expect(screen.getByText("Informe um nome entre 3 e 100 caracteres.")).toBeInTheDocument();
    expect(screen.getByText("Informe um e-mail válido (até 100 caracteres).")).toBeInTheDocument();
    expect(screen.getByText("Informe um curso entre 2 e 100 caracteres.")).toBeInTheDocument();
    expect(screen.getByText("Informe um tema entre 5 e 150 caracteres.")).toBeInTheDocument();
    expect(screen.getByText("A mensagem deve ter entre 10 e 1000 caracteres.")).toBeInTheDocument();
    expect(criarSolicitacao).not.toHaveBeenCalled();
  });

  test("usa o id devolvido no 201 para disponibilizar acompanhamento sem buscar de novo", async () => {
    vi.mocked(criarSolicitacao).mockResolvedValue({
      id: 84,
      status: "PENDENTE",
      tema: "Tema de teste",
      mensagem: "Mensagem válida de teste",
      justificativa: null,
      criadoEm: "2026-09-28T12:00:00Z",
      aluno: { id: 2, nome: "Ana Souza", email: "ana@example.com", curso: "ADS" },
      orientador: { id: 7, nome: "Prof. Teste", email: "prof@example.com", vagasDisponiveis: 1 },
    });

    render(<MemoryRouter><SolicitacaoForm orientadorId={7} /></MemoryRouter>);
    fireEvent.change(screen.getByLabelText("Nome completo"), { target: { value: "Ana Souza" } });
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "ana@example.com" } });
    fireEvent.change(screen.getByLabelText("Curso"), { target: { value: "ADS" } });
    fireEvent.change(screen.getByLabelText("Tema pretendido"), { target: { value: "Tema de teste" } });
    fireEvent.change(screen.getByLabelText("Mensagem para o orientador"), { target: { value: "Mensagem válida de teste" } });
    fireEvent.click(screen.getByRole("button", { name: "Enviar solicitação" }));

    expect(await screen.findByText("#84")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Acompanhar solicitação" })).toHaveAttribute("href", "/acompanhar/84");
    expect(criarSolicitacao).toHaveBeenCalledWith({
      orientadorId: 7,
      aluno: { nome: "Ana Souza", email: "ana@example.com", curso: "ADS" },
      tema: "Tema de teste",
      mensagem: "Mensagem válida de teste",
    });
  });
});