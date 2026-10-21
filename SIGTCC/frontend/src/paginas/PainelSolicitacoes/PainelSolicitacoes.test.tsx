import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { vi, describe, it, expect, beforeEach } from "vitest";
import { PainelSolicitacoesPage } from "./index";
import {
  listarSolicitacoesPorOrientador,
  atualizarStatusSolicitacao,
} from "../../utils/solicitacaoService";
import { obterPerfilProfessor } from "../../utils/professorService";
import { AuthProvider } from "../../auth/AuthContext";
import { ApiError } from "../../utils/apiErro";
import type { Solicitacao } from "../../types/Solicitacao";
import type { PerfilProfessor } from "../../types/Professor";
import {
  listarPrazosVigentes,
  cadastrarPrazo,
  atualizarPrazo,
} from "../../utils/prazoEntregaService";

vi.mock("../../utils/solicitacaoService", () => ({
  listarSolicitacoesPorOrientador: vi.fn(),
  atualizarStatusSolicitacao: vi.fn(),
}));

vi.mock("../../utils/professorService", () => ({
  obterPerfilProfessor: vi.fn(),
}));

vi.mock("../../utils/prazoEntregaService", () => ({
  listarPrazosVigentes: vi.fn(),
  cadastrarPrazo: vi.fn(),
  atualizarPrazo: vi.fn(),
}));

const perfilBase: PerfilProfessor = {
  id: 1,
  nome: "Prof. Dr. Roberto",
  email: "roberto@ifsc.edu.br",
  departamento: "DAELN",
  responsavelTurma: false,
};

describe("PainelSolicitacoesPage (Issue #87)", () => {
  const mockSolicitacoes: Solicitacao[] = [
    {
      id: 101,
      status: "PENDENTE",
      tema: "Arquitetura Hexagonal com Spring Boot",
      mensagem:
        "Gostaria de desenvolver meu TCC focado em boas práticas de desacoplamento.",
      justificativa: null,
      criadoEm: "2026-09-28T08:00:00Z",
      aluno: {
        id: 1,
        nome: "Gabriel Silva",
        email: "gabriel.silva@aluno.ifsc.edu.br",
        curso: "Análise e Desenvolvimento de Sistemas",
      },
      orientador: {
        id: 1,
        nome: "Prof. Dr. Roberto",
        email: "roberto@ifsc.edu.br",
        vagasDisponiveis: 3,
      },
    },
    {
      id: 102,
      status: "PENDENTE",
      tema: "Microserviços e Resiliência",
      mensagem:
        "Tenho interesse no estudo de circuit breakers em sistemas distribuídos.",
      justificativa: null,
      criadoEm: "2026-09-27T14:30:00Z",
      aluno: {
        id: 2,
        nome: "Mariana Costa",
        email: "mariana.costa@aluno.ifsc.edu.br",
        curso: "Engenharia de Telecomunicações",
      },
      orientador: {
        id: 1,
        nome: "Prof. Dr. Roberto",
        email: "roberto@ifsc.edu.br",
        vagasDisponiveis: 3,
      },
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(obterPerfilProfessor).mockResolvedValue(perfilBase);
  });

  it("deve exibir estado de carregamento inicial e depois a lista de solicitações com indicador de vagas", async () => {
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue(
      mockSolicitacoes,
    );
    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    expect(screen.getByText("Carregando solicitações...")).toBeInTheDocument();

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando solicitações..."),
      ).not.toBeInTheDocument();
    });

    expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    expect(screen.getByText("Mariana Costa")).toBeInTheDocument();
    expect(
      screen.getByText("Arquitetura Hexagonal com Spring Boot"),
    ).toBeInTheDocument();
    expect(screen.getByText("Vagas disponíveis:")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
  });

  it("deve renderizar botões com rótulos estritos 'Aceitar' e 'Recusar' e não conter o rótulo 'Aprovar'", async () => {
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue(
      mockSolicitacoes,
    );
    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(
        screen.queryByText("Carregando solicitações..."),
      ).not.toBeInTheDocument();
    });

    const botoesAceitar = screen.getAllByRole("button", {
      name: "Aceitar",
    });
    const botoesRecusar = screen.getAllByRole("button", {
      name: "Recusar",
    });

    expect(botoesAceitar).toHaveLength(2);
    expect(botoesRecusar).toHaveLength(2);
    expect(
      screen.queryByRole("button", { name: /aprovar/i }),
    ).not.toBeInTheDocument();
  });

  it("deve aceitar uma solicitação, removê-la da tela e atualizar o indicador de vagas com o retorno da API", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue(
      mockSolicitacoes,
    );
    vi.mocked(atualizarStatusSolicitacao).mockResolvedValue({
      ...mockSolicitacoes[0],
      status: "ACEITA",
      orientador: {
        ...mockSolicitacoes[0].orientador,
        vagasDisponiveis: 2,
      },
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    const botoesAceitar = screen.getAllByRole("button", {
      name: "Aceitar",
    });

    await user.click(botoesAceitar[0]);

    await waitFor(() => {
      expect(atualizarStatusSolicitacao).toHaveBeenCalledWith(101, {
        status: "ACEITA",
      });
    });

    expect(screen.queryByText("Gabriel Silva")).not.toBeInTheDocument();
    expect(screen.getByText("Mariana Costa")).toBeInTheDocument();
    expect(screen.getByText("2")).toBeInTheDocument();
    expect(
      screen.getByText("Solicitação aceita com sucesso!"),
    ).toBeInTheDocument();
  });

  it("ao clicar em Recusar, deve abrir campo de justificativa obrigatório com mínimo de 10 caracteres", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    const botaoRecusar = screen.getByRole("button", {
      name: "Recusar",
    });
    await user.click(botaoRecusar);

    const textarea = screen.getByPlaceholderText(/informe o motivo da recusa/i);
    const botaoConfirmar = screen.getByRole("button", {
      name: "Confirmar Recusa",
    });

    // Inicialmente vazio.
    expect(botaoConfirmar).toBeDisabled();
    // Novo contador: quantidade atual / limite máximo.
    expect(screen.getByText("0 / 500 caracteres")).toBeInTheDocument();

    // Com 5 caracteres: botão continua desabilitado.
    await user.type(textarea, "Pouco");
    expect(botaoConfirmar).toBeDisabled();
    expect(screen.getByText("5 / 500 caracteres")).toBeInTheDocument();

    // Com 10 ou mais caracteres: botão habilitado.
    await user.type(textarea, " motivo detalhado");
    expect(botaoConfirmar).toBeEnabled();
    expect(screen.getByText("22 / 500 caracteres")).toBeInTheDocument();
  });

  it("deve limitar a justificativa a 500 caracteres", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    await user.click(
      screen.getByRole("button", {
        name: "Recusar",
      }),
    );

    const textarea = screen.getByPlaceholderText(/informe o motivo da recusa/i);

    // Verifica se a trava nativa do HTML está aplicada
    expect(textarea).toHaveAttribute("maxLength", "500");

    // Usa o fireEvent.change para injetar 500 caracteres instantaneamente
    // Assim não causa o erro de timeout (5000ms) que acontece ao simular 600 teclas uma a uma
    fireEvent.change(textarea, { target: { value: "a".repeat(500) } });

    expect(textarea).toHaveValue("a".repeat(500));
    expect(screen.getByText("500 / 500 caracteres")).toBeInTheDocument();
  });

  it("deve recusar uma solicitação com justificativa válida e removê-la da tela", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);
    vi.mocked(atualizarStatusSolicitacao).mockResolvedValue({
      ...mockSolicitacoes[0],
      status: "RECUSADA",
      justificativa: "Tema fora da minha linha de pesquisa atual.",
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    await user.click(
      screen.getByRole("button", {
        name: "Recusar",
      }),
    );

    const textarea = screen.getByPlaceholderText(/informe o motivo da recusa/i);
    await user.type(textarea, "Tema fora da minha linha de pesquisa atual.");

    await user.click(
      screen.getByRole("button", {
        name: "Confirmar Recusa",
      }),
    );

    await waitFor(() => {
      expect(atualizarStatusSolicitacao).toHaveBeenCalledWith(101, {
        status: "RECUSADA",
        justificativa: "Tema fora da minha linha de pesquisa atual.",
      });
    });

    expect(screen.queryByText("Gabriel Silva")).not.toBeInTheDocument();
    expect(
      screen.getByText("Solicitação recusada com sucesso."),
    ).toBeInTheDocument();
  });

  it("deve permitir cancelar a recusa sem alterar nada", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    await user.click(
      screen.getByRole("button", {
        name: "Recusar",
      }),
    );

    expect(
      screen.getByRole("button", {
        name: "Confirmar Recusa",
      }),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", {
        name: "Cancelar",
      }),
    );

    expect(
      screen.queryByRole("button", {
        name: "Confirmar Recusa",
      }),
    ).not.toBeInTheDocument();

    expect(
      screen.getByRole("button", {
        name: "Aceitar",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", {
        name: "Recusar",
      }),
    ).toBeInTheDocument();
  });

  it("deve exibir mensagem de erro 422 quando o orientador não possuir vagas disponíveis", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);
    vi.mocked(atualizarStatusSolicitacao).mockRejectedValue(
      new ApiError(422, "O orientador não possui vagas disponíveis."),
    );

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    await user.click(
      screen.getByRole("button", {
        name: "Aceitar",
      }),
    );

    await waitFor(() => {
      expect(
        screen.getByText("O orientador não possui vagas disponíveis."),
      ).toBeInTheDocument();
    });
  });

  it("deve exibir mensagem de erro 409 quando a solicitação foi respondida anteriormente", async () => {
    const user = userEvent.setup();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([
      mockSolicitacoes[0],
    ]);
    vi.mocked(atualizarStatusSolicitacao).mockRejectedValue(
      new ApiError(
        409,
        "Esta solicitação foi respondida e não pode ser alterada.",
      ),
    );

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Gabriel Silva")).toBeInTheDocument();
    });

    await user.click(
      screen.getByRole("button", {
        name: "Aceitar",
      }),
    );

    await waitFor(() => {
      expect(
        screen.getByText(
          "Esta solicitação foi respondida e não pode ser alterada.",
        ),
      ).toBeInTheDocument();
    });
  });

  it("deve exibir aviso amigável quando não houver solicitações pendentes", async () => {
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([]);

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(
        screen.getByText("Nenhuma solicitação pendente no momento."),
      ).toBeInTheDocument();
    });
  });

  it("deve exibir a aba 'Gestão da Turma/Calendário' quando o professor for responsável pela turma", async () => {
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([]);
    vi.mocked(obterPerfilProfessor).mockResolvedValue({
      ...perfilBase,
      responsavelTurma: true,
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    expect(
      await screen.findByRole("tab", { name: "Orientandos" }),
    ).toBeInTheDocument();
    expect(
      await screen.findByRole("tab", { name: "Gestão da Turma/Calendário" }),
    ).toBeInTheDocument();
  });

  it("não deve exibir a aba 'Gestão da Turma/Calendário' quando o professor não for responsável pela turma", async () => {
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([]);
    vi.mocked(obterPerfilProfessor).mockResolvedValue({
      ...perfilBase,
      responsavelTurma: false,
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    expect(
      await screen.findByRole("tab", { name: "Orientandos" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("tab", { name: "Gestão da Turma/Calendário" }),
    ).not.toBeInTheDocument();
  });
});
describe("Fluxo do Calendário de Entregas no Painel do Professor (US05 - Issue #141)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listarSolicitacoesPorOrientador).mockResolvedValue([]);
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
  });

  it("permite ao professor responsável pela turma cadastrar os três tipos de prazo e editar prazo existente", async () => {
    const user = userEvent.setup();
    vi.mocked(obterPerfilProfessor).mockResolvedValue({
      ...perfilBase,
      responsavelTurma: true,
    });

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
        dataLimite: "2026-12-15",
      });

    vi.mocked(atualizarPrazo).mockResolvedValue({
      id: 1,
      semestre: "2026/2",
      tipo: "PROJETO_TCC",
      descricao: "Projeto de TCC",
      dataLimite: "2026-11-05",
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    const abaCalendario = await screen.findByRole("tab", {
      name: "Gestão da Turma/Calendário",
    });
    await user.click(abaCalendario);

    const selectTipo = await screen.findByLabelText("Tipo de entrega *");
    const inputData = screen.getByLabelText("Data limite *");

    // 1. Cadastra PROJETO_TCC
    await user.selectOptions(selectTipo, "PROJETO_TCC");
    fireEvent.change(inputData, { target: { value: "2026-10-30" } });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("30/10/2026")).toBeInTheDocument();

    // 2. Cadastra TCC1
    await user.selectOptions(selectTipo, "TCC1");
    fireEvent.change(inputData, { target: { value: "2026-11-20" } });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("20/11/2026")).toBeInTheDocument();

    // 3. Cadastra TCC2
    await user.selectOptions(selectTipo, "TCC2");
    fireEvent.change(inputData, { target: { value: "2026-12-15" } });
    await user.click(screen.getByRole("button", { name: "Cadastrar prazo" }));
    expect(await screen.findByText("15/12/2026")).toBeInTheDocument();

    // 4. Edita PROJETO_TCC já cadastrado
    const botoesEditar = screen.getAllByRole("button", { name: "Editar" });
    await user.click(botoesEditar[0]);
    fireEvent.change(inputData, { target: { value: "2026-11-05" } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));

    expect(
      await screen.findByText("Prazo atualizado com sucesso."),
    ).toBeInTheDocument();
    expect(screen.getByText("05/11/2026")).toBeInTheDocument();
  });

  it("bloqueia o acesso ao formulário de cadastro de prazos para professor não responsável pela turma", async () => {
    vi.mocked(obterPerfilProfessor).mockResolvedValue({
      ...perfilBase,
      responsavelTurma: false,
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <PainelSolicitacoesPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    expect(
      await screen.findByRole("tab", { name: "Orientandos" }),
    ).toBeInTheDocument();

    expect(
      screen.queryByRole("tab", { name: "Gestão da Turma/Calendário" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Cadastrar prazo" }),
    ).not.toBeInTheDocument();
  });
});
