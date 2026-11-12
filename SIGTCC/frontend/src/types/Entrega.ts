import type { TipoEntrega } from "./PrazoEntrega";

export type StatusEntrega =
  "SUBMETIDO" | "AJUSTES_NECESSARIOS" | "APROVADO" | "REPROVADO";

export interface ParecerEntrega {
  id: number;
  resultado: Exclude<StatusEntrega, "SUBMETIDO">;
  comentario: string | null;
  registradoEm: string;
}

export interface Entrega {
  id: number;
  semestre: string;
  tipo: TipoEntrega;
  descricaoTipo: string;
  urlDocumento: string;
  status: StatusEntrega;
  atrasada: boolean;
  enviadoEm: string;
  aluno: { id: number; nome: string; email: string; curso: string };
  orientador: { id: number; nome: string; email: string };
  parecer: ParecerEntrega | null;
}

export interface NovaEntrega {
  tipo: TipoEntrega;
  urlDocumento: string;
}

export interface NovoParecer {
  resultado: Exclude<StatusEntrega, "SUBMETIDO">;
  comentario?: string | null;
}
