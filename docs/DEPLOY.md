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

Na raiz do repositório existe o arquivo consolidado `.env.example` (também espelhado em `SIGTCC/.env.example`) com todas as variáveis utilizadas por `docker-compose.yml` e `docker-compose.prod.yml`.

Antes de iniciar qualquer ambiente, gere o seu arquivo `.env` dentro da pasta `SIGTCC/` (onde ficam os arquivos do Docker Compose):

```bash
cp .env.example SIGTCC/.env
```

_(Ajuste as senhas locais no arquivo `SIGTCC/.env` gerado, se desejar. O arquivo `.env` é ignorado pelo Git e nunca deve ser commitado)._

### 1.2. Referência de Variáveis de Ambiente

| Variável                           | Descrição                                                                       | Exemplo Seguro (Local)                                                                         |
| :--------------------------------- | :------------------------------------------------------------------------------ | :--------------------------------------------------------------------------------------------- |
| `MYSQL_DATABASE`                   | Nome do banco de dados criado na inicialização do contêiner MySQL               | `gestao_tcc`                                                                                   |
| `MYSQL_USER`                       | Usuário da aplicação no MySQL                                                   | `tcc_user`                                                                                     |
| `MYSQL_PASSWORD`                   | Senha do usuário da aplicação no MySQL                                          | `senha_exemplo_db`                                                                             |
| `MYSQL_ROOT_PASSWORD`              | Senha do usuário `root` do MySQL                                                | `senha_exemplo_root`                                                                           |
| `SPRING_DATASOURCE_URL`            | URL JDBC de conexão do backend com o contêiner `db`                             | `jdbc:mysql://db:3306/gestao_tcc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME`       | Usuário JDBC utilizado pelo Spring Boot (deve coincidir com `MYSQL_USER`)       | `tcc_user`                                                                                     |
| `SPRING_DATASOURCE_PASSWORD`       | Senha JDBC utilizada pelo Spring Boot (deve coincidir com `MYSQL_PASSWORD`)     | `senha_exemplo_db`                                                                             |
| `KEYCLOAK_ADMIN_USERNAME`          | Usuário administrador inicial do console do Keycloak                            | `admin`                                                                                        |
| `KEYCLOAK_ADMIN_PASSWORD`          | Senha do administrador inicial do console do Keycloak                           | `admin_senha`                                                                                  |
| `VITE_KEYCLOAK_URL`                | URL pública/acessível do Keycloak consumida pelo navegador no frontend          | `http://localhost:8081`                                                                        |
| `VITE_KEYCLOAK_REALM`              | Nome do Realm do Keycloak utilizado pelo SIGTCC                                 | `sigtcc`                                                                                       |
| `VITE_KEYCLOAK_CLIENT_ID`          | Client ID público configurado no Keycloak para a SPA React                      | `sigtcc-frontend`                                                                              |
| `APP_SUBMISSAO_DOMINIOS_SUGERIDOS` | Lista de domínios sugeridos como dica de UX na submissão por link (US06)        | `drive.google.com,github.com,onedrive.live.com`                                                |
| `VITE_API_URL`                     | URL base da API do backend injetada no build do frontend                        | `http://localhost:8080`                                                                        |
| `KEYCLOAK_HOSTNAME`                | Hostname ou URL pública configurada no Keycloak em modo produção (`--hostname`) | `localhost`                                                                                    |
| `APP_CORS_ALLOWED_ORIGINS`         | Origens permitidas no CORS do backend Spring Boot (separadas por vírgula)       | `http://localhost`                                                                             |

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

> **Nota sobre CI/CD (GHCR):** A cada push aprovado na branch `entrega-8`, o workflow `.github/workflows/docker-build.yml` também compila e publica automaticamente as imagens `ghcr.io/ifsc-es2/sigtcc-backend` e `ghcr.io/ifsc-es2/sigtcc-frontend` (tags `latest` e `<git-sha>`) no GitHub Container Registry.

### Passo a Passo para Subir o Staging

1. **Encerre o ambiente de desenvolvimento local** (caso esteja rodando) para liberar as portas `3306`, `8080` e `8081`:

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
KEYCLOAK_HOSTNAME=https://<url-gerada-para-8081>.trycloudflare.com
APP_CORS_ALLOWED_ORIGINS=https://<url-gerada-para-80>.trycloudflare.com
```

4. **Construa e suba o stack de produção/staging:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

5. **Libere a origem pública no Keycloak (Web Origins):**
   - Acesse o console administrativo do Keycloak em `http://localhost:8081` (ou pela URL do túnel 2) com as credenciais definidas em `KEYCLOAK_ADMIN_USERNAME` e `KEYCLOAK_ADMIN_PASSWORD`.
   - Selecione o realm **`sigtcc`** -> **Clients** -> **`sigtcc-frontend`**.
   - Em **Web origins**, adicione a URL pública do frontend (`https://<url-gerada-para-80>.trycloudflare.com`) ou `+` / `*` durante a sessão de validação, e clique em **Save**.

6. **Validação (Smoke Test) e Compartilhamento:**
   - Acesse a URL pública do Frontend (`https://<url-gerada-para-80>.trycloudflare.com`), realize login com um usuário de teste e compartilhe o link com a equipe.

---

## 4. Estratégia de Rollback

Em caso de falha crítica durante um deploy ou atualização no ambiente de _staging_, siga o procedimento abaixo para retornar rapidamente à última versão estável:

1. **Derrubar os contêineres da versão com falha:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env down
```

> **Atenção com Banco de Dados (Flyway):** O comando `down` preserva os volumes `db_data` e `keycloak_data`. Caso a versão defeituosa tenha executado uma _migration_ Flyway incompatível com a versão anterior no ambiente de testes/staging, utilize `docker compose -f docker-compose.prod.yml --env-file .env down -v` para recriar o banco limpo com as _migrations_ da versão estável.

2. **Reverter o código para a tag ou commit estável anterior:**

```bash
git checkout <tag-ou-commit-estavel>
# Exemplo: git checkout v0.3.0
```

3. **Reconstruir as imagens sem cache e subir o ambiente estabilizado:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env build --no-cache
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

_(Alternativamente, é possível referenciar diretamente a imagem estável anterior publicada no GHCR usando a tag do commit `ghcr.io/ifsc-es2/sigtcc-backend:<sha>` e `ghcr.io/ifsc-es2/sigtcc-frontend:<sha>`)._

---

## 5. Limitações Conhecidas (Staging)

- **URLs Efêmeras do Cloudflare Quick Tunnels:** As URLs gratuitas geradas pelo `trycloudflare.com` mudam a cada reinício do processo `cloudflared`. Como o Vite embute `VITE_API_URL` e `VITE_KEYCLOAK_URL` estaticamente durante o `npm run build` no `Dockerfile` do frontend, sempre que um túnel da API ou do Keycloak for reiniciado é obrigatório atualizar o `.env` e reexecutar `docker compose -f docker-compose.prod.yml --env-file .env up -d --build frontend`.
- **Web Origins do Keycloak:** O arquivo `SIGTCC/keycloak/realm-import.json` configura por padrão `webOrigins: ["http://localhost:5173", "http://localhost"]`. Para que o login via Direct Access Grant funcione a partir de um domínio externo (`*.trycloudflare.com`), é necessário autorizar a URL pública do frontend em **Web origins** do client `sigtcc-frontend` no painel do Keycloak (ou usar `["*"]` apenas durante sessões temporárias de validação; em produção real, deve-se restringir estritamente ao domínio oficial).
- **Importação Inicial do Realm (`realm-import.json`):** O Keycloak só executa o `--import-realm` quando o volume `keycloak_data` está vazio. Caso o arquivo `realm-import.json` seja atualizado no repositório, é necessário remover o volume do Keycloak (`docker compose down -v` ou remoção específica do volume) ou aplicar a alteração pelo console administrativo.
- **Ausência de SLA no Túnel Gratuito:** Os Quick Tunnels do Cloudflare destinam-se exclusivamente a testes e homologação temporária, não possuindo garantia de disponibilidade contínua e estando sujeitos a limites de conexões simultâneas.
