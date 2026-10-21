import { OrientadorList } from "../../components/OrientadorList";
import { PainelPrazos } from "../../components/PainelPrazos";
import { PageShell } from "../OrientadorCadastro";

export function OrientadorListagemPage() {
  return (
    <PageShell active="catalogo">
      <PainelPrazos />
      <OrientadorList />
    </PageShell>
  );
}