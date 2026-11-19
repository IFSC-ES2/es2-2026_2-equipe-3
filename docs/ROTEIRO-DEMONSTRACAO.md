# Roteiro da Demonstração Final do MVP — SIGTCC (`v1.0.0`)

- **Sistema:** Sistema de Gestão Centralizada de TCC (SIGTCC)
- **Versão Demonstrada (Tag Oficial):** [`v1.0.0`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v1.0.0) (Versão Final — Entrega 10)
- **Tempo Estimado da Demonstração:** 10 a 15 minutos
- **Rastreabilidade:** Issues [#280](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/280) e [#286](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/286)

---

## 1. Ambientes e Pré-condição Reprodutível

A demonstração da versão **`v1.0.0`** pode ser conduzida tanto no ambiente oficial de _staging_ (exposto com HTTPS fixo via Tailscale Funnel) quanto localmente via Docker Compose:

| Serviço             | Ambiente de Staging (`v1.0.0`)                 | Ambiente Local (`docker compose up`) |
| :------------------ | :--------------------------------------------- | :----------------------------------- |
| **Frontend (SPA)**  | `https://sigtcc.cobbler-albacore.ts.net`       | `http://localhost:5173`              |
| **Backend (API)**   | `https://sigtcc.cobbler-albacore.ts.net:8443`  | `http://localhost:8080`              |
| **Keycloak (Auth)** | `https://sigtcc.cobbler-albacore.ts.net:10000` | `http://localhost:8081`              |

### 1.1. Preparação / Recriação do Cenário Limpo antes da Demonstração

Para garantir que o banco de dados esteja recém-criado e populado com a massa de dados oficial da Issue [#280](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/280):

```bash
# 1. (Opcional) Zerar volumes para partir de um ambiente recém-criado
cd SIGTCC && docker compose down -v && docker compose up -d && cd ..

# 2. Popular linhas de pesquisa dos orientadores e prazos da turma vigente 2026/2
bash SIGTCC/scripts/seed-demo.sh
```

_(Para executar contra o ambiente de staging: `KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 bash SIGTCC/scripts/seed-demo.sh`)._

---

## 2. Usuários e Massa de Dados Inicial do Cenário

### 2.1. Credenciais dos Usuários por Papel (Realm `sigtcc`)

| Papel no Sistema                  | E-mail (Login)                      | Senha          | Role no Keycloak | Identificador e Papel no Banco                                                         |
| :-------------------------------- | :---------------------------------- | :------------- | :--------------- | :------------------------------------------------------------------------------------- |
| **Aluno**                         | `aluno@example.com`                 | `aluno123`     | `ROLE_ALUNO`     | Criado via _upsert_ pelo e-mail do JWT ao enviar a primeira solicitação (US04).        |
| **Professor Orientador**          | `professor.orientador@example.com`  | `professor123` | `ROLE_PROFESSOR` | Orientador `ID 1` (`DAELN`, ativo, `3` vagas disponíveis).                             |
| **Professor Responsável (Turma)** | `professor.responsavel@example.com` | `professor123` | `ROLE_PROFESSOR` | Orientador `ID 2` (`DAELN`, ativo, `3` vagas) e responsável pela `Turma_TCC` `2026/2`. |

### 2.2. Estado Inicial Preparado por `seed-demo.sh`

- **Orientador 1 (`Professor Orientador`):** `3` vagas disponíveis; linhas de pesquisa: `Engenharia de Software`, `Arquitetura de Sistemas` e `Qualidade de Software`.
- **Orientador 2 (`Professor Responsável pela Turma`):** `3` vagas disponíveis; linhas de pesquisa: `Inteligência Artificial`, `Ciência de Dados` e `Engenharia de Software`.
- **Turma Vigente:** `2026/2` (`vigente = true`, `professor_responsavel_id = 2`).
- **Prazos Vigentes Cadastrados:**
  - `PROJETO_TCC`: `+30 dias` a partir da data de execução;
  - `TCC1`: `+60 dias` a partir da data de execução;
  - `TCC2`: `+90 dias` a partir da data de execução.

---

## 3. Roteiro Passo a Passo da Demonstração (Fluxo Ponta a Ponta)

O roteiro abaixo percorre sequencialmente as histórias **US01 a US07** sobre o ambiente recém-criado:

### Passo 1 — Login e Controle de Acesso por Papel (US01)

- **Usuário:** `aluno@example.com` / `aluno123` (`ROLE_ALUNO`)
- **Rota / Tela:** `/login` → redirecionamento automático para `/orientadores`
- **Dados e Ações:**
  1. Tentar acessar uma rota protegida (ex.: `/minhas-entregas`) sem sessão ativa: o componente `ProtectedRoute` redireciona para `/login`.
  2. Informar `aluno@example.com` com senha incorreta (`errada123`): exibir a mensagem de erro na própria tela ([ADR-0014](adrs/ADR-0014.md)).
  3. Autenticar com `aluno@example.com` e senha `aluno123`.
  4. Demonstrar que a barra de navegação exibe apenas ações de aluno (`Catálogo de Orientadores`, `Minhas Entregas`) e que a tentativa de abrir `/gestao-calendario` ou `/painel-solicitacoes` exibe Acesso Negado (`403`).

### Passo 2 — Catálogo de Orientadores e Busca Combinada (US02 e US03)

- **Usuário:** `aluno@example.com` (`ROLE_ALUNO`)
- **Rota / Tela:** `/orientadores` (`GET /api/v1/orientadores?area=...&temVagas=...`)
- **Dados e Ações:**
  1. Visualizar a vitrine inicial com os dois orientadores ativos (`Professor Orientador` e `Professor Responsável pela Turma`, ambos com `3` vagas disponíveis) e o componente de contagem regressiva dos prazos vigentes (`PROJETO_TCC`, `TCC1`, `TCC2`).
  2. Filtrar por linha de pesquisa **`Arquitetura de Sistemas`** e marcar **"Somente com vagas disponíveis"**: apenas `Professor Orientador` (`ID 1`) é listado (comparação exata case-insensitive corrigida na `#264`).
  3. Filtrar por **`Inteligência Artificial`**: apenas `Professor Responsável pela Turma` (`ID 2`) é listado.
  4. Filtrar por **`Computação Quântica`**: exibir o estado vazio (_"Nenhum orientador encontrado"_) e acionar **"Limpar filtros"** para restaurar a listagem completa.

### Passo 3 — Envio de Solicitação de Orientação pelo Aluno (US04)

- **Usuário:** `aluno@example.com` (`ROLE_ALUNO`)
- **Rota / Tela:** `/orientadores` → botão **"Solicitar Orientação"** no card do `Professor Orientador` (`ID 1`) → `/solicitar-orientacao/1` (`POST /api/v1/solicitacoes`)
- **Dados Preenchidos:**
  - **Orientador selecionado:** `Professor Orientador` (`orientadorId: 1`)
  - **Curso:** `Engenharia de Software`
  - **Tema pretendido:** `Arquitetura de Microsserviços e Observabilidade em Sistemas Acadêmicos`
  - **Mensagem / proposta inicial:** `Olá professor, gostaria de desenvolver meu TCC na linha de Arquitetura de Sistemas aplicando padrões de resiliência.`
- **Resultado Esperado:**
  - O backend extrai `email` (`aluno@example.com`) e `name` (`Aluno de Teste`) diretamente do token JWT ([ADR-0008](adrs/ADR-0008.md)), executa as 3 estratégias de validação (`ValidacaoVagasDisponiveis`, `ValidacaoOrientadorAtivo`, `ValidacaoSolicitacaoDuplicada` — padrão **Strategy**) e cria a solicitação com status `PENDENTE` (`HTTP 201 Created`).
  - O aluno é direcionado para a tela de acompanhamento (`/acompanhar-solicitacao/{id}`) exibindo o status **`PENDENTE`**.

### Passo 4 — Aceite da Solicitação pelo Professor Orientador e Decremento de Vagas (US04)

- **Usuário:** `professor.orientador@example.com` / `professor123` (`ROLE_PROFESSOR`)
- **Rota / Tela:** `/login` → `/painel-solicitacoes` (`GET /api/v1/solicitacoes/orientador/1` e `PATCH /api/v1/solicitacoes/{id}/status`)
- **Dados e Ações:**
  1. Fazer logout do aluno e autenticar como `professor.orientador@example.com` (`professor123`).
  2. Observar que para este docente **não** aparece a aba de Gestão do Calendário (`GET /api/v1/professores/me` retorna `responsavelTurma: false`).
  3. No **Painel de Solicitações**, localizar a proposta enviada por `Aluno de Teste` (`status: PENDENTE`).
  4. Clicar em **"Aceitar"**: a solicitação transita para **`ACEITA`** (`HTTP 200 OK`).
- **Resultado Esperado:**
  - O `SolicitacaoService` publica `SolicitacaoStatusChangedEvent` e o `AtualizadorVagasObserver` (padrão **Observer**, síncrono e na mesma transação com `@Version`) decrementa automaticamente as vagas do `Professor Orientador` de **`3` para `2`**, atualizado imediatamente no cabeçalho do painel (`#266`).

### Passo 5 — Gestão do Calendário de Prazos pelo Professor Responsável pela Turma (US05)

- **Usuário:** `professor.responsavel@example.com` / `professor123` (`ROLE_PROFESSOR`, responsável pela `Turma_TCC` `2026/2`)
- **Rota / Tela:** `/gestao-calendario` (`GET /api/v1/prazos`, `POST /api/v1/prazos` e `PATCH /api/v1/prazos/{id}`)
- **Dados e Ações:**
  1. Fazer logout e autenticar como `professor.responsavel@example.com` (`professor123`).
  2. Demonstrar que o backend identifica `responsavelTurma: true` ([ADR-0010](adrs/ADR-0010.md)) e habilita a gestão do calendário da turma vigente `2026/2`.
  3. Visualizar a tabela com os 3 prazos vigentes (`Projeto de TCC` em `+30 dias`, `TCC 1` em `+60 dias`, `TCC 2` em `+90 dias`) e suas contagens regressivas.
  4. Clicar em **"Editar"** em `Projeto de TCC`:
     - Testar regra de negócio: informar uma data no passado (ex.: `2025-01-10`) → o backend rejeita com `HTTP 400 Bad Request` (_"A data limite não pode estar no passado."_).
     - Informar uma data válida futura anterior ao `TCC 1` (ex.: `+25 dias`) e clicar em **"Salvar alterações"** → `HTTP 200 OK` (_"Prazo atualizado com sucesso."_).

### Passo 6 — Submissão de Entrega por Link Externo HTTPS pelo Aluno (US06)

- **Usuário:** `aluno@example.com` / `aluno123` (`ROLE_ALUNO`)
- **Rota / Tela:** `/minhas-entregas` (`POST /api/v1/entregas` e `GET /api/v1/entregas/minhas`)
- **Dados Preenchidos:**
  1. Autenticar novamente como `aluno@example.com` (`aluno123`) e acessar **Minhas Entregas** (`/minhas-entregas`).
  2. Demonstrar validação de protocolo seguro ([ADR-0013](adrs/ADR-0013.md)):
     - Selecionar **`Projeto de TCC`** (`PROJETO_TCC`) e informar um link inseguro `http://drive.google.com/file/d/projeto-tcc-v1` → rejeitado com `HTTP 400 Bad Request` (exigência estrita de `@URL(protocol = "https")`).
  3. Informar o link HTTPS válido:
     - **Tipo:** `PROJETO_TCC`
     - **URL do documento:** `https://drive.google.com/file/d/1A2B3C-projeto-tcc-v1/view`
- **Resultado Esperado:**
  - Entrega criada com `HTTP 201 Created`, status inicial **`SUBMETIDO`** e flag **`atrasada: false`** (pois a data atual é anterior ao prazo limite de `PROJETO_TCC`).

### Passo 7 — Avaliação e Emissão de Parecer com `AJUSTES_NECESSARIOS` pelo Orientador (US07)

- **Usuário:** `professor.orientador@example.com` / `professor123` (`ROLE_PROFESSOR`)
- **Rota / Tela:** `/avaliar-entregas` (`GET /api/v1/entregas/orientador` e `POST /api/v1/entregas/{id}/parecer`)
- **Dados Preenchidos:**
  1. Autenticar como `professor.orientador@example.com` (`professor123`) e acessar **Avaliar Entregas** (`/avaliar-entregas`).
  2. Visualizar a entrega de `Projeto de TCC` do orientando `Aluno de Teste` com link clicável para `https://drive.google.com/file/d/1A2B3C-projeto-tcc-v1/view` e status `SUBMETIDO`.
  3. Testar regra de negócio: selecionar **`Ajustes Necessários`** (`AJUSTES_NECESSARIOS`) sem preencher comentário → o backend rejeita com `HTTP 422 Unprocessable Entity` (_"O comentário é obrigatório quando o parecer exige ajustes ou reprova a entrega."_).
  4. Preencher o parecer completo:
     - **Resultado:** `AJUSTES_NECESSARIOS`
     - **Comentário:** `A fundamentação teórica está consistente, mas é necessário detalhar o diagrama de componentes C3 na seção 4 antes da aprovação.`
- **Resultado Esperado:**
  - Parecer registrado com trava pessimista (`PESSIMISTIC_WRITE`) retornando `HTTP 200 OK`; o status da entrega muda para **`AJUSTES_NECESSARIOS`**.

### Passo 8 — Reenvio pelo Aluno após Ajustes e Aprovação Final pelo Orientador (US06 e US07)

- **Usuários:** `aluno@example.com` (`ROLE_ALUNO`) → `professor.orientador@example.com` (`ROLE_PROFESSOR`)
- **Rotas / Telas:** `/minhas-entregas` (`POST /api/v1/entregas`) → `/avaliar-entregas` (`POST /api/v1/entregas/{id}/parecer`)
- **Dados e Ações:**
  1. **Como Aluno (`aluno@example.com`):**
     - Acessar `/minhas-entregas` e visualizar o status **`AJUSTES_NECESSARIOS`** junto ao comentário do orientador.
     - Reenviar a entrega de **`Projeto de TCC`** (`PROJETO_TCC`) com o novo link revisado:
       `https://drive.google.com/file/d/1A2B3C-projeto-tcc-v2-revisado/view`
     - O backend atualiza o registro existente (`HTTP 200 OK`), retornando o status para **`SUBMETIDO`**.
  2. **Como Professor Orientador (`professor.orientador@example.com`):**
     - Acessar `/avaliar-entregas`, conferir o novo link `https://drive.google.com/file/d/1A2B3C-projeto-tcc-v2-revisado/view` e registrar o parecer final:
       - **Resultado:** `APROVADO`
       - **Comentário:** `Ajustes no diagrama C3 verificados. Projeto de TCC aprovado!`
     - O status da entrega passa para **`APROVADO`** (`HTTP 200 OK`), encerrando o ciclo da etapa (novos pareceres ou reenvios para `PROJETO_TCC` ficam bloqueados com `HTTP 409 Conflict`).

---

## 4. Limitações Conhecidas e Riscos Residuais a Mencionar na Apresentação

Durante a apresentação final, a equipe destacará de forma transparente os seguintes pontos documentados em [`docs/relatorio-de-bugs.md`](relatorio-de-bugs.md), [`docs/riscos.md`](riscos.md) e [`docs/RETROSPECTIVA.md`](RETROSPECTIVA.md):

1. **Escopo Postergado (US08 — Agendamento de Bancas):**
   - A história US08 (`#15`, 5 SP) foi formalmente postergada para versões futuras (`v1.1+`) a fim de priorizar o _hardening_ de segurança, controle de concorrência (`@Version` e `PESSIMISTIC_WRITE`) e estabilidade das histórias centrais US01 a US07 (`29 SP` entregues = `100%` da baseline planejada em `docs/BASELINE.md`).
2. **Submissão por Link Externo e Governança do Arquivo ([ADR-0013](adrs/ADR-0013.md), Riscos `R11` e `R12`):**
   - O SIGTCC armazena e valida links HTTPS, sem custódia de binários no servidor. A permissão de leitura do link na nuvem e a integridade do conteúdo externo permanecem sob responsabilidade do aluno autor, e o reenvio após `AJUSTES_NECESSARIOS` sobrescreve a URL anterior na mesma etapa.
3. **Ausência de Notificações Ativas ([ADR-0007](adrs/ADR-0007.md)):**
   - Por decisão arquitetural para evitar infraestrutura de SMTP/WebSockets no MVP, o acompanhamento de solicitações, prazos e pareceres opera por consulta sob demanda (_pull model_) nos painéis da aplicação.
4. **Autenticação Frontend via Direct Access Grant ([ADR-0014](adrs/ADR-0014.md), Risco `R09`):**
   - Adotado no MVP para viabilizar o formulário de login próprio em React com testes automatizados de componente (`#130`), com token armazenado em `sessionStorage` e renovação automática via `refresh_token`. Para produção corporativa, recomenda-se migrar para Authorization Code + PKCE.
5. **Infraestrutura de Staging e Seed `V5` (`BUG-15`, Risco `R13`):**
   - O ambiente de _staging_ roda via **Tailscale Funnel** com URLs HTTPS fixas hospedadas em máquina da equipe ([`docs/staging.md`](staging.md)), dependendo de o host estar ligado.
   - A migração `V5` utiliza IDs fixos (`1` e `2`), sendo 100% segura em bancos recém-criados (comprovado por `MigracoesFlywayTest`), mantida inalterada para preservar o checksum do Flyway ([ADR-0009](adrs/ADR-0009.md)).

---

## 5. Guia Rápido para Defesa Técnica (Todos os Integrantes)

| Tópico da Banca                | Resumo Técnico para Resposta de Qualquer Integrante                                                                                                                                                                                                                      | Referência no Repositório                                                   |
| :----------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------- |
| **Arquitetura e Segurança**    | SPA React 19.2 + API Spring Boot 4.1.1 (Java 21) + MySQL 8.0 (Flyway `V1`–`V9`) + Keycloak 26.8.0. O backend é um OAuth2 Resource Server sem estado (`STATELESS`) que valida JWT localmente via JWKS e checa titularidade do recurso na camada de serviço.               | [`docs/ARQUITETURA.md`](ARQUITETURA.md), `ADR-0011`, `ADR-0012`, `ADR-0014` |
| **Padrões de Projeto OO**      | **Strategy** (`br.edu.ifsc.gestao_tcc.validation`) desacopla as 3 regras de elegibilidade da US04 respeitando o OCP; **Observer** (`event` + `observer`) decrementa vagas de forma síncrona na mesma transação (`@Transactional`), combinado com `@Version` (`V8`).      | [`docs/PADROES-DE-PROJETO.md`](PADROES-DE-PROJETO.md), `ADR-0007`           |
| **Testes e Cobertura (M-01)**  | Backend com testes unitários (JUnit 5/Mockito), de integração MockMvc + H2, concorrência e validação das migrations Flyway (`MigracoesFlywayTest`, `> 97%` de linhas). Frontend com Vitest + Testing Library (`> 80%` de linhas).                                        | [`docs/METRICAS.md`](METRICAS.md), `docs/ACEITACAO.md`                      |
| **CI/CD e Qualidade**          | 5 workflows no GitHub Actions (`backend-ci.yml`, `frontend-ci.yml`, `verificacao-docs.yml`, `verificar-formatacao.yml` com Prettier + `actionlint 1.7.7`, e `docker-build.yml` publicando imagens no GHCR com tags de versão e SHA), protegidos por _ruleset_ no GitHub. | [`docs/ci.md`](ci.md), [`.github/workflows/`](../.github/workflows/)        |
| **Deploy, Staging e Rollback** | Staging com `docker-compose.prod.yml` (builds multi-stage JRE 21 e Nginx) exposto com HTTPS fixo via Tailscale Funnel (`:443`, `:8443`, `:10000`), `webOrigins` restrito no Keycloak, validado por `smoke-test.sh` (22 checks) e preparado para demo com `seed-demo.sh`. | [`docs/DEPLOY.md`](DEPLOY.md), [`docs/staging.md`](staging.md)              |
