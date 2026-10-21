export type StatusContagemRegressiva =
  | "em-dia"
  | "proximo"
  | "hoje"
  | "encerrado";

export interface ContagemRegressivaInfo {
  diasRestantes: number;
  texto: string;
  status: StatusContagemRegressiva;
}

export function formatarDataPrazo(dataIso: string): string {
  try {
    return new Date(`${dataIso}T00:00:00`).toLocaleDateString("pt-BR");
  } catch {
    return dataIso;
  }
}

export function calcularContagemRegressiva(
  dataIso: string,
  dataReferencia: Date = new Date(),
): ContagemRegressivaInfo {
  const dataAlvo = new Date(`${dataIso}T00:00:00`);
  const inicioAlvo = new Date(
    dataAlvo.getFullYear(),
    dataAlvo.getMonth(),
    dataAlvo.getDate(),
  );
  const inicioHoje = new Date(
    dataReferencia.getFullYear(),
    dataReferencia.getMonth(),
    dataReferencia.getDate(),
  );

  const MS_POR_DIA = 1000 * 60 * 60 * 24;
  const diasRestantes = Math.round(
    (inicioAlvo.getTime() - inicioHoje.getTime()) / MS_POR_DIA,
  );

  if (diasRestantes > 1) {
    return {
      diasRestantes,
      texto: `Faltam ${diasRestantes} dias`,
      status: diasRestantes <= 7 ? "proximo" : "em-dia",
    };
  }

  if (diasRestantes === 1) {
    return {
      diasRestantes: 1,
      texto: "Falta 1 dia",
      status: "proximo",
    };
  }

  if (diasRestantes === 0) {
    return {
      diasRestantes: 0,
      texto: "Vence hoje",
      status: "hoje",
    };
  }

  const diasAtraso = Math.abs(diasRestantes);
  return {
    diasRestantes,
    texto:
      diasAtraso === 1
        ? "Prazo encerrado há 1 dia"
        : `Prazo encerrado há ${diasAtraso} dias`,
    status: "encerrado",
  };
}