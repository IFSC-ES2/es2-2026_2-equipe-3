import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { SolicitacaoForm } from "./index";
import { criarSolicitacao } from "../../utils/solicitacaoService";
import {
  AuthContext,
  type AuthContextValor,
} from "../../auth/authContextInstance";
import { ApiError } from "../../utils/apiErro";

vi.mock("../../utils/solicitacaoService", () => ({
  criarSolicitacao: vi.fn(),
}));

const authAluno: AuthContextValor = {
  sessao: {
    accessToken: "token",
    email: "ana@example.com",
    nome: "Ana Souza",
    papeis: ["ROLE_ALUNO"],
  },
  carregando: false,
  autenticar: vi.fn(),
  sair: vi.fn(),
  temPapel: (papel) => papel === "ROLE_ALUNO",
};

function renderizarFormulario() {
  return render(
    <AuthContext.Provider value={authAluno}>
      <MemoryRouter>
        <SolicitacaoForm orientadorId={7} />
      </MemoryRouter>
    </AuthContext.Provider>,
  );
}

function preencherCamposEditaveis() {
  fireEvent.change(screen.getByLabelText("Curso"), {
    target: { value: "ADS" },
  });
  fireEvent.change(screen.getByLabelText("Tema pretendido"), {
    target: { value: "Tema de teste" },
  });
  fireEvent.change(screen.getByLabelText("Mensagem para o orientador"), {
    target: { value: "Mensagem válida de teste" },
  });
}

describe("SolicitacaoForm", () => {
  beforeEach(() => vi.clearAllMocks());

  test("preenche nome e e-mail com os dados da sessão, sem permitir edição", () => {
    renderizarFormulario();

    const nome = screen.getByLabelText("Nome completo");
    const email = screen.getByLabelText("E-mail");

    expect(nome).toHaveValue("Ana Souza");
    expect(nome).toHaveAttribute("readonly");
    expect(email).toHaveValue("ana@example.com");
    expect(email).toHaveAttribute("readonly");
  });

  test("exibe erros para os campos obrigatórios sem chamar a API", () => {
    renderizarFormulario();

    fireEvent.click(screen.getByRole("button", { name: "Enviar solicitação" }));

    expect(
      screen.getByText("Informe um curso entre 2 e 100 caracteres."),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Informe um tema entre 5 e 150 caracteres."),
    ).toBeInTheDocument();
    expect(
      screen.getByText("A mensagem deve ter entre 10 e 1000 caracteres."),
    ).toBeInTheDocument();
    expect(criarSolicitacao).not.toHaveBeenCalled();
  });

  test("envia apenas o curso, sem nome e e-mail do aluno, e usa o id devolvido no 201", async () => {
    vi.mocked(criarSolicitacao).mockResolvedValue({
      id: 84,
      status: "PENDENTE",
      tema: "Tema de teste",
      mensagem: "Mensagem válida de teste",
      justificativa: null,
      criadoEm: "2026-09-28T12:00:00Z",
      aluno: {
        id: 2,
        nome: "Ana Souza",
        email: "ana@example.com",
        curso: "ADS",
      },
      orientador: {
        id: 7,
        nome: "Prof. Teste",
        email: "prof@example.com",
        vagasDisponiveis: 1,
      },
    });

    renderizarFormulario();
    preencherCamposEditaveis();
    fireEvent.click(screen.getByRole("button", { name: "Enviar solicitação" }));

    expect(await screen.findByText("#84")).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "Acompanhar solicitação" }),
    ).toHaveAttribute("href", "/acompanhar/84");
    expect(criarSolicitacao).toHaveBeenCalledWith({
      orientadorId: 7,
      curso: "ADS",
      tema: "Tema de teste",
      mensagem: "Mensagem válida de teste",
    });
  });

  test("exibe mensagem de solicitação duplicada quando a API retorna 409", async () => {
    vi.mocked(criarSolicitacao).mockRejectedValue(
      new ApiError(
        409,
        "Você possui uma solicitação pendente para este orientador.",
      ),
    );

    renderizarFormulario();
    preencherCamposEditaveis();
    fireEvent.click(screen.getByRole("button", { name: "Enviar solicitação" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Solicitação duplicada: Você possui uma solicitação pendente para este orientador.",
    );
  });
});
