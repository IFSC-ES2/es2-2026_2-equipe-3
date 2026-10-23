import { createContext } from "react";
import type { Papel, SessaoAutenticada } from "./authService";

export interface AuthContextValor {
  sessao: SessaoAutenticada | null;
  carregando: boolean;
  autenticar: (email: string, senha: string) => Promise<SessaoAutenticada>;
  sair: () => void;
  temPapel: (papel: Papel) => boolean;
}

export const AuthContext = createContext<AuthContextValor | undefined>(
  undefined,
);
