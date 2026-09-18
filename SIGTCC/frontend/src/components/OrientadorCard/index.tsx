import type { Orientador } from "../../types/Orientador.ts";

interface OrientadorCardProps {
  orientador: Orientador;
}

export function OrientadorCard({ orientador }: OrientadorCardProps) {
  return (
    <article className="orientador-card">
      <div className="card-heading">
        <div>
          <p className="card-eyebrow">Orientador</p>
          <h3>{orientador.nome}</h3>
        </div>
        <span className="vagas-badge">{orientador.vagasDisponiveis} vagas</span>
      </div>
      <p className="card-email">{orientador.email}</p>
      {orientador.departamento && (
        <p className="card-departamento">{orientador.departamento}</p>
      )}
      {orientador.biografia && (
        <p className="card-biografia">{orientador.biografia}</p>
      )}
      <div className="card-linhas">
        <strong>Áreas de pesquisa</strong>
        <ul>
          {orientador.linhasDePesquisa.map((linha, index) => (
            <li key={`${linha}-${index}`}>{linha}</li>
          ))}
        </ul>
      </div>
    </article>
  );
}
