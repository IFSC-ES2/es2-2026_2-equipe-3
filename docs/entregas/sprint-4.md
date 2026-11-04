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
| US06 - Submissão de Entregas do TCC             |    Não estimada    | Não iniciada                | Escopo retomado nesta sprint (seção 3).                                                      |
| US07 - Avaliação e Parecer das Entregas         |    Não estimada    | Não iniciada                | Escopo retomado nesta sprint (seção 3).                                                      |

Ao final da Sprint 3, o MVP entrega o fluxo completo de autenticação, catálogo, solicitação/aceite de orientação e calendário de prazos. As duas histórias que faltam para fechar o ciclo de acompanhamento do TCC são a submissão de documentos pelo aluno (US06) e a avaliação com parecer pelo orientador (US07), que haviam ficado no backlog desde a Sprint 2.

### 1.2 Riscos herdados da Sprint 3

Situação registrada em [`docs/riscos.md`](../riscos.md) na revisão da Sprint 3, relevante para o planejamento da Sprint 4:

| Risco                                                         | Prioridade  | Situação ao final da Sprint 3                                                                                              |
| :------------------------------------------------------------ | :---------: | :------------------------------------------------------------------------------------------------------------------------- |
| R01 - Desistência ou baixa participação                       | Crítica (9) | Concretizado (baixa participação de um integrante durante a Sprint 3). Exige atenção na distribuição de carga da Sprint 4. |
| R04 - Crescimento do escopo                                   | Crítica (9) | Concretizado (escopo maior que o baseline original). Reforça a necessidade de ajustar escopo à capacidade.                 |
| R03 - Atraso de integração backend/frontend                   |  Média (3)  | Mitigado pela prática de contrato de dados no início da sprint; mantida na Sprint 4.                                       |
| R05 - Divergência de ambientes (Docker)                       |  Média (4)  | Mitigado, com atenção à expansão da infraestrutura (novo volume de uploads nesta sprint).                                  |
| R09 - Complexidade e instabilidade na integração com Keycloak | Crítica (9) | Ativo; acompanhado pelo DevOps/Infra e pela QA.                                                                            |

R01 e R04 se reforçam: a equipe segue com menos capacidade do que o planejado no baseline, na mesma sprint em que o escopo de infraestrutura (staging, CI, volume de uploads) aumenta. Por isso, o planejamento da Sprint 4 distribui o trabalho em issues pequenas, com responsáveis definidos por atividade e dependências explícitas, reduzindo a dependência de qualquer integrante isolado.

---

## 2. Meta da Sprint 4

> **"Fechar o ciclo de acompanhamento do TCC: permitir que o aluno submeta o PDF do seu trabalho dentro (ou fora) do prazo (US06) e que o orientador baixe o documento e registre um parecer sobre a entrega (US07), com as entregas obrigatórias de DevOps da sprint (ambiente de staging, pipeline de CI atualizado, deploy automatizado e `docs/DEPLOY.md`) e a governança do projeto (métricas e riscos) atualizadas."**

---

## 3. Escopo da Sprint 4

### 3.1 Histórias selecionadas

| Item                                    | Issue | Estimativa |
| :-------------------------------------- | :---: | :--------: |
| US06 - Submissão de Entregas do TCC     |  #13  | A estimar  |
| US07 - Avaliação e Parecer das Entregas |  #14  | A estimar  |

### 3.2 Regras de negócio definidas para a Sprint 4

A equipe definiu as seguintes regras de negócio e decisões de infraestrutura antes do início da implementação:

- **Estratégia de armazenamento (US06):** os arquivos são salvos em volume Docker local. Não será utilizado storage externo (S3, MinIO ou similar) nesta sprint.
- **Regra de atraso (US06):** o sistema permite o envio do documento mesmo após o prazo limite. Um envio fora do prazo é marcado com uma flag (`atrasada`) no banco de dados e exibido no front-end como "Entregue com atraso". Não há bloqueio de envio por atraso.
- **Regra de reenvio (US06):** o envio é único por etapa. O aluno só pode enviar um novo arquivo para a mesma etapa se o parecer do orientador for "Ajustes Necessários". Nos demais status (`SUBMETIDO`, `APROVADO`, `REPROVADO`), o reenvio é bloqueado.
- **Notificações (US07):** não há disparo de e-mail nesta sprint. A mudança de status do parecer é refletida apenas visualmente no painel do aluno.

### 3.3 Fatiamento do trabalho

Para permitir Pull Requests pequenos e garantir a rastreabilidade individual, as duas histórias foram fatiadas em issues técnicas menores por área (DevOps, Backend, Frontend e QA), além de duas issues de Governança/Processo, totalizando 15 issues. Cada atividade possui responsáveis definidos, podendo haver colaboração entre integrantes quando necessário.

| Issue                                              | Área                | Responsável     |   Início   |    Fim     |
| :------------------------------------------------- | :------------------ | :-------------- | :--------: | :--------: |
| Volume Docker para uploads de TCC                  | DevOps              | Eduardo         | 03/11/2026 | 03/11/2026 |
| Migration e entidades de Entrega (US06)            | Backend             | Damares         | 03/11/2026 | 04/11/2026 |
| CI pipeline para entrega-8                         | DevOps              | Eduardo         | 04/11/2026 | 05/11/2026 |
| Service e endpoint de submissão de entrega (US06)  | Backend             | Talles          | 04/11/2026 | 06/11/2026 |
| Tela de upload de TCC (US06)                       | Frontend            | Marcus          | 05/11/2026 | 06/11/2026 |
| Configuração de ambiente de staging acessível      | DevOps              | Eduardo         | 06/11/2026 | 09/11/2026 |
| Endpoint de download de documento (US07)           | Backend             | Talles          | 06/11/2026 | 09/11/2026 |
| Testes de integração - submissão de entrega (US06) | QA                  | Willian         | 06/11/2026 | 09/11/2026 |
| Migration e endpoint de parecer (US07)             | Backend             | Damares, Talles | 09/11/2026 | 10/11/2026 |
| Tela de avaliação e parecer do orientador (US07)   | Frontend            | Marcus          | 09/11/2026 | 10/11/2026 |
| docs/DEPLOY.md com .env.example consolidado        | DevOps              | Eduardo         | 09/11/2026 | 10/11/2026 |
| docs/METRICAS.md atualizado                        | Governança/Processo | Willian         | 10/11/2026 | 10/11/2026 |
| Testes de integração - avaliação e parecer (US07)  | QA                  | Willian         | 10/11/2026 | 11/11/2026 |
| docs/riscos.md revisado                            | Governança/Processo | Willian         | 10/11/2026 | 11/11/2026 |
| Release v0.4.0 (Sprint 4 / Entrega 8)              | DevOps              | Eduardo         | 11/11/2026 | 11/11/2026 |

---

## 4. Fundamentação

- **Backlog:** US06 e US07 são as duas últimas histórias do fluxo central do MVP (catálogo, orientação, calendário e, agora, acompanhamento de entregas) ainda não iniciadas, conforme [`docs/BASELINE.md`](../BASELINE.md) e a inspeção da Sprint 3.
- **Riscos:** R01 e R04, concretizados na Sprint 3, orientam o fatiamento em issues pequenas e com responsável único (seção 3.3), reduzindo a exposição da sprint à ausência de um integrante. R09 (Keycloak) segue acompanhado, já que US06 e US07 dependem da autorização por papel implementada na Sprint 3.
- **Entregas obrigatórias de DevOps:** a Sprint 4 inclui, além do código das histórias, as entregas de infraestrutura exigidas pela disciplina: ambiente de staging acessível, atualização do pipeline de CI, documentação de deploy (`docs/DEPLOY.md` com `.env.example`) e a release `v0.4.0`.
- **Governança:** a sprint reserva issues específicas para atualizar `docs/METRICAS.md` e `docs/riscos.md`, mantendo a documentação de processo alinhada ao estado real do projeto.

---

## 5. Riscos Considerados no Planejamento

- **R01 (desistência ou baixa participação):** concretizado na Sprint 3. Mitigação adotada nesta sprint: issues pequenas, com dono único e dependências explícitas, de forma que o atraso de uma pessoa tenha impacto localizado e visível no board.
- **R04 (crescimento do escopo):** concretizado na Sprint 3. Contenção nesta sprint: o escopo de US06/US07 foi fechado antes do início da implementação (seção 3.2), sem abertura para novas funcionalidades durante a sprint.
- **R09 (instabilidade do Keycloak):** US06 e US07 dependem de autorização por papel (`ROLE_ALUNO`, `ROLE_PROFESSOR`) e de vínculo aluno/orientador. Mitigação: reaproveitar a configuração do realm já validada na Sprint 3, sem alterações no Keycloak nesta sprint.
- **Novo - Ambiente de staging único:** a Sprint 4 introduz um ambiente de staging compartilhado pela primeira vez. Se ficar indisponível, pode bloquear a validação das demais issues. Mitigação: smoke test logo após a subida (issue de staging) e ambiente local do `docker-compose.yml` como alternativa de validação.
- **Novo - Armazenamento local sem redundância:** a decisão de usar volume Docker local (seção 3.2) implica que os arquivos enviados não têm backup automático nesta sprint. Mitigação: registrar essa limitação em `docs/riscos.md` (issue de governança) e em `docs/DEPLOY.md`, com orientação de backup manual do volume caso a entrega deva ser preservada.
- **Novo - Cronograma concentrado entre 09/11 e 11/11:** as issues de Backend (parecer), Frontend (avaliação), DEPLOY.md, QA de US07, métricas, riscos e release se concentram nos últimos três dias da sprint. Mitigação: priorizar o início antecipado das issues de Backend e Infra que não dependem de US06 estar 100% concluída.

A revisão completa será registrada em [`docs/riscos.md`](../riscos.md) ao final da sprint (seção 7).

---

## 6. Governança e Fluxo de Trabalho

- **Ramificação base:** todo o trabalho da sprint parte da `entrega-8` e retorna a ela por Pull Request.
- **Fatiamento:** cada issue da seção 3.3 é resolvida em uma branch própria, criada a partir da `entrega-8`, com PR pequeno e focado em uma única área (DevOps, Backend, Frontend ou QA).
- **Critérios de merge:** ao menos uma aprovação de outro integrante, pipeline de CI verde e integração por commit de mesclagem.
- **Rastreabilidade:** cada PR referencia a issue correspondente.
- **Integração final:** PR revisado da `entrega-8` para a `main`, seguido da release `v0.4.0`.

### 6.1. Papéis e Responsabilidades

| Integrante | Papel na Sprint 4                           | Responsabilidades                                                                                                                                |
| :--------- | :------------------------------------------ | :----------------------------------------------------------------------------------------------------------------------------------------------- |
| Damares    | Arquiteta de Software / Backend             | Desenvolver as entidades e funcionalidades de backend relacionadas às entregas e aos pareceres.                                                  |
| Eduardo    | DevOps/Infraestrutura                       | Configurar o volume Docker, atualizar o pipeline de CI, preparar o ambiente de staging e documentar o processo de deploy.                        |
| Marcus     | Frontend                                    | Desenvolver as telas de submissão e avaliação das entregas.                                                                                      |
| Talles     | Backend                                     | Implementar os serviços e endpoints de submissão, download e avaliação das entregas.                                                             |
| Willian    | Scrum Master e Engenheiro de Qualidade (QA) | Acompanhar a sprint, facilitar o processo, acompanhar o board, realizar os testes de integração e atualizar a documentação de métricas e riscos. |

---

## 7. Andamento das Atividades

> Seção a ser preenchida ao longo da sprint pelo Scrum Master, com o registro de impedimentos, replanejamentos e atualizações relevantes do board.

- 03/11/2026: Sprint iniciada. Planejamento inicial registrado (seções 1 a 6). Issues técnicas da seção 3.3 cadastradas no board.

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
