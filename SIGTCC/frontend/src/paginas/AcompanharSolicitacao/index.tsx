import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { Solicitacao } from "../../types/Solicitacao";
import { ApiError } from "../../utils/orientadorService";
import { obterSolicitacao } from "../../utils/solicitacaoService";
import { PageShell } from "../OrientadorCadastro";

const rotulosStatus = {
  PENDENTE: "Aguardando resposta",
  ACEITA: "Solicitação aceita",
  RECUSADA: "Solicitação recusada",
  CANCELADA: "Solicitação cancelada",
};

export function AcompanharSolicitacaoPage() {
  const { id: idParam } = useParams();
  const id = Number(idParam);
  const [solicitacao, setSolicitacao] = useState<Solicitacao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [naoEncontrada, setNaoEncontrada] = useState(false);
  const [erro, setErro] = useState("");
  const idInvalido = !Number.isInteger(id) || id < 1;

  useEffect(() => {
    let ativa = true;
    if (idInvalido) {
      return () => {
        ativa = false;
      };
    }

    void obterSolicitacao(id)
      .then((dados) => {
        if (ativa) setSolicitacao(dados);
      })
      .catch((falha: unknown) => {
        if (!ativa) return;
        if (falha instanceof ApiError && falha.status === 404) {
          setNaoEncontrada(true);
        } else {
          setErro(
            falha instanceof Error
              ? falha.message
              : "Não foi possível carregar a solicitação.",
          );
        }
      })
      .finally(() => {
        if (ativa) setCarregando(false);
      });

    return () => {
      ativa = false;
    };
  }, [id, idInvalido]);

  return (
    <PageShell active="catalogo">
      <section className="orientador-form acompanhamento">
        <p className="section-kicker">Acompanhamento público</p>
        <h2>Status da solicitação</h2>
        {carregando && (
          <p className="feedback loading">Carregando solicitação...</p>
        )}
        {(naoEncontrada || idInvalido) && (
          <p className="feedback erro" role="alert">
            Não encontramos uma solicitação com esse código. Confira o link ou o
            número informado.
          </p>
        )}
        {erro && (
          <p className="feedback erro" role="alert">
            {erro}
          </p>
        )}
        {solicitacao && (
          <div className="acompanhamento-detalhes">
            <p
              className={`status-indicador status-${solicitacao.status.toLowerCase()}`}
              role="status"
            >
              <span aria-hidden="true" />
              {rotulosStatus[solicitacao.status]}
            </p>
            <dl>
              <div>
                <dt>Código</dt>
                <dd>#{solicitacao.id}</dd>
              </div>
              <div>
                <dt>Tema</dt>
                <dd>{solicitacao.tema}</dd>
              </div>
              <div>
                <dt>Orientador</dt>
                <dd>{solicitacao.orientador.nome}</dd>
              </div>
              <div>
                <dt>Enviada em</dt>
                <dd>
                  {new Date(solicitacao.criadoEm).toLocaleDateString("pt-BR")}
                </dd>
              </div>
            </dl>
            {solicitacao.status === "RECUSADA" && solicitacao.justificativa && (
              <div className="justificativa-recusa">
                <h3>Justificativa do orientador</h3>
                <p>{solicitacao.justificativa}</p>
              </div>
            )}
          </div>
        )}
      </section>
    </PageShell>
  );
}
