import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { OrientadorList } from "./index.tsx";
import { listarOrientadores } from "../../utils/orientadorService.ts";
import type { Orientador } from "../../types/Orientador.ts";

vi.mock("../../utils/orientadorService.ts", () => ({
  listarOrientadores: vi.fn(),
}));

const renderOrientadorList = () =>
  render(
    <MemoryRouter>
      <OrientadorList />
    </MemoryRouter>,
  );

describe("OrientadorList", () => {
  const mockOrientadores: Orientador[] = [
    {
      id: 1,
      nome: "Dr. Adriano Lima",
      email: "adriano@ifsc.edu.br",
      vagasDisponiveis: 3,
      linhasDePesquisa: ["IA"],
      ativo: true,
    },
    {
      id: 2,
      nome: "Dra. Maria",
      email: "Maria@ifsc.edu.br",
      vagasDisponiveis: 1,
      linhasDePesquisa: ["Redes"],
      ativo: false,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("deve exibir o estado de carregamento inicial", () => {
    vi.mocked(listarOrientadores).mockReturnValue(new Promise(() => {}));

    renderOrientadorList();

    expect(screen.getByText("Carregando orientadores...")).toBeInTheDocument();
  });

  test("deve carregar e renderizar apenas orientadores ativos na montagem", async () => {
    vi.mocked(listarOrientadores).mockResolvedValue(mockOrientadores);

    renderOrientadorList();

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando orientadores..."),
      ).not.toBeInTheDocument();
    });

    expect(screen.getByText("Dr. Adriano Lima")).toBeInTheDocument();
    expect(screen.queryByText("Dra. Inativa")).not.toBeInTheDocument();
  });

  test("deve exibir mensagem de erro se a chamada à API falhar", async () => {
    vi.mocked(listarOrientadores).mockRejectedValue(new Error("Erro de rede"));

    renderOrientadorList();

    await waitFor(() => {
      expect(
        screen.getByText("Erro ao carregar orientadores"),
      ).toBeInTheDocument();
    });

    expect(screen.queryByText("Dr. Adriano Lima")).not.toBeInTheDocument();
  });

  test("deve exibir empty state quando não houver orientadores ativos retornados", async () => {
    vi.mocked(listarOrientadores).mockResolvedValue([mockOrientadores[1]]);

    renderOrientadorList();

    await waitFor(() => {
      expect(
        screen.getByText("Nenhum orientador encontrado"),
      ).toBeInTheDocument();
    });
  });

  test("deve chamar a API com a linha de pesquisa digitada, refletindo o filtro automaticamente", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue([]);

    renderOrientadorList();

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenCalledWith({
        area: undefined,
        temVagas: undefined,
      });
    });

    const inputArea = screen.getByPlaceholderText(
      "Ex.: Engenharia de Software",
    );
    await user.type(inputArea, "IA");

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenLastCalledWith({
        area: "IA",
        temVagas: undefined,
      });
    });
  });

  test("deve combinar o filtro de linha de pesquisa com o alternador de vagas disponíveis", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue([]);

    renderOrientadorList();

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenCalledWith({
        area: undefined,
        temVagas: undefined,
      });
    });

    const inputArea = screen.getByPlaceholderText(
      "Ex.: Engenharia de Software",
    );
    await user.type(inputArea, "IA");

    const toggleVagas = screen.getByLabelText("Somente com vagas disponíveis");
    await user.click(toggleVagas);

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenLastCalledWith({
        area: "IA",
        temVagas: true,
      });
    });
  });

  test("botão 'Limpar filtros' deve estar desabilitado sem filtros e habilitado após aplicá-los", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue([]);

    renderOrientadorList();

    const botaoLimpar = screen.getByRole("button", { name: "Limpar filtros" });
    expect(botaoLimpar).toBeDisabled();

    const toggleVagas = screen.getByLabelText("Somente com vagas disponíveis");
    await user.click(toggleVagas);

    expect(botaoLimpar).toBeEnabled();
  });

  test("deve limpar os filtros e voltar a listar todos os orientadores ao clicar em 'Limpar filtros'", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue([]);

    renderOrientadorList();

    const inputArea = screen.getByPlaceholderText(
      "Ex.: Engenharia de Software",
    );
    await user.type(inputArea, "IA");

    const toggleVagas = screen.getByLabelText("Somente com vagas disponíveis");
    await user.click(toggleVagas);

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenLastCalledWith({
        area: "IA",
        temVagas: true,
      });
    });

    await user.click(screen.getByRole("button", { name: "Limpar filtros" }));

    expect(inputArea).toHaveValue("");
    expect(toggleVagas).not.toBeChecked();

    await waitFor(() => {
      expect(vi.mocked(listarOrientadores)).toHaveBeenLastCalledWith({
        area: undefined,
        temVagas: undefined,
      });
    });
  });
});
