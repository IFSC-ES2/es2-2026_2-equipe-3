export type TipoEntrega = "PROJETO_TCC" | "TCC1" | "TCC2";

export const TIPOS_ENTREGA: { valor: TipoEntrega; rotulo: string }[] = [
  { valor: "PROJETO_TCC", rotulo: "Projeto de TCC" },
  { valor: "TCC1", rotulo: "TCC 1" },
  { valor: "TCC2", rotulo: "TCC 2" },
];

export interface PrazoEntrega {
  id: number;
  semestre: string;
  tipo: TipoEntrega;
  descricao: string;
  dataLimite: string;
}

export interface NovoPrazoEntrega {
  tipo: TipoEntrega;
  dataLimite: string;
}

export interface AtualizaPrazoEntrega {
  dataLimite: string;
}
