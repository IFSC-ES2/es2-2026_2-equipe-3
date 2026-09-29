import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import type { ErroValidacaoCampo } from "../../types/Erro";
import type { NovaSolicitacao } from "../../types/Solicitacao";
import { ApiError } from "../../utils/orientadorService";
import { criarSolicitacao } from "../../utils/solicitacaoService";

interface SolicitacaoFormProps {
  orientadorId: number;
}

type CampoFormulario =
  | "aluno.nome"
  | "aluno.email"
  | "aluno.curso"
  | "tema"
  | "mensagem";

export function SolicitacaoForm({ orientadorId }: SolicitacaoFormProps) {
  const [formulario, setFormulario] = useState({
    aluno: { nome: "", email: "", curso: "" },
    tema: "",
    mensagem: "",
  });
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});
  const [erroEnvio, setErroEnvio] = useState("");
  const [sucessoId, setSucessoId] = useState<number | null>(null);
  const [enviando, setEnviando] = useState(false);

  const atualizarCampo = (campo: CampoFormulario, valor: string) => {
    setFormulario((atual) => {
      if (campo.startsWith("aluno.")) {
        const propriedade = campo.slice(6) as keyof typeof atual.aluno;
        return { ...atual, aluno: { ...atual.aluno, [propriedade]: valor } };
      }
      return { ...atual, [campo]: valor };
    });
    setErrosCampo((atual) => ({ ...atual, [campo]: "" }));
  };

  const validar = (): Record<string, string> => {
    const novosErros: Record<string, string> = {};
    const nome = formulario.aluno.nome.trim();
    const email = formulario.aluno.email.trim();
    const curso = formulario.aluno.curso.trim();
    const tema = formulario.tema.trim();
    const mensagem = formulario.mensagem.trim();

    if (nome.length < 3 || nome.length > 100) {
      novosErros["aluno.nome"] = "Informe um nome entre 3 e 100 caracteres.";
    }
    if (email.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      novosErros["aluno.email"] = "Informe um e-mail válido (até 100 caracteres).";
    }
    if (curso.length < 2 || curso.length > 100) {
      novosErros["aluno.curso"] = "Informe um curso entre 2 e 100 caracteres.";
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
      aluno: {
        nome: formulario.aluno.nome.trim(),
        email: formulario.aluno.email.trim(),
        curso: formulario.aluno.curso.trim(),
      },
      tema: formulario.tema.trim(),
      mensagem: formulario.mensagem.trim(),
    };

    setEnviando(true);
    try {
      const solicitacao = await criarSolicitacao(dados);
      setSucessoId(solicitacao.id);
    } catch (erro) {
      if (erro instanceof ApiError && erro.status === 400 && Array.isArray(erro.detalhes)) {
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
        setErroEnvio(erro instanceof Error ? erro.message : "Não foi possível enviar a solicitação.");
      }
    } finally {
      setEnviando(false);
    }
  };

  if (sucessoId !== null) {
    return (
      <section className="orientador-form solicitacao-sucesso" aria-live="polite">
        <p className="section-kicker">Solicitação enviada</p>
        <h2>Agora é aguardar o retorno do orientador.</h2>
        <p>Seu código de acompanhamento é <strong>#{sucessoId}</strong>.</p>
        <Link className="primary-button success-link" to={`/acompanhar/${sucessoId}`}>
          Acompanhar solicitação
        </Link>
      </section>
    );
  }

  const campoComErro = (campo: CampoFormulario) => errosCampo[campo] || undefined;

  return (
    <form className="orientador-form solicitacao-form" onSubmit={enviar} noValidate>
      <div className="form-title">
        <p className="section-kicker">Solicitação de orientação</p>
        <h2>Conte sua proposta de TCC</h2>
      </div>
      {erroEnvio && <p className="feedback erro" role="alert">{erroEnvio}</p>}
      <div className="form-grid">
        <label className="form-group" htmlFor="aluno-nome">
          Nome completo
          <input id="aluno-nome" required minLength={3} maxLength={100} value={formulario.aluno.nome} onChange={(evento) => atualizarCampo("aluno.nome", evento.target.value)} aria-invalid={Boolean(campoComErro("aluno.nome"))} aria-describedby={campoComErro("aluno.nome") ? "erro-aluno-nome" : undefined} />
          {campoComErro("aluno.nome") && <span id="erro-aluno-nome" className="erro-texto">{campoComErro("aluno.nome")}</span>}
        </label>
        <label className="form-group" htmlFor="aluno-email">
          E-mail
          <input id="aluno-email" type="email" required maxLength={100} value={formulario.aluno.email} onChange={(evento) => atualizarCampo("aluno.email", evento.target.value)} aria-invalid={Boolean(campoComErro("aluno.email"))} aria-describedby={campoComErro("aluno.email") ? "erro-aluno-email" : undefined} />
          {campoComErro("aluno.email") && <span id="erro-aluno-email" className="erro-texto">{campoComErro("aluno.email")}</span>}
        </label>
        <label className="form-group form-group-wide" htmlFor="aluno-curso">
          Curso
          <input id="aluno-curso" required minLength={2} maxLength={100} value={formulario.aluno.curso} onChange={(evento) => atualizarCampo("aluno.curso", evento.target.value)} aria-invalid={Boolean(campoComErro("aluno.curso"))} aria-describedby={campoComErro("aluno.curso") ? "erro-aluno-curso" : undefined} />
          {campoComErro("aluno.curso") && <span id="erro-aluno-curso" className="erro-texto">{campoComErro("aluno.curso")}</span>}
        </label>
        <label className="form-group form-group-wide" htmlFor="solicitacao-tema">
          Tema pretendido
          <input id="solicitacao-tema" required minLength={5} maxLength={150} value={formulario.tema} onChange={(evento) => atualizarCampo("tema", evento.target.value)} aria-invalid={Boolean(campoComErro("tema"))} aria-describedby={campoComErro("tema") ? "erro-solicitacao-tema" : undefined} />
          {campoComErro("tema") && <span id="erro-solicitacao-tema" className="erro-texto">{campoComErro("tema")}</span>}
        </label>
        <label className="form-group form-group-wide" htmlFor="solicitacao-mensagem">
          Mensagem para o orientador
          <textarea id="solicitacao-mensagem" required minLength={10} maxLength={1000} value={formulario.mensagem} onChange={(evento) => atualizarCampo("mensagem", evento.target.value)} aria-invalid={Boolean(campoComErro("mensagem"))} aria-describedby={campoComErro("mensagem") ? "erro-solicitacao-mensagem" : undefined} />
          {campoComErro("mensagem") && <span id="erro-solicitacao-mensagem" className="erro-texto">{campoComErro("mensagem")}</span>}
        </label>
      </div>
      <button className="primary-button" type="submit" disabled={enviando}>
        {enviando ? "Enviando..." : "Enviar solicitação"}
      </button>
    </form>
  );
}