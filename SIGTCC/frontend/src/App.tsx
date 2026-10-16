import { Navigate, Route, Routes } from "react-router-dom";
import { OrientadorCadastroPage } from "./paginas/OrientadorCadastro";
import { OrientadorEdicaoPage } from "./paginas/OrientadorEdicao";
import { OrientadorListagemPage } from "./paginas/Orientadores";
import { PainelSolicitacoesPage } from "./paginas/PainelSolicitacoes";
import { SolicitarOrientacaoPage } from "./paginas/SolicitarOrientacao";
import { AcompanharSolicitacaoPage } from "./paginas/AcompanharSolicitacao";
import { LoginPage } from "./paginas/Login";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import "./App.css";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute papelExigido="ROLE_PROFESSOR">
            <OrientadorCadastroPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/orientadores"
        element={
          <ProtectedRoute>
            <OrientadorListagemPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/perfil-editar"
        element={
          <ProtectedRoute papelExigido="ROLE_PROFESSOR">
            <OrientadorEdicaoPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/painel-orientador"
        element={
          <ProtectedRoute papelExigido="ROLE_PROFESSOR">
            <PainelSolicitacoesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/solicitar/:orientadorId"
        element={
          <ProtectedRoute>
            <SolicitarOrientacaoPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/acompanhar/:id"
        element={
          <ProtectedRoute>
            <AcompanharSolicitacaoPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/painel-orientador/:orientadorId"
        element={
          <ProtectedRoute papelExigido="ROLE_PROFESSOR">
            <PainelSolicitacoesPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
