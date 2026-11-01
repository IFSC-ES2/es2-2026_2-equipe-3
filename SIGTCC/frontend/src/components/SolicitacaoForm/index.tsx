import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import type { ErroValidacaoCampo } from "../../types/Erro";
import type { NovaSolicitacao } from "../../types/Solicitacao";
import { ApiError } from "../../utils/apiErro";
import { criarSolicitacao } from "../../utils/solicitacaoService";

interface SolicitacaoFormProps {
  orientadorId: number;
}

type CampoFormulario = "curso" | "tema" | "mensagem";

export function SolicitacaoForm({ orientadorId }: SolicitacaoFormProps) {
  const { sessao } = useAuth();
  const nomeAluno = sessao?.nome ?? "";
  const emailAluno = sessao?.email ?? "";

  const [formulario, setFormulario] = useState<Record<CampoFormulario, string>>(
    {
      curso: "",
      tema: "",
      mensagem: "",
    },
  );
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});
  const [erroEnvio, setErroEnvio] = useState("");
  const [sucessoId, setSucessoId] = useState<number | null>(null);
  const [enviando, setEnviando] = useState(false);

  const atualizarCampo = (campo: CampoFormulario, valor: string) => {
    setFormulario((atual) => ({ ...atual, [campo]: valor }));
    setErrosCampo((atual) => ({ ...atual, [campo]: "" }));
  };

  const validar = (): Record<string, string> => {
    const novosErros: Record<string, string> = {};
    const curso = formulario.curso.trim();
    const tema = formulario.tema.trim();
    const mensagem = formulario.mensagem.trim();

    if (curso.length < 2 || curso.length > 100) {
      novosErros.curso = "Informe um curso entre 2 e 100 caracteres.";
    }
    if (tema.length < 5 || tema.length > 150) {
      novosErros.tema = "Informe um tema entre 5 e 150 caracteres.";
    }
    if (mensagem.length < 10 || mensagem.length > 1000) {
      novosErros.mensagem = "A mensagem deve ter entre 10 e 1000 caracteres.";
    }
    return novosErros;
  };

  const enviar = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const validacao = validar();
    setErrosCampo(validacao);
    setErroEnvio("");
    if (Object.keys(validacao).length > 0) return;

    const dados: NovaSolicitacao = {
      orientadorId,
      curso: formulario.curso.trim(),
      tema: formulario.tema.trim(),
      mensagem: formulario.mensagem.trim(),
    };

    setEnviando(true);
    try {
      const solicitacao = await criarSolicitacao(dados);
      setSucessoId(solicitacao.id);
    } catch (erro) {
      if (
        erro instanceof ApiError &&
        erro.status === 400 &&
        Array.isArray(erro.detalhes)
      ) {
        const errosDaApi = erro.detalhes.reduce<Record<string, string>>(
          (acumulado, detalhe: ErroValidacaoCampo) => {
            acumulado[detalhe.campo] = detalhe.mensagem;
            return acumulado;
          },
          {},
        );
        setErrosCampo(errosDaApi);
      } else if (erro instanceof ApiError && erro.status === 409) {
        setErroEnvio(`Solicitação duplicada: ${erro.message}`);
      } else if (erro instanceof ApiError && erro.status === 422) {
        setErroEnvio(`Orientador indisponível: ${erro.message}`);
      } else {
        setErroEnvio(
          erro instanceof Error
            ? erro.message
            : "Não foi possível enviar a solicitação.",
        );
      }
    } finally {
      setEnviando(false);
    }
  };

  if (sucessoId !== null) {
    return (
      <section
        className="orientador-form solicitacao-sucesso"
        aria-live="polite"
      >
        <p className="section-kicker">Solicitação enviada</p>
        <h2>Agora é aguardar o retorno do orientador.</h2>
        <p>
          Seu código de acompanhamento é <strong>#{sucessoId}</strong>.
        </p>
        <Link
          className="primary-button success-link"
          to={`/acompanhar/${sucessoId}`}
        >
          Acompanhar solicitação
        </Link>
      </section>
    );
  }

  const campoComErro = (campo: CampoFormulario) =>
    errosCampo[campo] || undefined;

  return (
    <form
      className="orientador-form solicitacao-form"
      onSubmit={enviar}
      noValidate
    >
      <div className="form-title">
        <p className="section-kicker">Solicitação de orientação</p>
        <h2>Conte sua proposta de TCC</h2>
      </div>
      {erroEnvio && (
        <p className="feedback erro" role="alert">
          {erroEnvio}
        </p>
      )}
      <div className="form-grid">
        <label className="form-group" htmlFor="aluno-nome">
          Nome completo
          <input id="aluno-nome" value={nomeAluno} readOnly />
        </label>
        <label className="form-group" htmlFor="aluno-email">
          E-mail
          <input id="aluno-email" type="email" value={emailAluno} readOnly />
        </label>
        <label className="form-group form-group-wide" htmlFor="aluno-curso">
          Curso
          <input
            id="aluno-curso"
            required
            minLength={2}
            maxLength={100}
            value={formulario.curso}
            onChange={(evento) => atualizarCampo("curso", evento.target.value)}
            aria-invalid={Boolean(campoComErro("curso"))}
            aria-describedby={
              campoComErro("curso") ? "erro-aluno-curso" : undefined
            }
          />
          {campoComErro("curso") && (
            <span id="erro-aluno-curso" className="erro-texto">
              {campoComErro("curso")}
            </span>
          )}
        </label>
        <label
          className="form-group form-group-wide"
          htmlFor="solicitacao-tema"
        >
          Tema pretendido
          <input
            id="solicitacao-tema"
            required
            minLength={5}
            maxLength={150}
            value={formulario.tema}
            onChange={(evento) => atualizarCampo("tema", evento.target.value)}
            aria-invalid={Boolean(campoComErro("tema"))}
            aria-describedby={
              campoComErro("tema") ? "erro-solicitacao-tema" : undefined
            }
          />
          {campoComErro("tema") && (
            <span id="erro-solicitacao-tema" className="erro-texto">
              {campoComErro("tema")}
            </span>
          )}
        </label>
        <label
          className="form-group form-group-wide"
          htmlFor="solicitacao-mensagem"
        >
          Mensagem para o orientador
          <textarea
            id="solicitacao-mensagem"
            required
            minLength={10}
            maxLength={1000}
            value={formulario.mensagem}
            onChange={(evento) =>
              atualizarCampo("mensagem", evento.target.value)
            }
            aria-invalid={Boolean(campoComErro("mensagem"))}
            aria-describedby={
              campoComErro("mensagem") ? "erro-solicitacao-mensagem" : undefined
            }
          />
          {campoComErro("mensagem") && (
            <span id="erro-solicitacao-mensagem" className="erro-texto">
              {campoComErro("mensagem")}
            </span>
          )}
        </label>
      </div>
      <button className="primary-button" type="submit" disabled={enviando}>
        {enviando ? "Enviando..." : "Enviar solicitação"}
      </button>
    </form>
  );
}
