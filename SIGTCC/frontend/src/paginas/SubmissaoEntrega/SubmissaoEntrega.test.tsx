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
    vi.resetAllMocks();
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

  async function preencherLink(url: string) {
    fireEvent.change(await screen.findByLabelText(/^Link do documento/), {
      target: { value: url },
    });
  }

  it("bloqueia link que não usa HTTPS sem chamar a API", async () => {
    renderizarPagina();
    await preencherLink("http://documento.exemplo");

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("Informe um link válido iniciado por https://."),
    ).toBeInTheDocument();
    expect(criarEntrega).not.toHaveBeenCalled();
  });

  it("bloqueia link vazio sem chamar a API", async () => {
    renderizarPagina();

    const botao = await screen.findByRole("button", {
      name: "Enviar entrega",
    });

    fireEvent.click(botao);

    expect(
      await screen.findByText("Informe um link válido iniciado por https://."),
    ).toBeInTheDocument();

    expect(criarEntrega).not.toHaveBeenCalled();
  });

  it("bloqueia URL HTTPS malformada sem chamar a API", async () => {
    renderizarPagina();
    await preencherLink("https://");

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("Informe um link válido iniciado por https://."),
    ).toBeInTheDocument();
    expect(criarEntrega).not.toHaveBeenCalled();
  });

  it("envia uma submissão e mostra o status retornado", async () => {
    vi.mocked(criarEntrega).mockResolvedValue(entrega);
    renderizarPagina();
    await preencherLink(entrega.urlDocumento);

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

  it("exibe o indicador de entrega com atraso", async () => {
    vi.mocked(listarEntregasDoAluno).mockResolvedValue([
      { ...entrega, atrasada: true },
    ]);

    renderizarPagina();

    expect(
      await screen.findByText("Entregue com atraso"),
    ).toBeInTheDocument();
  });

  it.each(["SUBMETIDO", "APROVADO", "REPROVADO"] as const)(
    "bloqueia novo envio quando o status é %s",
    async (status) => {
      vi.mocked(listarEntregasDoAluno).mockResolvedValue([
        { ...entrega, status },
      ]);

      renderizarPagina();

      const botao = await screen.findByRole("button", {
        name: "Enviar entrega",
      });

      expect(botao).toBeDisabled();
      expect(criarEntrega).not.toHaveBeenCalled();
    },
  );

  it("permite reenvio quando o status é AJUSTES_NECESSARIOS", async () => {
    const novaUrl = "https://documento.exemplo/tcc-revisado";

    vi.mocked(listarEntregasDoAluno).mockResolvedValue([
      { ...entrega, status: "AJUSTES_NECESSARIOS" },
    ]);

    vi.mocked(criarEntrega).mockResolvedValue({
      ...entrega,
      urlDocumento: novaUrl,
      status: "SUBMETIDO",
      atrasada: false,
    });

    renderizarPagina();
    await preencherLink(novaUrl);

    const botao = await screen.findByRole("button", {
      name: "Reenviar entrega",
    });

    expect(botao).toBeEnabled();

    fireEvent.click(botao);

    expect(
      await screen.findByText("Entrega submetida com sucesso."),
    ).toBeInTheDocument();

    await waitFor(() =>
      expect(criarEntrega).toHaveBeenCalledWith({
        tipo: "PROJETO_TCC",
        urlDocumento: novaUrl,
      }),
    );
  });

  it("exibe mensagem apropriada quando o backend retorna 403", async () => {
    vi.mocked(criarEntrega).mockRejectedValue(
      new ApiError(403, "Acesso negado."),
    );

    renderizarPagina();
    await preencherLink(entrega.urlDocumento);

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText(
        "É necessário ter uma orientação aceita para enviar a entrega.",
      ),
    ).toBeInTheDocument();
  });

  it("exibe mensagem apropriada quando o backend retorna 409", async () => {
    vi.mocked(criarEntrega).mockRejectedValue(
      new ApiError(409, "Entrega já submetida."),
    );

    renderizarPagina();
    await preencherLink(entrega.urlDocumento);

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText(
        "Esta etapa já foi submetida e não está liberada para reenvio.",
      ),
    ).toBeInTheDocument();
  });

  it("exibe mensagem do backend quando retorna 422", async () => {
    const mensagem = "O prazo desta etapa ainda não foi cadastrado.";

    vi.mocked(criarEntrega).mockRejectedValue(
      new ApiError(422, mensagem),
    );

    renderizarPagina();
    await preencherLink(entrega.urlDocumento);

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(await screen.findByText(mensagem)).toBeInTheDocument();
  });

  it("exibe a mensagem de validação da API para status 400", async () => {
    vi.mocked(criarEntrega).mockRejectedValue(
      new ApiError(400, "A URL do documento não é válida."),
    );

    renderizarPagina();
    await preencherLink(entrega.urlDocumento);

    fireEvent.click(screen.getByRole("button", { name: "Enviar entrega" }));

    expect(
      await screen.findByText("A URL do documento não é válida."),
    ).toBeInTheDocument();
  });
});
