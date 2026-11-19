# Testes de Aceitação do MVP — SIGTCC (`v1.0.0-rc.1`)

- **Sistema:** Sistema de Gestão Centralizada de TCC (SIGTCC)
- **Marco de Entrega:** Entrega 9 / Entrega 10 — Release Candidate e Apresentação Final
- **Versão:** [`v1.0.0-rc.1`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v1.0.0-rc.1) (base da `v1.0.0`)
- **Rastreabilidade:** Issues [#267](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/267) e [#312](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/312).

---

## 1. Introdução e Metodologia

Este documento estabelece a especificação, execução e o registro dos resultados dos **Testes de Aceitação (Acceptance Tests - AT)** das funcionalidades centrais do Produto Mínimo Viável (MVP) do SIGTCC.

Os testes foram desenhados para validar a aderência do sistema aos critérios de aceitação de cada User Story entregue (**US01 a US07**), contemplando:

1. **Fluxos Principais (Caminho Feliz):** Execução bem-sucedida das jornadas de alunos e professores;
2. **Cenários Negativos de Segurança e Autorização:** Tentativas de acesso não autenticado (`HTTP 401 Unauthorized`) e operações proibidas por perfil (`HTTP 403 Forbidden`);
3. **Cenários Negativos de Regra de Negócio e Validação de Dados:** Rejeição de campos/URLs inválidas (`HTTP 400 Bad Request`), conflitos de concorrência ou unicidade (`HTTP 409 Conflict`) e violações semânticas de negócio (`HTTP 422 Unprocessable Entity`);
4. **Dupla Abordagem de Validação:** Execução manual orientada à interface do usuário (UI) combinada com testes automatizados (Spring Boot MockMvc, Vitest) e o script [`SIGTCC/scripts/smoke-test.sh`](../SIGTCC/scripts/smoke-test.sh).

---

## 2. Ambientes de Teste e Usuários Homologados

Os testes foram executados no ambiente de **_staging_** (acessível externamente via Tailscale Funnel com HTTPS fixo) e validados também em ambiente **local** via Docker Compose:

- **Frontend SPA:** `https://sigtcc.cobbler-albacore.ts.net` (local: `http://localhost:5173`)
- **Backend API REST:** `https://sigtcc.cobbler-albacore.ts.net:8443` (local: `http://localhost:8080`)
- **Keycloak Auth Server:** `https://sigtcc.cobbler-albacore.ts.net:10000` (local: `http://localhost:8081`)

### 2.1. Credenciais dos Usuários de Teste (Realm `sigtcc`)

| Papel no Sistema          | E-mail / Login                      | Senha          | Role Keycloak    | Identificador e Vínculo Inicial                                                                |
| :------------------------ | :---------------------------------- | :------------- | :--------------- | :--------------------------------------------------------------------------------------------- |
| **Aluno**                 | `aluno@example.com`                 | `aluno123`     | `ROLE_ALUNO`     | Criado no banco de dados via _upsert_ pelo e-mail do JWT na primeira solicitação.              |
| **Professor Orientador**  | `professor.orientador@example.com`  | `professor123` | `ROLE_PROFESSOR` | Orientador `ID 1` (`DAELN`, ativo, 3 vagas disponíveis).                                       |
| **Professor Responsável** | `professor.responsavel@example.com` | `professor123` | `ROLE_PROFESSOR` | Orientador `ID 2` (`DAELN`, ativo, 3 vagas) e responsável pela `Turma_TCC` vigente (`2026/2`). |

---

## 3. Matriz Resumo dos Casos de Teste de Aceitação

|    ID     | Funcionalidade / História                                         |   Tipo de Execução    | Cobertura Automatizada                                                                                                                                                                    |        Status Final        |
| :-------: | :---------------------------------------------------------------- | :-------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------: |
| **AT-01** | **US01 — Autenticação, Sessão e RBAC via Keycloak**               | Manual e Automatizada | `smoke-test.sh` (bloco 1 e 2), `Login.test.tsx`, `ProtectedRoute.test.tsx`, `SecurityConfigTest.java`                                                                                     |        **Aprovado**        |
| **AT-02** | **US02 — Perfil do Orientador e Vagas Disponíveis**               | Manual e Automatizada | `OrientadorControllerTest.java`, `OrientadorControllerIntegrationTest.java`, `OrientadorServiceTest.java`, `OrientadorEdicao.test.tsx`                                                    |        **Aprovado**        |
| **AT-03** | **US03 — Catálogo e Busca Combinada de Orientadores**             | Manual e Automatizada | `OrientadorRepositoryTest.java`, `Orientadores.test.tsx`                                                                                                                                  |        **Aprovado**        |
| **AT-04** | **US04 — Solicitação e Aceite de Orientação (Strategy/Observer)** | Manual e Automatizada | `smoke-test.sh` (bloco 3), `SolicitacaoControllerIntegrationTest.java`, `AtualizadorVagasObserverTest.java`, `SolicitacaoConcorrenciaIntegrationTest.java`, `PainelSolicitacoes.test.tsx` |        **Aprovado**        |
| **AT-05** | **US05 — Calendário de Prazos de Entrega da Turma**               | Manual e Automatizada | `smoke-test.sh` (bloco 3), `PrazoEntregaControllerIntegrationTest.java`, `PrazoEntregaServiceTest.java`, `GestaoCalendario.test.tsx`                                                      |        **Aprovado**        |
| **AT-06** | **US06 — Submissão de Entregas do TCC por Link HTTPS**            | Manual e Automatizada | `smoke-test.sh` (blocos 4 e 5), `EntregaControllerIntegrationTest.java`, `EntregaServiceTest.java`, `SubmissaoEntrega.test.tsx`                                                           |        **Aprovado**        |
| **AT-07** | **US07 — Avaliação e Parecer de Entregas pelo Orientador**        | Manual e Automatizada | `smoke-test.sh` (blocos 5 e 6), `EntregaParecerIntegrationTest.java`, `EntregaServiceTest.java`, `AvaliacaoEntrega.test.tsx`                                                              | **Aprovado com ressalvas** |

---

## 4. Detalhamento dos Casos de Teste de Aceitação

```
========================================================================================
CASO DE TESTE: AT-01
HISTÓRIA / REQUISITO: US01 — Autenticação e Perfis de Usuário via Keycloak (5 SP)
ISSUES RELACIONADAS: #8 (Épico), #127, #128, #129, #130, #131, #143, #144, #275 (BUG-18)
========================================================================================
```

- **Objetivo:** Verificar a autenticação de usuários contra o Keycloak via Direct Access Grant (ROPC), armazenamento do token JWT no frontend (`sessionStorage`), controle de rotas protegidas por papel (`ROLE_ALUNO` vs. `ROLE_PROFESSOR`) e preservação de rota de destino após o login.
- **Pré-condições:** Contêineres de Keycloak, Backend e Frontend em execução; realm `sigtcc` importado com os 3 usuários de teste.

#### Cenários Executados:

1. **Cenário 1.1 — Login com Sucesso por Papel (Caminho Feliz):**
   - **Critério validado:** `#8`, Cenários 1, 3 e 4.
   - **Passos:** Acessar `/login`, informar `aluno@example.com` / `aluno123` e submeter. Em seguida, deslogar e logar com `professor.orientador@example.com` / `professor123` e `professor.responsavel@example.com` / `professor123`.
   - **Resultado Esperado:** Autenticação concluída (`HTTP 200`), redirecionamento para `/orientadores` (aluno) ou `/painel-orientador` (professor), com exibição da aba `"Gestão da Turma/Calendário"` apenas para o professor responsável pela turma vigente.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 1: login dos 3 perfis), `Login.test.tsx`, `PainelSolicitacoes.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 1.2 — Credenciais Inválidas (Negativo - 401):**
   - **Critério validado:** `#8`, Cenário 2.
   - **Passos:** Acessar `/login`, informar `aluno@example.com` com senha incorreta `senhaInvalida999`.
   - **Resultado Esperado:** O Keycloak recusa a emissão de token (`HTTP 401 Unauthorized`). O frontend exibe mensagem de erro na tela de login, sem expor stacktrace.
   - **Cobertura automatizada:** `Login.test.tsx` e `authService.test.ts`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 1.3 — Acesso a Rota e Recurso Não Autorizados (Negativo - 403):**
   - **Critério validado:** `#8`, Cenários 3 e 4; `#12`, Cenário 2.
   - **Passos:** Autenticado como Aluno (`aluno@example.com`), tentar navegar diretamente para `/painel-orientador` (onde fica a aba `"Gestão da Turma/Calendário"`). Na API, como aluno ou professor não responsável, tentar alterar prazos em `POST /api/v1/prazos` ou `PATCH /api/v1/prazos/{id}`.
   - **Resultado Esperado:** Acesso bloqueado pelo componente `ProtectedRoute` com tela de `"Acesso Negado"` no frontend e resposta `HTTP 403 Forbidden` no backend.
   - **Cobertura automatizada:** `ProtectedRoute.test.tsx`, `AcessoNegado.test.tsx`, `SecurityConfigTest.java`, `PrazoEntregaControllerIntegrationTest.java`.
   - **Resultado Obtido:** **Aprovado**.

4. **Cenário 1.4 — Preservação de Rota e Parâmetros pós-login (BUG-18 / PR #297):**
   - **Critério validado:** `#8`, Cenário 5 e `#275` (BUG-18).
   - **Passos:** Sem sessão ativa, tentar acessar `/acompanhar/15?tab=orientadores#topo`.
   - **Resultado Esperado:** Redirecionamento para `/login`. Ao autenticar com sucesso, redireciona automaticamente de volta para `/acompanhar/15?tab=orientadores#topo` com rota, query parameters e hash preservados.
   - **Cobertura automatizada:** `ProtectedRoute.test.tsx` e `Login.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-02
HISTÓRIA / REQUISITO: US02 — Cadastro e Perfil de Orientador e Controle de Vagas (5 SP)
ISSUES RELACIONADAS: #9 (Épico), #36 a #43, #48 a #53, #273 (BUG-11), #277, #279
========================================================================================
```

- **Objetivo:** Verificar a persistência, consulta e edição de perfil de docente, limites de tamanho nos campos de cadastro, vinculação do e-mail ao JWT autenticado e gestão de vagas disponíveis.
- **Pré-condições:** Banco inicial carregado com migração `V5` (professores com `id=1` e `id=2`).

#### Cenários Executados:

1. **Cenário 2.1 — Consulta e Atualização do Próprio Perfil (Caminho Feliz):**
   - **Critério validado:** `#9`, Cenário 1.
   - **Passos:** Logado como `professor.orientador@example.com`, acessar `"Meu Perfil"` em `/perfil-editar`, alterar a biografia para _"Docente permanente com foco em Arquitetura de Software"_ e salvar.
   - **Resultado Esperado:** Dados atualizados com `HTTP 200 OK` (`PATCH /api/v1/orientadores/1`).
   - **Cobertura automatizada:** `OrientadorControllerIntegrationTest.java` e `OrientadorEdicao.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 2.2 — Violação de Limites de Tamanho nos DTOs (Negativo - 400 - BUG-11):**
   - **Critério validado:** `#9`, Cenário 2 e `#273` (BUG-11).
   - **Passos:** Enviar requisição com `nome` fora do intervalo de 3 a 100 caracteres, `biografia` (campo opcional) acima de 500 caracteres ou `vagasDisponiveis` negativo.
   - **Resultado Esperado:** As validações `@Size(min = 3, max = 100)` em `nome`, `@Size(max = 500)` em `biografia` e `@Min(0)` em `vagasDisponiveis` rejeitam a requisição com `HTTP 400 Bad Request` no `ErrorResponseDTO`.
   - **Cobertura automatizada:** `OrientadorControllerTest.java`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 2.3 — Tentativa de Modificar Perfil de Outro Docente (Negativo - 403):**
   - **Critério validado:** `#9` + `#200`/`#204` (BUG-02 / BUG-06).
   - **Passos:** Autenticado como Professor `ID 1`, disparar `PATCH /api/v1/orientadores/2` tentando alterar os dados do Professor `ID 2`.
   - **Resultado Esperado:** A camada de serviço valida a titularidade do recurso e rejeita a operação com `HTTP 403 Forbidden` e a mensagem `"Você só pode alterar o seu próprio cadastro de orientador."`.
   - **Cobertura automatizada:** `OrientadorServiceTest.java` e `OrientadorControllerIntegrationTest.java`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-03
HISTÓRIA / REQUISITO: US03 — Busca e Filtragem de Orientadores (3 SP)
ISSUES RELACIONADAS: #10 (Épico), #133, #134, #135, #136, #264 (BUG-14)
========================================================================================
```

- **Objetivo:** Validar a consulta da vitrine de orientadores, filtro por linha de pesquisa com correspondência exata case-insensitive, filtro por disponibilidade de vagas, estado vazio e limpeza de filtros.
- **Pré-condições:** Pelo menos 2 orientadores cadastrados com linhas de pesquisa distintas (ex.: `Arquitetura de Sistemas` e `Inteligência Artificial`).

#### Cenários Executados:

1. **Cenário 3.1 — Filtro Exato por Linha de Pesquisa (BUG-14 / PR #270):**
   - **Critério validado:** `#10`, Cenários 1 e 3; `#264` (BUG-14).
   - **Passos:** Acessar `/orientadores`, selecionar `"Arquitetura de Sistemas"` no filtro de linha de pesquisa e aplicar. Testar também com variação de maiúsculas/minúsculas.
   - **Resultado Esperado:** Apenas orientadores que contenham exatamente essa linha de pesquisa são listados (`Professor Orientador` - ID 1), sem falsos positivos por substring.
   - **Cobertura automatizada:** `OrientadorRepositoryTest.java` e `Orientadores.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 3.2 — Filtro por Vagas Disponíveis:**
   - **Critério validado:** `#10`, Cenários 2 e 3.
   - **Passos:** Marcar a opção `"Somente com vagas disponíveis"`.
   - **Resultado Esperado:** Orientadores com `vagasDisponiveis > 0` são exibidos; docentes com vagas zeradas são ocultados.
   - **Cobertura automatizada:** `OrientadorRepositoryTest.java` e `Orientadores.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 3.3 — Busca sem Resultados e Limpeza de Filtros (Estado Vazio):**
   - **Critério validado:** `#10`, Cenários 4 e 5.
   - **Passos:** Selecionar/buscar uma linha sem correspondência e, em seguida, acionar `"Limpar filtros"`.
   - **Resultado Esperado:** A interface apresenta o estado vazio sem erro e, ao limpar os filtros, restaura a listagem completa.
   - **Cobertura automatizada:** `Orientadores.test.tsx` e `OrientadorList.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-04
HISTÓRIA / REQUISITO: US04 — Solicitação e Aceite de Orientação + Padrões OO (5 SP)
ISSUES RELACIONADAS: #11 (Épico), #79 a #99, #206 (Concorrência R07), #263 (BUG-13), #266
========================================================================================
```

- **Objetivo:** Validar o envio de propostas de orientação por alunos, aplicação das regras de elegibilidade via padrão **Strategy**, transição de status para `ACEITA` ou `RECUSADA` com justificativa obrigatória, decremento atômico de vagas via padrão **Observer** e proteção contra concorrência via bloqueio otimista (`@Version`).
- **Pré-condições:** Orientador ativo com 3 vagas cadastradas; aluno autenticado.

#### Cenários Executados:

1. **Cenário 4.1 — Envio e Aceite de Solicitação com Decremento de Vagas (Caminho Feliz):**
   - **Critério validado:** `#11`, Cenários 1, 6 e 7.
   - **Passos:** Logado como `aluno@example.com`, enviar solicitação para o orientador ID 1 com curso, tema e mensagem. Em seguida, logar como `professor.orientador@example.com`, acessar `/painel-orientador` e clicar em **"Aceitar"**.
   - **Resultado Esperado:** Solicitação criada com status `PENDENTE` (`HTTP 201`). Ao aceitar (`HTTP 200`), o evento `SolicitacaoStatusChangedEvent` é publicado e o `AtualizadorVagasObserver` decrementa as vagas de 3 para 2 dentro da mesma transação.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 3: criação e aceite), `SolicitacaoControllerIntegrationTest.java`, `AtualizadorVagasObserverTest.java`, `PainelSolicitacoes.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 4.2 — Recusa sem Justificativa (Negativo - 400):**
   - **Critério validado:** `#11`, Cenário 10.
   - **Passos:** No painel do orientador (`/painel-orientador`), acionar a recusa de uma solicitação com campo `justificativa` ausente ou em branco.
   - **Resultado Esperado:** Rejeição com `HTTP 400 Bad Request` e mensagem `"A justificativa é obrigatória para recusar a solicitação."`.
   - **Cobertura automatizada:** `SolicitacaoControllerTest.java` e `SolicitacaoControllerIntegrationTest.java`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 4.3 — Solicitação Duplicada Pendente (Negativo - 409 - Strategy):**
   - **Critério validado:** `#11`, Cenário 4.
   - **Passos:** O mesmo aluno submeter uma nova solicitação para um orientador para o qual já possui solicitação com status `PENDENTE`.
   - **Resultado Esperado:** A estratégia `ValidacaoSolicitacaoDuplicada` intercepta o envio e retorna `HTTP 409 Conflict` com a mensagem `"Você possui uma solicitação pendente para este orientador."`.
   - **Cobertura automatizada:** `ValidacaoSolicitacaoDuplicadaTest.java` e `SolicitacaoControllerIntegrationTest.java`.
   - **Resultado Obtido:** **Aprovado**.

4. **Cenário 4.4 — Concorrência na Última Vaga (Negativo - 409 - Bloqueio Otimista):**
   - **Critério validado:** `#11`, Cenário 8 e `#206` (R07).
   - **Passos:** Orientador com 1 vaga restante receber duas requisições simultâneas de aceite.
   - **Resultado Esperado:** Uma requisição obtém sucesso (`HTTP 200`) e a concorrente recebe `HTTP 409 Conflict` via `OptimisticLockingFailureException` (`@Version` na tabela `perfis_professores`, migração `V8`).
   - **Cobertura automatizada:** `SolicitacaoConcorrenciaIntegrationTest.java`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-05
HISTÓRIA / REQUISITO: US05 — Calendário de Prazos de Entrega do TCC (5 SP)
ISSUES RELACIONADAS: #12 (Épico), #137 a #142, #145 a #158, #276 (BUG-20)
========================================================================================
```

- **Objetivo:** Verificar o cadastro/edição de prazos (`PROJETO_TCC`, `TCC1`, `TCC2`), restrição ao professor responsável pela turma vigente, contagem regressiva e validações temporais.
- **Pré-condições:** Turma vigente `2026/2` vinculada ao professor responsável (`ID 2`).

#### Cenários Executados:

1. **Cenário 5.1 — Consulta dos Prazos da Turma (Caminho Feliz):**
   - **Critério validado:** `#12`, Cenários 1, 3 e 6 (via `docs/contrato-dados-us05.md`).
   - **Passos:** Usuário autenticado (aluno ou professor) consulta os prazos através de `GET /api/v1/prazos` e visualiza a contagem regressiva na UI.
   - **Resultado Esperado:** Retorno dos prazos ordenados cronologicamente com `tipo`, `dataLimite` e `diasRestantes`.
   - **Cobertura automatizada:** `PrazoEntregaControllerIntegrationTest.java`, `PainelPrazos.test.tsx`, `prazoEntregaService.test.ts`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 5.2 — Atualização de Prazo pelo Professor Responsável (Caminho Feliz):**
   - **Critério validado:** `#12`, Cenário 4.
   - **Passos:** Autenticado como `professor.responsavel@example.com`, acessar `/painel-orientador`, abrir a aba `"Gestão da Turma/Calendário"`, selecionar a etapa e atualizar a data limite para uma data futura válida.
   - **Resultado Esperado:** Atualização concluída com `HTTP 200 OK` (`PATCH /api/v1/prazos/{id}`).
   - **Cobertura automatizada:** `PrazoEntregaControllerIntegrationTest.java` e `GestaoCalendario.test.tsx` (`smoke-test.sh` cobre o `POST /api/v1/prazos` no bloco 3).
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 5.3 — Professor Não Responsável Tenta Alterar Prazo (Negativo - 403):**
   - **Critério validado:** `#12`, Cenário 2.
   - **Passos:** Autenticado como `professor.orientador@example.com` (que não é o responsável pela turma), tentar disparar `POST` ou `PATCH` em `/api/v1/prazos`.
   - **Resultado Esperado:** O backend bloqueia a requisição com `HTTP 403 Forbidden`.
   - **Cobertura automatizada:** `PrazoEntregaControllerIntegrationTest.java` e `PrazoEntregaServiceTest.java`.
   - **Resultado Obtido:** **Aprovado**.

4. **Cenário 5.4 — Data Limite no Passado (Negativo - 400):**
   - **Critério validado:** `#12`, Cenário 5 (`docs/contrato-dados-us05.md`, seção 8).
   - **Passos:** Enviar atualização de prazo informando uma data anterior ao dia atual (ex.: `2025-01-01`).
   - **Resultado Esperado:** Rejeição com `HTTP 400 Bad Request` informando que a data limite deve estar no futuro.
   - **Cobertura automatizada:** `PrazoEntregaControllerIntegrationTest.java` e `PrazoEntregaServiceTest.java`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-06
HISTÓRIA / REQUISITO: US06 — Submissão de Entregas por Link Externo HTTPS (3 SP)
ISSUES RELACIONADAS: #13 (Épico), #208, #209, #216, #218, #223, #226, #228
========================================================================================
```

- **Objetivo:** Validar o envio de trabalhos pelos alunos por meio de link externo HTTPS ([ADR-0013](adrs/ADR-0013.md)), cálculo automático da flag de atraso com base no prazo da turma e consulta de histórico próprio.
- **Pré-condições:** Aluno com solicitação `ACEITA` por um orientador e etapa vigente configurada (`PROJETO_TCC`).

#### Cenários Executados:

1. **Cenário 6.1 — Submissão no Prazo com Link HTTPS (Caminho Feliz):**
   - **Critério validado:** `#13`, Cenários 1 e 7 (adaptado pela `ADR-0013` em `docs/contrato-dados-us06.md`, decisão D7) e `#216`.
   - **Passos:** Logado como Aluno, acessar `/minhas-entregas`, selecionar o tipo `PROJETO_TCC`, informar o link `https://drive.google.com/file/d/meu-tcc-v1/view` e submeter.
   - **Resultado Esperado:** Entrega criada com `HTTP 201 Created`, status `SUBMETIDO`, `enviadoEm` preenchido pelo servidor com `Clock` injetado e flag `atrasada: false`.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 4), `EntregaControllerIntegrationTest.java`, `SubmissaoEntrega.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

2. **Cenário 6.2 — Submissão com Protocolo Inseguro HTTP (Negativo - 400):**
   - **Critério validado:** `#13`, Cenário 2 e `#218` (`ADR-0013`).
   - **Passos:** Tentar enviar submissão informando URL com protocolo HTTP: `http://drive.google.com/arquivo`.
   - **Resultado Esperado:** A validação de URL rejeita a requisição com `HTTP 400 Bad Request` e a mensagem `"A URL do documento deve ser válida e utilizar o protocolo https."`.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 4), `EntregaControllerIntegrationTest.java`, `SubmissaoEntrega.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 6.3 — Acesso Negado ao Consultar Entrega Alheia (Negativo - 403):**
   - **Critério validado:** `#14`, Cenário 6 (autorização por vínculo) e `#223` (`docs/contrato-dados-us06.md`).
   - **Passos:** Autenticado como outro usuário não vinculado à entrega (outro aluno ou professor que não é o orientador da entrega), tentar acessar `GET /api/v1/entregas/{id}`.
   - **Resultado Esperado:** A camada de serviço valida a propriedade/vínculo do recurso e bloqueia com `HTTP 403 Forbidden`.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 5: professor não orientador recebe `403`), `EntregaControllerIntegrationTest.java`, `EntregaServiceTest.java`.
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-07
HISTÓRIA / REQUISITO: US07 — Avaliação e Parecer de Entregas pelo Orientador (3 SP)
ISSUES RELACIONADAS: #14 (Épico), #209, #210, #215, #227, #229
========================================================================================
```

- **Objetivo:** Verificar a consulta das entregas dos orientandos vinculados, emissão de parecer (`APROVADO`, `REPROVADO` ou `AJUSTES_NECESSARIOS`), comentário obrigatório em ajustes/reprovação, controle de concorrência com trava pessimista (`PESSIMISTIC_WRITE`) e reenvio de novo link pelo aluno.
- **Pré-condições:** Entrega submetida pelo aluno com status `SUBMETIDO`.

#### Cenários Executados:

1. **Cenário 7.1 — Parecer com Ajustes Necessários e Reenvio do Aluno (Caminho Feliz com Ressalva de UI):**
   - **Critério validado:** `#14`, Cenário 3, e `#13`, Cenário 6 (`#209`, `#210`, `#215`, `#226`, `docs/contrato-dados-us07.md`).
   - **Passos:** Logado como `professor.orientador@example.com`, acessar `/avaliar-entregas`, selecionar o trabalho do orientando e registrar parecer `AJUSTES_NECESSARIOS` com comentário obrigatório. Em seguida, logar como Aluno em `/minhas-entregas` e reenviar novo link `https://drive.google.com/file/d/meu-tcc-v2/view`.
   - **Resultado Esperado:** Parecer registrado com `HTTP 200 OK`, status da entrega muda para `AJUSTES_NECESSARIOS` e o aluno visualiza o status e reenvia novo link (voltando a `SUBMETIDO`).
   - **Ressalva observada:** Na tela `/minhas-entregas`, o aluno visualiza o status `AJUSTES_NECESSARIOS` e o aviso para reenvio, mas o texto do comentário do orientador (embora retornado pela API) não é exibido no frontend.
   - **Cobertura automatizada:** `EntregaParecerIntegrationTest.java`, `EntregaServiceTest.java`, `AvaliacaoEntrega.test.tsx`, `SubmissaoEntrega.test.tsx`. (O fluxo completo de ajustes e reenvio pela UI foi validado também manualmente).
   - **Resultado Obtido:** **Aprovado com ressalvas**.

2. **Cenário 7.2 — Parecer de Ajustes sem Comentário (Negativo - 422):**
   - **Critério validado:** `#14`, Cenário 5 e `#215` (`docs/contrato-dados-us07.md`, decisão D2).
   - **Passos:** O orientador submeter parecer `AJUSTES_NECESSARIOS` ou `REPROVADO` deixando o comentário vazio.
   - **Resultado Esperado:** Rejeição com `HTTP 422 Unprocessable Entity` informando que o comentário é obrigatório quando há exigência de ajustes ou reprovação.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 5), `EntregaParecerIntegrationTest.java`, `AvaliacaoEntrega.test.tsx`.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 7.3 — Aprovação Final e Bloqueio de Alterações Posteriores (Caminho Feliz + 409):**
   - **Critério validado:** `#14`, Cenários 2 e 7, e `#13`, Cenário 5 (`#215`, `#226`).
   - **Passos:** O orientador emitir parecer `APROVADO`. Em seguida, o orientador tentar emitir novo parecer na mesma entrega ou o aluno tentar reenviar link.
   - **Resultado Esperado:** Parecer `APROVADO` encerra o ciclo daquela etapa (`HTTP 200`). Novas tentativas de avaliação ou reenvio retornam `HTTP 409 Conflict`.
   - **Cobertura automatizada:** `smoke-test.sh` (bloco 5: novo parecer após `APROVADO` retorna `409`), `EntregaParecerIntegrationTest.java`, `EntregaServiceTest.java`.
   - **Resultado Obtido:** **Aprovado**.

4. **Cenário 7.4 — Professor Não Vinculado Tenta Avaliar Entrega (Negativo - 403):**
   - **Critério validado:** `#14`, Cenário 6 e `#215`.
   - **Passos:** Um docente que não é o orientador daquele aluno tentar disparar `POST /api/v1/entregas/{id}/parecer`.
   - **Resultado Esperado:** Acesso negado com `HTTP 403 Forbidden` e a mensagem `"Somente o orientador vinculado à entrega pode registrar parecer."`.
   - **Cobertura automatizada:** `EntregaParecerIntegrationTest.java` e `EntregaServiceTest.java` (`smoke-test.sh` verifica o bloqueio `403` para aluno tentando registrar parecer e para professor não orientador no `GET`).
   - **Resultado Obtido:** **Aprovado**.

- **Status do Caso:** **Aprovado com ressalvas** (ressalva no Cenário 7.1 quanto à exibição do texto do comentário na tela `/minhas-entregas`).

---

## 5. Evidências de Execução Automatizada

### 5.1. Execução do Script de Smoke Test (`SIGTCC/scripts/smoke-test.sh`)

- **Data de execução:** `19/11/2026`
- **Ambiente:** _Staging_ (`API_URL=https://sigtcc.cobbler-albacore.ts.net:8443`, `KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000`) com banco recém-criado (`down -v && up -d`).

```text
Smoke test -> API: https://sigtcc.cobbler-albacore.ts.net:8443 | Keycloak: https://sigtcc.cobbler-albacore.ts.net:10000

1) Login via Keycloak
  OK    login do aluno
  OK    login do orientador
  OK    login do responsável

2) Backend aceita o token e protege as rotas
  OK    sem token a API recusa (HTTP 401)
  OK    com token a API responde (HTTP 200)

3) Dados de apoio (solicitação aceita e prazo)
  OK    aluno cria solicitação (HTTP 201)
  OK    aluno lê a solicitação de volta do banco (HTTP 200)
  OK    acentos preservados na ida e volta (MySQL)
  OK    orientador aceita a solicitação (HTTP 200)
  OK    professor responsável cadastra o prazo (HTTP 201)

4) Entrega por link (ADR-0013)
  OK    link http:// é rejeitado (HTTP 400)
  OK    link https:// é aceito (HTTP 201)
  OK    status inicial é SUBMETIDO

5) Avaliação do orientador
  OK    aluno não pode registrar parecer (HTTP 403)
  OK    professor que não é o orientador não vê a entrega (HTTP 403)
  OK    orientador lista as entregas dos orientandos (HTTP 200)
  OK    a entrega enviada aparece na lista
  OK    ajustes sem comentário são rejeitados (HTTP 422)
  OK    orientador aprova a entrega (HTTP 200)
  OK    novo parecer após APROVADO é bloqueado (HTTP 409)

6) Aluno acompanha o resultado
  OK    aluno lista suas entregas (HTTP 200)
  OK    status refletido para o aluno

Smoke test concluído: 22 verificações passaram.
```

### 5.2. Suíte de Testes Unitários e de Integração do Backend (Gradle)

- **Comando de Execução:** `./gradlew test` (em `SIGTCC/backend/gestao-tcc`)
- **Resultados:** **402 testes** executados em **43 arquivos** de relatório (`0` falhas, `0` erros, `0` ignorados).
- **Cobertura (M-01):** Registrada em [`docs/metricas/M-01.md`](metricas/M-01.md) com **97,78%** de cobertura de linhas (`662/677`) e **88,07%** de branches (`192/218`), acima da meta de **≥ 80%**.

### 5.3. Suíte de Testes de Componentes e Rotas do Frontend (Vitest)

- **Comando de Execução:** `npm test -- --run` (em `SIGTCC/frontend`)
- **Resultados:** **149 testes** aprovados em **22 arquivos** (`149 passed`).
- **Cobertura (M-01):** Registrada em [`docs/metricas/M-01.md`](metricas/M-01.md) com **80,99%** de linhas (`80,67%` statements, `83,71%` functions, `74,09%` branches), atendendo à meta da métrica M-01 de **≥ 80%**.

---

## 6. Conclusão e Parecer de Aceitação

Com base na execução dos casos de teste **AT-01 a AT-07** e na comprovação automatizada via suítes do Gradle, Vitest e `smoke-test.sh`:

> **Os casos AT-01 a AT-06 foram Aprovados e o caso AT-07 foi Aprovado com ressalvas (o aluno visualiza o status `AJUSTES_NECESSARIOS` e reenvia a entrega em `/minhas-entregas`, mas o texto do comentário do parecer não é exibido na interface). O MVP do SIGTCC atende aos critérios de aceitação estabelecidos para US01 a US07 e está apto para a demonstração final.**
