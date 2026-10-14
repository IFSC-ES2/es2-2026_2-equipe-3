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

const orientadores: Orientador[] = [
  {
    id: 1,
    nome: "Ana IA",
    email: "ana@ifsc.edu.br",
    vagasDisponiveis: 2,
    linhasDePesquisa: ["Inteligência Artificial"],
    ativo: true,
  },
  {
    id: 2,
    nome: "Bruno IA",
    email: "bruno@ifsc.edu.br",
    vagasDisponiveis: 0,
    linhasDePesquisa: ["Inteligência Artificial"],
    ativo: true,
  },
  {
    id: 3,
    nome: "Carla Redes",
    email: "carla@ifsc.edu.br",
    vagasDisponiveis: 4,
    linhasDePesquisa: ["Redes de Computadores"],
    ativo: true,
  },
];

const renderizar = () =>
  render(
    <MemoryRouter>
      <OrientadorList />
    </MemoryRouter>,
  );

const aguardarCarregamento = () =>
  waitFor(() =>
    expect(
      screen.queryByText("Carregando orientadores..."),
    ).not.toBeInTheDocument(),
  );

describe("Filtros do Catálogo de Orientadores (#136) - cenários já suportados", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("filtro por linha de pesquisa envia o termo e exibe somente o resultado retornado", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores)
      .mockResolvedValueOnce(orientadores)
      .mockResolvedValueOnce([orientadores[2]]);

    renderizar();
    await aguardarCarregamento();
    expect(screen.getByText("Ana IA")).toBeInTheDocument();

    await user.type(screen.getByLabelText("Área de atuação"), "Redes");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));

    await waitFor(() =>
      expect(screen.queryByText("Ana IA")).not.toBeInTheDocument(),
    );
    expect(listarOrientadores).toHaveBeenLastCalledWith("Redes");
    expect(screen.getByText("Carla Redes")).toBeInTheDocument();
  });

  test("filtro com termo só de espaços é tratado como listagem completa", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue(orientadores);

    renderizar();
    await aguardarCarregamento();

    await user.type(screen.getByLabelText("Área de atuação"), "   ");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));

    await waitFor(() => expect(listarOrientadores).toHaveBeenCalledTimes(2));
    expect(listarOrientadores).toHaveBeenLastCalledWith(undefined);
  });

  test("estado de nenhum resultado é exibido quando a filtragem retorna lista vazia", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores)
      .mockResolvedValueOnce(orientadores)
      .mockResolvedValueOnce([]);

    renderizar();
    await aguardarCarregamento();

    await user.type(screen.getByLabelText("Área de atuação"), "Robótica");
    await user.click(screen.getByRole("button", { name: "Filtrar" }));

    expect(
      await screen.findByText("Nenhum orientador encontrado"),
    ).toBeInTheDocument();
    expect(screen.queryByText("Ana IA")).not.toBeInTheDocument();
  });
});

describe.skip("Filtros do Catálogo de Orientadores (#136) - dependem das issues #134 e #135", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("filtro por vagas disponíveis: toggle 'Somente com vagas disponíveis' consulta com temVagas", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue(orientadores);

    renderizar();
    await aguardarCarregamento();

    await user.click(screen.getByLabelText("Somente com vagas disponíveis"));

    await waitFor(() =>
      expect(listarOrientadores).toHaveBeenLastCalledWith(undefined, true),
    );
  });

  test("combinação de filtros: linha de pesquisa e vagas são enviados juntos", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue(orientadores);

    renderizar();
    await aguardarCarregamento();

    await user.type(screen.getByLabelText("Área de atuação"), "Inteligência");
    await user.click(screen.getByLabelText("Somente com vagas disponíveis"));

    await waitFor(() =>
      expect(listarOrientadores).toHaveBeenLastCalledWith("Inteligência", true),
    );
  });

  test("limpar filtros retorna à listagem completa", async () => {
    const user = userEvent.setup();
    vi.mocked(listarOrientadores).mockResolvedValue(orientadores);

    renderizar();
    await aguardarCarregamento();

    await user.type(screen.getByLabelText("Área de atuação"), "Inteligência");
    await user.click(screen.getByLabelText("Somente com vagas disponíveis"));
    await user.click(screen.getByRole("button", { name: "Limpar filtros" }));

    await waitFor(() =>
      expect(listarOrientadores).toHaveBeenLastCalledWith(undefined, undefined),
    );
    expect(
      screen.getByLabelText("Somente com vagas disponíveis"),
    ).not.toBeChecked();
  });

  test("estado vazio sugere ajustar ou limpar os filtros", async () => {
    vi.mocked(listarOrientadores).mockResolvedValue([]);

    renderizar();

    expect(
      await screen.findByText("Nenhum orientador encontrado"),
    ).toBeInTheDocument();
    expect(screen.getByText(/ajuste ou limpe os filtros/i)).toBeInTheDocument();
  });
});