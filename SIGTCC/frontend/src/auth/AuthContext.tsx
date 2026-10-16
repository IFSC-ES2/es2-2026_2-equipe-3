import { useCallback, useEffect, useState, type ReactNode } from "react";
import {
  login as loginService,
  logout as logoutService,
  recuperarSessaoArmazenada,
  renovarToken,
  type Papel,
  type SessaoAutenticada,
} from "./authService";
import { AuthContext } from "./authContextInstance";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<SessaoAutenticada | null>(() =>
    recuperarSessaoArmazenada(),
  );

  const [carregando, setCarregando] = useState(() => sessao === null);

  useEffect(() => {
    if (sessao !== null) return;

    let cancelado = false;
    renovarToken().then((sessaoRenovada) => {
      if (cancelado) return;
      setSessao(sessaoRenovada);
      setCarregando(false);
    });

    return () => {
      cancelado = true;
    };
  }, []);

  const autenticar = useCallback(async (email: string, senha: string) => {
    const novaSessao = await loginService(email, senha);
    setSessao(novaSessao);
    return novaSessao;
  }, []);

  const sair = useCallback(() => {
    logoutService();
    setSessao(null);
  }, []);

  const temPapel = useCallback(
    (papel: Papel) => sessao?.papeis.includes(papel) ?? false,
    [sessao],
  );

  return (
    <AuthContext.Provider
      value={{ sessao, carregando, autenticar, sair, temPapel }}
    >
      {children}
    </AuthContext.Provider>
  );
}
