import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, test, expect, beforeEach } from "vitest";
import { ProtectedRoute } from "./ProtectedRoute";
import { AuthProvider } from "./AuthContext";
import { STORAGE_KEY } from "./authService";

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

function renderComRota(
  caminhoInicial: string,
  papelExigido?: "ROLE_ALUNO" | "ROLE_PROFESSOR",
) {
  return render(
    <MemoryRouter initialEntries={[caminhoInicial]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Tela de login</div>} />
          <Route path="/" element={<div>Página inicial</div>} />
          <Route
            path="/area-protegida"
            element={
              <ProtectedRoute papelExigido={papelExigido}>
                <div>Conteúdo protegido</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe("ProtectedRoute", () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  test("bloqueia o acesso e redireciona para /login quando não há usuário autenticado", async () => {
    renderComRota("/area-protegida");

    expect(await screen.findByText("Tela de login")).toBeInTheDocument();
    expect(screen.queryByText("Conteúdo protegido")).not.toBeInTheDocument();
  });

  test("permite o acesso quando o usuário está autenticado e nenhum papel específico é exigido", async () => {
    armazenarSessaoFake(["ROLE_ALUNO"], "aluno@example.com");

    renderComRota("/area-protegida");

    expect(await screen.findByText("Conteúdo protegido")).toBeInTheDocument();
  });

  test("redireciona para a página inicial quando o usuário autenticado não possui o papel exigido", async () => {
    armazenarSessaoFake(["ROLE_ALUNO"], "aluno@example.com");

    renderComRota("/area-protegida", "ROLE_PROFESSOR");

    expect(await screen.findByText("Página inicial")).toBeInTheDocument();
    expect(screen.queryByText("Conteúdo protegido")).not.toBeInTheDocument();
  });

  test("permite o acesso quando o usuário autenticado possui o papel exigido", async () => {
    armazenarSessaoFake(["ROLE_PROFESSOR"], "professor.responsavel@example.com");

    renderComRota("/area-protegida", "ROLE_PROFESSOR");

    expect(await screen.findByText("Conteúdo protegido")).toBeInTheDocument();
  });
});