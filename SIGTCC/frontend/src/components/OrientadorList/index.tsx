import { useEffect, useState } from "react";
import type { Orientador } from "../../types/Orientador.ts";
import { listarOrientadores } from "../../utils/orientadorService.ts";
import { OrientadorCard } from "../OrientadorCard";

export function OrientadorList() {
  const [orientadores, setOrientadores] = useState<Orientador[]>([]);
  const [area, setArea] = useState("");
  const [somenteComVagas, setSomenteComVagas] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    const areaFiltro = area.trim();

    const carregar = async () => {
      setCarregando(true);
      setErro("");
      try {
        const dados = await listarOrientadores({
          area: areaFiltro || undefined,
          temVagas: somenteComVagas || undefined,
        });
        if (ativo)
          setOrientadores(dados.filter((orientador) => orientador.ativo));
      } catch {
        if (ativo) {
          setErro("Erro ao carregar orientadores");
          setOrientadores([]);
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    const temporizador = window.setTimeout(() => void carregar(), 300);
    return () => {
      ativo = false;
      window.clearTimeout(temporizador);
    };
  }, [area, somenteComVagas]);

  const limparFiltros = () => {
    setArea("");
    setSomenteComVagas(false);
  };

  const filtrosAtivos = area.trim() !== "" || somenteComVagas;

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
          <input
            id="filtro-area"
            value={area}
            onChange={(evento) => setArea(evento.target.value)}
            placeholder="Ex.: Engenharia de Software"
          />
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
