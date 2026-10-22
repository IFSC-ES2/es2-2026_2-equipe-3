import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import { rotaInicialPorPapel } from "../../auth/rotaInicial";
import { PageShell } from "../OrientadorCadastro";

export function AcessoNegadoPage() {
  const navigate = useNavigate();
  const { sessao } = useAuth();

  return (
    <PageShell>
      <div className="feedback erro" role="alert">
        <h1>Acesso negado</h1>
        <p>Você não tem permissão para acessar esta página.</p>
        <button
          className="text-button"
          type="button"
          onClick={() =>
            navigate(rotaInicialPorPapel(sessao?.papeis ?? []), {
              replace: true,
            })
          }
        >
          Voltar ao início
        </button>
      </div>
    </PageShell>
  );
}
