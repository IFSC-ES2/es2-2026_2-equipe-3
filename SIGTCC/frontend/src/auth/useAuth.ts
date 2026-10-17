import { useContext } from "react";
import { AuthContext, type AuthContextValor } from "./authContextInstance";

export function useAuth(): AuthContextValor {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error("useAuth deve ser usado dentro de um AuthProvider.");
  }
  return contexto;
}
