import { useEffect, useState } from "react";
import {
  TIPOS_ENTREGA,
  type PrazoEntrega,
  type TipoEntrega,
} from "../../types/PrazoEntrega.ts";
import { listarPrazosVigentes } from "../../utils/prazoEntregaService.ts";
import {
  calcularContagemRegressiva,
  formatarDataPrazo,
} from "../../utils/contagemRegressiva.ts";
import "./PainelPrazos.css";

interface PainelPrazosProps {
  dataReferencia?: Date;
}

type ItemTipoEntrega = { valor: TipoEntrega; rotulo: string };

export function PainelPrazos({ dataReferencia }: PainelPrazosProps) {
  const [prazos, setPrazos] = useState<PrazoEntrega[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [agora, setAgora] = useState<Date>(() => new Date());

  useEffect(() => {
    let ativo = true;

    const carregar = async () => {
      try {
        const dados = await listarPrazosVigentes();
        if (!ativo) return;
        setPrazos(Array.isArray(dados) ? dados : []);
        setErro("");
      } catch (falha) {
        if (!ativo) return;
        setErro(
          falha instanceof Error
            ? falha.message
            : "Erro ao carregar os prazos de entrega.",
        );
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    void carregar();
    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    if (dataReferencia) return;
    const intervalo = window.setInterval(() => {
      setAgora(new Date());
    }, 60_000);
    return () => {
      window.clearInterval(intervalo);
    };
  }, [dataReferencia]);

  const dataAtual = dataReferencia ?? agora;

  return (
    <section className="painel-prazos" aria-label="Prazos de entrega">
      <div className="painel-prazos-cabecalho">
        <h2>Prazos de Entrega</h2>
        <span>Calendário acadêmico vigente</span>
      </div>

      {carregando ? (
        <p className="prazos-feedback">Carregando prazos de entrega...</p>
      ) : erro ? (
        <p className="prazos-feedback prazos-feedback-erro">{erro}</p>
      ) : (
        <div className="painel-prazos-grid">
          {TIPOS_ENTREGA.map(({ valor, rotulo }: ItemTipoEntrega) => {
            const prazo = prazos.find((item) => item.tipo === valor);
            const contagem = prazo
              ? calcularContagemRegressiva(prazo.dataLimite, dataAtual)
              : null;

            return (
              <article
                key={valor}
                className="prazo-card"
                data-testid={`prazo-card-${valor}`}
              >
                <span className="prazo-tipo">{rotulo}</span>
                <strong className="prazo-data">
                  {prazo ? formatarDataPrazo(prazo.dataLimite) : "—"}
                </strong>
                {contagem ? (
                  <span
                    className={`prazo-badge prazo-badge-${contagem.status}`}
                  >
                    {contagem.texto}
                  </span>
                ) : (
                  <span className="prazo-badge prazo-badge-indefinido">
                    Prazo não definido
                  </span>
                )}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}