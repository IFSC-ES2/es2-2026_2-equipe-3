import type { TipoEntrega } from "./PrazoEntrega";

export type StatusEntrega =
  "SUBMETIDO" | "AJUSTES_NECESSARIOS" | "APROVADO" | "REPROVADO";

export interface Entrega {
  id: number;
  tipo: TipoEntrega;
  status: StatusEntrega;
  urlDocumento: string;
  dataEnvio: string;
  atrasada: boolean;
  prazoDataLimite?: string | null;
  aluno?: { id: number; nome: string; email: string };
  comentarioParecer?: string | null;
}

export interface NovaEntrega {
  tipo: TipoEntrega;
  urlDocumento: string;
}

export interface NovoParecer {
  status: Exclude<StatusEntrega, "SUBMETIDO">;
  comentario: string;
}
