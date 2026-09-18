import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import type { Orientador } from "../../types/Orientador.ts";
import { listarOrientadores } from "../../utils/orientadorService.ts";
import { OrientadorCard } from "../OrientadorCard";

export function OrientadorList() {
  const [orientadores, setOrientadores] = useState<Orientador[]>([]);
  const [area, setArea] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  const carregar = async (filtro?: string) => {
    setCarregando(true);
    setErro("");
    try {
      const dados = await listarOrientadores(filtro);
      setOrientadores(dados.filter((orientador) => orientador.ativo));
    } catch {
      setErro("Erro ao carregar orientadores");
      setOrientadores([]);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    let ativo = true;

    const carregarInicial = async () => {
      setCarregando(true);
      setErro("");
      try {
        const dados = await listarOrientadores();
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

    void carregarInicial();
    return () => {
      ativo = false;
    };
  }, []);

  const filtrar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    void carregar(area.trim() || undefined);
  };

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
      <form className="filtro-form" onSubmit={filtrar}>
        <label htmlFor="filtro-area">Área de atuação</label>
        <div className="filter-controls">
          <input
            id="filtro-area"
            value={area}
            onChange={(evento) => setArea(evento.target.value)}
            placeholder="Ex.: Engenharia de Software"
          />
          <button className="primary-button" type="submit">
            Filtrar
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
