# Guia de Deploy e Staging - SIGTCC

Este documento estabelece o passo a passo completo para qualquer integrante da equipe configurar e executar o **ambiente local de desenvolvimento** e o **ambiente de _staging_** (validação externa de entregas) do SIGTCC, além do procedimento de _rollback_ e limitações conhecidas.

---

## 1. Pré-requisitos

Certifique-se de ter instalado na máquina:

- **Git**
- **Docker** (24+) e **Docker Compose** (v2)
- **Node.js** (versão 20+) e **npm** _(opcional, apenas caso deseje rodar o frontend fora do Docker)_
- **JDK 21** _(opcional, apenas caso deseje rodar o backend fora do Docker)_
- **Cloudflared** ([Cloudflare Tunnel client](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/)) _(necessário apenas para expor o ambiente de staging externamente)_

### 1.1. Configuração das Variáveis de Ambiente

Na raiz do repositório existe o arquivo oficial consolidado `.env.example`, que também é espelhado identicamente em `SIGTCC/.env.example` para conveniência. Ambos contêm todas as variáveis utilizadas por `docker-compose.yml` e `docker-compose.prod.yml`. A fonte canônica do projeto é o `.env.example` da raiz.

Antes de iniciar qualquer ambiente, gere o seu arquivo `.env` dentro da pasta `SIGTCC/` (onde ficam os arquivos do Docker Compose):

```bash
cp .env.example SIGTCC/.env
```

> [!IMPORTANT]
> **Atenção para quem já executava o projeto localmente:** Os volumes criados anteriormente (`db_data` / `db_data_dev`) guardam a senha definida na primeira inicialização do contêiner MySQL (`tcc_password`). Caso você altere as senhas no `.env`, o backend falhará na autenticação com o banco existente. Se isso ocorrer, apague o volume com `docker compose down -v` ou mantenha as credenciais já configuradas no seu `.env` anterior. O arquivo `.env` é ignorado pelo Git e nunca deve ser commitado.

### 1.2. Referência de Variáveis de Ambiente

| Variável                           | Descrição                                                                                                                                            | Exemplo Seguro (Local)                                                                         |
| :--------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------- |
| `MYSQL_DATABASE`                   | Nome do banco de dados criado na inicialização do contêiner MySQL                                                                                    | `gestao_tcc`                                                                                   |
| `MYSQL_USER`                       | Usuário da aplicação no MySQL                                                                                                                        | `tcc_user`                                                                                     |
| `MYSQL_PASSWORD`                   | Senha do usuário da aplicação no MySQL                                                                                                               | `tcc_password`                                                                                 |
| `MYSQL_ROOT_PASSWORD`              | Senha do usuário `root` do MySQL                                                                                                                     | `root_password`                                                                                |
| `SPRING_DATASOURCE_URL`            | URL JDBC de conexão do backend com o contêiner `db`                                                                                                  | `jdbc:mysql://db:3306/gestao_tcc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME`       | Usuário JDBC utilizado pelo Spring Boot (deve coincidir com `MYSQL_USER`)                                                                            | `tcc_user`                                                                                     |
| `SPRING_DATASOURCE_PASSWORD`       | Senha JDBC utilizada pelo Spring Boot (deve coincidir com `MYSQL_PASSWORD`)                                                                          | `tcc_password`                                                                                 |
| `KEYCLOAK_ADMIN_USERNAME`          | Usuário administrador inicial do console do Keycloak                                                                                                 | `admin`                                                                                        |
| `KEYCLOAK_ADMIN_PASSWORD`          | Senha do administrador inicial do console do Keycloak                                                                                                | `admin_password`                                                                               |
| `VITE_KEYCLOAK_URL`                | URL pública/acessível do Keycloak consumida pelo navegador no frontend                                                                               | `http://localhost:8081`                                                                        |
| `VITE_KEYCLOAK_REALM`              | Nome do Realm do Keycloak. Utilizado no dev local nativo (Vite); no contêiner de staging/produção utiliza o fallback do código (`sigtcc`)            | `sigtcc`                                                                                       |
| `VITE_KEYCLOAK_CLIENT_ID`          | Client ID público para a SPA React. Utilizado no dev local nativo; no contêiner de staging/produção utiliza o fallback do código (`sigtcc-frontend`) | `sigtcc-frontend`                                                                              |
| `APP_SUBMISSAO_DOMINIOS_SUGERIDOS` | Lista de domínios sugeridos como dica de UX na submissão por link (US06)                                                                             | `drive.google.com,github.com,onedrive.live.com`                                                |
| `VITE_API_URL`                     | URL base da API do backend injetada no build do frontend                                                                                             | `http://localhost:8080`                                                                        |
| `KEYCLOAK_PUBLIC_URL`              | URL pública base do Keycloak no staging/produção para o issuer JWT e `--hostname`                                                                    | `http://localhost:8081`                                                                        |
| `APP_CORS_ALLOWED_ORIGINS`         | Origens permitidas no CORS do backend Spring Boot (separadas por vírgula)                                                                            | `http://localhost`                                                                             |

---

## 2. Ambiente Local (Desenvolvimento)

No ambiente de desenvolvimento (`SIGTCC/docker-compose.yml`), os serviços rodam nas seguintes portas locais:

- **Frontend (Vite Dev Server):** `http://localhost:5173`
- **Backend (API Spring Boot):** `http://localhost:8080`
- **Keycloak (IAM Console):** `http://localhost:8081`
- **MySQL:** `localhost:3306`

### Opção A: Subir todo o stack via Docker (Recomendado)

O `docker-compose.yml` já contempla todos os 4 serviços (`db`, `keycloak`, `backend` e `frontend`) com _hot-reload_ via volumes:

1. Acesse a pasta `SIGTCC` e inicie os contêineres:

```bash
cd SIGTCC
docker compose --env-file .env up -d
```

2. Acompanhe o status até que todos os serviços estejam saudáveis (`healthy`):

```bash
docker compose ps
```

### Opção B: Infraestrutura no Docker + Frontend nativo no host

Caso prefira rodar o Vite diretamente no seu terminal para depuração rápida:

1. Suba apenas banco, Keycloak e backend via Docker:

```bash
cd SIGTCC
docker compose --env-file .env up -d db keycloak backend
```

2. Em outro terminal, inicialize o frontend localmente:

```bash
cd SIGTCC/frontend
npm install
npm run dev
```

### Credenciais de Teste (Realm `sigtcc`)

O Keycloak importa automaticamente o arquivo `SIGTCC/keycloak/realm-import.json` na primeira execução com os seguintes usuários de teste:

| E-mail                              | Senha          | Papel / Perfil                                 |
| :---------------------------------- | :------------- | :--------------------------------------------- |
| `aluno@example.com`                 | `aluno123`     | Aluno (`ROLE_ALUNO`)                           |
| `professor.orientador@example.com`  | `professor123` | Professor orientador (`ROLE_PROFESSOR`)        |
| `professor.responsavel@example.com` | `professor123` | Professor responsável pela `Turma_TCC` vigente |

---

## 3. Ambiente de Staging (Validação Externa via Cloudflare)

O ambiente de _staging_ utiliza o arquivo `SIGTCC/docker-compose.prod.yml` (com builds multi-stage otimizados: JRE 21 para o backend e Nginx na porta `80` para o frontend) aliado ao **Cloudflare Quick Tunnels** (`trycloudflare.com`) para expor a aplicação temporariamente à equipe durante validações de entrega.

> Para instruções operacionais adicionais, comandos complementares e histórico de validação da Sprint 4 (Issue #220), consulte também [`docs/staging.md`](file:///docs/staging.md).

> **Nota sobre CI/CD (GHCR):** A cada push aprovado na branch `entrega-8`, o workflow `.github/workflows/docker-build.yml` também compila e publica automaticamente as imagens `ghcr.io/ifsc-es2/sigtcc-backend` e `ghcr.io/ifsc-es2/sigtcc-frontend` (tags `latest` e `<git-sha>`) no GitHub Container Registry. O staging com túnel dinâmico não utiliza essas imagens diretamente porque o frontend precisa embutir as URLs efêmeras geradas a cada execução.

### Passo a Passo para Subir o Staging

1. **Encerre o ambiente de desenvolvimento local** (caso esteja rodando) para liberar as portas `8080` (API) e `8081` (Keycloak), e a porta `80` ou `5173` do Frontend:

```bash
cd SIGTCC
docker compose down
```

2. **Abra três terminais separados e inicie os túneis do Cloudflare** para o Backend (`8080`), Keycloak (`8081`) e Frontend (`80`). O `cloudflared` gera as URLs públicas `https://<subdominio>.trycloudflare.com` imediatamente:

```bash
# Terminal 1 - Túnel da API (Backend)
cloudflared tunnel --url http://localhost:8080

# Terminal 2 - Túnel do Keycloak (IAM)
cloudflared tunnel --url http://localhost:8081

# Terminal 3 - Túnel do Frontend (Nginx)
cloudflared tunnel --url http://localhost:80
```

3. **Atualize o arquivo `SIGTCC/.env`** com as 3 URLs HTTPS geradas no passo anterior:

```dotenv
VITE_API_URL=https://<url-gerada-para-8080>.trycloudflare.com
VITE_KEYCLOAK_URL=https://<url-gerada-para-8081>.trycloudflare.com
KEYCLOAK_PUBLIC_URL=https://<url-gerada-para-8081>.trycloudflare.com
APP_CORS_ALLOWED_ORIGINS=https://<url-gerada-para-80>.trycloudflare.com
```

4. **Construa e suba o stack de produção/staging com banco zerado:**

> O script de smoke test do passo 6 cria entidades no banco e não é repetível sobre o mesmo banco sem falhar em conflitos. Sempre zere o banco com `-v` antes de subir:

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env down -v
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

5. **Verificação de Web Origins no Keycloak (se necessário):**
   - O arquivo `SIGTCC/keycloak/realm-import.json` já vem configurado com `webOrigins: ["*"]` para staging, de modo que o client `sigtcc-frontend` já aceita requisições da URL do túnel automaticamente em volumes novos.
   - Essa intervenção manual só é necessária caso exista um volume persistido antigo do Keycloak que não tenha sido recriado com `docker compose down -v`. Nesse cenário:
     - Acesse o console administrativo do Keycloak em `http://localhost:8081` (ou pela URL do túnel 2) com as credenciais definidas em `KEYCLOAK_ADMIN_USERNAME` e `KEYCLOAK_ADMIN_PASSWORD`.
     - Selecione o realm **`sigtcc`** -> **Clients** -> **`sigtcc-frontend`**.
     - Em **Web origins**, adicione a URL pública do frontend (`https://<url-gerada-para-80>.trycloudflare.com`) ou `*`, e clique em **Save**.

6. **Validação (Smoke Test) e Compartilhamento:**
   - Execute o comando oficial do smoke test automatizado (criado na Issue #220) a partir da raiz do repositório para validação da API e autenticação em staging:
     ```bash
     KEYCLOAK_URL=<url-do-keycloak> API_URL=<url-do-backend> bash SIGTCC/scripts/smoke-test.sh
     ```
     _(Exemplo: `KEYCLOAK_URL=https://<url-gerada-para-8081>.trycloudflare.com API_URL=https://<url-gerada-para-8080>.trycloudflare.com bash SIGTCC/scripts/smoke-test.sh`)_
   - O script valida a autenticação com os três papéis, criação e aceite de solicitação, prazos, submissão por link HTTPS, avaliação e retorno de erros (401, 403, 409, 422).
   - Em seguida, acesse a URL pública do Frontend (`https://<url-gerada-para-80>.trycloudflare.com`), realize login com um usuário de teste (ex: `aluno@example.com` / `aluno123`) e compartilhe o link com a equipe.

---

## 4. Estratégia de Rollback

Em caso de falha crítica durante um deploy ou atualização no ambiente de _staging_, siga o procedimento abaixo para retornar rapidamente à última versão estável:

1. **Derrubar os contêineres e zerar os volumes da versão com falha:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env down -v
```

> [!WARNING]
> **Incompatibilidade de Migrações (Flyway):** Reverter o código para um commit anterior que possua menos migrações de banco **sempre** exige o uso de `down -v`, pois o Flyway valida a integridade do histórico (`flyway_schema_history`) e não realiza migrações retroativas (não há rollback automático de schema no banco de dados). Versões legadas (como a tag `v0.3.0` da Sprint 3) também não contam com o serviço Keycloak nem com as tabelas de entregas da Sprint 4.

2. **Reverter o código para o commit estável anterior:**

```bash
git checkout <sha-estavel-anterior>
# Exemplo: git checkout 687f2cb (ou para a tag estável v0.4.0)
```

3. **Reconstruir as imagens e subir o ambiente estabilizado:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

> **Sobre as Imagens do GHCR no Rollback:** O arquivo `SIGTCC/docker-compose.prod.yml` usa a diretiva `build:` para injetar as URLs dinâmicas dos túneis Cloudflare no frontend. Para utilizar diretamente as imagens estáveis do GitHub Container Registry (`ghcr.io/ifsc-es2/sigtcc-backend:<sha>` e `ghcr.io/ifsc-es2/sigtcc-frontend:<sha>`), seria necessário alterar o compose substituindo `build:` por `image: ghcr.io/...`, o que só é aplicável caso o staging utilize URLs estáticas pré-determinadas. Por essa razão, no fluxo padrão de staging o rollback é realizado via `git checkout <sha>` seguido de build local com `docker compose up -d --build`.

---

## 5. Limitações Conhecidas (Staging)

- **URLs Efêmeras do Cloudflare Quick Tunnels:** As URLs gratuitas geradas pelo `trycloudflare.com` mudam a cada reinício do processo `cloudflared`. Como o Vite embute `VITE_API_URL` e `VITE_KEYCLOAK_URL` estaticamente durante o `npm run build` no `Dockerfile` do frontend, sempre que um túnel da API ou do Keycloak for reiniciado é obrigatório atualizar o `.env` e reexecutar `docker compose -f docker-compose.prod.yml --env-file .env up -d --build frontend`.
- **Web Origins do Keycloak:** O arquivo `SIGTCC/keycloak/realm-import.json` vem configurado com `webOrigins: ["*"]` para staging. Essa intervenção manual só é necessária se existir um volume legado persistido do Keycloak sem ter executado `docker compose down -v`. Em produção final, o curinga `*` deve ser substituído estritamente pelo domínio corporativo homologado.
- **Importação Inicial do Realm (`realm-import.json`):** O Keycloak só executa o `--import-realm` quando o volume `keycloak_data` está vazio. Caso o arquivo `realm-import.json` seja atualizado no repositório, é necessário remover o volume do Keycloak (`docker compose down -v` ou remoção específica do volume) ou aplicar a alteração pelo console administrativo.
- **Este staging não usa as imagens do GHCR:** O `docker-compose.prod.yml` constrói backend e frontend na própria máquina (`--build`), pois as URLs do túnel mudam a cada execução. As imagens publicadas pelo workflow `docker-build.yml` destinam-se a ambientes com URLs e hostnames fixos.
- **Ausência de SLA no Túnel Gratuito:** Os Quick Tunnels do Cloudflare destinam-se exclusivamente a testes e homologação temporária, não possuindo garantia de disponibilidade contínua e estando sujeitos a limites de conexões simultâneas.
