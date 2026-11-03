import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../../auth/AuthContext";
import {
  criarEntrega,
  listarEntregasDoAluno,
} from "../../utils/entregaService";
import { SubmissaoEntregaPage } from "./index";

vi.mock("../../utils/entregaService", () => ({
  criarEntrega: vi.fn(),
  listarEntregasDoAluno: vi.fn(),
  listarEntregasDoOrientador: vi.fn(),
  registrarParecer: vi.fn(),
}));

describe("SubmissaoEntregaPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listarEntregasDoAluno).mockResolvedValue([]);
  });

  function renderizarPagina() {
    return render(
      <AuthProvider>
        <MemoryRouter>
          <SubmissaoEntregaPage />
        </MemoryRouter>
      </AuthProvider>,
    );
  }

  it("bloqueia um link que não usa HTTPS sem chamar a API", async () => {
    renderizarPagina();
    fireEvent.change(await screen.findByLabelText(/^Link do documento/), {
      target: { value: "http://documento.exemplo" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("Informe um link válido iniciado por https://."),
    ).toBeInTheDocument();
    expect(criarEntrega).not.toHaveBeenCalled();
  });

  it("envia o link e mostra confirmação", async () => {
    vi.mocked(criarEntrega).mockResolvedValue({
      id: 21,
      tipo: "PROJETO_TCC",
      status: "SUBMETIDO",
      urlDocumento: "https://documento.exemplo/tcc",
      dataEnvio: "2026-11-03T12:00:00Z",
      atrasada: false,
    });
    renderizarPagina();
    fireEvent.change(await screen.findByLabelText(/^Link do documento/), {
      target: { value: "https://documento.exemplo/tcc" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("Entrega submetida com sucesso."),
    ).toBeInTheDocument();
    await waitFor(() =>
      expect(criarEntrega).toHaveBeenCalledWith({
        tipo: "PROJETO_TCC",
        urlDocumento: "https://documento.exemplo/tcc",
      }),
    );
    expect(screen.getAllByText("Submetido")).toHaveLength(2);
  });
});
