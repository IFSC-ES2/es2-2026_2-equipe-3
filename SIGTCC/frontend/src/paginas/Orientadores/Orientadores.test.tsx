import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi, beforeEach, afterEach } from "vitest";
import { OrientadorListagemPage } from "./index";
import { AuthProvider } from "../../auth/AuthContext";
import { listarPrazosVigentes } from "../../utils/prazoEntregaService";

vi.mock("../../utils/orientadorService", () => ({
  listarOrientadores: vi.fn().mockResolvedValue([]),
}));

vi.mock("../../utils/prazoEntregaService", () => ({
  listarPrazosVigentes: vi.fn().mockResolvedValue([]),
}));

describe("OrientadorListagemPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
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
});
