# Sprint 4 - Entrega 8

- **Data de Início:** 03/11/2026
- **Data da Entrega:** 11/11/2026
- **Scrum Master da Sprint 4:** Willian Ferreira dos Santos
- **Marco de Release:** `v0.4.0`
- **Branch de Trabalho:** `entrega-8`

> Este documento é vivo: criado no início da Sprint 4 com o planejamento inicial (seções 1 a 6) e atualizado ao longo da sprint com o andamento das atividades, impedimentos, replanejamentos (seção 7), Pull Requests (seção 8), retrospectiva (seção 9) e o incremento final entregue (seção 10).

---

## 1. Inspeção: Estado do MVP ao Final da Sprint 3

### 1.1 Funcionalidades

| História                                        |     Estimativa     | Estado ao final da Sprint 3 | Observação                                                                                   |
| :---------------------------------------------- | :----------------: | :-------------------------- | :------------------------------------------------------------------------------------------- |
| US01 - Autenticação e Perfis                    | 5 SP (a reestimar) | Concluída                   | Login via Keycloak com `ROLE_ALUNO` e `ROLE_PROFESSOR`, rotas `/api/**` protegidas.          |
| US02 - Cadastro de Perfil de Orientador e Vagas |        5 SP        | Concluída (Sprint 1)        | CRUD de orientadores, catálogo e edição de perfil.                                           |
| US03 - Busca e Filtragem de Orientadores        |        3 SP        | Concluída                   | Filtro por linha de pesquisa, por vagas disponíveis, combinação e estado vazio.              |
| US04 - Solicitação e Aceite de Orientação       |        5 SP        | Concluída (Sprint 2)        | Envio, listagem, aceite/recusa e acompanhamento.                                             |
| US05 - Calendário de Prazos                     |     A estimar      | Concluída                   | CRUD de prazos por tipo de entrega, vinculado à `Turma_TCC`, visível para aluno e professor. |
| US06 - Submissão de Entregas do TCC             |    Não estimada    | Não iniciada                | Escopo retomado nesta sprint, com submissão por link externo HTTPS (seção 3).                |
| US07 - Avaliação e Parecer das Entregas         |    Não estimada    | Não iniciada                | Escopo retomado nesta sprint (seção 3).                                                      |

Ao final da Sprint 3, o MVP entrega o fluxo completo de autenticação, catálogo, solicitação/aceite de orientação e calendário de prazos. As duas histórias que faltam para fechar o ciclo de acompanhamento do TCC são a submissão do link externo do trabalho pelo aluno (US06) e a avaliação com emissão de parecer pelo orientador (US07), que haviam ficado no backlog desde a Sprint 2.

### 1.2 Riscos herdados da Sprint 3

Situação registrada em [`docs/riscos.md`](../riscos.md) na revisão da Sprint 3, relevante para o planejamento da Sprint 4:

| Risco                                                         | Prioridade  | Situação ao final da Sprint 3                                                                                              |
| :------------------------------------------------------------ | :---------: | :------------------------------------------------------------------------------------------------------------------------- |
| R01 - Desistência ou baixa participação                       | Crítica (9) | Concretizado (baixa participação de um integrante durante a Sprint 3). Exige atenção na distribuição de carga da Sprint 4. |
| R04 - Crescimento do escopo                                   | Crítica (9) | Concretizado (escopo maior que o baseline original). Reforça a necessidade de ajustar escopo à capacidade.                 |
| R03 - Atraso de integração backend/frontend                   |  Média (3)  | Mitigado pela prática de contrato de dados no início da sprint; mantida na Sprint 4.                                       |
| R05 - Divergência de ambientes (Docker)                       |  Média (4)  | Mitigado, com atenção à configuração do ambiente de staging e à padronização da infraestrutura nesta sprint.               |
| R09 - Complexidade e instabilidade na integração com Keycloak | Crítica (9) | Ativo; acompanhado pelo DevOps/Infra e pela QA.                                                                            |

R01 e R04 se reforçam: a equipe segue com menos capacidade do que o planejado no baseline, na mesma sprint em que o escopo de infraestrutura (staging e CI) aumenta. Por isso, o planejamento da Sprint 4 distribui o trabalho em issues pequenas, com responsáveis definidos por atividade e dependências explícitas, reduzindo a dependência de qualquer integrante isolado.

---

## 2. Meta da Sprint 4

> **"Fechar o ciclo de acompanhamento do TCC: permitir que o aluno registre o link externo seguro (URL HTTPS) de acesso ao seu trabalho, dentro ou fora do prazo (US06), e que o orientador acesse o trabalho por meio desse link e registre um parecer sobre a entrega (US07), contemplando também as entregas obrigatórias de DevOps da sprint (ambiente de staging, pipeline de CI atualizado e documentação de deploy em `docs/DEPLOY.md`) e a governança do projeto (métricas e riscos) atualizadas."**

---

## 3. Escopo da Sprint 4

### 3.1 Histórias selecionadas

| Item                                    | Issue | Estimativa |
| :-------------------------------------- | :---: | :--------: |
| US06 - Submissão de Entregas do TCC     |  #13  | A estimar  |
| US07 - Avaliação e Parecer das Entregas |  #14  | A estimar  |

### 3.2 Regras de negócio definidas para a Sprint 4

A equipe definiu as seguintes regras de negócio e decisões de implementação antes do início da implementação:

- **Estratégia de submissão (US06):** o aluno informa um link externo para acessar o trabalho, em vez de enviar um arquivo ao sistema. O endereço deve utilizar o protocolo HTTPS, conforme definido na ADR-0013 e no contrato de dados da US06 (`docs/contrato-dados-us06.md`). A URL será validada com a anotação `@URL(protocol = "https")` e armazenada no banco de dados em uma coluna `VARCHAR(2048)`. O sistema não realizará upload, download ou armazenamento de arquivos binários.
- **Regra de atraso (US06):** o sistema permite a submissão do link mesmo após o prazo limite. Uma submissão fora do prazo é marcada com uma flag (`atrasada`) no banco de dados e exibida no front-end como "Entregue com atraso". Não há bloqueio de submissão por atraso.
- **Regra de reenvio (US06):** o aluno poderá informar um novo link para a mesma etapa somente quando o último parecer do orientador possuir o resultado `AJUSTES_NECESSARIOS`. Nesse caso, o sistema atualizará a entrega existente, substituindo o link anterior, retornando o status para `SUBMETIDO`, recalculando a flag `atrasada` e atualizando o campo `enviadoEm`. O último parecer registrado será mantido até que uma nova avaliação seja realizada. Quando o resultado for `APROVADO` ou `REPROVADO`, a etapa será encerrada e não permitirá novos envios. Enquanto a entrega estiver com status `AJUSTES_NECESSARIOS`, aguardando o reenvio pelo aluno, não será permitido registrar um novo parecer. Após o reenvio, a entrega retornará ao status `SUBMETIDO` e poderá ser avaliada novamente pelo orientador.
- **Acesso ao link externo (US06/US07):** o aluno deve fornecer um link com permissões de acesso adequadas para que o orientador consiga visualizar o trabalho. A interface deverá apresentar instruções para orientar o aluno sobre a configuração dessas permissões.
- **Notificações (US07):** não há disparo de e-mail nesta sprint. A mudança de status do parecer é refletida apenas visualmente no painel do aluno.

### 3.3 Fatiamento do trabalho

Para permitir Pull Requests pequenos e rastreabilidade individual, as duas histórias foram fatiadas em 17 issues técnicas e de governança, distribuídas entre DevOps, Backend, Frontend e QA. Além dessas atividades, a issue #207 contempla o planejamento inicial e a condução da Sprint 4, sob responsabilidade do Scrum Master. O detalhamento das atividades, critérios de aceitação, estimativas, cronogramas, dependências e responsáveis está registrado nas respectivas issues do GitHub Projects.

| Issue                                                                   | Área                | Responsável     | Início     | Fim        |
| ----------------------------------------------------------------------- | ------------------- | --------------- | ---------- | ---------- |
| #207 — Planejamento inicial e condução da Sprint 4                      | Scrum / Governança  | Willian         | 03/11/2026 | 11/11/2026 |
| #208 — Modelagem de dados: Entrega (Flyway V6)                          | Backend             | Damares         | 03/11/2026 | 04/11/2026 |
| #209 — Contratos de dados das APIs (US06 e US07)                        | Backend             | Damares, Talles | 03/11/2026 | 04/11/2026 |
| #218 — Configuração de validação de link e variáveis de ambiente (US06) | DevOps              | Eduardo         | 03/11/2026 | 03/11/2026 |
| #223 — Migration e entidades de submissão (US06)                        | Backend             | Damares         | 03/11/2026 | 04/11/2026 |
| #219 — CI pipeline para entrega-8                                       | DevOps              | Eduardo         | 04/11/2026 | 05/11/2026 |
| #216 — Service e endpoint de submissão por link (US06)                  | Backend             | Talles          | 04/11/2026 | 06/11/2026 |
| #226 — Tela de submissão de link de entrega (US06)                      | Frontend            | Marcus          | 05/11/2026 | 06/11/2026 |
| #220 — Configuração de ambiente de staging acessível                    | DevOps              | Eduardo         | 06/11/2026 | 09/11/2026 |
| #210 — Endpoint de consulta da submissão para o orientador (US07)       | Backend             | Talles          | 06/11/2026 | 09/11/2026 |
| #228 — Cenários de integração: submissão por link (US06)                | QA                  | Willian         | 06/11/2026 | 09/11/2026 |
| #215 — Migration e endpoint de parecer (US07)                           | Backend             | Damares, Talles | 09/11/2026 | 10/11/2026 |
| #227 — Tela de avaliação e parecer do orientador (US07)                 | Frontend            | Marcus          | 09/11/2026 | 10/11/2026 |
| #221 — docs/DEPLOY.md com .env.example consolidado                      | DevOps              | Eduardo         | 09/11/2026 | 10/11/2026 |
| #230 — docs/METRICAS.md e métricas atualizados                          | Governança/Processo | Willian         | 10/11/2026 | 11/11/2026 |
| #229 — Cenários de integração: avaliação e parecer (US07)               | QA                  | Willian         | 10/11/2026 | 11/11/2026 |
| #231 — Revisar docs/riscos.md para a Sprint 4                           | Governança/Processo | Willian         | 10/11/2026 | 11/11/2026 |
| #222 — Release v0.4.0 (Sprint 4 / Entrega 8)                            | DevOps              | Eduardo         | 11/11/2026 | 11/11/2026 |

---

## 4. Fundamentação

- **Backlog:** US06 e US07 são as duas últimas histórias do fluxo central do MVP (catálogo, orientação, calendário e, agora, acompanhamento de entregas) ainda não iniciadas, conforme [`docs/BASELINE.md`](../BASELINE.md) e a inspeção da Sprint 3.
- **Riscos:** R01 e R04, concretizados na Sprint 3, orientam o fatiamento em atividades pequenas, com responsabilidades definidas e dependências explícitas (seção 3.3), reduzindo a exposição da sprint à ausência de um integrante. R09 (Keycloak) segue acompanhado, já que US06 e US07 dependem da autorização por papel implementada na Sprint 3.
- **Entregas obrigatórias de DevOps:** a Sprint 4 inclui, além do código das histórias, as entregas de infraestrutura exigidas pela disciplina: ambiente de staging acessível, atualização do pipeline de CI, documentação de deploy (`docs/DEPLOY.md` com `.env.example`) e a release `v0.4.0`.
- **Governança:** a sprint reserva issues específicas para atualizar `docs/METRICAS.md` e `docs/riscos.md`, mantendo a documentação de processo alinhada ao estado real do projeto.

---

## 5. Riscos Considerados no Planejamento

- **R01 (desistência ou baixa participação):** concretizado na Sprint 3. Mitigação adotada nesta sprint: atividades pequenas, com responsabilidades definidas e dependências explícitas, de forma que o atraso de uma pessoa tenha impacto localizado e visível no board.
- **R04 (crescimento do escopo):** concretizado na Sprint 3. Contenção nesta sprint: o escopo de US06/US07 foi fechado antes do início da implementação (seção 3.2), sem abertura para novas funcionalidades durante a sprint.
- **R09 (instabilidade do Keycloak):** US06 e US07 dependem de autorização por papel (`ROLE_ALUNO`, `ROLE_PROFESSOR`) e de vínculo aluno/orientador. Mitigação: reaproveitar a configuração do realm já validada na Sprint 3, sem alterações no Keycloak nesta sprint.
- **Novo - Ambiente de staging único:** a Sprint 4 introduz um ambiente de staging compartilhado pela primeira vez. Se ficar indisponível, pode bloquear a validação das demais issues. Mitigação: smoke test logo após a subida (issue de staging) e ambiente local do `docker-compose.yml` como alternativa de validação.
- **Novo - Link externo privado ou sem permissão de acesso:** o link informado pelo aluno pode exigir autenticação, estar restrito a usuários específicos ou não permitir o acesso pelo orientador. Isso pode impedir a avaliação da entrega mesmo quando a submissão foi realizada corretamente. Mitigação: apresentar instruções na interface para que o aluno configure as permissões de compartilhamento do link e permita a visualização pelo orientador, conforme a ADR-0013.
- **Novo - Cronograma concentrado entre 09/11 e 11/11:** as issues de Backend (parecer), Frontend (avaliação), DEPLOY.md, QA de US07, métricas, riscos e release se concentram nos últimos três dias da sprint. Mitigação: priorizar o início antecipado das issues de Backend e Infra que não dependem de US06 estar 100% concluída.
- **Novo - Alteração do conteúdo após o prazo:** o conteúdo do trabalho hospedado em serviços externos pode ser modificado após o encerramento do prazo, mesmo que o timestamp da submissão do link permaneça inalterado. Mitigação: registrar o timestamp da submissão e orientar o aluno a fornecer links com acesso somente para leitura ou que identifiquem uma versão imutável do conteúdo, conforme a ADR-0013.

A revisão completa dos riscos será registrada em [`docs/riscos.md`](../riscos.md) ao final da sprint.

---

## 6. Governança e Fluxo de Trabalho

- **Ramificação base:** todo o trabalho da sprint parte da `entrega-8` e retorna a ela por Pull Request.
- **Fatiamento:** as atividades de implementação da seção 3.3 são organizadas em branches próprias, criadas a partir da `entrega-8`, com PRs pequenos e focados em atividades relacionadas. As atividades de Scrum e Governança são acompanhadas e registradas na documentação da sprint.
- **Critérios de merge:** ao menos uma aprovação de outro integrante, pipeline de CI verde e integração por commit de mesclagem.
- **Rastreabilidade:** cada PR referencia a issue correspondente.
- **Integração final:** PR revisado da `entrega-8` para a `main`, seguido da release `v0.4.0`.

### 6.1. Papéis e Responsabilidades

| Integrante | Papel na Sprint 4                           | Responsabilidades                                                                                                                                          |
| :--------- | :------------------------------------------ | :--------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Damares    | Arquiteta de Software / Backend             | Desenvolver as entidades e funcionalidades de backend relacionadas às submissões e aos pareceres.                                                          |
| Eduardo    | DevOps/Infraestrutura                       | Configurar as variáveis de ambiente para submissão por link, atualizar o pipeline de CI, preparar o ambiente de staging e documentar o processo de deploy. |
| Marcus     | Frontend                                    | Desenvolver as telas de submissão e avaliação das entregas.                                                                                                |
| Talles     | Backend                                     | Implementar os serviços e endpoints de submissão, consulta das entregas e avaliação por parecer.                                                           |
| Willian    | Scrum Master e Engenheiro de Qualidade (QA) | Acompanhar a sprint, facilitar o processo, acompanhar o board, realizar os testes de integração e atualizar a documentação de métricas e riscos.           |

---

## 7. Andamento das Atividades

> Seção a ser preenchida ao longo da sprint pelo Scrum Master, com o registro de impedimentos, replanejamentos e atualizações relevantes do board.

- 03/11/2026: Sprint iniciada. Planejamento inicial registrado (seções 1 a 6). Issues de implementação, infraestrutura, QA e governança organizadas no board.

### 7.1. Correções de Bugs

Além das atividades previstas no escopo inicial das US06 e US07, a Sprint 4 também contemplou a resolução de bugs identificados no sistema e registrados em issues específicas no GitHub. Essas correções foram planejadas para o período de 03/11/2026, com início e data-alvo definidos para o mesmo dia, e integradas à branch `entrega-8` por meio de Pull Requests.

As correções abrangeram problemas relacionados à autorização, ao controle de acesso, à integridade das informações e à estabilidade da aplicação.

| Issue | Bug identificado | Situação |
| :---: | :--- | :---: |
| [#199](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/199) | Qualquer usuário autenticado aceita ou recusa qualquer solicitação. | Concluída |
| [#200](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/200) | CRUD de orientadores aberto a qualquer usuário autenticado. | Concluída |
| [#201](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/201) | Solicitação criada com o e-mail do corpo da requisição, e não com o do usuário logado. | Concluída |
| [#202](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/202) | Qualquer usuário logado consegue ler qualquer solicitação. | Concluída |
| [#203](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/203) | Painel do professor mostra solicitações do orientador errado. | Concluída |
| [#204](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/204) | "Meu perfil" depende do `localStorage` e pode editar o perfil de outra pessoa. | Concluída |
| [#205](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/205) | Sessão expira em aproximadamente cinco minutos e as chamadas passam a falhar com HTTP 401. | Concluída |
| [#206](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/206) | Condição de corrida no controle de vagas do orientador. | Concluída |

Todas as oito issues foram concluídas e, conforme o acompanhamento da equipe, suas respectivas correções foram integradas à branch `entrega-8`. Essas atividades complementares contribuíram para reforçar a segurança do controle de acesso, a consistência dos dados e a estabilidade do sistema durante a Sprint 4.

---

## 8. Pull Requests da Sprint 4

> Seção a ser preenchida ao longo da sprint, com a lista de PRs abertos, revisados e integrados à `entrega-8`.

| PR  | Issue | Descrição | Status |
| :-- | :---- | :-------- | :----- |
| -   | -     | -         | -      |

---

## 9. Retrospectiva da Sprint 4

> Seção a ser preenchida ao final da sprint.

---

## 10. Incremento Entregue

> Seção a ser preenchida ao final da sprint, com os itens efetivamente entregues, os itens postergados ou removidos e as respectivas justificativas.

---

## 11. Contribuições Individuais Detalhadas

Cada integrante registra abaixo suas contribuições da Sprint 4, com as issues e os PRs como evidência. O detalhamento do uso de IA em cada artefato está em [`USO-IA.md`](../../USO-IA.md) (Entrega 8).

### 11.1 Damares do Socorro Gonçalves Gaia

- **Papel:** Arquiteta de Software / Backend.
- **Identificadores Git:** `Damares Gaia <damaresgaia@gmail.com>`, `121111955+damaresgaia@users.noreply.github.com` (usuária GitHub: `damaresgaia`).
- **Atividades e Entregas na Sprint 4:**
  - **Modelagem de Dados e Migração da Entidade Entrega (Issue #208 - PR #233):** Criação da migration Flyway `V6__criar_tabela_entrega.sql` modelando a tabela `entrega` vinculada à `turma_tcc`, suporte à URL externa HTTPS (ADR-0013), flag de submissão atrasada, anotações de validação e repositório `EntregaRepository`.
  - **Modelagem de Dados e Migração da Entidade Parecer (Issue #215 - PR #237):** Concepção da migration Flyway `V7__criar_tabela_parecer.sql` estruturando a entidade `Parecer`, enum `ResultadoParecer` (`APROVADO`, `REPROVADO`, `AJUSTES_NECESSARIOS`), relacionamento com `Entrega` e `Professor`, e cobertura completa com testes automatizados de integração JPA (`ParecerRepositoryTest`) validando integridade referencial, constraints e aplicação das migrations Flyway em ambiente de testes H2 configurado em modo de compatibilidade MySQL (`MODE=MySQL`).
  - **Especificação dos Contratos de Dados das APIs de US06 e US07 (Issue #209 - PR #234):** Elaboração dos documentos de especificação de contrato de API [`docs/contrato-dados-us06.md`](../contrato-dados-us06.md) (submissão de entregas por link externo HTTPS) e [`docs/contrato-dados-us07.md`](../contrato-dados-us07.md) (avaliação e emissão de parecer), definindo schemas de request/response, validações, códigos de status HTTP e cenários de teste BDD em colaboração com o backend.
  - **Correção da Renovação Automática de Token e Interceptor Frontend (Issue #205 - PR #225):** Atuação conjunta na identificação da causa raiz do loop infinito de requisições no `api.ts`, implementação do mecanismo de fila de requisições pendentes durante o refresh de token e configuração da renovação silenciosa transparente com tratamento seguro de concorrência.
  - **Atualização da Arquitetura do Sistema Real e Padrões de Projeto (Issue #239 - PR #245):** Atualização dos diagramas arquiteturais C4 ([`docs/diagramas/c2-conteineres.md`](../diagramas/c2-conteineres.md)) incorporando as rotas da release `v0.4.0` (US06 e US07), atualização do fluxo de refresh token e separação arquitetural de responsabilidades em [`docs/ARQUITETURA.md`](../ARQUITETURA.md) e [`ADR-0012`](../adrs/ADR-0012.md), além da documentação dos novos padrões estruturais (Value Object com `@Embeddable` e Interceptor com retry queue) em [`docs/PADROES-DE-PROJETO.md`](../PADROES-DE-PROJETO.md).
  - **Acompanhamento e Memória de Cálculo de Métricas (Issue #230):** Atualização da métrica [`M-06: Cumprimento da Capacidade`](../metricas/M-06.md) para a Sprint 4, com registro histórico nominal por integrante e consolidação de 41h executadas de 42h planejadas (97,6% de aderência - Meta Desejada).
  - **Revisão de Código (Code Review) e Governança:** Revisão técnica formal, validação de segurança e aprovação de Pull Requests fundamentais da sprint:
    - **PR #238 (Issue #216):** Service e endpoint de submissão de entrega por link (US06), revisado e aprovado com validação das regras de negócio de entrega atrasada e verificação de URL HTTPS.
    - **PR #240 (Issue #210):** Endpoint de consulta de submissão para orientador (US07), revisado e aprovado com validação de permissões de acesso do orientador.
    - **PR #243 (Issue #221):** Documentação de deploy (`docs/DEPLOY.md`) e script de smoke test, revisado em duas iterações técnicas rigorosas com apontamentos sobre a URL pública do Keycloak e alinhamento de variáveis de ambiente.

### 11.2 Eduardo Cardoso Oliveira

- **Papel:** Engenheiro de DevOps / Infraestrutura.
- **Identificadores Git:** `Eduardo Cardoso <eduardo.co22@aluno.ifsc.edu.br>` (usuário GitHub: `EduCardoso22`).
- **Atividades e Entregas na Sprint 4:**
  - **Validação de Links e Variáveis de Ambiente da US06 (Issue #218 - PR #235):**
    - Configuração e propagação segura da variável `APP_SUBMISSAO_DOMINIOS_SUGERIDOS` no backend Spring Boot (`application.properties`) e no `.env.example`, sem introduzir acoplamentos ou dependências desnecessárias de volume.
    - Atualização do [`docs/contrato-dados-us06.md`](../contrato-dados-us06.md), [`docs/ARQUITETURA.md`](../ARQUITETURA.md) e [`docs/qualidade.md`](../qualidade.md) para refletir a estratégia de submissão por link externo HTTPS (ADR-0013) com validação estrita `@URL(protocol = "https")` e persistência em coluna de 2048 caracteres.
    - Implementação de testes automatizados de validação de protocolo HTTPS, persistência e suporte a CORS.
    - Adequação das regras de segurança no `SecurityConfig.java` para o endpoint `/api/v1/entregas/**` com exigência de `ROLE_ALUNO`.
  - **Atualização do Pipeline de CI e Publicação de Imagens no GHCR (Issue #219 - PR #238):**
    - Criação do workflow [`.github/workflows/docker-build.yml`](../../.github/workflows/docker-build.yml) para automação do build multi-stage e publicação das imagens de Backend e Frontend no GitHub Container Registry (GHCR), com tagueamento determinístico (`latest` e SHA do commit).
    - Otimização do cache das camadas Docker utilizando a API nativa do GitHub Actions (`type=gha`), reduzindo expressivamente o tempo de build.
    - Configuração de isolamento de dependências com a diretiva `needs` interna no workflow, garantindo que o build e push de contêineres ocorram somente após a aprovação com sucesso dos jobs de testes unitários.
    - Atualização completa de [`docs/ci.md`](../ci.md), documentando os 5 workflows ativos, gatilhos de execução, políticas de bloqueio de branch e Quality Gates obrigatórios da `entrega-8`.
    - Atualização do template de Pull Request ([`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md)) para inclusão das verificações de CI e evidências da Sprint 4.
  - **Configuração e Provisionamento do Ambiente de Staging Acessível (Issue #220 - PR #244):**
    - Criação da orquestração de homologação/produção em [`SIGTCC/docker-compose.prod.yml`](../../SIGTCC/docker-compose.prod.yml), parametrizando variáveis de segurança do Keycloak (`KC_HTTP_ENABLED`, `KC_HOSTNAME_STRICT`, `KC_HOSTNAME`) e validação de schema do Hibernate (`hibernate.ddl-auto=validate`).
    - Integração e provisionamento de tunelamento seguro via **Cloudflare Tunnels**, expondo os serviços conteinerizados da VPS sob domínio seguro HTTPS (`staging.sigtcc.online`) sem necessidade de liberação de portas de firewall ou IPs públicos dedicados.
    - Elaboração do artefato oficial [`docs/staging.md`](../staging.md), documentando a arquitetura remota, credenciais administrativas, consumo de imagens GHCR e um roteiro executável de _Smoke Test_ via cURL/PowerShell para validação automatizada de saúde dos 4 componentes (MySQL 8.0, Keycloak 24 IAM, Backend API REST e Frontend SPA).
  - **Guia Oficial de Deploy e Consolidação de Variáveis de Ambiente (Issue #221 - PR #243):**
    - Redação integral e publicação do guia oficial de implantação [`docs/DEPLOY.md`](../DEPLOY.md), contemplando pré-requisitos, instruções passo a passo para deploy local e em staging a partir de ambiente limpo, configuração de `KEYCLOAK_PUBLIC_URL` e `webOrigins`, rotinas de smoke test e plano de rollback com comandos detalhados.
    - Consolidação definitiva do arquivo de referência [`.env.example`](../../.env.example), unificando todas as variáveis de configuração de ambiente de forma consistente, rastreável e auditável, eliminando valores sensíveis expostos.
    - Alinhamento de referências cruzadas entre `DEPLOY.md`, `staging.md` e os arquivos compose do projeto.
  - **Acompanhamento da Métrica de Processo M-04 — Lead Time de Pull Requests (Issue #230):**
    - Atualização da ficha técnica [`docs/metricas/M-04.md`](../metricas/M-04.md) com o histórico completo de Pull Requests da sprint (PRs #211 a #245), calculando os tempos exatos de ciclo entre criação e merge.
    - Elaboração da memória de cálculo da Sprint 4: média de **12,3h**, mediana de **5,7h**, 80% dos PRs Dentro da Meta ($\le 24$h), 20% na Faixa Aceitável ($24$h a $48$h) e **0% em Zona de Alerta**, alcançando **100% de conformidade com o SLA** e demonstrando a eficácia dos Quality Gates automatizados no GitHub Actions.
    - Análise histórica comparativa frente ao Baseline e às Sprints 1, 2 e 3, com diagnóstico específico do Lead Time das entregas de infraestrutura (média de 18,2h).
  - **Reavaliação dos Riscos Técnicos de DevOps e Infraestrutura (Issue #231):**
    - Reavaliação formal dos riscos sob responsabilidade de DevOps em [`docs/riscos.md`](../riscos.md): **R05** (Divergência de Ambientes e Instabilidade Docker) mitigado definitivamente pela padronização dos contêineres e documentação de deploy, e **R10** (Indisponibilidade do Ambiente de Staging) mitigado pela alta disponibilidade do Cloudflare Tunnels e roteiro de smoke tests.
  - **Revisão de Código (Code Review) e Governança:**
    - Atuação contínua na revisão técnica, verificação de qualidade de código, conformidade com os pipelines de CI e aprovação de Pull Requests de todos os integrantes integrados à `entrega-8`:
      - **PR #211 (Issue #200):** Fix de fechamento de endpoints abertos de usuário (Talles Souza).
      - **PR #213 (Issue #201):** Fix de extração de e-mail do aluno logado via token JWT (Talles Souza).
      - **PR #217 (Issue #204):** Fix de integridade de dados do usuário em LocalStorage (Talles Souza).
      - **PR #224 (Issue #202):** Fix de restrição de leitura de solicitações com validação de vínculo (Willian Ferreira).
      - **PR #233 (Issue #208):** Modelagem JPA, constraints e migração Flyway V6 da entidade Entrega (Damares Gaia).
      - **PR #234 (Issue #209):** Especificação dos contratos de dados das APIs de US06 e US07 (Damares Gaia / Talles Souza).
      - **PR #236 (Issue #216):** Service e endpoint `POST /api/v1/entregas` de submissão por link HTTPS (Talles Souza).
      - **PR #237 (Issue #215):** Modelagem da entidade Parecer, enum de resultado e migração Flyway V7 (Damares Gaia).
      - **PR #240 (Issue #210):** Endpoints de consulta de submissão para orientador e aluno (Talles Souza).
      - **PR #241 (Issue #206):** Tratamento de condição de corrida com bloqueio otimista `@Version` e migração Flyway V8 (Talles Souza).
      - **PR #242 (Issues #226/#227):** Interface frontend para submissão de link e avaliação de parecer com testes Vitest (Marcus Jhuan).
      - **PR #245 (Issue #239):** Atualização da documentação arquitetural C4 e padrões estruturais do sistema (Damares Gaia).

### 11.3 Marcus Jhuan Epifanio Lima

> Seção a ser preenchida pelo integrante.

### 11.4 Talles Souza da Cruz

- **Papel na sprint:** Desenvolvedor Backend.
- **Período:** 01/11/2026 a 11/11/2026.

#### Contratos de dados

- **Roles e Respostas de Erro nos Contratos de US06 e US07 (Issue #209 - PR #234):** Complemento dos contratos [`docs/contrato-dados-us06.md`](../contrato-dados-us06.md) e [`docs/contrato-dados-us07.md`](../contrato-dados-us07.md), elaborados em conjunto com Damares, detalhando por endpoint a role exigida e as respostas de erro.

#### Implementação da submissão de entregas (US06)

- **Service e Endpoint de Submissão por Link (Issue #216 - PR #236):** Implementação do `POST /api/v1/entregas`, com os DTOs de entrada e saída e a atualização da entidade `Entrega`. O endpoint valida a URL HTTPS, marca a entrega como `atrasada` quando enviada após o prazo e permite o reenvio somente quando o último parecer é `AJUSTES_NECESSARIOS`, substituindo o link e retornando a entrega ao status `SUBMETIDO`.

#### Implementação da avaliação e do parecer (US07)

- **Consulta das Submissões (Issue #210 - PR #240):** Implementação dos endpoints `GET /api/v1/entregas/orientador`, `GET /api/v1/entregas/minhas` e `GET /api/v1/entregas/{id}`, que permitem ao orientador consultar as entregas dos seus orientandos e ao aluno acompanhar as próprias entregas. Proteção das rotas por role no `SecurityConfig` e verificação do vínculo entre orientador, aluno e entrega.
- **Endpoint de Registro de Parecer (Issue #215 - PR #237):** Implementação do `POST /api/v1/entregas/{id}/parecer` sobre a modelagem da migração `V7` e da entidade `Parecer` feitas por Damares. O endpoint registra os resultados `APROVADO`, `AJUSTES_NECESSARIOS` e `REPROVADO`, exige comentário em ajustes e reprovação e bloqueia uma nova avaliação de entrega já encerrada. Para evitar que pareceres simultâneos sobrescrevam uma decisão final, a entrega é carregada com trava pessimista (`PESSIMISTIC_WRITE`). As respostas de entrega passaram a exibir o parecer mais recente.

#### Correção de defeitos

| Issue | PR   | Defeito                                                                                      | Correção                                                                                                                                                                                                                                      |
| :---- | :--- | :------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| #200  | #211 | Qualquer usuário autenticado podia alterar ou remover o cadastro de qualquer orientador.     | Restrição da escrita de orientadores a `ROLE_PROFESSOR` e ao próprio professor, identificado pelo e-mail do token. Contrato da US02 atualizado e testes de integração do `OrientadorController` adicionados.                                  |
| #201  | #213 | A solicitação de orientação identificava o aluno por dados enviados no formulário.           | Identificação do aluno pelo usuário autenticado (e-mail do token), com a remoção desses campos do formulário. Contrato da US04 atualizado e testes de integração e do `authService` adicionados.                                              |
| #204  | #217 | A tela "Meu perfil" buscava o orientador por um id salvo no `localStorage`.                  | Obtenção do id do perfil pelo endpoint `/professores/me`, com mensagem clara quando o usuário não possui perfil de orientador. Testes de `OrientadorEdicao` atualizados.                                                                      |
| #206  | #241 | Dois aceites simultâneos podiam ultrapassar o número de vagas do orientador (_lost update_). | Lock otimista com `@Version` em `PerfilOrientador`, migração `V8` e resposta `409` para o conflito. Teste de integração que reproduz a corrida e validação manual com MySQL real: de 5 aceites simultâneos com 1 vaga, apenas 1 foi aprovado. |

#### Revisão de código e governança

Integração (merge) à `entrega-8` dos PRs dos demais integrantes:

- **Damares Gaia:** PRs #212 e #214.
- **Eduardo Cardoso:** PRs #235 e #238.
- **Willian Ferreira:** PRs #232 e #246.

### 11.5 Willian Ferreira dos Santos

> Seção a ser preenchida pelo integrante.
