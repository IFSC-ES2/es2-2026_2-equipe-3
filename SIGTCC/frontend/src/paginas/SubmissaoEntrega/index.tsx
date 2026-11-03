import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { PageShell } from "../OrientadorCadastro";
import { TIPOS_ENTREGA, type TipoEntrega } from "../../types/PrazoEntrega";
import type { Entrega } from "../../types/Entrega";
import {
  criarEntrega,
  listarEntregasDoAluno,
} from "../../utils/entregaService";
import { ApiError } from "../../utils/apiErro";
import "./SubmissaoEntrega.css";

const rotuloStatus: Record<Entrega["status"], string> = {
  SUBMETIDO: "Submetido",
  AJUSTES_NECESSARIOS: "Ajustes necessários",
  APROVADO: "Aprovado",
  REPROVADO: "Reprovado",
};

function urlHttpsValida(valor: string) {
  try {
    return new URL(valor).protocol === "https:";
  } catch {
    return false;
  }
}

export function SubmissaoEntregaPage() {
  const [entregas, setEntregas] = useState<Entrega[]>([]);
  const [tipo, setTipo] = useState<TipoEntrega>("PROJETO_TCC");
  const [urlDocumento, setUrlDocumento] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [mensagem, setMensagem] = useState("");
  const [sucesso, setSucesso] = useState(false);
  const [erroUrl, setErroUrl] = useState("");

  useEffect(() => {
    let ativo = true;
    listarEntregasDoAluno()
      .then((dados) => {
        if (ativo) setEntregas(dados);
      })
      .catch((erro: unknown) => {
        if (ativo) {
          setMensagem(
            erro instanceof Error ? erro.message : "Erro ao carregar entregas.",
          );
        }
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, []);

  const entregaAtual = entregas.find((entrega) => entrega.tipo === tipo);
  const podeReenviar = entregaAtual?.status === "AJUSTES_NECESSARIOS";

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const url = urlDocumento.trim();
    if (!urlHttpsValida(url)) {
      setErroUrl("Informe um link válido iniciado por https://.");
      return;
    }

    setErroUrl("");
    setMensagem("");
    setEnviando(true);
    try {
      const salva = await criarEntrega({ tipo, urlDocumento: url });
      setEntregas((anteriores) => [
        ...anteriores.filter((entrega) => entrega.tipo !== tipo),
        salva,
      ]);
      setUrlDocumento("");
      setSucesso(true);
      setMensagem("Entrega submetida com sucesso.");
    } catch (erro) {
      setSucesso(false);
      if (erro instanceof ApiError && erro.status === 403) {
        setMensagem(
          "É necessário ter uma orientação aceita para enviar a entrega.",
        );
      } else if (erro instanceof ApiError && erro.status === 409) {
        setMensagem(
          "Esta etapa já foi submetida e não está liberada para reenvio.",
        );
      } else if (erro instanceof ApiError && erro.status === 422) {
        setMensagem(erro.message || "O link informado não foi aceito.");
      } else {
        setMensagem(
          erro instanceof Error
            ? erro.message
            : "Não foi possível enviar a entrega.",
        );
      }
    } finally {
      setEnviando(false);
    }
  }

  const motivoBloqueio = entregaAtual
    ? podeReenviar
      ? "O orientador solicitou ajustes. Envie o novo link da versão revisada."
      : `O reenvio fica disponível quando o status for “Ajustes necessários”. Status atual: ${rotuloStatus[entregaAtual.status]}.`
    : "Informe o link HTTPS do documento compartilhado para submeter esta etapa.";

  return (
    <PageShell active="submissao" mensagem={mensagem} sucesso={sucesso}>
      <section className="entregas-page">
        <div className="entregas-heading">
          <p className="section-kicker">Área do aluno</p>
          <h2>Submissão de entrega</h2>
          <p>
            Compartilhe um link HTTPS com permissão de leitura para seu
            orientador.
          </p>
        </div>

        {carregando ? (
          <p className="feedback loading" role="status">
            Carregando suas entregas...
          </p>
        ) : (
          <div className="entregas-grid">
            <form className="entrega-form" onSubmit={handleSubmit} noValidate>
              <label className="form-group" htmlFor="tipo-entrega-aluno">
                Etapa da entrega
                <select
                  id="tipo-entrega-aluno"
                  value={tipo}
                  onChange={(event) =>
                    setTipo(event.target.value as TipoEntrega)
                  }
                >
                  {TIPOS_ENTREGA.map((item) => (
                    <option key={item.valor} value={item.valor}>
                      {item.rotulo}
                    </option>
                  ))}
                </select>
              </label>
              {entregaAtual && (
                <div
                  className={`entrega-status status-entrega-${entregaAtual.status.toLowerCase()}`}
                >
                  <strong>{rotuloStatus[entregaAtual.status]}</strong>
                  {entregaAtual.atrasada && <span>Entregue com atraso</span>}
                </div>
              )}
              {entregaAtual?.prazoDataLimite && (
                <p className="entrega-prazo">
                  Prazo:{" "}
                  {new Date(
                    `${entregaAtual.prazoDataLimite}T00:00:00`,
                  ).toLocaleDateString("pt-BR")}
                </p>
              )}
              <label className="form-group" htmlFor="url-documento">
                Link do documento
                <input
                  id="url-documento"
                  type="url"
                  value={urlDocumento}
                  onChange={(event) => setUrlDocumento(event.target.value)}
                  placeholder="https://..."
                  aria-invalid={Boolean(erroUrl)}
                  aria-describedby={
                    erroUrl ? "url-documento-erro" : "url-documento-ajuda"
                  }
                  required
                />
                {erroUrl ? (
                  <span className="erro-texto" id="url-documento-erro">
                    {erroUrl}
                  </span>
                ) : (
                  <span className="field-help" id="url-documento-ajuda">
                    Confira se o acesso está liberado para leitura.
                  </span>
                )}
              </label>
              <p className="form-help">{motivoBloqueio}</p>
              <button
                className="primary-button"
                type="submit"
                disabled={enviando || Boolean(entregaAtual && !podeReenviar)}
              >
                {enviando
                  ? "Enviando..."
                  : podeReenviar
                    ? "Reenviar entrega"
                    : "Enviar entrega"}
              </button>
              {enviando && (
                <p className="feedback loading" role="status">
                  Enviando link para validação...
                </p>
              )}
            </form>

            <aside className="entrega-historico" aria-label="Entrega atual">
              <h3>Entrega da etapa</h3>
              {entregaAtual ? (
                <>
                  <p>{rotuloStatus[entregaAtual.status]}</p>
                  <time dateTime={entregaAtual.dataEnvio}>
                    Enviada em{" "}
                    {new Date(entregaAtual.dataEnvio).toLocaleString("pt-BR")}
                  </time>
                  <a
                    href={entregaAtual.urlDocumento}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Abrir documento compartilhado
                  </a>
                </>
              ) : (
                <p>Nenhuma entrega registrada para esta etapa.</p>
              )}
            </aside>
          </div>
        )}
      </section>
    </PageShell>
  );
}
