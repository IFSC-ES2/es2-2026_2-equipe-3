import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { OrientadorForm } from "./index.tsx";
import {
  cadastrarOrientador,
  atualizarOrientador,
} from "../../utils/orientadorService.ts";
import { ApiError } from "../../utils/apiErro.ts";
import type { Orientador } from "../../types/Orientador.ts";

vi.mock("../../utils/orientadorService.ts", () => {
  return {
    cadastrarOrientador: vi.fn(),
    atualizarOrientador: vi.fn(),
  };
});

vi.mock("../LinhaPesquisa", () => ({
  LinhaPesquisaInput: ({
    onChange,
  }: {
    onChange: (linhas: string[]) => void;
  }) => (
    <button
      type="button"
      onClick={() => onChange(["Engenharia de Software"])}
      data-testid="mock-add-linha"
    >
      Adicionar Linha Mock
    </button>
  ),
}));

describe("OrientadorForm", () => {
  const mockOnSucesso = vi.fn();
  const mockOnErro = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("deve renderizar o formulário vazio para modo de cadastro", () => {
    render(
      <OrientadorForm
        emailAutenticado=""
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    expect(screen.getByText("Cadastrar Novo Orientador")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Cadastrar orientador" }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/nome \*/i)).toHaveValue("");
  });

  test("deve renderizar o formulário preenchido para modo de edição", () => {
    const orientadorExistente: Orientador = {
      id: 1,
      nome: "Dr. Teste",
      email: "teste@ifsc.edu.br",
      vagasDisponiveis: 2,
      departamento: "DAE",
      biografia: "Bio teste",
      linhasDePesquisa: ["IA"],
      ativo: true,
    };

    render(
      <OrientadorForm
        emailAutenticado="teste@ifsc.edu.br"
        orientador={orientadorExistente}
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    expect(screen.getByText("Editar Perfil")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Atualizar perfil" }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/nome \*/i)).toHaveValue("Dr. Teste");
    expect(screen.getByLabelText(/vagas disponíveis/i)).toHaveValue(2);
  });

  test("deve exibir erros de validação local e bloquear envio se os campos obrigatórios estiverem vazios", async () => {
    const user = userEvent.setup();
    render(
      <OrientadorForm
        emailAutenticado=""
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    const botaoSubmit = screen.getByRole("button", {
      name: "Cadastrar orientador",
    });
    await user.click(botaoSubmit);

    expect(screen.getByText("O nome é obrigatório")).toBeInTheDocument();
    expect(screen.getByText("O e-mail é obrigatório")).toBeInTheDocument();
    expect(
      screen.getByText("Informe ao menos uma linha de pesquisa"),
    ).toBeInTheDocument();

    expect(cadastrarOrientador).not.toHaveBeenCalled();
  });

  test("deve limpar o erro do campo ao voltar a digitar nele", async () => {
    const user = userEvent.setup();
    render(
      <OrientadorForm
        emailAutenticado=""
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    await user.click(
      screen.getByRole("button", { name: "Cadastrar orientador" }),
    );
    expect(screen.getByText("O nome é obrigatório")).toBeInTheDocument();

    const inputNome = screen.getByLabelText(/nome \*/i);
    await user.type(inputNome, "A");

    expect(screen.queryByText("O nome é obrigatório")).not.toBeInTheDocument();
  });

  test("deve submeter o formulário corretamente no modo de cadastro", async () => {
    const user = userEvent.setup();
    const mockResultado = { id: 1, nome: "Novo Orientador" };

    vi.mocked(cadastrarOrientador).mockResolvedValue(
      mockResultado as unknown as Orientador,
    );

    render(
      <OrientadorForm
        emailAutenticado="jwt.professor@ifsc.edu.br"
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    await user.type(screen.getByLabelText(/nome \*/i), "Novo Orientador");
    expect(screen.getByLabelText(/e-mail \*/i)).toHaveValue(
      "jwt.professor@ifsc.edu.br",
    );
    expect(screen.getByLabelText(/e-mail \*/i)).toBeDisabled();

    const inputVagas = screen.getByLabelText(/vagas disponíveis/i);
    await user.clear(inputVagas);
    await user.type(inputVagas, "3");

    await user.click(screen.getByTestId("mock-add-linha"));

    await user.click(
      screen.getByRole("button", { name: "Cadastrar orientador" }),
    );

    await waitFor(() => {
      expect(cadastrarOrientador).toHaveBeenCalledWith({
        nome: "Novo Orientador",
        email: "jwt.professor@ifsc.edu.br",
        departamento: "",
        biografia: "",
        vagasDisponiveis: 3,
        linhasDePesquisa: ["Engenharia de Software"],
      });
      expect(mockOnSucesso).toHaveBeenCalledWith(mockResultado);
    });

    expect(screen.getByLabelText(/nome \*/i)).toHaveValue("");
  });

  test("deve submeter o formulário corretamente no modo de edição", async () => {
    const user = userEvent.setup();
    const orientadorExistente: Orientador = {
      id: 1,
      nome: "Dr. Teste",
      email: "teste@ifsc.edu.br",
      vagasDisponiveis: 2,
      departamento: "DAE",
      biografia: "Bio teste",
      linhasDePesquisa: ["IA"],
      ativo: true,
    };
    const mockResultado = {
      ...orientadorExistente,
      nome: "Dr. Teste Atualizado",
    };

    vi.mocked(atualizarOrientador).mockResolvedValue(
      mockResultado as unknown as Orientador,
    );

    render(
      <OrientadorForm
        emailAutenticado="teste@ifsc.edu.br"
        orientador={orientadorExistente}
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    const inputNome = screen.getByLabelText(/nome \*/i);
    await user.clear(inputNome); // Limpa o valor antigo
    await user.type(inputNome, "Dr. Teste Atualizado"); // Digita o novo

    await user.click(screen.getByRole("button", { name: "Atualizar perfil" }));

    await waitFor(() => {
      expect(atualizarOrientador).toHaveBeenCalledWith(1, {
        nome: "Dr. Teste Atualizado",
        email: "teste@ifsc.edu.br",
        departamento: "DAE",
        linhasDePesquisa: ["IA"],
        vagasDisponiveis: 2,
        biografia: "Bio teste",
      });
      expect(mockOnSucesso).toHaveBeenCalledWith(mockResultado);
    });
  });

  test("normaliza campos nulos no perfil e envia apenas campos editáveis no PATCH", async () => {
    const user = userEvent.setup();
    const orientadorComCamposNulos: Orientador = {
      id: 8,
      nome: "Professora Exemplo",
      email: "professora@ifsc.edu.br",
      vagasDisponiveis: 1,
      departamento: null,
      biografia: null,
      linhasDePesquisa: ["IA"],
      ativo: true,
    };
    vi.mocked(atualizarOrientador).mockResolvedValue(
      orientadorComCamposNulos,
    );

    render(
      <OrientadorForm
        emailAutenticado="professora@ifsc.edu.br"
        orientador={orientadorComCamposNulos}
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    expect(screen.getByLabelText(/departamento/i)).toHaveValue("");
    expect(screen.getByLabelText(/biografia/i)).toHaveValue("");

    await user.click(screen.getByRole("button", { name: "Atualizar perfil" }));

    await waitFor(() => {
      expect(atualizarOrientador).toHaveBeenCalledWith(8, {
        nome: "Professora Exemplo",
        email: "professora@ifsc.edu.br",
        departamento: "",
        linhasDePesquisa: ["IA"],
        vagasDisponiveis: 1,
        biografia: "",
      });
    });
  });

  test("deve processar erros de validação vindos do backend (ApiError 400)", async () => {
    const user = userEvent.setup();
    vi.mocked(cadastrarOrientador).mockRejectedValue(
      new ApiError(400, "Erro de validação", [
        { campo: "email", mensagem: "E-mail já cadastrado" },
      ]),
    );

    render(
      <OrientadorForm
        emailAutenticado="professor@ifsc.edu.br"
        onSucesso={mockOnSucesso}
        onErro={mockOnErro}
      />,
    );

    await user.type(screen.getByLabelText(/nome \*/i), "Fulano");
    await user.click(screen.getByTestId("mock-add-linha"));

    await user.click(
      screen.getByRole("button", { name: "Cadastrar orientador" }),
    );

    await waitFor(() => {
      expect(screen.getByText("E-mail já cadastrado")).toBeInTheDocument();
      expect(mockOnErro).toHaveBeenCalled();
    });
  });
});
