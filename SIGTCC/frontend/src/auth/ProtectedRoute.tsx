import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "./useAuth";
import type { Papel } from "./authService";
import { AcessoNegadoPage } from "../paginas/AcessoNegado";

interface ProtectedRouteProps {
  children: ReactNode;
  papelExigido?: Papel;
}

export function ProtectedRoute({
  children,
  papelExigido,
}: ProtectedRouteProps) {
  const { sessao, carregando, temPapel } = useAuth();

  if (carregando) {
    return null;
  }

  if (!sessao) {
    return <Navigate to="/login" replace />;
  }

  if (papelExigido && !temPapel(papelExigido)) {
    return <AcessoNegadoPage />;
  }

  return <>{children}</>;
}
