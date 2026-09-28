export type StatusSolicitacao =
  "PENDENTE" | "ACEITA" | "RECUSADA" | "CANCELADA";

export interface AlunoSolicitacao {
  id: number;
  nome: string;
  email: string;
  curso: string;
}

export interface OrientadorSolicitacao {
  id: number;
  nome: string;
  email: string;
  vagasDisponiveis: number;
}

export interface Solicitacao {
  id: number;
  status: StatusSolicitacao;
  tema: string;
  mensagem: string;
  justificativa: string | null;
  criadoEm: string;
  aluno: AlunoSolicitacao;
  orientador: OrientadorSolicitacao;
}

export interface AtualizaStatusRequest {
  status: "ACEITA" | "RECUSADA";
  justificativa?: string;
}
