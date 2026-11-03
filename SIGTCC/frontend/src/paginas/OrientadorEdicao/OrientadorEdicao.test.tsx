import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { OrientadorEdicaoPage } from "./index";
import { obterOrientador } from "../../utils/orientadorService";
import { obterPerfilProfessor } from "../../utils/professorService";
import { ApiError } from "../../utils/apiErro";
import { AuthProvider } from "../../auth/AuthContext";
import type { Orientador } from "../../types/Orientador";
import type { PerfilProfessor } from "../../types/Professor";

vi.mock("../../utils/orientadorService", () => ({
  obterOrientador: vi.fn(),
}));

vi.mock("../../utils/professorService", () => ({
  obterPerfilProfessor: vi.fn(),
}));

const perfilLogado: PerfilProfessor = {
  id: 5,
  nome: "Dr. Logado",
  email: "professor.orientador@example.com",
  departamento: null,
  responsavelTurma: false,
};

const orientadorLogado: Orientador = {
  id: 5,
  nome: "Dr. Logado",
  email: "professor.orientador@example.com",
  vagasDisponiveis: 2,
  linhasDePesquisa: ["IA"],
  ativo: true,
};

function renderizarPagina() {
  render(
    <AuthProvider>
      <MemoryRouter>
        <OrientadorEdicaoPage />
      </MemoryRouter>
    </AuthProvider>,
  );
}

describe("OrientadorEdicaoPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  test("carrega o perfil do professor autenticado via /professores/me", async () => {
    vi.mocked(obterPerfilProfessor).mockResolvedValue(perfilLogado);
    vi.mocked(obterOrientador).mockResolvedValue(orientadorLogado);

    renderizarPagina();

    expect(screen.getByText("Carregando perfil...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Editar Perfil")).toBeInTheDocument();
      expect(screen.getByDisplayValue("Dr. Logado")).toBeInTheDocument();
    });
    expect(obterPerfilProfessor).toHaveBeenCalledTimes(1);
    expect(obterOrientador).toHaveBeenCalledWith(5);
  });

  test("ignora orientadorId do localStorage e não abre o perfil de outro professor (Issue #204)", async () => {
    localStorage.setItem("orientadorId", "1");
    vi.mocked(obterPerfilProfessor).mockResolvedValue(perfilLogado);
    vi.mocked(obterOrientador).mockResolvedValue(orientadorLogado);

    renderizarPagina();

    await waitFor(() => {
      expect(screen.getByDisplayValue("Dr. Logado")).toBeInTheDocument();
    });
    expect(obterOrientador).toHaveBeenCalledTimes(1);
    expect(obterOrientador).toHaveBeenCalledWith(5);
    expect(obterOrientador).not.toHaveBeenCalledWith(1);
  });

  test("exibe aviso quando o usuário não possui perfil de orientador (404)", async () => {
    vi.mocked(obterPerfilProfessor).mockRejectedValue(
      new ApiError(404, "Professor não encontrado."),
    );

    renderizarPagina();

    expect(
      await screen.findByText(
        "Seu usuário não possui um perfil de orientador cadastrado.",
      ),
    ).toBeInTheDocument();
    expect(obterOrientador).not.toHaveBeenCalled();
  });

  test("exibe a mensagem do erro quando /professores/me falha", async () => {
    vi.mocked(obterPerfilProfessor).mockRejectedValue(
      new Error(
        "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
      ),
    );

    renderizarPagina();

    expect(
      await screen.findByText(
        "Não foi possível conectar ao servidor. Verifique se o backend está rodando.",
      ),
    ).toBeInTheDocument();
    expect(obterOrientador).not.toHaveBeenCalled();
  });
});
