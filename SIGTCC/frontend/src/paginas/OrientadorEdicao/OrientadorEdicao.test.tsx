import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi, beforeEach } from "vitest";
import { OrientadorEdicaoPage } from "./index";
import { obterOrientador } from "../../utils/orientadorService";

vi.mock("../../utils/orientadorService", () => ({
  obterOrientador: vi.fn(),
}));

describe("OrientadorEdicaoPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  test("deve exibir erro se não estiver autenticado (sem ID no localStorage)", async () => {
    render(
      <MemoryRouter>
        <OrientadorEdicaoPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(
        screen.getByText(
          "Você não está autenticado. Faça o cadastro primeiro.",
        ),
      ).toBeInTheDocument();
    });
  });

  test("deve carregar o formulário de edição se estiver autenticado no localStorage", async () => {
    localStorage.setItem("orientadorId", "1");
    vi.mocked(obterOrientador).mockResolvedValue({
      id: 1,
      nome: "Dr. Teste",
      email: "teste@ifsc.edu.br",
      vagasDisponiveis: 2,
      linhasDePesquisa: ["IA"],
      ativo: true,
    });

    render(
      <MemoryRouter>
        <OrientadorEdicaoPage />
      </MemoryRouter>,
    );

    expect(screen.getByText("Carregando perfil...")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText("Editar Perfil")).toBeInTheDocument();
      expect(screen.getByDisplayValue("Dr. Teste")).toBeInTheDocument();
    });
  });
});
