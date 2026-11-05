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
