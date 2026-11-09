import { useEffect, useState } from "react";
import { PageShell } from "../OrientadorCadastro";
import {
  listarEntregasDoOrientador,
  registrarParecer,
} from "../../utils/entregaService";
import type { Entrega, StatusEntrega } from "../../types/Entrega";
import { ApiError } from "../../utils/apiErro";
import "./AvaliacaoEntrega.css";

type StatusParecer = Exclude<StatusEntrega, "SUBMETIDO">;

const opcoesParecer: { valor: StatusParecer; rotulo: string }[] = [
  { valor: "APROVADO", rotulo: "Aprovado" },
  { valor: "AJUSTES_NECESSARIOS", rotulo: "Ajustes necessários" },
  { valor: "REPROVADO", rotulo: "Reprovado" },
];

export function AvaliacaoEntregaPage() {
  const [entregas, setEntregas] = useState<Entrega[]>([]);
  const [entregaSelecionada, setEntregaSelecionada] = useState<number | null>(
    null,
  );
  const [status, setStatus] = useState<StatusParecer>("APROVADO");
  const [comentario, setComentario] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erroCarregamento, setErroCarregamento] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [mensagem, setMensagem] = useState("");
  const [sucesso, setSucesso] = useState(false);
  const [erroComentario, setErroComentario] = useState("");

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try {
        const dados = await listarEntregasDoOrientador();
        if (ativo) {
          setEntregas(dados);
          setEntregaSelecionada(dados[0]?.id ?? null);
        }
      } catch (erro) {
        if (ativo)
          setErroCarregamento(
            erro instanceof Error ? erro.message : "Erro ao carregar entregas.",
          );
      } finally {
        if (ativo) setCarregando(false);
      }
    }
    void carregar();
    return () => {
      ativo = false;
    };
  }, []);

  const entrega = entregas.find((item) => item.id === entregaSelecionada);
  const aguardandoReenvio = entrega?.status === "AJUSTES_NECESSARIOS";
  const somenteLeitura =
    aguardandoReenvio ||
    entrega?.status === "APROVADO" ||
    entrega?.status === "REPROVADO";
  const exigeComentario =
    status === "AJUSTES_NECESSARIOS" || status === "REPROVADO";

  async function handleSalvar() {
    const texto = comentario.trim();
    if (exigeComentario && !texto) {
      setErroComentario("O comentário é obrigatório para este parecer.");
      return;
    }
    if (!entrega) return;

    setErroComentario("");
    setMensagem("");
    setSalvando(true);
    try {
      const atualizada = await registrarParecer(entrega.id, {
        resultado: status,
        comentario: texto,
      });
      setEntregas((anteriores) =>
        anteriores.map((item) =>
          item.id === atualizada.id ? atualizada : item,
        ),
      );
      if (atualizada.status !== "SUBMETIDO") setStatus(atualizada.status);
      setComentario(atualizada.parecer?.comentario ?? texto);
      setMensagem("Parecer registrado.");
      setSucesso(true);
    } catch (erro) {
      setSucesso(false);
      if (erro instanceof ApiError && erro.status === 409) {
        setMensagem("Esta entrega já recebeu um parecer definitivo.");
      } else if (erro instanceof ApiError && erro.status === 403) {
        setMensagem("Você não tem autorização para avaliar esta entrega.");
      } else {
        setMensagem(
          erro instanceof Error
            ? erro.message
            : "Não foi possível registrar o parecer.",
        );
      }
    } finally {
      setSalvando(false);
    }
  }

  return (
    <PageShell active="avaliacao" mensagem={mensagem} sucesso={sucesso}>
      <section className="avaliacao-page">
        <header className="avaliacao-heading">
          <p className="section-kicker">Área do orientador</p>
          <h2>Avaliação de entregas</h2>
        </header>
        {carregando ? (
          <p className="feedback loading" role="status">
            Carregando entregas...
          </p>
        ) : erroCarregamento ? (
          <p className="feedback erro" role="alert">
            Não foi possível carregar as entregas: {erroCarregamento}
          </p>
        ) : entregas.length === 0 ? (
          <p className="empty-state">Não há entregas para avaliar.</p>
        ) : (
          <div className="avaliacao-layout">
            <nav className="avaliacao-lista" aria-label="Entregas recebidas">
              {entregas.map((item) => (
                <button
                  className={
                    item.id === entregaSelecionada
                      ? "entrega-opcao selecionada"
                      : "entrega-opcao"
                  }
                  key={item.id}
                  type="button"
                  onClick={() => {
                    setEntregaSelecionada(item.id);
                    setStatus(
                      item.status === "SUBMETIDO" ? "APROVADO" : item.status,
                    );
                    setComentario(item.parecer?.comentario ?? "");
                    setMensagem("");
                  }}
                >
                  <strong>
                    {item.aluno?.nome ?? `Aluno #${item.aluno?.id ?? item.id}`}
                  </strong>
                  <span>
                    {item.tipo.replace("_", " ")} ·{" "}
                    {item.status.replaceAll("_", " ")}
                  </span>
                </button>
              ))}
            </nav>
            {entrega && (
              <article className="avaliacao-detalhe">
                <div className="avaliacao-aluno">
                  <div>
                    <p className="card-eyebrow">
                      {entrega.tipo.replaceAll("_", " ")}
                    </p>
                    <h3>{entrega.aluno?.nome ?? "Entrega recebida"}</h3>
                    {entrega.aluno?.email && <p>{entrega.aluno.email}</p>}
                  </div>
                  {entrega.atrasada && (
                    <span className="atraso-badge">Entregue com atraso</span>
                  )}
                </div>
                <a
                  className="primary-button documento-link"
                  href={entrega.urlDocumento}
                  target="_blank"
                  rel="noreferrer"
                >
                  Abrir documento
                </a>
                <div className="avaliacao-form">
                  <label className="form-group" htmlFor="status-parecer">
                    Parecer
                    <select
                      id="status-parecer"
                      value={status}
                      onChange={(event) =>
                        setStatus(event.target.value as StatusParecer)
                      }
                      disabled={somenteLeitura}
                    >
                      {opcoesParecer.map((opcao) => (
                        <option key={opcao.valor} value={opcao.valor}>
                          {opcao.rotulo}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="form-group" htmlFor="comentario-parecer">
                    Comentário{" "}
                    {exigeComentario && !somenteLeitura && (
                      <span aria-hidden="true">*</span>
                    )}
                    <textarea
                      id="comentario-parecer"
                      value={comentario}
                      onChange={(event) => setComentario(event.target.value)}
                      disabled={somenteLeitura}
                      aria-invalid={Boolean(erroComentario)}
                      aria-describedby={
                        erroComentario ? "comentario-parecer-erro" : undefined
                      }
                    />
                    {erroComentario && (
                      <span className="erro-texto" id="comentario-parecer-erro">
                        {erroComentario}
                      </span>
                    )}
                  </label>
                  {somenteLeitura ? (
                    <p className="form-help">
                      {aguardandoReenvio
                        ? "Aguardando o reenvio do aluno; não é possível registrar outro parecer agora."
                        : "Parecer definitivo. Os campos estão somente para leitura."}
                    </p>
                  ) : (
                    <button
                      className="primary-button"
                      type="button"
                      disabled={salvando}
                      onClick={() => void handleSalvar()}
                    >
                      {salvando ? "Salvando..." : "Registrar parecer"}
                    </button>
                  )}
                </div>
              </article>
            )}
          </div>
        )}
      </section>
    </PageShell>
  );
}
