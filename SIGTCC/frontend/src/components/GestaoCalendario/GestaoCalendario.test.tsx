import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, test, expect, vi, beforeEach } from "vitest";
import { GestaoCalendario } from "./index.tsx";
import { ApiError } from "../../utils/apiErro";
import {
  atualizarPrazo,
  cadastrarPrazo,
  listarPrazosVigentes,
} from "../../utils/prazoEntregaService.ts";
import type { PrazoEntrega } from "../../types/PrazoEntrega.ts";

vi.mock("../../utils/prazoEntregaService.ts", async () => {
  const actual = await vi.importActual<
    typeof import("../../utils/prazoEntregaService.ts")
  >("../../utils/prazoEntregaService.ts");
  return {
    ...actual,
    listarPrazosVigentes: vi.fn(),
    cadastrarPrazo: vi.fn(),
    atualizarPrazo: vi.fn(),
  };
});

describe("GestaoCalendario", () => {
  const prazoProjetoExistente: PrazoEntrega = {
    id: 10,
    semestre: "2026/2",
    tipo: "PROJETO_TCC",
    descricao: "Projeto de TCC",
    dataLimite: "2026-11-30",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("deve exibir o estado de carregamento inicial", () => {
    vi.mocked(listarPrazosVigentes).mockReturnValue(new Promise(() => {}));
    render(<GestaoCalendario />);
    expect(
      screen.getByText("Carregando calendário de entregas..."),
    ).toBeInTheDocument();
  });

  test("deve listar os prazos vigentes e marcar os tipos ainda não cadastrados", async () => {
    vi.mocked(listarPrazosVigentes).mockResolvedValue([prazoProjetoExistente]);

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(screen.getByText("30/11/2026")).toBeInTheDocument();
    });

    const linhas = screen.getAllByText("Não cadastrado");
    expect(linhas).toHaveLength(2); // TCC 1 e TCC 2 ainda sem prazo
  });

  test("deve cadastrar um novo prazo para um tipo ainda não cadastrado", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
    vi.mocked(cadastrarPrazo).mockResolvedValue({
      id: 20,
      semestre: "2026/2",
      tipo: "TCC1",
      descricao: "TCC 1",
      dataLimite: "2027-03-15",
    });

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando calendário de entregas..."),
      ).not.toBeInTheDocument();
    });

    await user.selectOptions(
      screen.getByLabelText("Tipo de entrega *"),
      "TCC1",
    );
    expect(
      screen.getByRole("button", { name: "Cadastrar prazo" }),
    ).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2027-03-15" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));

    await waitFor(() => {
      expect(cadastrarPrazo).toHaveBeenCalledWith({
        tipo: "TCC1",
        dataLimite: "2027-03-15",
      });
    });

    expect(
      await screen.findByText("Prazo cadastrado com sucesso."),
    ).toBeInTheDocument();
  });

  test("deve permitir o cadastro dos três tipos de entrega (Projeto de TCC, TCC 1 e TCC 2)", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);

    vi.mocked(cadastrarPrazo)
      .mockResolvedValueOnce({
        id: 1,
        semestre: "2026/2",
        tipo: "PROJETO_TCC",
        descricao: "Projeto de TCC",
        dataLimite: "2026-10-30",
      })
      .mockResolvedValueOnce({
        id: 2,
        semestre: "2026/2",
        tipo: "TCC1",
        descricao: "TCC 1",
        dataLimite: "2026-11-20",
      })
      .mockResolvedValueOnce({
        id: 3,
        semestre: "2026/2",
        tipo: "TCC2",
        descricao: "TCC 2",
        dataLimite: "2026-12-10",
      });

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando calendário de entregas..."),
      ).not.toBeInTheDocument();
    });

    // 1. Cadastra Projeto de TCC
    await user.selectOptions(
      screen.getByLabelText("Tipo de entrega *"),
      "PROJETO_TCC",
    );
    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2026-10-30" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("30/10/2026")).toBeInTheDocument();

    // 2. Cadastra TCC 1
    await user.selectOptions(
      screen.getByLabelText("Tipo de entrega *"),
      "TCC1",
    );
    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2026-11-20" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("20/11/2026")).toBeInTheDocument();

    // 3. Cadastra TCC 2
    await user.selectOptions(
      screen.getByLabelText("Tipo de entrega *"),
      "TCC2",
    );
    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2026-12-10" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("10/12/2026")).toBeInTheDocument();

    expect(screen.queryByText("Não cadastrado")).not.toBeInTheDocument();
    expect(cadastrarPrazo).toHaveBeenCalledTimes(3);
  });

  test("deve editar um prazo já cadastrado a partir da mesma tela", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([prazoProjetoExistente]);
    vi.mocked(atualizarPrazo).mockResolvedValue({
      ...prazoProjetoExistente,
      dataLimite: "2026-12-10",
    });

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(screen.getByText("30/11/2026")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Editar" }));

    const campoData = screen.getByLabelText("Data limite *");
    expect(campoData).toHaveValue("2026-11-30");
    expect(
      screen.getByRole("button", { name: "Salvar alterações" }),
    ).toBeInTheDocument();

    fireEvent.change(campoData, { target: { value: "2026-12-10" } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));

    await waitFor(() => {
      expect(atualizarPrazo).toHaveBeenCalledWith(prazoProjetoExistente.id, {
        dataLimite: "2026-12-10",
      });
    });

    expect(
      await screen.findByText("Prazo atualizado com sucesso."),
    ).toBeInTheDocument();
    expect(screen.getByText("10/12/2026")).toBeInTheDocument();
  });

  test("deve exibir as mensagens de erro de validação retornadas pelo backend (422 e 400)", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
    vi.mocked(cadastrarPrazo).mockRejectedValueOnce(
      new ApiError(422, "Prazo deve ser posterior ao do Projeto de TCC."),
    );

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando calendário de entregas..."),
      ).not.toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2020-01-01" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));

    expect(
      await screen.findByText("Prazo deve ser posterior ao do Projeto de TCC."),
    ).toBeInTheDocument();

    // Testa erro 400 com detalhe específico do campo dataLimite
    vi.mocked(cadastrarPrazo).mockRejectedValueOnce(
      new ApiError(400, "Erro de validação", [
        {
          campo: "dataLimite",
          mensagem: "A data limite deve ser uma data futura.",
        },
      ]),
    );

    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));

    expect(
      await screen.findByText("A data limite deve ser uma data futura."),
    ).toBeInTheDocument();
  });

  test("deve exibir erro de bloqueio (403) caso um professor não responsável tente cadastrar prazo", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
    vi.mocked(cadastrarPrazo).mockRejectedValue(
      new ApiError(
        403,
        "Apenas o professor responsável pela turma pode gerenciar prazos.",
      ),
    );

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando calendário de entregas..."),
      ).not.toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("Data limite *"), {
      target: { value: "2026-11-30" },
    });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Apenas o professor responsável pela turma pode gerenciar prazos.",
    );
  });

  test("deve exigir a data limite antes de enviar o formulário", async () => {
    const user = userEvent.setup();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);

    render(<GestaoCalendario />);

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando calendário de entregas..."),
      ).not.toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));

    expect(
      screen.getByText("A data limite é obrigatória."),
    ).toBeInTheDocument();
    expect(cadastrarPrazo).not.toHaveBeenCalled();
  });
});
