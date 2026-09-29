import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { PageShell } from "../OrientadorCadastro";
import type { Solicitacao } from "../../types/Solicitacao";
import {
  listarSolicitacoesPorOrientador,
  atualizarStatusSolicitacao,
} from "../../utils/solicitacaoService";
import { ApiError } from "../../utils/orientadorService";
import "./PainelSolicitacoes.css";

export function PainelSolicitacoesPage() {
  const { orientadorId: paramId } = useParams<{ orientadorId?: string }>();
  const idOrientador = Number(
    paramId || localStorage.getItem("orientadorId") || 1,
  );

  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[]>([]);
  const [vagasDisponiveis, setVagasDisponiveis] = useState<number | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [mensagem, setMensagem] = useState("");
  const [sucesso, setSucesso] = useState(false);
  const [processandoId, setProcessandoId] = useState<number | null>(null);

  // Controle de recusa por ID de solicitacao
  const [solicitacaoRecusandoId, setSolicitacaoRecusandoId] = useState<
    number | null
  >(null);
  const [justificativa, setJustificativa] = useState("");

  useEffect(() => {
    let ativo = true;

    const carregarInicial = async () => {
      setCarregando(true);
      try {
        const dados = await listarSolicitacoesPorOrientador(
          idOrientador,
          "PENDENTE",
        );
        if (ativo) {
          setSolicitacoes(dados);
          if (dados.length > 0 && dados[0].orientador) {
            setVagasDisponiveis(dados[0].orientador.vagasDisponiveis);
          }
        }
      } catch (erro) {
        if (ativo) {
          setSucesso(false);
          if (erro instanceof ApiError) {
            setMensagem(`Erro (${erro.status}): ${erro.message}`);
          } else if (erro instanceof Error) {
            setMensagem(erro.message);
          } else {
            setMensagem("Erro ao carregar solicitações.");
          }
        }
      } finally {
        if (ativo) {
          setCarregando(false);
        }
      }
    };

    void carregarInicial();
    return () => {
      ativo = false;
    };
  }, [idOrientador]);

  const handleAceitar = async (id: number) => {
    setProcessandoId(id);
    setMensagem("");
    try {
      const resposta = await atualizarStatusSolicitacao(id, {
        status: "ACEITA",
      });
      setSolicitacoes((prev) => prev.filter((item) => item.id !== id));
      if (
        resposta.orientador &&
        typeof resposta.orientador.vagasDisponiveis === "number"
      ) {
        setVagasDisponiveis(resposta.orientador.vagasDisponiveis);
      } else if (vagasDisponiveis !== null && vagasDisponiveis > 0) {
        setVagasDisponiveis(vagasDisponiveis - 1);
      }
      setSucesso(true);
      setMensagem("Solicitação aceita com sucesso!");
    } catch (erro) {
      setSucesso(false);
      if (erro instanceof ApiError) {
        if (erro.status === 422) {
          setMensagem(
            erro.message ||
              "O orientador não possui vagas disponíveis para aceitar esta solicitação.",
          );
        } else if (erro.status === 409) {
          setMensagem(
            erro.message ||
              "Esta solicitação já foi respondida e não pode ser alterada.",
          );
        } else {
          setMensagem(
            erro.message || `Erro ${erro.status} ao processar o aceite.`,
          );
        }
      } else if (erro instanceof Error) {
        setMensagem(erro.message);
      } else {
        setMensagem("Erro desconhecido ao aceitar solicitação.");
      }
    } finally {
      setProcessandoId(null);
    }
  };

  const handleIniciarRecusa = (id: number) => {
    setSolicitacaoRecusandoId(id);
    setJustificativa("");
    setMensagem("");
  };

  const handleCancelarRecusa = () => {
    setSolicitacaoRecusandoId(null);
    setJustificativa("");
  };

  const handleConfirmarRecusa = async (id: number) => {
    const textoLimpo = justificativa.trim();
    if (textoLimpo.length < 10) {
      setSucesso(false);
      setMensagem(
        "A justificativa é obrigatória e deve ter pelo menos 10 caracteres.",
      );
      return;
    }

    setProcessandoId(id);
    setMensagem("");
    try {
      await atualizarStatusSolicitacao(id, {
        status: "RECUSADA",
        justificativa: textoLimpo,
      });
      setSolicitacoes((prev) => prev.filter((item) => item.id !== id));
      setSolicitacaoRecusandoId(null);
      setJustificativa("");
      setSucesso(true);
      setMensagem("Solicitação recusada com sucesso.");
    } catch (erro) {
      setSucesso(false);
      if (erro instanceof ApiError) {
        if (erro.status === 409) {
          setMensagem(
            erro.message ||
              "Esta solicitação já foi respondida e não pode ser alterada.",
          );
        } else {
          setMensagem(
            erro.message || `Erro ${erro.status} ao recusar solicitação.`,
          );
        }
      } else if (erro instanceof Error) {
        setMensagem(erro.message);
      } else {
        setMensagem("Erro desconhecido ao recusar solicitação.");
      }
    } finally {
      setProcessandoId(null);
    }
  };

  const formatarData = (dataIso: string) => {
    try {
      const data = new Date(dataIso);
      return data.toLocaleDateString("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return dataIso;
    }
  };

  return (
    <PageShell active="painel" mensagem={mensagem} sucesso={sucesso}>
      <div className="painel-solicitacoes">
        <div className="painel-topo">
          <div className="painel-titulo-bloco">
            <h1>Painel de Solicitações de Orientação</h1>
            <p>
              Gerencie e responda às solicitações de TCC enviadas pelos alunos
            </p>
          </div>

          <div className="painel-vagas-badge">
            <span>Vagas disponíveis:</span>
            <strong>
              {vagasDisponiveis !== null ? vagasDisponiveis : "—"}
            </strong>
          </div>
        </div>

        {carregando ? (
          <div className="painel-carregando" role="status">
            Carregando solicitações...
          </div>
        ) : solicitacoes.length === 0 ? (
          <div className="painel-vazio">
            Nenhuma solicitação pendente no momento.
          </div>
        ) : (
          <div className="solicitacoes-lista">
            {solicitacoes.map((item) => {
              const estaRecusando = solicitacaoRecusandoId === item.id;
              const emProcessamento = processandoId === item.id;
              const justificativaValida = justificativa.trim().length >= 10;

              return (
                <article
                  key={item.id}
                  className="solicitacao-card"
                  data-testid={`solicitacao-${item.id}`}
                >
                  <header className="solicitacao-cabecalho">
                    <div className="solicitacao-aluno">
                      <h2>{item.aluno.nome}</h2>
                      <div className="solicitacao-aluno-info">
                        <span>
                          <strong>Curso:</strong> {item.aluno.curso}
                        </span>
                        <span>
                          <strong>E-mail:</strong> {item.aluno.email}
                        </span>
                        <span>
                          <strong>Data:</strong> {formatarData(item.criadoEm)}
                        </span>
                      </div>
                    </div>
                    <span className="solicitacao-tag">Pendente</span>
                  </header>

                  <div className="solicitacao-corpo">
                    <div className="solicitacao-tema">
                      <strong>Tema proposto:</strong> {item.tema}
                    </div>
                    <div className="solicitacao-mensagem">
                      <strong>Mensagem do aluno:</strong>
                      <p>{item.mensagem}</p>
                    </div>
                  </div>

                  {estaRecusando ? (
                    <div className="recusa-painel">
                      <label htmlFor={`justificativa-${item.id}`}>
                        Justificativa da recusa (obrigatória, mínimo 10
                        caracteres):
                      </label>
                      <textarea
                        id={`justificativa-${item.id}`}
                        className="recusa-textarea"
                        placeholder="Informe o motivo da recusa para orientar o estudante..."
                        value={justificativa}
                        onChange={(e) => setJustificativa(e.target.value)}
                        disabled={emProcessamento}
                        rows={3}
                      />
                      <div className="recusa-rodape">
                        <span
                          className={`recusa-contador ${
                            !justificativaValida ? "invalido" : ""
                          }`}
                        >
                          {justificativa.trim().length} / 10 caracteres mínimos
                        </span>
                        <div className="recusa-botoes">
                          <button
                            type="button"
                            className="btn-cancelar"
                            onClick={handleCancelarRecusa}
                            disabled={emProcessamento}
                          >
                            Cancelar
                          </button>
                          <button
                            type="button"
                            className="btn-confirmar-recusa"
                            onClick={() => handleConfirmarRecusa(item.id)}
                            disabled={!justificativaValida || emProcessamento}
                          >
                            {emProcessamento
                              ? "Processando..."
                              : "Confirmar Recusa"}
                          </button>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="solicitacao-acoes">
                      <button
                        type="button"
                        className="btn-recusar"
                        onClick={() => handleIniciarRecusa(item.id)}
                        disabled={emProcessamento}
                      >
                        Recusar
                      </button>
                      <button
                        type="button"
                        className="btn-aceitar"
                        onClick={() => handleAceitar(item.id)}
                        disabled={emProcessamento}
                      >
                        {emProcessamento ? "Processando..." : "Aceitar"}
                      </button>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        )}
      </div>
    </PageShell>
  );
}
