import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Orientador } from "../../types/Orientador.ts";
import { ApiError } from "../../utils/apiErro.ts";
import { obterOrientador } from "../../utils/orientadorService.ts";
import { obterPerfilProfessor } from "../../utils/professorService.ts";
import { OrientadorForm } from "../../components/OrientadorForm";
import { PageShell } from "../OrientadorCadastro";

export function OrientadorEdicaoPage() {
  const navigate = useNavigate();
  const [orientador, setOrientador] = useState<Orientador | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [sucesso, setSucesso] = useState(false);

  useEffect(() => {
    let ativo = true;

    const carregarPerfil = async () => {
      try {
        const perfil = await obterPerfilProfessor();
        const dados = await obterOrientador(perfil.id);
        if (ativo) setOrientador(dados);
      } catch (falha: unknown) {
        if (ativo) {
          setErro(
            falha instanceof ApiError && falha.status === 404
              ? "Seu usuário não possui um perfil de orientador cadastrado."
              : falha instanceof Error
                ? falha.message
                : "Não foi possível carregar o perfil.",
          );
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    void carregarPerfil();
    return () => {
      ativo = false;
    };
  }, []);

  if (carregando)
    return (
      <PageShell active="perfil">
        <p className="feedback loading">Carregando perfil...</p>
      </PageShell>
    );
  if (erro)
    return (
      <PageShell active="perfil">
        <div className="feedback erro" role="alert">
          {erro}
          <button
            className="text-button"
            type="button"
            onClick={() => navigate("/")}
          >
            Ir para o cadastro
          </button>
        </div>
      </PageShell>
    );

  return (
    <PageShell active="perfil" mensagem={mensagem} sucesso={sucesso}>
      {orientador && (
        <OrientadorForm
          orientador={orientador}
          onSucesso={(atualizado) => {
            setOrientador(atualizado);
            setSucesso(true);
            setMensagem("Perfil atualizado com sucesso!");
          }}
          onErro={(falha) => {
            setSucesso(false);
            setMensagem(`Erro: ${falha}`);
          }}
        />
      )}
    </PageShell>
  );
}
