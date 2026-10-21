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

  test("deve exibir os três tipos de prazos vigentes (Projeto de TCC, TCC 1, TCC 2) com contagem regressiva no painel do aluno", async () => {
    vi.setSystemTime(new Date("2026-10-10T10:00:00"));
    vi.mocked(listarPrazosVigentes).mockResolvedValue([
      {
        id: 1,
        semestre: "2026/2",
        tipo: "PROJETO_TCC",
        descricao: "Projeto de TCC",
        dataLimite: "2026-10-25",
      },
      {
        id: 2,
        semestre: "2026/2",
        tipo: "TCC1",
        descricao: "TCC 1",
        dataLimite: "2026-11-15",
      },
      {
        id: 3,
        semestre: "2026/2",
        tipo: "TCC2",
        descricao: "TCC 2",
        dataLimite: "2026-12-05",
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

    expect(screen.getByText("TCC 1")).toBeInTheDocument();
    expect(screen.getByText("15/11/2026")).toBeInTheDocument();
    expect(screen.getByText("Faltam 36 dias")).toBeInTheDocument();

    expect(screen.getByText("TCC 2")).toBeInTheDocument();
    expect(screen.getByText("05/12/2026")).toBeInTheDocument();
    expect(screen.getByText("Faltam 56 dias")).toBeInTheDocument();
  });
});
