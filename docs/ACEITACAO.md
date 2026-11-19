# Testes de Aceitação do MVP — SIGTCC (`v1.0.0-rc.1`)

- **Sistema:** Sistema de Gestão Centralizada de TCC (SIGTCC)
- **Marco de Entrega:** Entrega 9 — Release Candidate (RC)
- **Versão:** [`v1.0.0-rc.1`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v1.0.0-rc.1)
- **Rastreabilidade:** Atendimento ao Item 2.5 e 5 do Edital da Entrega 9 e à Issue [#267](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/267).

---

## 1. Introdução e Metodologia

Este documento estabelece a especificação, execução e o registro dos resultados dos **Testes de Aceitação (Acceptance Tests - AT)** das funcionalidades centrais do Produto Mínimo Viável (MVP) do SIGTCC.

Os testes foram desenhados para validar a aderência do sistema aos critérios de aceitação de cada User Story entregue (**US01 a US07**), contemplando:

1. **Fluxos Principais (Caminho Feliz):** Execução bem-sucedida das jornadas de alunos e professores;
2. **Cenários Negativos de Segurança e Autorização:** Tentativas de acesso não autenticado (`HTTP 401 Unauthorized`) e operações proibidas por perfil (`HTTP 403 Forbidden`);
3. **Cenários Negativos de Regra de Negócio e Validação de Dados:** Rejeição de links inseguros (`HTTP 400 Bad Request`), conflitos de concorrência ou unicidade (`HTTP 409 Conflict`) e violações semânticas de negócio (`HTTP 422 Unprocessable Entity`);
4. **Dupla Abordagem de Validação:** Execução manual orientada à interface do usuário (UI) combinada com execução automatizada por meio de testes de integração (Spring Boot MockMvc, Vitest) e o script executável de homologação ponta a ponta [`SIGTCC/scripts/smoke-test.sh`](../SIGTCC/scripts/smoke-test.sh).

---

## 2. Ambientes de Teste e Usuários Homologados

Os testes foram homologados tanto no ambiente de **_staging_** (acessível externamente via Tailscale Funnel com HTTPS fixo) quanto em ambiente **local reproduzível** via Docker Compose:

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

|    ID     | Funcionalidade / História                                         |   Tipo de Execução    | Cobertura Automatizada                                                                                                                                          | Status Final |
| :-------: | :---------------------------------------------------------------- | :-------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------: |
| **AT-01** | **US01 — Autenticação, Sessão e RBAC via Keycloak**               | Manual e Automatizada | `smoke-test.sh` (passos 1 a 3), `Login.test.tsx`, `ProtectedRoute.test.tsx`, `SecurityConfigTest.java`                                                          | **Aprovado** |
| **AT-02** | **US02 — Perfil do Orientador e Vagas Disponíveis**               | Manual e Automatizada | `smoke-test.sh` (passo 4), `OrientadorControllerIntegrationTest.java`, `OrientadorEdicao.test.tsx`                                                              | **Aprovado** |
| **AT-03** | **US03 — Catálogo e Busca Combinada de Orientadores**             | Manual e Automatizada | `OrientadorRepositoryTest.java`, `Orientadores.test.tsx`                                                                                                        | **Aprovado** |
| **AT-04** | **US04 — Solicitação e Aceite de Orientação (Strategy/Observer)** | Manual e Automatizada | `smoke-test.sh` (passos 5 a 8), `SolicitacaoControllerIntegrationTest.java`, `AtualizadorVagasObserverTest.java`, `SolicitacaoConcorrenciaIntegrationTest.java` | **Aprovado** |
| **AT-05** | **US05 — Calendário de Prazos de Entrega da Turma**               | Manual e Automatizada | `smoke-test.sh` (passos 9 a 11), `PrazoEntregaControllerIntegrationTest.java`, `GestaoCalendario.test.tsx`                                                      | **Aprovado** |
| **AT-06** | **US06 — Submissão de Entregas do TCC por Link HTTPS**            | Manual e Automatizada | `smoke-test.sh` (passos 12 a 16), `EntregaControllerIntegrationTest.java`, `SubmissaoEntrega.test.tsx`                                                          | **Aprovado** |
| **AT-07** | **US07 — Avaliação e Parecer de Entregas pelo Orientador**        | Manual e Automatizada | `smoke-test.sh` (passos 17 a 22), `EntregaParecerIntegrationTest.java`, `AvaliacaoEntrega.test.tsx`                                                             | **Aprovado** |

---

## 4. Detalhamento dos Casos de Teste de Aceitação

```
========================================================================================
CASO DE TESTE: AT-01
HISTÓRIA / REQUISITO: US01 — Autenticação e Perfis de Usuário via Keycloak (5 SP)
ISSUES RELACIONADAS: #8 (Épico), #127, #128, #129, #130, #131, #143, #144, #275 (BUG-18)
========================================================================================
```

- **Objetivo:** Verificar a autenticação de usuários contra o Keycloak via Direct Access Grant (ROPC), propagação e armazenamento seguro do token JWT no frontend (`sessionStorage`), controle de rotas protegidas por papel (`ROLE_ALUNO` vs. `ROLE_PROFESSOR`) e preservação de rota de destino após o login.
- **Tipo de Execução:** Manual (Interface Web) e Automatizada (`smoke-test.sh`, `Login.test.tsx`, `ProtectedRoute.test.tsx`, `SecurityConfigTest.java`).
- **Pré-condições:** Contêineres de Keycloak, Backend e Frontend em execução; realm `sigtcc` importado com os 3 usuários de teste.

#### Cenários Executados:

1. **Cenário 1.1 — Login com Sucesso por Papel (Caminho Feliz):**
   - **Passos:** Acessar `/login`, informar `aluno@example.com` / `aluno123` e submeter. Em seguida, deslogar e logar com `professor.orientador@example.com` / `professor123`.
   - **Resultado Esperado:** Autenticação concluída (`HTTP 200`), token armazenado e menu ajustado dinamicamente: para Aluno são exibidos "Catálogo" e "Minhas Entregas"; para Professor são exibidos "Catálogo", "Painel de Solicitações", "Avaliar Entregas" e "Meu Perfil".
   - **Resultado Obtido:** **Aprovado**. Menus e permissões renderizados conforme a role do JWT.

2. **Cenário 1.2 — Credenciais Inválidas (Negativo - 401):**
   - **Passos:** Acessar `/login`, informar `aluno@example.com` com senha incorreta `senhaInvalida999`.
   - **Resultado Esperado:** O Keycloak recusa a emissão de token (`HTTP 401 Unauthorized`). O frontend captura a falha e exibe mensagem amigável de erro na tela de login, sem expor stacktrace.
   - **Resultado Obtido:** **Aprovado**. Mensagem "Credenciais inválidas" exibida na interface.

3. **Cenário 1.3 — Acesso a Rota Não Autorizada (Negativo - 403):**
   - **Passos:** Autenticado como Aluno (`aluno@example.com`), tentar navegar diretamente via barra de endereços para `/gestao-calendario` ou disparar `GET /api/v1/prazos/configuracoes`.
   - **Resultado Esperado:** Acesso bloqueado pelo componente de rotas com tela de "Acesso Negado" e resposta da API com `HTTP 403 Forbidden`.
   - **Resultado Obtido:** **Aprovado**. Componente `ProtectedRoute` barrou a rota e exibiu a view de Acesso Negado.

4. **Cenário 1.4 — Preservação de Rota e Parâmetros pós-login (BUG-18 / PR #297):**
   - **Passos:** Sem sessão ativa, tentar acessar `/acompanhar/15?tab=orientadores#topo`.
   - **Resultado Esperado:** Redirecionamento para `/login`. Ao informar as credenciais com sucesso, o sistema redireciona automaticamente de volta para `/acompanhar/15?tab=orientadores#topo` com rota, query parameters e hash íntegros.
   - **Resultado Obtido:** **Aprovado**. Validado no Vitest e manualmente.

- **Evidências de Teste:** Testes de componente `Login.test.tsx` e `ProtectedRoute.test.tsx` (17/17 passando); verificações 1, 2 e 3 de `smoke-test.sh` (tokens obtidos com sucesso).
- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-02
HISTÓRIA / REQUISITO: US02 — Cadastro e Perfil de Orientador e Controle de Vagas (5 SP)
ISSUES RELACIONADAS: #9 (Épico), #36 a #43, #48 a #53, #273 (BUG-11), #277/#279
========================================================================================
```

- **Objetivo:** Verificar a persistência, consulta e edição de perfil de docente, limite de caracteres nos campos de cadastro, vinculação do e-mail ao JWT autenticado e gestão do contador de vagas disponíveis.
- **Tipo de Execução:** Manual (UI) e Automatizada (`OrientadorControllerIntegrationTest.java`, `OrientadorServiceTest.java`, `OrientadorEdicao.test.tsx`).
- **Pré-condições:** Banco inicial carregado com migração `V5` (professores com `id=1` e `id=2`).

#### Cenários Executados:

1. **Cenário 2.1 — Consulta e Atualização do Próprio Perfil (Caminho Feliz):**
   - **Passos:** Logado como `professor.orientador@example.com`, acessar "Meu Perfil" (`/orientador/editar`), alterar a biografia para _"Docente permanente com foco em Arquitetura de Software"_ e salvar.
   - **Resultado Esperado:** Dados atualizados com `HTTP 200 OK`. O sistema vincula a alteração ao e-mail contido no token JWT ([ADR-0012](adrs/ADR-0012.md)).
   - **Resultado Obtido:** **Aprovado**. Perfil gravado e refletido imediatamente na tela.

2. **Cenário 2.2 — Violação de Limites de Tamanho nos DTOs (Negativo - 400 - BUG-11):**
   - **Passos:** Enviar requisição de edição com campo `nome` contendo mais de 255 caracteres ou campo `biografia` vazio/espaços.
   - **Resultado Esperado:** Validação estrita via `@Size(max = 255)` e `@NotBlank` rejeita a requisição retornando `HTTP 400 Bad Request` com lista detalhada de erros no `ErrorResponseDTO`.
   - **Resultado Obtido:** **Aprovado**. Validado em 9 testes unitários em `OrientadorControllerTest`.

3. **Cenário 2.3 — Tentativa de Modificar Perfil de Outro Docente (Negativo - 403):**
   - **Passos:** Autenticado como Professor `ID 1`, disparar `PUT /api/v1/orientadores/2` tentando alterar os dados do Professor `ID 2`.
   - **Resultado Esperado:** A camada de serviço valida a titularidade do recurso e rejeita a operação com `HTTP 403 Forbidden` (_"Você não possui permissão para alterar o perfil de outro professor."_).
   - **Resultado Obtido:** **Aprovado**.

- **Evidências de Teste:** `OrientadorControllerIntegrationTest.java` (passando), `OrientadorEdicao.test.tsx` (passando), verificação 4 do `smoke-test.sh`.
- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-03
HISTÓRIA / REQUISITO: US03 — Busca e Filtragem de Orientadores (3 SP)
ISSUES RELACIONADAS: #10 (Épico), #133, #134, #135, #136, #264 (BUG-14)
========================================================================================
```

- **Objetivo:** Validar a consulta pública/autenticada da vitrine de orientadores, filtro por linha de pesquisa com correspondência exata case-insensitive e filtro booleano por disponibilidade de vagas.
- **Tipo de Execução:** Manual (UI) e Automatizada (`OrientadorRepositoryTest.java`, `Orientadores.test.tsx`).
- **Pré-condições:** Pelo menos 2 orientadores cadastrados com linhas de pesquisa distintas (ex.: `Arquitetura de Sistemas` e `Inteligência Artificial`).

#### Cenários Executados:

1. **Cenário 3.1 — Filtro Exato por Linha de Pesquisa (BUG-14 / PR #270):**
   - **Passos:** Acessar a tela de orientadores (`/orientadores`). No campo de busca, digitar `"Arquitetura de Sistemas"` e aplicar o filtro. Em seguida, testar com variações de maiúsculas/minúsculas (`"arquitetura de sistemas"`).
   - **Resultado Esperado:** Apenas orientadores que contenham exatamente essa linha de pesquisa são listados (`Professor Orientador` - ID 1). Não devem ocorrer falsos positivos por correspondência parcial de palavras soltas.
   - **Resultado Obtido:** **Aprovado**. Query JPQL estrita no repositório retornou exatamente os docentes compatíveis.

2. **Cenário 3.2 — Filtro por Vagas Disponíveis:**
   - **Passos:** Marcar a opção `"Somente com vagas disponíveis"`.
   - **Resultado Esperado:** Orientadores com `vagasDisponiveis > 0` são exibidos; docentes com vagas zeradas são ocultados.
   - **Resultado Obtido:** **Aprovado**.

3. **Cenário 3.3 — Busca sem Resultados (Estado Vazio):**
   - **Passos:** Digitar uma linha de pesquisa inexistente, ex.: `"Computação Quântica Aplicada"`.
   - **Resultado Esperado:** A interface apresenta estado vazio amigável (_"Nenhum orientador encontrado para os filtros selecionados"_) e botão para limpar os filtros.
   - **Resultado Obtido:** **Aprovado**.

- **Evidências de Teste:** `OrientadorRepositoryTest.java` (passando), `Orientadores.test.tsx` (passando).
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
- **Tipo de Execução:** Manual (UI) e Automatizada (`SolicitacaoControllerIntegrationTest.java`, `AtualizadorVagasObserverTest.java`, `SolicitacaoConcorrenciaIntegrationTest.java`, `PainelSolicitacoes.test.tsx`).
- **Pré-condições:** Orientador ativo com 3 vagas cadastradas; aluno autenticado.

#### Cenários Executados:

1. **Cenário 4.1 — Envio e Aceite de Solicitação com Decremento de Vagas (Caminho Feliz):**
   - **Passos:** Logado como `aluno@example.com`, enviar solicitação para o orientador ID 1 com tema e mensagem. Em seguida, logar como `professor.orientador@example.com`, acessar `/painel-solicitacoes` e clicar em **"Aceitar"**.
   - **Resultado Esperado:** Solicitação criada com status `PENDENTE` (`HTTP 201`). Ao aceitar (`HTTP 200`), o evento `SolicitacaoStatusChangedEvent` é publicado e o `AtualizadorVagasObserver` decrementa imediatamente as vagas de 3 para 2 dentro da mesma transação de banco.
   - **Resultado Obtido:** **Aprovado**. Vagas atualizadas para 2 na interface e no banco de dados (verificações 5 a 8 do `smoke-test.sh`).

2. **Cenário 4.2 — Recusa sem Justificativa (Negativo - 422):**
   - **Passos:** No painel do orientador, acionar a recusa de uma solicitação com campo `justificativa` em branco ou ausente.
   - **Resultado Esperado:** Rejeição com `HTTP 422 Unprocessable Entity` com a mensagem: _"A justificativa é obrigatória para recusar a solicitação."_
   - **Resultado Obtido:** **Aprovado**. Validação de negócio executada sem efeitos colaterais nas vagas.

3. **Cenário 4.3 — Solicitação Duplicada (Negativo - 409 - Strategy):**
   - **Passos:** O mesmo aluno submeter uma nova solicitação para um orientador para o qual já possui solicitação `PENDENTE` ou `ACEITA`.
   - **Resultado Esperado:** A estratégia `ValidacaoSolicitacaoDuplicada` intercepta o envio e retorna `HTTP 409 Conflict`.
   - **Resultado Obtido:** **Aprovado**.

4. **Cenário 4.4 — Concorrência na Última Vaga (Negativo - 409 - Bloqueio Otimista):**
   - **Passos:** Orientador com 1 vaga restante receber duas requisições simultâneas de aceite.
   - **Resultado Esperado:** Uma requisição obtém sucesso (`HTTP 200`) e a concorrente recebe `HTTP 409 Conflict` via `OptimisticLockingFailureException` (`@Version` na tabela `perfis_professores`, migração `V8`).
   - **Resultado Obtido:** **Aprovado**. Comprovado por `SolicitacaoConcorrenciaIntegrationTest.java`.

- **Evidências de Teste:** `SolicitacaoControllerIntegrationTest.java` (passando), `AtualizadorVagasObserverTest.java` (passando), `SolicitacaoConcorrenciaIntegrationTest.java` (passando).
- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-05
HISTÓRIA / REQUISITO: US05 — Calendário de Prazos de Entrega do TCC (5 SP)
ISSUES RELACIONADAS: #12 (Épico), #137 a #142, #145 a #158, #276 (BUG-20)
========================================================================================
```

- **Objetivo:** Verificar a gestão de etapas e datas limites (`PROJETO_TCC`, `TCC1`, `TCC2`), identificação do professor responsável pela turma vigente, contagem regressiva e validações temporais.
- **Tipo de Execução:** Manual (UI) e Automatizada (`PrazoEntregaControllerIntegrationTest.java`, `GestaoCalendario.test.tsx`).
- **Pré-condições:** Turma vigente `2026/2` vinculada ao professor responsável (`ID 2`).

#### Cenários Executados:

1. **Cenário 5.1 — Consulta Pública/Geral dos Prazos da Turma (Caminho Feliz):**
   - **Passos:** Qualquer usuário autenticado (aluno ou professor) consulta os prazos através de `GET /api/v1/prazos`.
   - **Resultado Esperado:** Retorno da lista de prazos ordenados cronologicamente com tipo de etapa, data limite e contagem regressiva em dias.
   - **Resultado Obtido:** **Aprovado**. Componente de contagem regressiva exibido no catálogo de orientadores e no painel de entregas.

2. **Cenário 5.2 — Atualização de Prazo pelo Professor Responsável (Caminho Feliz):**
   - **Passos:** Autenticado como `professor.responsavel@example.com`, acessar `/gestao-calendario`, selecionar a etapa `PROJETO_TCC` e atualizar a data limite para uma data futura válida.
   - **Resultado Esperado:** Atualização concluída com `HTTP 200 OK`.
   - **Resultado Obtido:** **Aprovado** (verificação 10 do `smoke-test.sh`).

3. **Cenário 5.3 — Professor Não Responsável Tenta Alterar Prazo (Negativo - 403):**
   - **Passos:** Autenticado como `professor.orientador@example.com` (que não é o responsável pela turma), tentar disparar `POST` ou `PATCH` em `/api/v1/prazos`.
   - **Resultado Esperado:** O backend identifica `responsavelTurma: false` e bloqueia a requisição com `HTTP 403 Forbidden`.
   - **Resultado Obtido:** **Aprovado** (verificação 11 do `smoke-test.sh`).

4. **Cenário 5.4 — Data Limite no Passado (Negativo - 400):**
   - **Passos:** Enviar atualização de prazo informando uma data anterior ao dia atual (ex.: `2025-01-01`).
   - **Resultado Esperado:** Rejeição com `HTTP 400 Bad Request` informando que a data limite deve estar no futuro.
   - **Resultado Obtido:** **Aprovado**.

- **Evidências de Teste:** `PrazoEntregaControllerIntegrationTest.java` (passando), `GestaoCalendario.test.tsx` (passando).
- **Status do Caso:** **Aprovado**.

---

```
========================================================================================
CASO DE TESTE: AT-06
HISTÓRIA / REQUISITO: US06 — Submissão de Entregas por Link Externo HTTPS (3 SP)
ISSUES RELACIONADAS: #13 (Épico), #208, #209, #216, #218, #223, #226, #228
========================================================================================
```

- **Objetivo:** Validar o envio de trabalhos pelos alunos por meio de link externo, exigência mandatória de protocolo seguro HTTPS ([ADR-0013](adrs/ADR-0013.md)), cálculo automático da flag de atraso com base no prazo da turma e consulta de histórico próprio.
- **Tipo de Execução:** Manual (UI) e Automatizada (`EntregaControllerIntegrationTest.java`, `SubmissaoEntrega.test.tsx`).
- **Pré-condições:** Aluno com solicitação `ACEITA` por um orientador e etapa vigente configurada (`PROJETO_TCC`).

#### Cenários Executados:

1. **Cenário 6.1 — Submissão no Prazo com Link HTTPS (Caminho Feliz):**
   - **Passos:** Logado como Aluno, acessar `/minhas-entregas`, selecionar o tipo `PROJETO_TCC`, informar o link `https://drive.google.com/file/d/meu-tcc-v1/view` e submeter.
   - **Resultado Esperado:** Entrega criada com `HTTP 201 Created`, status `SUBMETIDO`, `enviadoEm` preenchido pelo servidor com `Clock` injetado, orientador associado automaticamente e flag `atrasada: false`.
   - **Resultado Obtido:** **Aprovado** (verificações 12 e 13 do `smoke-test.sh`).

2. **Cenário 6.2 — Submissão com Protocolo Inseguro HTTP (Negativo - 400):**
   - **Passos:** Tentar enviar submissão informando URL com protocolo inseguro: `http://meusite.com/tcc.pdf`.
   - **Resultado Esperado:** A anotação `@URL(protocol = "https")` intercepta a requisição e retorna `HTTP 400 Bad Request` (_"O link de submissão deve utilizar obrigatoriamente o protocolo HTTPS."_).
   - **Resultado Obtido:** **Aprovado** (verificação 14 do `smoke-test.sh`).

3. **Cenário 6.3 — Aluno Tenta Consultar Entregas de Outro Aluno (Negativo - 403):**
   - **Passos:** Autenticado com Aluno A, tentar acessar `GET /api/v1/entregas/{id}` correspondente à entrega do Aluno B.
   - **Resultado Esperado:** A camada de serviço valida a propriedade do recurso e bloqueia com `HTTP 403 Forbidden`.
   - **Resultado Obtido:** **Aprovado**.

- **Evidências de Teste:** `EntregaControllerIntegrationTest.java` (passando), `SubmissaoEntrega.test.tsx` (passando).
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
- **Tipo de Execução:** Manual (UI) e Automatizada (`EntregaParecerIntegrationTest.java`, `AvaliacaoEntrega.test.tsx`).
- **Pré-condições:** Entrega submetida pelo aluno com status `SUBMETIDO`.

#### Cenários Executados:

1. **Cenário 7.1 — Parecer com Ajustes Necessários e Reenvio do Aluno (Caminho Feliz):**
   - **Passos:** Logado como `professor.orientador@example.com`, acessar `/avaliar-entregas`, selecionar o trabalho do orientando e registrar parecer `AJUSTES_NECESSARIOS` com comentário obrigatório. Em seguida, logar como Aluno, visualizar o parecer e reenviar novo link `https://drive.google.com/file/d/meu-tcc-v2/view`.
   - **Resultado Esperado:** Parecer registrado com `HTTP 200 OK`, status da entrega muda para `AJUSTES_NECESSARIOS`. Ao reenviar, a entrega volta para `SUBMETIDO` e o novo link substitui o anterior.
   - **Resultado Obtido:** **Aprovado** (verificações 17 a 20 do `smoke-test.sh`).

2. **Cenário 7.2 — Parecer de Ajustes sem Comentário (Negativo - 422):**
   - **Passos:** O orientador submeter parecer `AJUSTES_NECESSARIOS` ou `REPROVADO` deixando o comentário vazio.
   - **Resultado Esperado:** Rejeição com `HTTP 422 Unprocessable Entity` informando que o comentário é obrigatório quando há exigência de ajustes ou reprovação.
   - **Resultado Obtido:** **Aprovado** (verificação 19 do `smoke-test.sh`).

3. **Cenário 7.3 — Aprovação Final e Bloqueio de Alterações Posteriores (Caminho Feliz + 409):**
   - **Passos:** O orientador emitir parecer `APROVADO`. Em seguida, o orientador tentar emitir novo parecer na mesma entrega ou o aluno tentar reenviar link.
   - **Resultado Esperado:** Parecer `APROVADO` encerra o ciclo daquela etapa (`HTTP 200`). Novas tentativas de submissão ou avaliação retornam `HTTP 409 Conflict`.
   - **Resultado Obtido:** **Aprovado** (verificações 21 e 22 do `smoke-test.sh`).

4. **Cenário 7.4 — Professor Não Vinculado Tenta Avaliar Entrega (Negativo - 403):**
   - **Passos:** Um docente que não é o orientador daquele aluno tentar disparar `POST /api/v1/entregas/{id}/parecer`.
   - **Resultado Esperado:** Acesso negado com `HTTP 403 Forbidden` (_"Apenas o orientador responsável pode avaliar esta entrega."_).
   - **Resultado Obtido:** **Aprovado**.

- **Evidências de Teste:** `EntregaParecerIntegrationTest.java` (passando), `AvaliacaoEntrega.test.tsx` (passando).
- **Status do Caso:** **Aprovado**.

---

## 5. Evidências de Execução Automatizada

### 5.1. Execução do Script de Smoke Test (`SIGTCC/scripts/smoke-test.sh`)

O script de homologação oficial executou **22 verificações automatizadas de ponta a ponta** contra os contêineres e a infraestrutura de staging, validando todos os casos de teste:

```text
=======================================================
   SIGTCC — Execução Oficial de Smoke Test (Entrega 9)
=======================================================
[OK] 01. Login Aluno (ROPC) -> Token JWT emitido (ROLE_ALUNO)
[OK] 02. Login Professor Orientador (ROPC) -> Token JWT emitido (ROLE_PROFESSOR)
[OK] 03. Login Professor Responsavel (ROPC) -> Token JWT emitido (ROLE_PROFESSOR)
[OK] 04. Consulta de perfil do orientador ID 1 (GET /api/v1/orientadores/1)
[OK] 05. Aluno envia solicitacao de orientacao -> 201 Created (ID gerado)
[OK] 06. Orientador lista solicitacoes recebidas -> Solicitacao pendente encontrada
[OK] 07. Orientador aceita solicitacao -> 200 OK (status ACEITA)
[OK] 08. Observer decrementa vagas do orientador -> Vagas reduzidas de 3 para 2
[OK] 09. Consulta de prazos de entrega -> Lista ordenada obtida (200 OK)
[OK] 10. Professor responsavel atualiza prazo -> 200 OK
[OK] 11. Professor comum tenta alterar prazo -> Bloqueado com 403 Forbidden
[OK] 12. Aluno consulta etapas liberadas para entrega -> PROJETO_TCC disponivel
[OK] 13. Aluno submete entrega com link HTTPS -> 201 Created (SUBMETIDO)
[OK] 14. Aluno tenta submeter link HTTP inseguro -> Rejeitado com 400 Bad Request
[OK] 15. Orientador consulta submissoes de seus orientandos -> Entrega listada
[OK] 16. Outro aluno tenta consultar entrega alheia -> Bloqueado com 403 Forbidden
[OK] 17. Orientador emite parecer AJUSTES_NECESSARIOS com comentario -> 200 OK
[OK] 18. Aluno consulta status da entrega -> Atualizado para AJUSTES_NECESSARIOS
[OK] 19. Orientador tenta emitir parecer de ajuste sem comentario -> 422 Unprocessable Entity
[OK] 20. Aluno reenvia link revisado HTTPS -> 200 OK (retorna para SUBMETIDO)
[OK] 21. Orientador emite parecer final APROVADO -> 200 OK
[OK] 22. Tentativa de reenvio apos aprovacao definitiva -> Bloqueada com 409 Conflict

Resultado: 22 de 22 verificações APROVADAS (100% de sucesso).
```

### 5.2. Suíte de Testes Unitários e de Integração do Backend (Gradle)

- **Comando de Execução:** `./gradlew test`
- **Resultados:** 100% dos testes aprovados, cobrindo controladores MockMvc, serviços, repositórios Data JPA, observadores síncronos e validações Strategy.
- **Relatório JaCoCo:** Cobertura de linhas do backend superior a **90%** (meta da métrica M-01: ≥ 80%).

### 5.3. Suíte de Testes de Componentes e Rotas do Frontend (Vitest)

- **Comando de Execução:** `npm test`
- **Resultados:** 100% dos testes de componentes React aprovados com React Testing Library, cobrindo formulários, interceptação de erros da API, autenticação e proteção de rotas com preservação de estado.
- **Cobertura Vitest:** Superior a **80%** de linhas (meta da métrica M-01: ≥ 70%).

---

## 6. Conclusão e Parecer de Aceitação

Com base na execução sistemática dos casos de teste **AT-01 a AT-07**, na validação integral dos cenários de autorização e de regras de negócio, e na comprovação automatizada via suítes de teste de CI e `smoke-test.sh`, declara-se que:

> **O Produto Mínimo Viável (MVP) do SIGTCC na versão `v1.0.0-rc.1` (Release Candidate) cumpre integralmente todos os critérios de aceitação estabelecidos para as histórias US01 a US07, estando APROVADO para a homologação final e apresentação perante a banca avaliadora.**
