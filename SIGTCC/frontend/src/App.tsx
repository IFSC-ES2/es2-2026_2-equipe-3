import { Navigate, Route, Routes } from "react-router-dom";
import { OrientadorCadastroPage } from "./paginas/OrientadorCadastro";
import { OrientadorEdicaoPage } from "./paginas/OrientadorEdicao";
import { OrientadorListagemPage } from "./paginas/Orientadores";
import "./App.css";

function App() {
  return (
    <Routes>
      <Route path="/" element={<OrientadorCadastroPage />} />
      <Route path="/orientadores" element={<OrientadorListagemPage />} />
      <Route path="/perfil-editar" element={<OrientadorEdicaoPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
