export interface OrientadorInput {
  nome: string;
  email: string;
  departamento?: string;
  linhasDePesquisa: string[];
  vagasDisponiveis: number;
  biografia?: string;
}

export interface Orientador extends OrientadorInput {
  id: number;
  ativo: boolean;
  criadoEm?: string;
}
