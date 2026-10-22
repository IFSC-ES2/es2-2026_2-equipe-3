import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, test, expect, beforeEach } from "vitest";
import { AcessoNegadoPage } from "./index";
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

describe("AcessoNegadoPage", () => {
  beforeEach(() => {
    sessionStorage.clear();
    armazenarSessaoFake(["ROLE_ALUNO"], "aluno@example.com");
  });

  test("exibe a mensagem de acesso negado", () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <AcessoNegadoPage />
        </AuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText("Acesso negado")).toBeInTheDocument();
    expect(
      screen.getByText("Você não tem permissão para acessar esta página."),
    ).toBeInTheDocument();
  });

  test("leva o aluno ao catálogo ao clicar em 'Voltar ao início'", async () => {
    render(
      <MemoryRouter initialEntries={["/painel-orientador"]}>
        <AuthProvider>
          <Routes>
            <Route path="/painel-orientador" element={<AcessoNegadoPage />} />
            <Route
              path="/orientadores"
              element={<div>Catálogo do aluno</div>}
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await userEvent.click(
      screen.getByRole("button", { name: "Voltar ao início" }),
    );

    expect(await screen.findByText("Catálogo do aluno")).toBeInTheDocument();
  });
});
