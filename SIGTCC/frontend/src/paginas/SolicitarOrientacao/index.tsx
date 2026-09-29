import { useParams } from "react-router-dom";
import { SolicitacaoForm } from "../../components/SolicitacaoForm";
import { PageShell } from "../OrientadorCadastro";

export function SolicitarOrientacaoPage() {
  const { orientadorId } = useParams();
  const id = Number(orientadorId);

  return (
    <PageShell active="catalogo">
      {Number.isInteger(id) && id > 0 ? (
        <SolicitacaoForm orientadorId={id} />
      ) : (
        <p className="feedback erro" role="alert">Orientador inválido.</p>
      )}
    </PageShell>
  );
}