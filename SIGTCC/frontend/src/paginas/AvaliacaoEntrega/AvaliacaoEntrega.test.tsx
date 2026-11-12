import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../../auth/AuthContext";
import {
  listarEntregasDoOrientador,
  registrarParecer,
} from "../../utils/entregaService";
import type { Entrega } from "../../types/Entrega";
import { AvaliacaoEntregaPage } from "./index";

vi.mock("../../utils/entregaService", () => ({
  criarEntrega: vi.fn(),
  listarEntregasDoAluno: vi.fn(),
  listarEntregasDoOrientador: vi.fn(),
  registrarParecer: vi.fn(),
}));

const entrega: Entrega = {
  id: 8,
  semestre: "2026/2",
  tipo: "TCC1",
  descricaoTipo: "TCC 1",
  urlDocumento: "https://documento.exemplo/tcc",
  status: "SUBMETIDO",
  atrasada: false,
  enviadoEm: "2026-11-03T12:00:00Z",
  aluno: {
    id: 4,
    nome: "Ana Souza",
    email: "ana@example.edu",
    curso: "ADS",
  },
  orientador: {
    id: 3,
    nome: "Professor",
    email: "professor@example.edu",
  },
  parecer: null,
};

describe("AvaliacaoEntregaPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listarEntregasDoOrientador).mockResolvedValue([entrega]);
  });

  function renderizarPagina() {
    return render(
      <AuthProvider>
        <MemoryRouter>
          <AvaliacaoEntregaPage />
        </MemoryRouter>
      </AuthProvider>,
    );
  }

  it("filtra as entregas por status", async () => {
    renderizarPagina();

    fireEvent.change(await screen.findByLabelText("Filtrar por status"), {
      target: { value: "SUBMETIDO" },
    });

    await waitFor(() =>
      expect(listarEntregasDoOrientador).toHaveBeenLastCalledWith("SUBMETIDO"),
    );
  });

  it("disponibiliza o documento para abertura em uma nova aba", async () => {
    renderizarPagina();

    const link = await screen.findByRole("link", {
      name: "Abrir documento",
    });

    expect(link).toHaveAttribute("href", entrega.urlDocumento);
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("exige comentário ao registrar ajustes necessários", async () => {
    renderizarPagina();

    fireEvent.change(await screen.findByLabelText("Parecer"), {
      target: { value: "AJUSTES_NECESSARIOS" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Registrar parecer" }));

    expect(
      await screen.findByText("O comentário é obrigatório para este parecer."),
    ).toBeInTheDocument();

    expect(registrarParecer).not.toHaveBeenCalled();
  });

  it("exige comentário ao registrar reprovação", async () => {
    renderizarPagina();

    fireEvent.change(await screen.findByLabelText("Parecer"), {
      target: { value: "REPROVADO" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Registrar parecer" }));

    expect(
      await screen.findByText("O comentário é obrigatório para este parecer."),
    ).toBeInTheDocument();

    expect(registrarParecer).not.toHaveBeenCalled();
  });

  it("envia comentário nulo ao aprovar sem comentário", async () => {
    vi.mocked(registrarParecer).mockResolvedValue({
      ...entrega,
      status: "APROVADO",
      parecer: {
        id: 12,
        resultado: "APROVADO",
        comentario: null,
        registradoEm: "2026-11-03T12:00:00Z",
      },
    });

    renderizarPagina();

    await screen.findByLabelText("Parecer");

    fireEvent.click(screen.getByRole("button", { name: "Registrar parecer" }));

    expect(registrarParecer).toHaveBeenCalledWith(8, {
      resultado: "APROVADO",
      comentario: null,
    });

    expect(
      await screen.findByText(
        "Parecer definitivo. Os campos estão somente para leitura.",
      ),
    ).toBeInTheDocument();

    expect(screen.getByLabelText("Parecer")).toBeDisabled();
  });

  it("mantém os campos em leitura enquanto aguarda reenvio", async () => {
    vi.mocked(listarEntregasDoOrientador).mockResolvedValue([
      { ...entrega, status: "AJUSTES_NECESSARIOS" },
    ]);

    renderizarPagina();

    expect(await screen.findByLabelText("Parecer")).toBeDisabled();
    expect(screen.getByLabelText("Comentário")).toBeDisabled();

    expect(
      screen.getByText(
        "Aguardando o reenvio do aluno; não é possível registrar outro parecer agora.",
      ),
    ).toBeInTheDocument();
  });

  it("bloqueia nova avaliação de entrega reprovada", async () => {
    vi.mocked(listarEntregasDoOrientador).mockResolvedValue([
      {
        ...entrega,
        status: "REPROVADO",
        parecer: {
          id: 13,
          resultado: "REPROVADO",
          comentario: "O trabalho não atende aos requisitos mínimos.",
          registradoEm: "2026-11-03T12:00:00Z",
        },
      },
    ]);

    renderizarPagina();

    expect(await screen.findByLabelText("Parecer")).toBeDisabled();
    expect(screen.getByLabelText("Comentário")).toBeDisabled();

    expect(
      screen.getByText(
        "Parecer definitivo. Os campos estão somente para leitura.",
      ),
    ).toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: "Registrar parecer" }),
    ).not.toBeInTheDocument();
  });

  it("mostra erro de carregamento em vez de uma lista vazia", async () => {
    vi.mocked(listarEntregasDoOrientador).mockRejectedValue(
      new Error("Erro ao processar a requisição"),
    );

    renderizarPagina();

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Não foi possível carregar as entregas",
    );

    expect(
      screen.queryByText("Não há entregas para avaliar."),
    ).not.toBeInTheDocument();
  });
});
