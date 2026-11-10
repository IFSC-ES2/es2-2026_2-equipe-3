import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../../auth/AuthContext";
import { ApiError } from "../../utils/apiErro";
import {
  criarEntrega,
  listarEntregasDoAluno,
} from "../../utils/entregaService";
import type { Entrega } from "../../types/Entrega";
import { SubmissaoEntregaPage } from "./index";

vi.mock("../../utils/entregaService", () => ({
  criarEntrega: vi.fn(),
  listarEntregasDoAluno: vi.fn(),
  listarEntregasDoOrientador: vi.fn(),
  registrarParecer: vi.fn(),
}));

const entrega: Entrega = {
  id: 21,
  semestre: "2026/2",
  tipo: "PROJETO_TCC",
  descricaoTipo: "Projeto de TCC",
  urlDocumento: "https://documento.exemplo/tcc",
  status: "SUBMETIDO",
  atrasada: false,
  enviadoEm: "2026-11-03T12:00:00Z",
  aluno: { id: 1, nome: "Ana", email: "ana@example.edu", curso: "ADS" },
  orientador: { id: 2, nome: "Professor", email: "prof@example.edu" },
  parecer: null,
};

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

  it("bloqueia link que não usa HTTPS sem chamar a API", async () => {
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

  it("envia uma submissão e mostra o status retornado", async () => {
    vi.mocked(criarEntrega).mockResolvedValue(entrega);
    renderizarPagina();
    fireEvent.change(await screen.findByLabelText(/^Link do documento/), {
      target: { value: entrega.urlDocumento },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("Entrega submetida com sucesso."),
    ).toBeInTheDocument();
    await waitFor(() =>
      expect(criarEntrega).toHaveBeenCalledWith({
        tipo: "PROJETO_TCC",
        urlDocumento: entrega.urlDocumento,
      }),
    );
    expect(screen.getAllByText("Submetido")).toHaveLength(2);
  });

  it("exibe a mensagem de validação da API para status 400", async () => {
    vi.mocked(criarEntrega).mockRejectedValue(
      new ApiError(400, "A URL do documento não é válida."),
    );
    renderizarPagina();
    fireEvent.change(await screen.findByLabelText(/^Link do documento/), {
      target: { value: entrega.urlDocumento },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("A URL do documento não é válida."),
    ).toBeInTheDocument();
  });
});
