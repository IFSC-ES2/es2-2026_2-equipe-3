# Guia de Deploy e Staging - SIGTCC

Este documento estabelece o passo a passo completo para qualquer integrante da equipe configurar e executar o **ambiente local de desenvolvimento** e o **ambiente de _staging_** (validação externa de entregas) do SIGTCC, além do procedimento de _rollback_ e limitações conhecidas.

---

## 1. Pré-requisitos

Certifique-se de ter instalado na máquina:

- **Git**
- **Docker** (24+) e **Docker Compose** (v2)
- **Node.js** (versão 20+) e **npm** _(opcional, apenas caso deseje rodar o frontend fora do Docker)_
- **JDK 21** _(opcional, apenas caso deseje rodar o backend fora do Docker)_
- **Tailscale** ([cliente Tailscale](https://tailscale.com/download)) com o **Funnel** habilitado no _tailnet_ _(necessário apenas na VM de staging na Azure; ver seção 3.2)_

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

**Onde cada variável é usada:**

- **`docker-compose.yml` (desenvolvimento):** `MYSQL_*`, `SPRING_DATASOURCE_*`, `KEYCLOAK_ADMIN_*` e `APP_SUBMISSAO_DOMINIOS_SUGERIDOS`. As URLs do frontend (`VITE_*`) já estão fixas em `localhost` no próprio compose.
- **`docker-compose.prod.yml` (_staging_):** as mesmas variáveis, mais `VITE_API_URL` e `VITE_KEYCLOAK_URL` (injetadas no build do frontend), `KEYCLOAK_PUBLIC_URL` (hostname do Keycloak e _issuer_ do JWT validado pelo backend) e `APP_CORS_ALLOWED_ORIGINS`.
- **`VITE_KEYCLOAK_REALM` e `VITE_KEYCLOAK_CLIENT_ID`:** não são lidas pelos arquivos do Compose; o frontend usa os valores padrão do código (`sigtcc` e `sigtcc-frontend`). Servem apenas para o `npm run dev` executado fora do Docker.

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

## 3. Ambiente de Staging (Validação Externa via Tailscale Funnel)

O ambiente de _staging_ utiliza o arquivo `SIGTCC/docker-compose.prod.yml` (com builds multi-stage otimizados: JRE 21 para o backend e Nginx na porta `80` para o frontend) aliado ao **Tailscale Funnel** para expor a aplicação por **URLs públicas e fixas com HTTPS** durante as avaliações da equipe e banca (conforme a Issue #260). O stack roda 24 horas por dia em uma VM dedicada na Microsoft Azure (seção 3.2).

> **Documento Oficial de Staging:** Para o passo a passo operacional completo, credenciais de teste, procedimento de reinício e histórico de homologação, consulte **[`docs/staging.md`](staging.md)**.

### 3.1. URLs Fixas do Ambiente

| Serviço             | URL Pública Fixa                               | Porta Local |
| :------------------ | :--------------------------------------------- | :---------- |
| **Frontend**        | `https://sigtcc.cobbler-albacore.ts.net`       | `80`        |
| **Backend (API)**   | `https://sigtcc.cobbler-albacore.ts.net:8443`  | `8080`      |
| **Keycloak (Auth)** | `https://sigtcc.cobbler-albacore.ts.net:10000` | `8081`      |

### 3.2. Provisionamento da VM na Azure (Host do Staging)

Desde 20/11/2026, o _staging_ roda em uma máquina virtual dedicada na Microsoft Azure (assinatura _Azure for Students_), e não mais na máquina de um integrante. Os passos abaixo recriam o host do zero; a configuração atual está registrada na seção 3 de [`docs/staging.md`](staging.md).

1. **Ativar o _Azure for Students_** em `azure.microsoft.com/free/students` com o e-mail institucional (`@aluno.ifsc.edu.br`). Não é exigido cartão de crédito; o crédito é de US$ 100.
2. **Conferir as regiões permitidas:** no portal, _Política → Atribuições → Allowed resource deployment regions → Parâmetros_. Na assinatura atual: `francecentral`, `chilecentral`, `southafricanorth`, `northcentralus` e `canadacentral`. Criar recursos fora dessa lista falha com `RequestDisallowedByAzure`.
3. **Criar a VM** em _Máquinas virtuais → Criar → Máquina virtual do Azure_ (não usar a oferta _Free account virtual machine_, limitada a 1 GiB de RAM):

| Campo                      | Valor                                                      |
| :------------------------- | :--------------------------------------------------------- |
| Grupo de recursos          | `sigtcc-rg`                                                |
| Nome                       | `sigtcc-vm`                                                |
| Região                     | `Chile Central` (ou outra região permitida com o tamanho)  |
| Opções de disponibilidade  | Nenhuma redundância de infraestrutura necessária           |
| Imagem                     | Ubuntu Server 24.04 LTS – x64 Gen2                         |
| Tamanho                    | `Standard_B2als_v2` (2 vCPUs, 4 GiB)                       |
| Autenticação               | Chave pública SSH, usuário `azureuser`, novo par de chaves |
| Portas de entrada públicas | Somente SSH (`22`)                                         |
| Disco do SO                | SSD Standard                                               |
| Desligamento automático    | **Desabilitado**                                           |

4. **Conectar por SSH** a partir do Windows (PowerShell), após restringir a permissão da chave baixada:

```powershell
icacls "$env:USERPROFILE\Downloads\sigtcc-vm_key.pem" /inheritance:r /grant:r "$($env:USERNAME):R"
ssh -i "$env:USERPROFILE\Downloads\sigtcc-vm_key.pem" azureuser@<IP_PUBLICO_DA_VM>
```

5. **Preparar a VM** (swap de 4 GiB, necessário para o build com 4 GiB de RAM, e Docker Engine). Caso o espelho `azure.archive.ubuntu.com` fique indisponível, troque-o pelo `archive.ubuntu.com`:

```bash
sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
sudo sed -i 's|http://azure.archive.ubuntu.com/ubuntu|http://archive.ubuntu.com/ubuntu|g' /etc/apt/sources.list.d/ubuntu.sources
sudo apt update && sudo apt upgrade -y
sudo apt install -y git curl jq
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker azureuser
sudo systemctl enable docker
```

6. **Registrar a VM no _tailnet_** do responsável pelo domínio `cobbler-albacore.ts.net` (o login pelo link exibido é feito com a conta dele):

```bash
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up --hostname=sigtcc-azure
```

No painel do Tailscale (_Machines_), remova o host anterior chamado `sigtcc` e renomeie `sigtcc-azure` para `sigtcc`. Assim as URLs da seção 3.1 passam a apontar para a VM sem nenhuma alteração no `.env` ou no `realm-import.json`.

7. **Clonar o repositório** e seguir a seção 3.3 a partir do passo 1:

```bash
git clone https://github.com/IFSC-ES2/es2-2026_2-equipe-3.git
cd es2-2026_2-equipe-3
```

### 3.3. Passo a Passo para Subir o Staging Fixo

1. **Criar o `SIGTCC/.env` a partir do `.env.example`** e substituir o bloco de _staging_ pelas URLs fixas. As demais variáveis (banco, Keycloak e domínios sugeridos) seguem a tabela da seção 1.2; no _staging_, troque as senhas de exemplo por valores próprios.

```dotenv
# --- Configurações de Staging (Tailscale Funnel - Issue #260) ---
VITE_API_URL=https://sigtcc.cobbler-albacore.ts.net:8443
VITE_KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000
KEYCLOAK_PUBLIC_URL=https://sigtcc.cobbler-albacore.ts.net:10000
APP_CORS_ALLOWED_ORIGINS=https://sigtcc.cobbler-albacore.ts.net
```

> As variáveis `VITE_*` são gravadas no _bundle_ durante o build do frontend. Se forem alteradas, é preciso reconstruir a imagem (`--build`).

2. **Ativar os túneis do Tailscale Funnel** para as três portas locais e conferir o mapeamento:

```bash
sudo tailscale funnel --bg --yes 80
sudo tailscale funnel --bg --yes --https=8443 8080
sudo tailscale funnel --bg --yes --https=10000 8081
tailscale funnel status
```

O `status` deve listar as três URLs da seção 3.1 apontando para `localhost:80`, `localhost:8080` e `localhost:8081`.

3. **Subir o stack na tag oficial (`v1.0.0`) com o banco zerado:**

```bash
git fetch --tags
git checkout v1.0.0
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env down -v
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml --env-file .env ps
```

Aguarde até que `db` e `keycloak` apareçam como `healthy` e `backend` e `frontend` como `Up` (estes dois não possuem _healthcheck_ no Compose). No primeiro start, o backend aplica as migrations `V1` a `V9` do Flyway e o Keycloak importa o realm `sigtcc`.

4. **Validar com o Smoke Test Automatizado** (a partir da raiz do repositório):

```bash
KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 bash SIGTCC/scripts/smoke-test.sh
```

> **Atenção:** O `smoke-test.sh` cadastra dados reais (uma solicitação aceita e um prazo de entrega) e não deve ser executado sobre um banco já populado com dados de demonstração. Execute-o uma única vez, logo após o `down -v` do passo 3.

5. **Preparar os dados de demonstração** conforme [`docs/staging.md`](staging.md) (issue [#280](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/280)). Para reiniciar o ambiente sem perder os dados (por exemplo, após reiniciar a VM), use o procedimento de reinício descrito no mesmo documento, sem `down -v`.

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
git checkout <tag-ou-sha-estavel-anterior>
# Exemplo: git checkout v1.0.0-rc.1 (release anterior à v1.0.0)
```

3. **Reconstruir as imagens e subir o ambiente estabilizado:**

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

4. **Revalidar o ambiente** com o _smoke test_ (seção 3.3, passo 4) e preparar novamente os dados de demonstração.

> **Sobre as Imagens do GHCR no Rollback:** O arquivo `SIGTCC/docker-compose.prod.yml` usa a diretiva `build:` porque as URLs do _staging_ (`VITE_API_URL` e `VITE_KEYCLOAK_URL`) são gravadas no _bundle_ do frontend durante o build. As imagens publicadas no GitHub Container Registry (`ghcr.io/ifsc-es2/sigtcc-backend:<sha>` e `ghcr.io/ifsc-es2/sigtcc-frontend:<sha>`) são geradas pelo CI sem essas URLs; usá-las exigiria trocar `build:` por `image: ghcr.io/...` e reconstruir o frontend com as URLs fixas. Por isso, no fluxo padrão de _staging_ o rollback é feito com `git checkout <tag>` seguido de `docker compose up -d --build`.

---

## 5. Limitações Conhecidas (Staging)

- **Importação Inicial do Realm (`realm-import.json`):** O Keycloak só executa o `--import-realm` quando o volume `keycloak_data` está vazio. Caso o arquivo `realm-import.json` seja atualizado no repositório, é necessário remover o volume do Keycloak (`docker compose -f docker-compose.prod.yml down -v`) ou aplicar a alteração pelo console administrativo.
- **Web Origins Restrito no Keycloak (Issue #260):** O arquivo `SIGTCC/keycloak/realm-import.json` não utiliza mais o curinga `*`, estando restrito estritamente a `https://sigtcc.cobbler-albacore.ts.net` e origens locais de desenvolvimento (`localhost`).
- **Disponibilidade Dependente do Crédito Estudantil:** O ambiente de staging roda em uma VM na Azure paga com o crédito de US$ 100 do _Azure for Students_ (~US$ 44/mês, previsão de término no início de fevereiro de 2027). Ao fim do crédito, a assinatura é desativada e o ambiente sai do ar, sem cobrança adicional. Docker, contêineres (`restart: always`) e túneis do Funnel (`--bg`) voltam automaticamente após reinícios da VM. Detalhes completos e procedimento de reinício em [`docs/staging.md`](staging.md).
- **Regiões Restritas da Assinatura Estudantil:** A VM só pode ser recriada nas regiões permitidas pela política da assinatura (seção 3.2, passo 2).
