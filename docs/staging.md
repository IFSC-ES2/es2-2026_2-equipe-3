# Ambiente de Staging Estável (Issues #220 e #260)

Staging roda o mesmo stack de produção (`SIGTCC/docker-compose.prod.yml`: MySQL, Keycloak, backend Spring Boot e frontend React) em uma máquina da equipe, exposto por URLs públicas e fixas através do **Tailscale Funnel**. O ambiente permanece no ar para avaliação contínua até a apresentação final (Entrega 10) e a defesa técnica (Entrega 11).

## 1. URLs Fixas do Ambiente

| Serviço             | URL Pública Fixa                               | Porta Local |
| :------------------ | :--------------------------------------------- | :---------- |
| **Frontend**        | `https://sigtcc.cobbler-albacore.ts.net`       | `80`        |
| **Backend (API)**   | `https://sigtcc.cobbler-albacore.ts.net:8443`  | `8080`      |
| **Keycloak (Auth)** | `https://sigtcc.cobbler-albacore.ts.net:10000` | `8081`      |

## 2. Segurança e Web Origins Restrito (Critério #260)

O client `sigtcc-frontend` em `SIGTCC/keycloak/realm-import.json` **não utiliza mais** o wildcard `*`. As origens de CORS (`webOrigins`) e URLs de redirecionamento estão estritamente restritas a:

- `https://sigtcc.cobbler-albacore.ts.net` (ambiente de staging oficial)
- `http://localhost:5173` e `http://localhost` (desenvolvimento local)

## 3. Responsável pela Disponibilidade

- **Responsável:** Eduardo Cardoso (Equipe 3)
- **Período de disponibilidade garantida:** Até a conclusão da Entrega 10 (Apresentação Final) e Entrega 11 (Defesa Técnica).

## 4. Procedimento de Configuração e Reinício

### Passo 1: Configurar as Variáveis de Ambiente (`.env`)

No diretório `SIGTCC/`, crie o arquivo `.env` a partir do modelo e configure as variáveis de staging com as URLs fixas:

```bash
cd SIGTCC
cp .env.example .env
```

Edite o `SIGTCC/.env` com as configurações do staging estável:

```dotenv
# Configurações de Staging (Tailscale Funnel - Issue #260)
VITE_API_URL=https://sigtcc.cobbler-albacore.ts.net:8443
VITE_KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000
KEYCLOAK_PUBLIC_URL=https://sigtcc.cobbler-albacore.ts.net:10000
APP_CORS_ALLOWED_ORIGINS=https://sigtcc.cobbler-albacore.ts.net
```

> **Aviso de Segurança:** Defina uma senha própria e segura em `KEYCLOAK_ADMIN_PASSWORD`. Como o console administrativo do Keycloak fica exposto publicamente na porta `10000`, nunca utilize senhas padrão de exemplo em ambientes acessíveis externamente. Caso o volume já tenha sido criado anteriormente, altere a senha diretamente pelo console (_Users → admin → Credentials_).

### Passo 2: Garantir os Túneis do Tailscale Funnel

Certifique-se de que o **Tailscale** e o **Docker Desktop** estão abertos e conectados.

Verifique o status do Funnel:

```bash
tailscale funnel status
```

Caso as rotas precisem ser reativadas em segundo plano:

```bash
tailscale funnel --bg --yes 80
tailscale funnel --bg --yes --https=8443 8080
tailscale funnel --bg --yes --https=10000 8081
```

### Passo 3: Checkout da Tag e Subida do Stack

Conforme o Critério 3 da Issue #260, o ambiente deve subir a partir da tag oficial de release:

```bash
git fetch --tags
git checkout v1.0.0-rc.1
```

Suba a aplicação com Docker Compose a partir de `SIGTCC/`:

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

_(Caso tenha havido alterações no `realm-import.json`, execute `docker compose -f docker-compose.prod.yml down -v` antes de subir para recriar o volume do Keycloak do zero)._

### Passo 4: Validação com Smoke Test (Atenção à Execução Única)

A partir da raiz do repositório (usando Git Bash no Windows ou terminal Linux/macOS):

```bash
KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 bash SIGTCC/scripts/smoke-test.sh
```

> **Importante:** O `smoke-test.sh` cadastra dados reais (uma solicitação aceita e um prazo de entrega) para validar o fluxo ponta a ponta. Portanto, ele só deve ser executado **uma única vez** logo após subir o ambiente com banco zerado (`down -v`), antes do cadastro dos dados oficiais de demonstração. Execuções repetidas sobre o mesmo banco retornarão conflito (HTTP 409) de dados já cadastrados.

## 5. Credenciais de Teste

Os usuários de teste pré-cadastrados no realm (`sigtcc`) para homologação, avaliação e demonstração são:

| Papel                     | E-mail / Usuário                    | Senha          | Finalidade de Teste                                           |
| :------------------------ | :---------------------------------- | :------------- | :------------------------------------------------------------ |
| **Aluno**                 | `aluno@example.com`                 | `aluno123`     | Solicitar orientação e submeter entrega por link HTTPS (US06) |
| **Professor Orientador**  | `professor.orientador@example.com`  | `professor123` | Aceitar solicitações e emitir parecer com avaliação (US07)    |
| **Professor Responsável** | `professor.responsavel@example.com` | `professor123` | Cadastrar prazos e gerenciar configurações da turma vigente   |

> **Console Administrativo do Keycloak:** O acesso ao console administrativo do Keycloak (`https://sigtcc.cobbler-albacore.ts.net:10000`) é de uso estritamente restrito do responsável pela infraestrutura do ambiente. As credenciais do administrador não são publicadas por motivos de segurança operacional.

## 6. Histórico de Execução e Validação

O fluxo completo de autenticação OAuth2/OIDC, autorização por papéis, controle de concorrência, submissão de entregas e emissão de pareceres foi homologado contra os endpoints HTTPS do Tailscale Funnel através do script `SIGTCC/scripts/smoke-test.sh`, totalizando **22 verificações bem-sucedidas** (3 autenticações de papéis de usuários + 19 verificações de regras de negócio e proteção de endpoints).

## 7. Limitações Conhecidas

- **Importação do Realm Condicionada a Volume Vazio:** O Keycloak só executa o `--import-realm` quando o volume `keycloak_data` está vazio. Qualquer modificação no arquivo `SIGTCC/keycloak/realm-import.json` exige recriar os volumes (`docker compose -f docker-compose.prod.yml down -v`).
- **Disponibilidade Dependente do Anfitrião:** O ambiente não roda em servidor em nuvem dedicado; depende de o computador do responsável permanecer ligado, sem suspensão de energia, com o Docker Desktop ativo e o cliente Tailscale conectado.
- **Associação de Domínio do Tailscale Funnel:** As URLs públicas fixas (`*.cobbler-albacore.ts.net`) estão vinculadas à conta do Tailscale do integrante responsável pela hospedagem.
