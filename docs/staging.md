# Ambiente de staging (Issue #220)

Staging roda o mesmo stack de produção (`SIGTCC/docker-compose.prod.yml`: MySQL, Keycloak, backend e frontend) em uma máquina da equipe, exposto por um túnel temporário. Não há servidor dedicado.

## Pré-requisitos

- Docker e Docker Compose.
- `cloudflared` instalado (túnel gratuito, sem conta).
- Acesso ao repositório, para copiar o `.env` de exemplo.

## Passos

1. Copie o modelo e preencha com valores **próprios** do ambiente. Nunca commite o `.env`:
   ```bash
   cd SIGTCC
   cp .env.example .env
   ```
2. Abra os túneis e anote as URLs públicas (uma por serviço):
   ```bash
   cloudflared tunnel --url http://localhost:8081   # Keycloak
   cloudflared tunnel --url http://localhost:8080   # Backend
   cloudflared tunnel --url http://localhost:80     # Frontend
   ```
3. No `.env`, troque as URLs de localhost pelas URLs públicas:
   - `KEYCLOAK_PUBLIC_URL` = URL do túnel do Keycloak (sem barra final).
   - `VITE_KEYCLOAK_URL` = mesma URL do Keycloak.
   - `VITE_API_URL` = URL do túnel do backend.
   - `APP_CORS_ALLOWED_ORIGINS` = URL do túnel do frontend.
4. Suba o stack a partir de `SIGTCC/`:
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env up -d --build
   ```
   O frontend é construído com as URLs do passo 3, então ele deve ser construído depois de definidas.
5. Rode o smoke test por API (critério 4 da #220). Use o Git Bash no Windows, a partir da raiz do repositório:
   ```bash
   KEYCLOAK_URL=<url-do-keycloak> API_URL=<url-do-backend> bash SIGTCC/scripts/smoke-test.sh
   ```
   Sem as variáveis, ele usa `localhost`. O script faz login com os três usuários de teste, cria uma solicitação aceita e um prazo, envia uma entrega por link `https://`, registra o parecer do orientador e confere as regras de acesso (401, 403, 409 e 422). Ele sai com código 0 se tudo passar e com 1 na primeira falha, mostrando a resposta da API.
   - Rode em banco zerado (`docker compose -f docker-compose.prod.yml down -v` antes de subir): o script cria dados e não é repetível sobre o mesmo banco.
6. Teste o login pelo navegador: abra a URL do frontend e entre com `aluno@example.com` / `aluno123`. A navegação nas telas de submissão e avaliação depende das issues #226 e #227; até elas entrarem, valide só o login.

## Credenciais de teste

Os usuários de teste estão em `SIGTCC/keycloak/realm-import.json` e são compartilhados com o time e o professor. Não são segredo de produção, mas devem ser trocados antes de qualquer uso real.

## Limitações conhecidas

- **CORS do Keycloak liberado para qualquer origem:** o login é feito pelo navegador, que chama o endpoint de token do Keycloak, e a URL do túnel do frontend muda a cada execução. Por isso o client `sigtcc-frontend` em `keycloak/realm-import.json` usa `webOrigins: ["*"]`. É só CORS (não altera papéis nem a autenticação), e o login continua exigindo usuário e senha. **O mesmo `realm-import.json` é usado no ambiente local**, então o `*` também vale para o desenvolvimento. Se o time subir um ambiente permanente, troque pelo domínio fixo do frontend.
- **Validado em execução real:** o fluxo acima (túneis, `.env` com as URLs públicas, subida do stack, smoke test e login de aluno e professor pelo navegador) foi executado com sucesso. A combinação `--hostname` com URL pública e `--proxy-headers=xforwarded` atrás do `cloudflared` funciona; sem `webOrigins` liberado no realm o login pelo túnel falha.
- **O realm só é importado com o volume do Keycloak vazio:** alterar `keycloak/realm-import.json` (por exemplo, o `webOrigins`) não tem efeito sobre um volume já existente. Depois de qualquer mudança no realm, rode `docker compose -f docker-compose.prod.yml down -v` e suba de novo.
- **Este staging não usa as imagens do GHCR:** o `docker-compose.prod.yml` constrói backend e frontend na própria máquina (`--build`), e não usa as imagens publicadas pelo `docker-build.yml` (#219). As URLs do túnel mudam a cada execução e o frontend embute `VITE_API_URL` e `VITE_KEYCLOAK_URL` no build, então a imagem do GHCR (construída com `STAGING_API_URL` e `STAGING_KEYCLOAK_URL`) só serve para um staging com URL fixa.
- As URLs de túnel mudam a cada execução. Ao reiniciar, repita os passos 2 a 4.
- O ambiente só existe enquanto a máquina e os túneis estiverem no ar.
- Os dados do MySQL e do Keycloak persistem em volumes Docker. Para zerar, use `docker compose -f docker-compose.prod.yml down -v`.