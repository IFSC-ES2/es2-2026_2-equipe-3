import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { LoginPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";
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
});
