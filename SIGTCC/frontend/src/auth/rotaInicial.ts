export function rotaInicialPorPapel(papeis: string[]): string {
  if (papeis.includes("ROLE_PROFESSOR")) return "/painel-orientador";
  if (papeis.includes("ROLE_ALUNO")) return "/orientadores";
  return "/";
}
