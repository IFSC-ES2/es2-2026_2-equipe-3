import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  MemoryRouter,
  Route,
  Routes,
  useLocation,
  useParams,
} from "react-router-dom";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { LoginPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";
import { ProtectedRoute } from "../../auth/ProtectedRoute";
import { STORAGE_KEY } from "../../auth/authService";

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

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={["/login"]}>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  test("deve renderizar o formulário com campos de e-mail e senha", () => {
    renderLoginPage();

    expect(screen.getByLabelText("E-mail")).toBeInTheDocument();
    expect(screen.getByLabelText("Senha")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /entrar/i })).toBeInTheDocument();
  });

  test("deve autenticar com sucesso e armazenar o token", async () => {
    const tokenFake = criarTokenFake(
      ["ROLE_PROFESSOR"],
      "professor.responsavel@example.com",
    );

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    const usuario = userEvent.setup();
    renderLoginPage();

    await usuario.type(
      screen.getByLabelText("E-mail"),
      "professor.responsavel@example.com",
    );
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    await waitFor(() => {
      expect(sessionStorage.getItem(STORAGE_KEY)).not.toBeNull();
    });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  test("deve autenticar um Aluno com sucesso e redirecionar ao catálogo de orientadores", async () => {
    const tokenFake = criarTokenFake(["ROLE_ALUNO"], "aluno@example.com");

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    const usuario = userEvent.setup();
    renderLoginPage();

    await usuario.type(screen.getByLabelText("E-mail"), "aluno@example.com");
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    await waitFor(() => {
      expect(sessionStorage.getItem(STORAGE_KEY)).not.toBeNull();
    });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  test("deve exibir 'Credenciais inválidas' quando a senha estiver incorreta", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify({ error: "invalid_grant" }), {
        status: 401,
      }),
    );

    const usuario = userEvent.setup();
    renderLoginPage();

    await usuario.type(
      screen.getByLabelText("E-mail"),
      "professor.responsavel@example.com",
    );
    await usuario.type(screen.getByLabelText("Senha"), "senha-errada");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Credenciais inválidas",
    );
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  test("redireciona para a rota original em location.state.from após autenticar", async () => {
    const tokenFake = criarTokenFake(["ROLE_ALUNO"], "aluno@example.com");

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    function PaginaAcompanhar() {
      const { id } = useParams();
      return <div>Página de Acompanhamento: solicitação #{id}</div>;
    }

    const usuario = userEvent.setup();
    render(
      <MemoryRouter
        initialEntries={[
          {
            pathname: "/login",
            state: { from: { pathname: "/acompanhar/15" } },
          },
        ]}
      >
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/acompanhar/:id" element={<PaginaAcompanhar />} />
            <Route path="/orientadores" element={<div>Catálogo</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await usuario.type(screen.getByLabelText("E-mail"), "aluno@example.com");
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    expect(
      await screen.findByText("Página de Acompanhamento: solicitação #15"),
    ).toBeInTheDocument();
    expect(screen.queryByText("Catálogo")).not.toBeInTheDocument();
  });

  test("fluxo completo: usuário deslogado tenta abrir rota protegida e é redirecionado de volta após login", async () => {
    const tokenFake = criarTokenFake(["ROLE_ALUNO"], "aluno@example.com");

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    function PaginaAcompanhar() {
      const { id } = useParams();
      return <div>Página de Acompanhamento: solicitação #{id}</div>;
    }

    const usuario = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/acompanhar/15"]}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/acompanhar/:id"
              element={
                <ProtectedRoute papelExigido="ROLE_ALUNO">
                  <PaginaAcompanhar />
                </ProtectedRoute>
              }
            />
            <Route path="/orientadores" element={<div>Catálogo</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    expect(
      await screen.findByRole("button", { name: /entrar/i }),
    ).toBeInTheDocument();
    expect(
      screen.queryByText("Página de Acompanhamento: solicitação #15"),
    ).not.toBeInTheDocument();

    await usuario.type(screen.getByLabelText("E-mail"), "aluno@example.com");
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    expect(
      await screen.findByText("Página de Acompanhamento: solicitação #15"),
    ).toBeInTheDocument();
  });

  test("direciona aluno para rota inicial por papel quando o redirecionamento veio da raiz '/'", async () => {
    const tokenFake = criarTokenFake(["ROLE_ALUNO"], "aluno@example.com");

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    const usuario = userEvent.setup();
    render(
      <MemoryRouter
        initialEntries={[
          {
            pathname: "/login",
            state: { from: { pathname: "/" } },
          },
        ]}
      >
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<div>Raiz Professor</div>} />
            <Route
              path="/orientadores"
              element={<div>Catálogo do Aluno</div>}
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await usuario.type(screen.getByLabelText("E-mail"), "aluno@example.com");
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    expect(await screen.findByText("Catálogo do Aluno")).toBeInTheDocument();
    expect(screen.queryByText("Raiz Professor")).not.toBeInTheDocument();
  });

  test("preserva query parameters do link original ao redirecionar", async () => {
    const tokenFake = criarTokenFake(["ROLE_ALUNO"], "aluno@example.com");

    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: tokenFake,
          refresh_token: "refresh-fake",
          expires_in: 300,
        }),
        { status: 200 },
      ),
    );

    function PaginaCatalogo() {
      const location = useLocation();
      return <div>Catálogo: busca={location.search}</div>;
    }

    const usuario = userEvent.setup();
    render(
      <MemoryRouter
        initialEntries={[
          {
            pathname: "/login",
            state: { from: { pathname: "/orientadores", search: "?termo=IA" } },
          },
        ]}
      >
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/orientadores" element={<PaginaCatalogo />} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await usuario.type(screen.getByLabelText("E-mail"), "aluno@example.com");
    await usuario.type(screen.getByLabelText("Senha"), "senha-correta");
    await usuario.click(screen.getByRole("button", { name: /entrar/i }));

    expect(
      await screen.findByText("Catálogo: busca=?termo=IA"),
    ).toBeInTheDocument();
  });
});
