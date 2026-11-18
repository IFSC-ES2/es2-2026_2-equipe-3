export interface OrientadorInput {
  nome: string;
  email: string;
  departamento?: string;
  linhasDePesquisa: string[];
  vagasDisponiveis: number;
  biografia?: string;
}

export interface Orientador extends Omit<
  OrientadorInput,
  "departamento" | "biografia"
> {
  id: number;
  ativo: boolean;
  criadoEm?: string;
  departamento?: string | null;
  biografia?: string | null;
}
