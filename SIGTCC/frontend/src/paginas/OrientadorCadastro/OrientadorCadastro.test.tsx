import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect } from "vitest";
import { OrientadorCadastroPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";

describe("OrientadorCadastroPage", () => {
  test("deve renderizar a página de cadastro corretamente", () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <OrientadorCadastroPage />
        </MemoryRouter>
      </AuthProvider>,
    );
    expect(screen.getByText("Cadastrar Novo Orientador")).toBeInTheDocument();
  });
});
