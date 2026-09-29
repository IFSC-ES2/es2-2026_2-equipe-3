import { Navigate, Route, Routes } from "react-router-dom";
import { OrientadorCadastroPage } from "./paginas/OrientadorCadastro";
import { OrientadorEdicaoPage } from "./paginas/OrientadorEdicao";
import { OrientadorListagemPage } from "./paginas/Orientadores";
import { PainelSolicitacoesPage } from "./paginas/PainelSolicitacoes";
import { SolicitarOrientacaoPage } from "./paginas/SolicitarOrientacao";
import { AcompanharSolicitacaoPage } from "./paginas/AcompanharSolicitacao";
import "./App.css";

function App() {
  return (
    <Routes>
      <Route path="/" element={<OrientadorCadastroPage />} />
      <Route path="/orientadores" element={<OrientadorListagemPage />} />
      <Route path="/perfil-editar" element={<OrientadorEdicaoPage />} />
      <Route path="/painel-orientador" element={<PainelSolicitacoesPage />} />
      <Route path="/solicitar/:orientadorId" element={<SolicitarOrientacaoPage />} />
      <Route path="/acompanhar/:id" element={<AcompanharSolicitacaoPage />} />
      <Route
        path="/painel-orientador/:orientadorId"
        element={<PainelSolicitacoesPage />}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
