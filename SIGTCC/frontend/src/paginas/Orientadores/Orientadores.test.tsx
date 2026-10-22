import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { OrientadorListagemPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";
import { STORAGE_KEY } from "../../auth/authService";
import { listarPrazosVigentes } from "../../utils/prazoEntregaService";

vi.mock("../../utils/orientadorService", () => ({
  listarOrientadores: vi.fn().mockResolvedValue([]),
}));

vi.mock("../../utils/prazoEntregaService", () => ({
  listarPrazosVigentes: vi.fn().mockResolvedValue([]),
}));

function criarTokenFake(roles: string[], email: string): string {
  const header = btoa(JSON.stringify({ alg: "none", typ: "JWT" }));
  const payload = btoa(
    JSON.stringify({
      email,
      realm_access: { roles },
      exp: Math.floor(Date.now() / 1000) + 300,
    }),
  );
  return `${header}.${payload}.assinatura-fake`;
}

function armazenarSessaoFake(roles: string[], email: string) {
  sessionStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      accessToken: criarTokenFake(roles, email),
      refreshToken: "refresh-fake",
      expiresAt: Date.now() + 5 * 60 * 1000,
    }),
  );
}

function renderPagina() {
  return render(
    <AuthProvider>
      <MemoryRouter>
        <OrientadorListagemPage />
      </MemoryRouter>
    </AuthProvider>,
  );
}

describe("OrientadorListagemPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
    vi.mocked(listarPrazosVigentes).mockResolvedValue([]);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test("deve renderizar a página do catálogo corretamente", () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <OrientadorListagemPage />
        </MemoryRouter>
      </AuthProvider>,
    );
    expect(screen.getByText("Catálogo de Orientadores")).toBeInTheDocument();
  });

  test("deve exibir os prazos vigentes com contagem regressiva no painel do aluno", async () => {
    vi.setSystemTime(new Date("2026-10-10T10:00:00"));
    vi.mocked(listarPrazosVigentes).mockResolvedValue([
      {
        id: 1,
        semestre: "2026/2",
        tipo: "PROJETO_TCC",
        descricao: "Projeto de TCC",
        dataLimite: "2026-10-25",
      },
    ]);

    render(
      <AuthProvider>
        <MemoryRouter>
          <OrientadorListagemPage />
        </MemoryRouter>
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("25/10/2026")).toBeInTheDocument();
    });
    expect(screen.getByText("Projeto de TCC")).toBeInTheDocument();
    expect(screen.getByText("Faltam 15 dias")).toBeInTheDocument();
  });

  test("oculta os botões exclusivos de professor quando o usuário é aluno", () => {
    armazenarSessaoFake(["ROLE_ALUNO"], "aluno@example.com");

    renderPagina();

    expect(
      screen.getByRole("button", { name: "Catálogo" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sair" })).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Novo cadastro" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Meu perfil" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Painel do Professor" }),
    ).not.toBeInTheDocument();
  });

  test("exibe os botões exclusivos de professor quando o usuário é professor", () => {
    armazenarSessaoFake(["ROLE_PROFESSOR"], "professor.orientador@example.com");

    renderPagina();

    expect(
      screen.getByRole("button", { name: "Novo cadastro" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Meu perfil" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Painel do Professor" }),
    ).toBeInTheDocument();
  });
});
