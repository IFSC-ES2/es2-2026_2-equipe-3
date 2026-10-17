export interface PerfilProfessor {
  id: number;
  nome: string;
  email: string;
  departamento: string | null;
  responsavelTurma: boolean;
}
