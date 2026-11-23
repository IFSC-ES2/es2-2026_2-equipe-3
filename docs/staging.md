# Ambiente de Staging Estável (Issues #220 e #260)

Staging roda o mesmo stack de produção (`SIGTCC/docker-compose.prod.yml`: MySQL, Keycloak, backend Spring Boot e frontend React) em uma **máquina virtual dedicada na Microsoft Azure**, exposto por URLs públicas e fixas através do **Tailscale Funnel**. O ambiente permanece no ar 24 horas por dia para avaliação contínua até a apresentação final (Entrega 10) e a defesa técnica (Entrega 11).

> **Migração em 20/11/2026:** o staging deixou de rodar na máquina pessoal de um integrante e passou a rodar na VM descrita na seção 3. As URLs públicas, o `realm-import.json` e a tag `v1.0.0` não mudaram, pois o novo host assumiu o mesmo nome (`sigtcc`) no _tailnet_.

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

## 3. Infraestrutura e Responsáveis

| Item                      | Valor                                                                                          |
| :------------------------ | :--------------------------------------------------------------------------------------------- |
| **Provedor**              | Microsoft Azure (assinatura _Azure for Students_, crédito de US$ 100)                          |
| **Região**                | Chile Central (`chilecentral`)                                                                 |
| **Máquina virtual**       | `sigtcc-vm`, tamanho `Standard_B2als_v2` (2 vCPUs, 4 GiB de RAM) + swap de 4 GiB               |
| **Sistema operacional**   | Ubuntu Server 24.04 LTS (x64), Docker Engine e Docker Compose v2                               |
| **Disco**                 | SSD Standard de 30 GiB                                                                         |
| **Acesso administrativo** | SSH (porta `22`) com chave privada, usuário `azureuser`                                        |
| **Exposição pública**     | Somente via Tailscale Funnel (portas `443`, `8443` e `10000`); demais portas bloqueadas no NSG |
| **Custo estimado**        | ~US$ 44/mês (VM + disco + IP público), coberto pelo crédito estudantil                         |

- **Responsável pela VM na Azure:** Talles Souza (Equipe 3)
- **Responsável pelo _tailnet_ e domínio `cobbler-albacore.ts.net`:** Eduardo Cardoso (Equipe 3)
- **Período de disponibilidade garantida:** Até a conclusão da Entrega 10 (Apresentação Final) e Entrega 11 (Defesa Técnica). O crédito estudantil cobre a VM até aproximadamente o início de fevereiro de 2027.

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

Conecte-se à VM por SSH (a chave privada fica com o responsável pela VM):

```powershell
ssh -i "$env:USERPROFILE\Downloads\sigtcc-vm_key.pem" azureuser@<IP_PUBLICO_DA_VM>
```

Na VM, o Docker Engine e o Tailscale iniciam automaticamente com o sistema, os contêineres usam `restart: always` e as rotas do Funnel criadas com `--bg` persistem após reinícios. Em operação normal, nenhum passo manual é necessário após um _reboot_.

Verifique o status do Funnel:

```bash
tailscale funnel status
```

Caso as rotas precisem ser reativadas em segundo plano:

```bash
sudo tailscale funnel --bg --yes 80
sudo tailscale funnel --bg --yes --https=8443 8080
sudo tailscale funnel --bg --yes --https=10000 8081
```

### Passo 3: Checkout da Tag e Subida do Stack

Conforme o Critério 3 da Issue #260, o ambiente deve subir a partir da tag oficial de release:

```bash
git fetch --tags
git checkout v1.0.0
```

Suba a aplicação com Docker Compose a partir de `SIGTCC/`:

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

_(Caso tenha havido alterações no `realm-import.json`, execute `docker compose -f docker-compose.prod.yml down -v` antes de subir para recriar o volume do Keycloak do zero)._

### Passo 4: Validação com Smoke Test (Atenção à Execução Única)

A partir da raiz do repositório na VM (`~/es2-2026_2-equipe-3`):

```bash
KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 bash SIGTCC/scripts/smoke-test.sh
```

> **Importante:** O `smoke-test.sh` cadastra dados reais (uma solicitação aceita e um prazo de entrega) para validar o fluxo ponta a ponta. Portanto, ele só deve ser executado **uma única vez** logo após subir o ambiente com banco zerado (`down -v`), antes do cadastro dos dados oficiais de demonstração. Execuções repetidas sobre o mesmo banco retornarão conflito (HTTP 409) de dados já cadastrados.

## 5. Credenciais de Teste e Cenário de Demonstração (Issues #280 e #286)

> **Roteiro Completo da Demonstração (`v1.0.0`):** O passo a passo completo dos 8 blocos da demonstração (usuário, rota, dados inseridos e resultado esperado em cada etapa), bem como as limitações residuais e o guia de defesa técnica, está documentado em **[`docs/ROTEIRO-DEMONSTRACAO.md`](ROTEIRO-DEMONSTRACAO.md)**.

Os usuários de teste pré-cadastrados no realm (`sigtcc`) para homologação, avaliação pelo professor e demonstração são:

| Papel                     | E-mail / Usuário                    | Senha          | Role no Keycloak | Finalidade de Teste e Vínculo Inicial                                                                         |
| :------------------------ | :---------------------------------- | :------------- | :--------------- | :------------------------------------------------------------------------------------------------------------ |
| **Aluno**                 | `aluno@example.com`                 | `aluno123`     | `ROLE_ALUNO`     | Consultar catálogo/prazos, solicitar orientação (US04) e submeter/reenviar entrega por link HTTPS (US06)      |
| **Professor Orientador**  | `professor.orientador@example.com`  | `professor123` | `ROLE_PROFESSOR` | Orientador ativo (`ID 1`, `3` vagas); aceitar/recusar solicitações (US04) e emitir parecer (US07)             |
| **Professor Responsável** | `professor.responsavel@example.com` | `professor123` | `ROLE_PROFESSOR` | Orientador ativo (`ID 2`, `3` vagas) e responsável pela `Turma_TCC` `2026/2` vigente; gerir calendário (US05) |

> **Console Administrativo do Keycloak:** O acesso ao console administrativo do Keycloak (`https://sigtcc.cobbler-albacore.ts.net:10000`) é de uso estritamente restrito do responsável pela infraestrutura do ambiente. As credenciais do administrador não são publicadas por motivos de segurança operacional.

### 5.1. Dados de Demonstração e Como Recriar o Cenário

Para preparar ou restaurar o cenário previsível de demonstração (orientadores com linhas de pesquisa e vagas, turma vigente `2026/2` e prazos `PROJETO_TCC`, `TCC1` e `TCC2` cadastrados), o repositório disponibiliza o script idempotente `SIGTCC/scripts/seed-demo.sh`:

- **No ambiente de _staging_:**

```bash
KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 \
API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 \
bash SIGTCC/scripts/seed-demo.sh
```

- **No ambiente local (`localhost`):**

```bash
bash SIGTCC/scripts/seed-demo.sh
```

O script configura e valida automaticamente (18 verificações):

1. **Orientadores ativos com linhas de pesquisa e vagas (`GET /api/v1/orientadores`):**
    - `Professor Orientador` (`professor.orientador@example.com`, depto `DAELN`, `3` vagas): linhas `Engenharia de Software`, `Arquitetura de Sistemas` e `Qualidade de Software`.
    - `Professor Responsável pela Turma` (`professor.responsavel@example.com`, depto `DAELN`, `3` vagas): linhas `Inteligência Artificial`, `Ciência de Dados` e `Engenharia de Software`.
2. **Turma vigente (`2026/2`):** confirmada com `responsavelTurma: true` para `professor.responsavel@example.com` (`GET /api/v1/professores/me`).
3. **Prazos da turma vigente (`GET /api/v1/prazos`):**
    - `PROJETO_TCC`: data limite em `+30 dias`
    - `TCC1`: data limite em `+60 dias`
    - `TCC2`: data limite em `+90 dias`

Caso seja necessário **zerar completamente o banco de _staging_** (por exemplo, após a execução do `smoke-test.sh` ou de testes exploratórios) para deixar o ambiente limpo para a demonstração final:

```bash
cd SIGTCC
docker compose -f docker-compose.prod.yml --env-file .env down -v
docker compose -f docker-compose.prod.yml --env-file .env up -d
cd ..
KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 \
API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 \
bash SIGTCC/scripts/seed-demo.sh
```

## 6. Histórico de Execução e Validação

- **19/11/2026 — Preparação para a Apresentação Final (`v1.0.0`):**
    - **Smoke test (`SIGTCC/scripts/smoke-test.sh`):** Executado contra os endpoints HTTPS do Tailscale Funnel (`https://sigtcc.cobbler-albacore.ts.net:8443` e `:10000`) com banco recém-criado, totalizando **22 verificações bem-sucedidas** (3 autenticações de papéis de usuários + 19 verificações de regras de negócio, concorrência e proteção de endpoints).
    - **Cenário limpo de demonstração (`SIGTCC/scripts/seed-demo.sh`):** Após novo reset completo dos volumes (`down -v && up -d`), o script foi executado sem `--validar-roteiro`, concluindo **18 de 18 verificações com sucesso** (login dos 3 perfis no Keycloak, 2 orientadores configurados com 3 vagas disponíveis, turma vigente `2026/2` e prazos `PROJETO_TCC`, `TCC1` e `TCC2` cadastrados, sem solicitações ou entregas prévias do aluno de teste).

- **20/11/2026 — Migração para a VM na Azure (`v1.0.0`):**
    - Stack construído na VM `sigtcc-vm` a partir da tag `v1.0.0`; o Flyway aplicou as migrations `V1` a `V9` e `db` e `keycloak` ficaram `healthy`.
    - **Smoke test (`SIGTCC/scripts/smoke-test.sh`):** executado contra as mesmas URLs HTTPS do Tailscale Funnel, agora servidas pela VM, com **22 verificações bem-sucedidas**.

## 7. Limitações Conhecidas

- **Importação do Realm Condicionada a Volume Vazio:** O Keycloak só executa o `--import-realm` quando o volume `keycloak_data` está vazio. Qualquer modificação no arquivo `SIGTCC/keycloak/realm-import.json` exige recriar os volumes (`docker compose -f docker-compose.prod.yml down -v`).
- **Disponibilidade Dependente do Crédito Estudantil:** A VM é paga com o crédito de US$ 100 do _Azure for Students_ (~US$ 44/mês). Quando o crédito acabar (previsão: início de fevereiro de 2027), a assinatura é desativada e o ambiente sai do ar, sem cobrança adicional. O saldo é acompanhado pelo _Education Hub_ e por um alerta de orçamento no _Cost Management_.
- **Restrição de Regiões da Assinatura Estudantil:** A política `Allowed resource deployment regions` da assinatura só permite criar recursos em `francecentral`, `chilecentral`, `southafricanorth`, `northcentralus` e `canadacentral`. Recriar a VM exige escolher uma dessas regiões.
- **Memória Limitada:** Com 4 GiB de RAM, o build das imagens depende do swap de 4 GiB configurado na VM; rebuilds completos (`--build`) levam de 5 a 10 minutos.
- **Associação de Domínio do Tailscale Funnel:** As URLs públicas fixas (`*.cobbler-albacore.ts.net`) estão vinculadas à conta do Tailscale do integrante responsável pelo _tailnet_. A VM precisa permanecer registrada nesse _tailnet_ com o nome `sigtcc`.
