import { useState } from "react";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import type { Orientador } from "../../types/Orientador.ts";
import { OrientadorForm } from "../../components/OrientadorForm";

export function OrientadorCadastroPage() {
  const navigate = useNavigate();
  const [mensagem, setMensagem] = useState("");
  const [sucesso, setSucesso] = useState(false);

  const handleSucesso = (orientador: Orientador) => {
    localStorage.setItem("orientadorId", String(orientador.id));
    localStorage.setItem("orientadorNome", orientador.nome);
    setSucesso(true);
    setMensagem(
      `Bem-vindo, ${orientador.nome}! Perfil cadastrado com sucesso.`,
    );
    window.setTimeout(() => navigate("/perfil-editar"), 1500);
  };

  return (
    <PageShell active="cadastro" mensagem={mensagem} sucesso={sucesso}>
      <OrientadorForm
        onSucesso={handleSucesso}
        onErro={(erro) => {
          setSucesso(false);
          setMensagem(`Erro: ${erro}`);
        }}
      />
    </PageShell>
  );
}

interface PageShellProps {
  active: "cadastro" | "catalogo" | "perfil";
  mensagem?: string;
  sucesso?: boolean;
  children: ReactNode;
}

export function PageShell({
  active,
  mensagem,
  sucesso,
  children,
}: PageShellProps) {
  const navigate = useNavigate();
  return (
    <div className="orientador-page">
      <header className="page-header">
        <div className="brand-mark">
          <span>GT</span>
          <div>
            <strong>Gestão de TCC</strong>
            <small>Orientação acadêmica</small>
          </div>
        </div>
        <nav className="page-navigation" aria-label="Navegação principal">
          <button
            className={active === "cadastro" ? "active" : ""}
            type="button"
            onClick={() => navigate("/")}
          >
            Novo cadastro
          </button>
          <button
            className={active === "catalogo" ? "active" : ""}
            type="button"
            onClick={() => navigate("/orientadores")}
          >
            Catálogo
          </button>
          <button
            className={active === "perfil" ? "active" : ""}
            type="button"
            onClick={() => navigate("/perfil-editar")}
          >
            Meu perfil
          </button>
        </nav>
      </header>
      {mensagem && (
        <div
          className={`notificacao ${sucesso ? "notificacao-sucesso" : "notificacao-erro"}`}
          role="status"
        >
          {mensagem}
        </div>
      )}
      <main className="page-content">{children}</main>
      <footer className="page-footer">
        Sistema de Gestão de TCC <span>•</span> Perfis de orientação
      </footer>
    </div>
  );
}
