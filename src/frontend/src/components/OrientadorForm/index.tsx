import { useState } from 'react';
import type { FormEvent, ChangeEvent } from 'react';
import type { Orientador, OrientadorInput } from '../../types/Orientador.ts';
import { ApiError, atualizarOrientador, cadastrarOrientador } from '../../utils/orientadorService.ts';
import { LinhaPesquisaInput } from '../LinhaPesquisa';

interface OrientadorFormProps {
  orientador?: Orientador;
  onSucesso: (orientador: Orientador) => void;
  onErro: (mensagem: string) => void;
}

type ErrosCampos = Record<string, string>;

const valoresIniciais: OrientadorInput = {
  nome: '',
  email: '',
  departamento: '',
  linhasDePesquisa: [],
  vagasDisponiveis: 0,
  biografia: '',
};

export function OrientadorForm({ orientador, onSucesso, onErro }: OrientadorFormProps) {
  const [dados, setDados] = useState<OrientadorInput>(orientador ?? valoresIniciais);
  const [erros, setErros] = useState<ErrosCampos>({});
  const [carregando, setCarregando] = useState(false);

  const alterarCampo = (evento: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = evento.target;
    setDados((atual) => ({
      ...atual,
      [name]: name === 'vagasDisponiveis' ? Number(value) : value,
    }));
    setErros((atual) => {
      const novos = { ...atual };
      delete novos[name];
      return novos;
    });
  };

  const validar = () => {
    const novosErros: ErrosCampos = {};
    if (!dados.nome.trim()) novosErros.nome = 'O nome é obrigatório';
    else if (dados.nome.trim().length < 3 || dados.nome.length > 100) novosErros.nome = 'O nome deve ter entre 3 e 100 caracteres';
    if (!dados.email.trim()) novosErros.email = 'O e-mail é obrigatório';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dados.email)) novosErros.email = 'Formato de e-mail inválido';
    if (dados.departamento && dados.departamento.length > 100) novosErros.departamento = 'O departamento deve ter no máximo 100 caracteres';
    if (dados.vagasDisponiveis < 0) novosErros.vagasDisponiveis = 'O número de vagas deve ser maior ou igual a zero';
    if (dados.biografia && dados.biografia.length > 500) novosErros.biografia = 'A biografia deve ter no máximo 500 caracteres';
    if (dados.linhasDePesquisa.length === 0) novosErros.linhasDePesquisa = 'Informe ao menos uma linha de pesquisa';
    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const enviar = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    if (!validar()) return;
    setCarregando(true);
    try {
      const resultado = orientador
        ? await atualizarOrientador(orientador.id, dados)
        : await cadastrarOrientador(dados);
      onSucesso(resultado);
      if (!orientador) setDados(valoresIniciais);
    } catch (erro: unknown) {
      if (erro instanceof ApiError && erro.status === 400 && Array.isArray(erro.detalhes)) {
        setErros(Object.fromEntries(erro.detalhes.map((detalhe) => [detalhe.campo, detalhe.mensagem])));
      }
      onErro(erro instanceof Error ? erro.message : 'Erro ao processar formulário');
    } finally {
      setCarregando(false);
    }
  };

  const erro = (campo: string) => erros[campo] && <span className="erro-texto">{erros[campo]}</span>;

  return (
    <form className="orientador-form" onSubmit={enviar} noValidate>
      <div className="form-title">
        <p className="section-kicker">{orientador ? 'Atualização de cadastro' : 'Primeiro acesso'}</p>
        <h2>{orientador ? 'Editar Perfil' : 'Cadastrar Novo Orientador'}</h2>
      </div>
      <div className="form-grid">
        <label className="form-group form-group-wide" htmlFor="nome">
          <span>Nome *</span>
          <input id="nome" name="nome" value={dados.nome} onChange={alterarCampo} placeholder="Seu Nome" disabled={carregando} />
          {erro('nome')}
        </label>
        <label className="form-group" htmlFor="email">
          <span>E-mail *</span>
          <input id="email" name="email" type="email" value={dados.email} onChange={alterarCampo} placeholder="Seuemail@email.com.br" disabled={carregando} />
          {erro('email')}
        </label>
        <label className="form-group" htmlFor="departamento">
          <span>Departamento</span>
          <input id="departamento" name="departamento" value={dados.departamento} onChange={alterarCampo} placeholder="Seu Departamento" disabled={carregando} />
          {erro('departamento')}
        </label>
        <label className="form-group" htmlFor="vagasDisponiveis">
          <span>Vagas Disponíveis *</span>
          <input id="vagasDisponiveis" name="vagasDisponiveis" type="number" min="0" value={dados.vagasDisponiveis} onChange={alterarCampo} disabled={carregando} />
          {erro('vagasDisponiveis')}
        </label>
        <label className="form-group form-group-wide" htmlFor="biografia">
          <span>Biografia</span>
          <textarea id="biografia" name="biografia" rows={4} value={dados.biografia} onChange={alterarCampo} placeholder="Conte um pouco sobre sua experiência..." disabled={carregando} />
          {erro('biografia')}
        </label>
      </div>
      <div className="form-group pesquisa-group">
        <span>Linhas de Pesquisa *</span>
        <LinhaPesquisaInput linhas={dados.linhasDePesquisa} onChange={(linhasDePesquisa) => setDados((atual) => ({ ...atual, linhasDePesquisa }))} />
        {erro('linhasDePesquisa')}
      </div>
      <button className="primary-button" type="submit" disabled={carregando}>
        {carregando ? 'Salvando...' : orientador ? 'Atualizar perfil' : 'Cadastrar orientador'}
      </button>
    </form>
  );
}
