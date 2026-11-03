import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../../auth/AuthContext";
import {
  listarEntregasDoOrientador,
  registrarParecer,
} from "../../utils/entregaService";
import { obterPerfilProfessor } from "../../utils/professorService";
import { AvaliacaoEntregaPage } from "./index";

vi.mock("../../utils/entregaService", () => ({
  criarEntrega: vi.fn(),
  listarEntregasDoAluno: vi.fn(),
  listarEntregasDoOrientador: vi.fn(),
  registrarParecer: vi.fn(),
}));

vi.mock("../../utils/professorService", () => ({
  obterPerfilProfessor: vi.fn(),
}));

const entrega = {
  id: 8,
  tipo: "TCC1" as const,
  status: "SUBMETIDO" as const,
  urlDocumento: "https://documento.exemplo/tcc",
  dataEnvio: "2026-11-03T12:00:00Z",
  atrasada: false,
  aluno: { id: 4, nome: "Ana Souza", email: "ana@example.edu" },
};

describe("AvaliacaoEntregaPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(obterPerfilProfessor).mockResolvedValue({
      id: 3,
      nome: "Professor",
      email: "professor@example.edu",
      departamento: null,
      responsavelTurma: false,
    });
    vi.mocked(listarEntregasDoOrientador).mockResolvedValue([entrega]);
  });

  function renderizarPagina() {
    return render(
      <AuthProvider>
        <MemoryRouter>
          <AvaliacaoEntregaPage />
        </MemoryRouter>
      </AuthProvider>,
    );
  }

  it("exige comentário ao registrar ajustes necessários", async () => {
    renderizarPagina();
    const seletor = await screen.findByLabelText("Parecer");
    fireEvent.change(seletor, { target: { value: "AJUSTES_NECESSARIOS" } });
    fireEvent.click(screen.getByRole("button", { name: "Registrar parecer" }));

    expect(
      await screen.findByText("O comentário é obrigatório para este parecer."),
    ).toBeInTheDocument();
    expect(registrarParecer).not.toHaveBeenCalled();
  });

  it("mantém os campos em leitura para parecer definitivo", async () => {
    vi.mocked(listarEntregasDoOrientador).mockResolvedValue([
      { ...entrega, status: "APROVADO", comentarioParecer: "Aprovado." },
    ]);
    renderizarPagina();

    expect(await screen.findByLabelText("Parecer")).toBeDisabled();
    expect(screen.getByLabelText("Comentário")).toBeDisabled();
    expect(
      screen.queryByRole("button", { name: "Registrar parecer" }),
    ).not.toBeInTheDocument();
  });

  it("bloqueia a edição assim que um parecer aprovado é salvo", async () => {
    vi.mocked(registrarParecer).mockResolvedValue({
      ...entrega,
      status: "APROVADO",
      comentarioParecer: "Trabalho aprovado.",
    });
    renderizarPagina();
    await screen.findByLabelText("Parecer");
    fireEvent.click(screen.getByRole("button", { name: "Registrar parecer" }));

    expect(
      await screen.findByText(
        "Parecer definitivo. Os campos estão somente para leitura.",
      ),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Parecer")).toBeDisabled();
  });

  it("distingue erro ao carregar de uma lista vazia", async () => {
    vi.mocked(listarEntregasDoOrientador).mockRejectedValue(
      new Error("Erro ao processar a requisição"),
    );
    renderizarPagina();

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Não foi possível carregar as entregas",
    );
    expect(
      screen.queryByText("Não há entregas para avaliar."),
    ).not.toBeInTheDocument();
  });
});
