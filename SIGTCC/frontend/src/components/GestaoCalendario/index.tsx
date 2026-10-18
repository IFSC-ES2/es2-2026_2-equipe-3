import { useEffect, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import {
  TIPOS_ENTREGA,
  type PrazoEntrega,
  type TipoEntrega,
} from "../../types/PrazoEntrega.ts";
import { ApiError } from "../../utils/orientadorService.ts";
import {
  atualizarPrazo,
  cadastrarPrazo,
  listarPrazosVigentes,
} from "../../utils/prazoEntregaService.ts";
import "./GestaoCalendario.css";

type ItemTipoEntrega = { valor: TipoEntrega; rotulo: string };
type ErrosCampos = Record<string, string>;

function formatarData(dataIso: string): string {
  try {
    return new Date(`${dataIso}T00:00:00`).toLocaleDateString("pt-BR");
  } catch {
    return dataIso;
  }
}

export function GestaoCalendario() {
  const [prazos, setPrazos] = useState<PrazoEntrega[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erroCarregamento, setErroCarregamento] = useState("");

  const [tipoSelecionado, setTipoSelecionado] =
    useState<TipoEntrega>("PROJETO_TCC");
  const [dataLimite, setDataLimite] = useState("");
  const [erros, setErros] = useState<ErrosCampos>({});
  const [erroGeral, setErroGeral] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let ativo = true;

    const carregar = async () => {
      try {
        const dados = await listarPrazosVigentes();
        if (!ativo) return;
        setPrazos(dados);
        setErroCarregamento("");
      } catch (erro) {
        if (!ativo) return;
        setErroCarregamento(
          erro instanceof Error
            ? erro.message
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

  const prazoDoTipoSelecionado = prazos.find(
    (prazo) => prazo.tipo === tipoSelecionado,
  );
  const emEdicao = Boolean(prazoDoTipoSelecionado);

  // Troca o tipo em edição e já sincroniza o formulário com o prazo
  // correspondente (se existir), em vez de delegar isso a um effect
  // separado — evita o antipadrão de "estado derivado via effect" e o
  // risco de essa sincronização disparar em momentos inesperados (ex.:
  // logo após salvar, quando `prazos` muda).
  const trocarTipoSelecionado = (tipo: TipoEntrega) => {
    setTipoSelecionado(tipo);
    const prazoExistente = prazos.find((prazo) => prazo.tipo === tipo);
    setDataLimite(prazoExistente?.dataLimite ?? "");
    setErros({});
    setErroGeral("");
    setSucesso("");
  };

  const selecionarTipo = (evento: ChangeEvent<HTMLSelectElement>) => {
    trocarTipoSelecionado(evento.target.value as TipoEntrega);
  };

  const alterarDataLimite = (evento: ChangeEvent<HTMLInputElement>) => {
    setDataLimite(evento.target.value);
    setErros((atual) => {
      const novos = { ...atual };
      delete novos.dataLimite;
      return novos;
    });
  };

  const enviar = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setSucesso("");

    if (!dataLimite) {
      setErros({ dataLimite: "A data limite é obrigatória." });
      return;
    }

    setSalvando(true);
    try {
      const atualizado = prazoDoTipoSelecionado
        ? await atualizarPrazo(prazoDoTipoSelecionado.id, { dataLimite })
        : await cadastrarPrazo({ tipo: tipoSelecionado, dataLimite });

      setPrazos((atual) => [
        ...atual.filter((prazo) => prazo.tipo !== tipoSelecionado),
        atualizado,
      ]);
      setSucesso(
        prazoDoTipoSelecionado
          ? "Prazo atualizado com sucesso."
          : "Prazo cadastrado com sucesso.",
      );
    } catch (erro) {
      if (
        erro instanceof ApiError &&
        erro.status === 400 &&
        Array.isArray(erro.detalhes)
      ) {
        setErros(
          Object.fromEntries(
            erro.detalhes.map((detalhe) => [detalhe.campo, detalhe.mensagem]),
          ),
        );
      }
      setErroGeral(
        erro instanceof Error
          ? erro.message
          : "Erro ao salvar o prazo de entrega.",
      );
    } finally {
      setSalvando(false);
    }
  };

  if (carregando) {
    return (
      <div className="painel-carregando" role="status">
        Carregando calendário de entregas...
      </div>
    );
  }

  return (
    <div className="gestao-calendario">
      <div className="calendario-lista">
        <h2>Prazos vigentes</h2>

        {erroCarregamento && (
          <p className="erro-texto" role="alert">
            {erroCarregamento}
          </p>
        )}

        <table className="calendario-tabela">
          <thead>
            <tr>
              <th>Entrega</th>
              <th>Data limite</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {TIPOS_ENTREGA.map(({ valor, rotulo }: ItemTipoEntrega) => {
              const prazo = prazos.find((item) => item.tipo === valor);
              return (
                <tr key={valor}>
                  <td>{rotulo}</td>
                  <td>
                    {prazo ? formatarData(prazo.dataLimite) : "Não cadastrado"}
                  </td>
                  <td>
                    <button
                      type="button"
                      className="text-button"
                      onClick={() => trocarTipoSelecionado(valor)}
                    >
                      {prazo ? "Editar" : "Cadastrar"}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <form className="calendario-form" onSubmit={enviar} noValidate>
        <h2>{emEdicao ? "Editar prazo" : "Cadastrar prazo"}</h2>

        <label className="form-group" htmlFor="tipo-entrega">
          <span>Tipo de entrega *</span>
          <select
            id="tipo-entrega"
            value={tipoSelecionado}
            onChange={selecionarTipo}
            disabled={salvando}
          >
            {TIPOS_ENTREGA.map(({ valor, rotulo }: ItemTipoEntrega) => (
              <option key={valor} value={valor}>
                {rotulo}
              </option>
            ))}
          </select>
          {erros.tipo && <span className="erro-texto">{erros.tipo}</span>}
        </label>

        <label className="form-group" htmlFor="data-limite">
          <span>Data limite *</span>
          <input
            id="data-limite"
            type="date"
            value={dataLimite}
            onChange={alterarDataLimite}
            disabled={salvando}
          />
          {erros.dataLimite && (
            <span className="erro-texto">{erros.dataLimite}</span>
          )}
        </label>

        {erroGeral && (
          <p className="erro-texto" role="alert">
            {erroGeral}
          </p>
        )}
        {sucesso && (
          <p className="calendario-sucesso" role="status">
            {sucesso}
          </p>
        )}

        <button type="submit" className="primary-button" disabled={salvando}>
          {salvando
            ? "Salvando..."
            : emEdicao
              ? "Salvar alterações"
              : "Cadastrar prazo"}
        </button>
      </form>
    </div>
  );
}
