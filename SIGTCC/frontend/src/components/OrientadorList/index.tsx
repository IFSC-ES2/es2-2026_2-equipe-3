import { useEffect, useState } from "react";
import type { Orientador } from "../../types/Orientador.ts";
import { listarOrientadores } from "../../utils/orientadorService.ts";
import { OrientadorCard } from "../OrientadorCard";

export function OrientadorList() {
  const [orientadores, setOrientadores] = useState<Orientador[]>([]);
  const [linhasDisponiveis, setLinhasDisponiveis] = useState<string[]>([]);
  const [area, setArea] = useState("");
  const [somenteComVagas, setSomenteComVagas] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;

    const carregar = async () => {
      setCarregando(true);
      setErro("");
      try {
        const dados = await listarOrientadores({
          area: area || undefined,
          temVagas: somenteComVagas || undefined,
        });
        if (!ativo) return;

        const ativos = dados.filter((orientador) => orientador.ativo);
        setOrientadores(ativos);

        // Acumula as linhas de pesquisa já vistas, para que o seletor não
        // perca opções conforme os filtros vão sendo aplicados.
        setLinhasDisponiveis((atuais) =>
          Array.from(
            new Set([
              ...atuais,
              ...ativos.flatMap((orientador) => orientador.linhasDePesquisa),
            ]),
          ).sort((a, b) => a.localeCompare(b)),
        );
      } catch {
        if (ativo) {
          setErro("Erro ao carregar orientadores");
          setOrientadores([]);
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    void carregar();
    return () => {
      ativo = false;
    };
  }, [area, somenteComVagas]);

  const limparFiltros = () => {
    setArea("");
    setSomenteComVagas(false);
  };

  const filtrosAtivos = area !== "" || somenteComVagas;

  return (
    <section className="catalogo">
      <div className="catalogo-heading">
        <div>
          <p className="section-kicker">Vitrine acadêmica</p>
          <h2>Catálogo de Orientadores</h2>
          <p>
            Encontre professores por área de atuação e conheça suas linhas de
            pesquisa.
          </p>
        </div>
      </div>
      <form
        className="filtro-form"
        onSubmit={(evento) => evento.preventDefault()}
      >
        <label htmlFor="filtro-area">Linha de pesquisa</label>
        <div className="filter-controls">
          <select
            id="filtro-area"
            value={area}
            onChange={(evento) => setArea(evento.target.value)}
          >
            <option value="">Todas as linhas</option>
            {linhasDisponiveis.map((linha) => (
              <option key={linha} value={linha}>
                {linha}
              </option>
            ))}
          </select>
          <label className="filtro-toggle" htmlFor="filtro-vagas">
            <input
              id="filtro-vagas"
              type="checkbox"
              checked={somenteComVagas}
              onChange={(evento) => setSomenteComVagas(evento.target.checked)}
            />
            Somente com vagas disponíveis
          </label>
          <button
            type="button"
            className="text-button"
            onClick={limparFiltros}
            disabled={!filtrosAtivos}
          >
            Limpar filtros
          </button>
        </div>
      </form>
      {carregando && (
        <p className="feedback loading">Carregando orientadores...</p>
      )}
      {erro && (
        <p className="feedback erro" role="alert">
          {erro}
        </p>
      )}
      {!carregando && !erro && orientadores.length === 0 && (
        <p className="empty-state">Nenhum orientador encontrado</p>
      )}
      {!carregando && orientadores.length > 0 && (
        <div className="cards-container">
          {orientadores.map((orientador) => (
            <OrientadorCard key={orientador.id} orientador={orientador} />
          ))}
        </div>
      )}
    </section>
  );
}
