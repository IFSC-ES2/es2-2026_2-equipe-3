import { useState, type FormEvent } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import { CredenciaisInvalidasError } from "../../auth/authService";
import { rotaInicialPorPapel } from "../../auth/rotaInicial";

interface LocationState {
  from?: { pathname: string };
}

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const { autenticar } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro(null);
    setEnviando(true);

    try {
      const sessao = await autenticar(email, senha);
      const destino =
        (location.state as LocationState | null)?.from?.pathname ??
        rotaInicialPorPapel(sessao.papeis);
      navigate(destino, { replace: true });
    } catch (erroCapturado) {
      if (erroCapturado instanceof CredenciaisInvalidasError) {
        setErro("Credenciais inválidas");
      } else {
        setErro(
          erroCapturado instanceof Error
            ? erroCapturado.message
            : "Erro ao realizar login",
        );
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="orientador-page login-page">
      <header className="page-header">
        <div className="brand-mark">
          <span>GT</span>
          <div>
            <strong>Gestão de TCC</strong>
            <small>Orientação acadêmica</small>
          </div>
        </div>
      </header>

      <main className="page-content login-content">
        <form className="login-form" onSubmit={handleSubmit} noValidate>
          <div className="form-title">
            <p className="section-kicker">Acesso restrito</p>
            <h2>Entrar no SIGTCC</h2>
          </div>

          <div className="form-group">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="username"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              name="senha"
              type="password"
              autoComplete="current-password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              required
            />
          </div>

          {erro && (
            <p role="alert" className="erro-texto login-erro">
              {erro}
            </p>
          )}

          <button className="primary-button" type="submit" disabled={enviando}>
            {enviando ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </main>

      <footer className="page-footer">
        Sistema de Gestão de TCC <span>•</span> Autenticação via Keycloak
      </footer>
    </div>
  );
}
