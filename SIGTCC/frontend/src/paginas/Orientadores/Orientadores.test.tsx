import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi } from "vitest";
import { OrientadorListagemPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";

vi.mock("../../utils/orientadorService", () => ({
  listarOrientadores: vi.fn().mockResolvedValue([]),
}));

describe("OrientadorListagemPage", () => {
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
});
