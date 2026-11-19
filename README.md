# Sistema de Gestão Centralizada de TCC

O **SIGTCC** é uma aplicação Web que centraliza o fluxo inicial do Trabalho de Conclusão de Curso. Ela reúne o catálogo de orientadores, a solicitação e o aceite de orientação, o calendário de prazos da turma e a submissão e avaliação das entregas do TCC. A versão candidata atual é a **`v1.0.0-rc.1`** (Entrega 9).

## MVP entregue — v1.0.0-rc.1

**Funcionalidades incluídas** (US01 a US07, 29 SP, 100% do [`BASELINE.md`](docs/BASELINE.md)):

| História | Funcionalidade                           | O que o usuário consegue fazer                                                                                                                               |
| :------- | :--------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| US01     | Autenticação e Perfis de Usuário         | Entrar com e-mail e senha (Keycloak) e ser direcionado ao painel de Aluno ou de Professor, com rotas e endpoints protegidos por papel.                       |
| US02     | Cadastro de Perfil de Orientador e Vagas | O professor cadastra e edita o próprio perfil de orientador, com departamento, linhas de pesquisa, biografia e número de vagas.                              |
| US03     | Busca e Filtragem de Orientadores        | O aluno consulta o catálogo e filtra por linha de pesquisa e por disponibilidade de vagas.                                                                   |
| US04     | Solicitação e Aceite de Orientação       | O aluno envia uma solicitação de orientação; o orientador aceita (consumindo uma vaga) ou recusa com justificativa, e o aluno acompanha o status.            |
| US05     | Configuração do Calendário de Entregas   | O professor responsável pela turma vigente cadastra os prazos de Projeto de TCC, TCC 1 e TCC 2; alunos e professores veem a contagem regressiva.             |
| US06     | Submissão de Entregas do TCC             | O aluno com orientação aceita submete cada etapa por link HTTPS, com marcação de atraso e reenvio após pedido de ajustes.                                    |
| US07     | Avaliação e Parecer das Entregas         | O orientador lista as entregas dos orientandos e registra o parecer (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`), que o aluno acompanha no seu painel. |

**Fora do escopo do MVP** (registrado em [`docs/RETROSPECTIVA.md`](docs/RETROSPECTIVA.md) e na seção 4.1 de [`docs/entregas/release-candidate.md`](docs/entregas/release-candidate.md)):

- **US08 — Agendamento e Organização de Banca:** postergada na Sprint 4 e fechada como fora do escopo do MVP.
- **Acervo público de TCCs (US09 e US10):** publicação e consulta pública dos trabalhos concluídos.
- **Upload de arquivos:** substituído pela submissão por link externo HTTPS ([`ADR-0013`](docs/adrs/ADR-0013.md)).
- **Perfil de Coordenador:** substituído pelo professor responsável pela `Turma_TCC` vigente ([`ADR-0010`](docs/adrs/ADR-0010.md)).
- Notificações por e-mail, push ou WebSocket; aplicativo mobile; assinatura digital (gov.br); geração de PDFs; integração com o sistema acadêmico (SIGAA).

**Limitação conhecida:** resta apenas o BUG-15 (seed `V5` com IDs fixos), que não afeta bancos criados do zero. Ver a seção [11.1](#111-limitações-conhecidas) e o [relatório de bugs](docs/relatorio-de-bugs.md).

## Acesso rápido

**Ambiente de _staging_** (URLs fixas com HTTPS até a defesa; detalhes na seção [12.1](#121-ambiente-de-staging-oficial-urls-fixas-até-a-defesa)):

| Serviço             | URL                                                                                          |
| :------------------ | :------------------------------------------------------------------------------------------- |
| Frontend            | [https://sigtcc.cobbler-albacore.ts.net](https://sigtcc.cobbler-albacore.ts.net)             |
| Backend (API)       | [https://sigtcc.cobbler-albacore.ts.net:8443](https://sigtcc.cobbler-albacore.ts.net:8443)   |
| Keycloak (Auth/IAM) | [https://sigtcc.cobbler-albacore.ts.net:10000](https://sigtcc.cobbler-albacore.ts.net:10000) |

**Credenciais de teste** (realm `sigtcc`, válidas no _staging_ e na execução local):

| E-mail                              | Senha          | Perfil                                                    |
| :---------------------------------- | :------------- | :-------------------------------------------------------- |
| `aluno@example.com`                 | `aluno123`     | Aluno (`ROLE_ALUNO`)                                      |
| `professor.orientador@example.com`  | `professor123` | Professor orientador (`ROLE_PROFESSOR`)                   |
| `professor.responsavel@example.com` | `professor123` | Professor responsável pela `Turma_TCC` vigente (`2026/2`) |

**Dados de demonstração:** em um banco novo, a migration `V5` cria os dois professores acima como orientadores ativos (3 vagas cada) e a turma vigente `2026/2`, sob responsabilidade de `professor.responsavel@example.com`. O aluno não tem solicitações nem entregas, o que permite percorrer o fluxo completo do MVP do início. A preparação do cenário completo de demonstração (linhas de pesquisa e prazos da turma) e o procedimento para recriá-lo ficam em [`docs/staging.md`](docs/staging.md), conforme as issues [#280](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/280) e [#286](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/286).

## Documentação do projeto

| Documento                                                               | Conteúdo                                                                                                                                                                                                                       |
| :---------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [Release Candidate](docs/entregas/release-candidate.md)                 | Planejamento, escopo, rastreabilidade, PRs e contribuições da Entrega 9 (`v1.0.0-rc.1`).                                                                                                                                       |
| [Testes de Aceitação](docs/ACEITACAO.md)                                | Casos `AT-01` a `AT-07` do MVP, com cenários, usuários e resultados.                                                                                                                                                           |
| [Arquitetura](docs/ARQUITETURA.md)                                      | Componentes, integrações, migrations e diagramas C4 da arquitetura implementada.                                                                                                                                               |
| [Decisões Arquiteturais (ADRs)](docs/adrs/)                             | `ADR-0001` a `ADR-0014`, com o status de cada decisão.                                                                                                                                                                         |
| [Padrões de Projeto](docs/PADROES-DE-PROJETO.md)                        | Strategy (elegibilidade da solicitação) e Observer (atualização de vagas e cancelamentos).                                                                                                                                     |
| [Métricas](docs/METRICAS.md)                                            | Plano de medição e fichas [`M-01`](docs/metricas/M-01.md) a [`M-06`](docs/metricas/M-06.md).                                                                                                                                   |
| [Riscos](docs/riscos.md)                                                | Registro, priorização e revisão final dos riscos do projeto.                                                                                                                                                                   |
| [Retrospectiva](docs/RETROSPECTIVA.md)                                  | Revisão final do MVP (planejado × entregue), retrospectiva das sprints e lições aprendidas.                                                                                                                                    |
| [Relatório de Bugs](docs/relatorio-de-bugs.md)                          | Triagem final dos 22 achados da revisão estática, com issue e PR de cada correção.                                                                                                                                             |
| [Deploy e Staging](docs/DEPLOY.md) e [Staging estável](docs/staging.md) | Variáveis de ambiente, execução local, _staging_ fixo, reinício e _rollback_.                                                                                                                                                  |
| [Integração Contínua](docs/ci.md)                                       | Workflows do GitHub Actions e checks obrigatórios dos PRs.                                                                                                                                                                     |
| [Fluxo de Trabalho](docs/fluxo-de-trabalho.md)                          | Branches, commits, Pull Requests e cerimônias.                                                                                                                                                                                 |
| [Declaração de Uso de IA](USO-IA.md)                                    | Uso de ferramentas de IA generativa por entrega e por integrante.                                                                                                                                                              |
| Contratos de dados da API                                               | [US01](docs/contrato-dados-us01.md), [US02](docs/contrato-dados-us02.md), [US04](docs/contrato-dados-us04.md), [US05](docs/contrato-dados-us05.md), [US06](docs/contrato-dados-us06.md) e [US07](docs/contrato-dados-us07.md). |

## 1. Equipe

- **DAMARES DO SOCORRO GONCALVES GAIA** (Matrícula: 202510703590)
- **EDUARDO CARDOSO OLIVEIRA** (Matrícula: 202510703625)
- **MARCUS JHUAN EPIFANIO LIMA** (Matrícula: 202320003071)
- **TALLES SOUZA DA CRUZ** (Matrícula: 202510703745)
- **WILLIAN FERREIRA DOS SANTOS** (Matrícula: 202320002996)

### 1.1. Papéis por Sprint

Os papéis foram revistos a cada sprint. O papel de Scrum Master é rotativo.

| Integrante                        | Sprint 1 (Entrega 5)                        | Sprint 2 (Entrega 6)                     | Sprint 3 (Entrega 7)                     | Sprint 4 (Entrega 8)                            | Release Candidate (Entrega 9)                   |
| :-------------------------------- | :------------------------------------------ | :--------------------------------------- | :--------------------------------------- | :---------------------------------------------- | :---------------------------------------------- |
| Damares do Socorro Gonçalves Gaia | Arquiteta de Software                       | **Scrum Master** e Arquiteta de Software | Arquiteta de Software                    | Arquiteta de Software e Desenvolvedora Backend  | Arquiteta de Software e Governança              |
| Eduardo Cardoso Oliveira          | **Scrum Master** e Engenheiro de Requisitos | Engenheiro de Requisitos                 | Engenheiro de Qualidade (QA)             | DevOps/Infraestrutura                           | Coordenação da Entrega 9, DevOps e Governança   |
| Marcus Jhuan Epifanio Lima        | Designer de UX/UI                           | Designer de UX/UI                        | Designer de UX/UI                        | Desenvolvedor Frontend                          | Desenvolvedor Frontend e UX                     |
| Talles Souza da Cruz              | Engenheiro de Qualidade (QA)                | Engenheiro de Qualidade (QA)             | **Scrum Master** e Desenvolvedor Backend | Desenvolvedor Backend                           | Coordenação da Entrega 9, QA e Backend          |
| Willian Ferreira dos Santos       | DevOps/Infra                                | DevOps/Infra                             | DevOps/Infra                             | **Scrum Master** e Engenheiro de Qualidade (QA) | **Scrum Master**, Engenheiro de QA e Governança |

## 2. Definição e Contextualização do Tema

- **Qual problema o sistema pretende resolver?**
  A gestão do Trabalho de Conclusão de Curso (TCC) sofre com a descentralização (uso de WhatsApp e e-mails para envio de arquivos) e com a falta de controle de prazos. Além disso, os alunos enfrentam grande dificuldade na fase inicial para descobrir quais são as áreas de atuação e as linhas de pesquisa de cada professor, dificultando o processo de encontrar um orientador compatível.

- **Em qual área de aplicação o problema está situado?**
  Educação e apoio à aprendizagem (Gestão Acadêmica).

- **Quem são os usuários?**
  1. **Alunos** (que buscam orientadores, desenvolvem o TCC e submetem arquivos).
  2. **Professores / Orientadores** (que divulgam suas linhas de pesquisa, avaliam entregas e participam de bancas).
  3. **Coordenadores de TCC** (que estipulam prazos no calendário acadêmico e gerenciam o fluxo).

- **Em qual local, organização, comunidade ou contexto o sistema poderia ser aplicado?**
  Uso interno em Instituições de Ensino Superior, focado em coordenadorias de curso da nossa própria instituição.

- **Por que o tema é relevante?**
  Ele moderniza e formaliza todo o ciclo do TCC. O sistema elimina a perda de histórico de comunicação, facilita o "match" entre aluno e orientador por meio do catálogo de pesquisa, e garante que o processo burocrático (prazos, envio de arquivos, agendamento de bancas) seja feito em um ambiente único, seguro e institucional.

- **Qual é a proposta do sistema para resolver ou apoiar a solução do problema?**
  Desenvolver uma aplicação Web de gestão centralizada de TCC. O sistema terá um catálogo onde os professores expõem suas linhas de pesquisa e disponibilidade de vagas. Após o vínculo, o sistema gerenciará todo o fluxo do trabalho: submissão oficial de documentos, controle do calendário de defesas e a publicação do trabalho final em um acervo para consulta pública.

## 3. Escopo do Produto Mínimo Viável (MVP)

O sistema será construído como uma aplicação Web Orientada a Objetos, com foco estrito nas regras de negócio iniciais e essenciais do fluxo acadêmico, garantindo viabilidade para o semestre letivo.

**O que o MVP fará (Funcionalidades Principais):**

1. **Autenticação e Perfis:** Acesso diferenciado para Alunos, Professores e Coordenadores.
2. **Catálogo de Orientadores (Match):** Vitrine onde os professores cadastram suas linhas de pesquisa, áreas de interesse e quantidade de vagas disponíveis, permitindo a vinculação inicial do aluno.
3. **Gestão de Documentos e Prazos:** Interface para o aluno fazer o upload das entregas obrigatórias (ex: Projeto de TCC, TCC 1, Versão Final) respeitando o calendário do Coordenador, com espaço para pareceres do orientador.

**O que ficará FORA do escopo neste momento (Candidatos para entregas/versões futuras):**

- Gestão de Bancas (Agendamento de defesas e registro de avaliadores convidados).
- Acervo Público Acadêmico (Página pública de busca de TCCs concluídos).
- Aplicativo Mobile.
- Assinatura digital avançada (gov.br) nas atas de defesa.
- Geração automática de PDFs complexos (atas e formulários formatados).
- Integração direta com o sistema acadêmico oficial de notas da instituição (ex: SIGAA).

## 4. Entrega 2: Inception e Planejamento

Durante a etapa de Inception, definimos o escopo técnico e de negócios do projeto, além de estabelecermos nossas regras de qualidade e arquitetura. Todos os artefatos desta fase podem ser acessados nos links abaixo:

- **[Visão do Produto e Escopo do MVP](docs/inception.md):** Definição do problema, proposta de valor, funcionalidades essenciais e o que está fora do escopo.
- **[Backlog Inicial e Board](https://github.com/orgs/IFSC-ES2/projects/31):** Quadro de acompanhamento de tarefas (Kanban) contendo as _User Stories_ priorizadas para o MVP.
- **[Definition of Done - DoD](docs/dod.md):** Nosso acordo de qualidade e critérios mínimos para considerar uma tarefa concluída.
- **[Decisões Arquiteturais - ADRs](docs/adrs/):** Registro das escolhas iniciais de stack tecnológica e infraestrutura.
- **[Declaração de Uso de IA](USO-IA.md):** Registro de uso de ferramentas de Inteligência Artificial durante o desenvolvimento do projeto.

## 5. Entrega 3: Estimativas e Métricas (Baseline)

Nesta etapa, formalizamos as estimativas, a capacidade planejada da equipe e a definição das métricas de produto, processo e projeto:

- **[Planejamento Inicial e Baseline](docs/BASELINE.md):** Recorte do backlog, estimativas, capacidade declarada e premissas do projeto.
- **[Registro da Abordagem de Estimativa](docs/ESTIMATIVAS.md):** Técnica de Planning Poker, Fibonacci (1, 2, 3, 5, 8, 13), participantes e justificativas.
- **[Plano de Medição e Métricas](docs/METRICAS.md):** Matriz geral e categorização dos 6 indicadores acompanhados.
- **Fichas Técnicas de Métricas:**
  - [`M-01.md`](docs/metricas/M-01.md): Cobertura de Testes de Código (Métrica de Produto)
  - [`M-02.md`](docs/metricas/M-02.md): Densidade de Defeitos Abertos (Métrica de Produto)
  - [`M-03.md`](docs/metricas/M-03.md): Velocidade da Equipe em SP (Métrica de Processo)
  - [`M-04.md`](docs/metricas/M-04.md): Lead Time de Pull Requests (Métrica de Processo)
  - [`M-05.md`](docs/metricas/M-05.md): Taxa de Conclusão do MVP (Métrica de Projeto)
  - [`M-06.md`](docs/metricas/M-06.md): Cumprimento da Capacidade (Métrica de Projeto)

## 6. Entrega 4: Riscos e consolidação

Nesta etapa, estabelecemos o registro e priorização de riscos do projeto, consolidamos o fluxo de trabalho colaborativo via Pull Requests e definimos a base de avaliação da qualidade do software:

- **[Riscos do Projeto](docs/riscos.md):** Registro inicial dos riscos identificados, contendo análise, priorização e plano de respostas aos ricos.
- **[Fluxo de Trabalho e Governança](docs/fluxo-de-trabalho.md):** Regras de branches, padrão de commits, cerimônias ágeis e checklist obrigatório para Pull Requests.
- **[Integração Contínua (CI)](docs/ci.md):** Configuração do pipeline de Integração Contínua (Github Actions), cobrindo verificação de docs, links e formatação (Prettier).
- **[Qualidade do Software](docs/qualidade.md):** Mapeamento dos atributos da norma ISO/IEC 25010 priorizados para o MVP.
- **Evidência de Integração:** O desenvolvimento desta etapa foi consolidado através do [Pull Request #34](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/34), que executou os checks de CI obrigatórios e passou pela revisão da equipe.

## 7. Entrega 5: Primeiro Incremento Funcional (Sprint 1)

Nesta etapa, implementamos o primeiro _vertical slice_ funcional do MVP (US02: Cadastro de Perfil de Orientador e Vagas), integrando interface Web em React, lógica de negócio em Spring Boot e persistência relacional com MySQL, além da automação de testes de unidade e pipeline de CI.

- **[Relatório de Fechamento da Sprint 1](docs/entregas/sprint-1.md):** Escopo planejado vs. executado, retrospectiva e registro detalhado de contribuições individuais.
- **[Contrato de Dados da API (US02)](docs/contrato-dados-us02.md):** Especificação formal dos endpoints REST e formatos de dados.
- **[Declaração de Uso de IA](USO-IA.md):** Registro da utilização de ferramentas de IA na Sprint 1.

### 7.1. O que já funcionava no MVP ao final da Sprint 1 (US02)

- **Cadastro de Orientador:** Formulário com validações em tempo real (nome, e-mail institucional único, departamento, número de vagas e linhas de pesquisa) via `POST /api/v1/orientadores`.
- **Vitrine / Catálogo de Orientadores:** Listagem dos orientadores com disponibilidade de vagas e filtragem dinâmica por linha de pesquisa via `GET /api/v1/orientadores?area=...`.
- **Consulta de Perfil:** Visualização detalhada dos dados de um orientador específico por ID via `GET /api/v1/orientadores/{id}`.
- **Edição de Perfil:** Atualização de dados cadastrais, biografia e ajuste no quantitativo de vagas ofertadas via `PATCH /api/v1/orientadores/{id}`.
- **Remoção de Perfil:** Desativação/exclusão de cadastro via `DELETE /api/v1/orientadores/{id}`.

## 8. Entrega 6: Fluxo de Solicitação e Aceite de Orientação (Sprint 2)

Nesta etapa, implementamos o fluxo completo de "match" entre aluno e orientador (US04 - Solicitação e Aceite de Orientação), integrado à fatia essencial da US03 (botão de ação rápida no catálogo construído na Sprint 1), e aplicamos dois padrões de projeto OO (Strategy e Observer) para desacoplar as regras de elegibilidade e as reações à mudança de status da solicitação.

- **[Relatório de Fechamento da Sprint 2](docs/entregas/sprint-2.md):** Meta da sprint, escopo planejado, justificativa de priorização e papéis da equipe.
- **[Contrato de Dados da API (US04)](docs/contrato-dados-us04.md):** Especificação formal dos endpoints de solicitação, modelos de dados, códigos de erro e máquina de estados.
- **[Documento de Padrões de Projeto](docs/PADROES-DE-PROJETO.md):** Detalhamento das implementações de Strategy (validações de elegibilidade) e Observer (atualização de vagas), com diagramas de classes e sequência.
- **Release:** [`v0.2.0`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v0.2.0)

### 8.1. O que já funciona no MVP ao final da Sprint 2

- **Botão "Solicitar Orientação" no Catálogo (US03 - fatia essencial):** Ação habilitada apenas quando o orientador possui vagas disponíveis (`vagasDisponiveis > 0`), com indicador visual claro de disponibilidade.
- **Envio de Solicitação (Aluno):** Formulário com nome, e-mail, curso, tema pretendido e mensagem/proposta inicial. O backend cria ou reaproveita o cadastro do `Aluno` pelo e-mail (sem exigir login) via `POST /api/v1/solicitacoes`.
- **Painel de Gestão do Orientador:** Listagem das solicitações recebidas, com filtro por status, via `GET /api/v1/solicitacoes/orientador/{orientadorId}`.
- **Aceite ou Recusa da Solicitação (Orientador):** O aceite decrementa automaticamente as vagas do orientador; a recusa exige justificativa (10 a 500 caracteres). Atualização via `PATCH /api/v1/solicitacoes/{id}/status`.
- **Acompanhamento de Status (Aluno):** Consulta do estado atual da solicitação (`PENDENTE`, `ACEITA` ou `RECUSADA`) via `GET /api/v1/solicitacoes/{id}`.

### 8.2. Endpoints da API - Solicitações de Orientação (US04)

| Método  | Rota                                             | Descrição                                                                               | Sucesso       | Quem usa           |
| :------ | :----------------------------------------------- | :-------------------------------------------------------------------------------------- | :------------ | :----------------- |
| `POST`  | `/api/v1/solicitacoes`                           | Aluno envia uma solicitação de orientação a um professor com vagas.                     | `201 Created` | Aluno              |
| `GET`   | `/api/v1/solicitacoes/orientador/{orientadorId}` | Lista as solicitações recebidas por um orientador (filtro opcional por status).         | `200 OK`      | Orientador         |
| `PATCH` | `/api/v1/solicitacoes/{id}/status`               | Orientador aceita (`ACEITA`) ou recusa (`RECUSADA`, com justificativa) uma solicitação. | `200 OK`      | Orientador         |
| `GET`   | `/api/v1/solicitacoes/{id}`                      | Detalha uma solicitação específica; também usado pelo aluno para acompanhar o status.   | `200 OK`      | Aluno e Orientador |

Especificação completa de request/response, validações e códigos de erro (`400`, `404`, `409`, `422`) em [`docs/contrato-dados-us04.md`](docs/contrato-dados-us04.md).

### 8.3. Padrões de Projeto Aplicados

- **Strategy:** As regras de elegibilidade para criar uma solicitação (vagas disponíveis, orientador ativo, ausência de solicitação pendente duplicada) foram encapsuladas em estratégias independentes (`ValidadorSolicitacaoStrategy` e implementações), evitando cadeias de `if/else` no serviço e respeitando o princípio Aberto/Fechado.
- **Observer:** A transição de status da solicitação publica um evento de domínio (`SolicitacaoStatusChangedEvent`), consumido de forma síncrona e transacional pelo `AtualizadorVagasObserver`, que decrementa a vaga do orientador sem acoplar essa lógica ao serviço principal.

Detalhamento completo, diagramas de classes e de sequência em [`docs/PADROES-DE-PROJETO.md`](docs/PADROES-DE-PROJETO.md).

### 8.4. Limitações Conhecidas desta Versão

- **Sem autenticação:** A US01 (Autenticação e Perfis com Spring Security/JWT) foi postergada para uma sprint futura. Todos os endpoints, incluindo os de solicitação e de aceite/recusa, são públicos nesta versão.
- **Sem notificação por e-mail (ou push/WebSocket):** O painel do orientador funciona por consulta sob demanda (`GET /api/v1/solicitacoes/orientador/{id}?status=PENDENTE`). Não há envio de e-mail, notificação push ou WebSocket quando uma nova solicitação é criada ou respondida.

## 9. Entrega 7: Autenticação, Busca e Calendário de Prazos (Sprint 3)

Nesta etapa, o MVP passou a ser autenticado e orientado a perfis. Implementamos o login via Keycloak com os papéis de Aluno e Professor (US01), concluímos a busca e filtragem do catálogo de orientadores (US03) e disponibilizamos o calendário de prazos gerido pelo professor responsável pela turma de TCC (US05). A sprint também consolidou a documentação da arquitetura real, as ADRs e uma refatoração orientada a design com métrica antes/depois.

- **[Inspeção, Replanejamento e Contribuições da Sprint 3](docs/entregas/sprint-3.md):** Estado do MVP ao final da Sprint 2, ajustes de escopo, meta da sprint e registro das contribuições individuais.
- **[Documento de Arquitetura](docs/ARQUITETURA.md):** Componentes em execução, responsabilidades, integrações e diferenças entre a arquitetura planejada e a implementada.
- **Diagramas C4:** [Contexto (C1)](docs/diagramas/c1-contexto.md) e [Contêineres (C2)](docs/diagramas/c2-conteineres.md).
- **Novas Decisões Arquiteturais (ADRs):**
  - [`ADR-0010`](docs/adrs/ADR-0010.md): Modelagem da entidade `Turma_TCC` em substituição ao papel de Coordenador
  - [`ADR-0011`](docs/adrs/ADR-0011.md): Adoção do Keycloak para autenticação e gestão de identidades
  - [`ADR-0012`](docs/adrs/ADR-0012.md): Backend como Resource Server OAuth2 com validação local de tokens JWT
  - [`ADR-0013`](docs/adrs/ADR-0013.md): Submissão de entregas por link externo em substituição ao upload de arquivos
  - [`ADR-0014`](docs/adrs/ADR-0014.md): Autenticação do frontend via Direct Access Grant do Keycloak
  - As ADRs anteriores (0001 a 0009) foram revisadas e receberam um bloco de revisão da Sprint 3 em [`docs/adrs/`](docs/adrs/).
- **[Reengenharia](docs/REENGENHARIA.md):** Problema real de design no tratamento de erros do cliente HTTP do frontend, refatoração REF-1 e comparação de métricas antes/depois.
- **[Plano de Medição e Métricas](docs/METRICAS.md):** Matriz geral das métricas, com as fichas de [M-01](docs/metricas/M-01.md) a [M-06](docs/metricas/M-06.md) acompanhadas a cada sprint.
- **[Riscos do Projeto](docs/riscos.md):** Revisão dos riscos ao final da Sprint 3.
- **Contratos de Dados da API:** [US01 - Perfil do Professor](docs/contrato-dados-us01.md) e [US05 - Calendário de Prazos](docs/contrato-dados-us05.md).
- **[Configuração do Keycloak](docs/keycloak/keycloak.md):** Realm, clients, roles, usuários de teste e passo a passo de configuração.
- **[Integração Contínua (CI)](docs/ci.md):** Pipeline atualizado para a Sprint 3, com evidência de execução.
- **[Declaração de Uso de IA](USO-IA.md):** Registro da utilização de ferramentas de IA na Sprint 3, por integrante.
- **Release:** `v0.3.0`, publicada após a integração da `entrega-7` na `main`.

### 9.1. O que já funciona no MVP ao final da Sprint 3

- **Login com Keycloak (US01):** Tela de login com e-mail e senha, mensagem de credenciais inválidas e redirecionamento para o painel de Aluno ou Professor conforme o papel do usuário (`ROLE_ALUNO` ou `ROLE_PROFESSOR`).
- **Rotas e endpoints protegidos (US01):** Todas as rotas `/api/**` exigem um token JWT válido emitido pelo Keycloak (`401` sem token). No frontend, rotas restritas ao professor redirecionam o aluno para uma página de Acesso Negado, e os botões exclusivos do professor ficam ocultos para o aluno.
- **Dashboard do Professor por responsabilidade (US01):** A aba de gestão da turma e do calendário aparece apenas para o professor responsável pela `Turma_TCC` vigente, verificado no banco de dados via `GET /api/v1/professores/me`.
- **Busca e filtragem de orientadores (US03):** Filtro por linha de pesquisa, filtro "Somente com vagas disponíveis", combinação dos dois, estado de "nenhum orientador encontrado" e ação de limpar filtros, via `GET /api/v1/orientadores?area=...&temVagas=...`.
- **Calendário de prazos (US05):** O professor responsável pela turma cadastra e edita os prazos de Projeto de TCC, TCC 1 e TCC 2. O backend rejeita datas no passado e sequências inválidas (TCC 1 antes do Projeto de TCC e TCC 2 antes do TCC 1).
- **Contagem regressiva dos prazos (US05):** Alunos e professores visualizam os prazos vigentes com a contagem regressiva até cada entrega.

### 9.2. Endpoints da API adicionados na Sprint 3

| Método  | Rota                     | Descrição                                                                                          | Sucesso       | Quem usa                         |
| :------ | :----------------------- | :------------------------------------------------------------------------------------------------- | :------------ | :------------------------------- |
| `GET`   | `/api/v1/professores/me` | Dados do professor autenticado e indicador `responsavelTurma`.                                     | `200 OK`      | Professor                        |
| `GET`   | `/api/v1/prazos`         | Lista os prazos da `Turma_TCC` vigente, na ordem do fluxo (Projeto de TCC, TCC 1, TCC 2).          | `200 OK`      | Aluno e Professor                |
| `POST`  | `/api/v1/prazos`         | Cadastra o prazo de um tipo de entrega na turma vigente.                                           | `201 Created` | Professor responsável pela turma |
| `PATCH` | `/api/v1/prazos/{id}`    | Altera a data limite de um prazo da turma vigente, reaplicando as regras de data.                  | `200 OK`      | Professor responsável pela turma |
| `GET`   | `/api/v1/orientadores`   | Lista orientadores com os filtros opcionais `area` (linha de pesquisa) e `temVagas` (combináveis). | `200 OK`      | Aluno e Professor                |

Todas as rotas exigem o cabeçalho `Authorization: Bearer <token>`. Especificação completa de request/response e códigos de erro (`400`, `401`, `403`, `404`, `409`, `422`) em [`docs/contrato-dados-us01.md`](docs/contrato-dados-us01.md) e [`docs/contrato-dados-us05.md`](docs/contrato-dados-us05.md).

### 9.3. Limitações Conhecidas desta Versão

- **Autenticação resolvida:** A limitação "Sem autenticação" da Entrega 6 (seção 8.4) foi resolvida nesta sprint pela US01.
- **Login por Direct Access Grant:** O frontend envia e-mail e senha diretamente ao endpoint de token do Keycloak (`grant_type=password`), em vez do fluxo Authorization Code com PKCE, e guarda os tokens no `sessionStorage`. A escolha e seus riscos estão registrados na [ADR-0014](docs/adrs/ADR-0014.md).
- **Ambiente apenas de desenvolvimento:** O Keycloak roda em modo `start-dev`, com credenciais padrão de administrador, e o realm traz usuários de teste com senhas conhecidas. Essa configuração não deve ser exposta fora do ambiente de avaliação.
- **Condição de corrida no aceite da última vaga (R07):** Ainda não há controle de concorrência (como lock otimista) no aceite de solicitações, conforme registrado em [`docs/riscos.md`](docs/riscos.md).
- **Submissão de documentos e pareceres não implementados:** As US06 e US07 (submissão por link e pareceres do orientador) e o agendamento de bancas de defesa permanecem no backlog do MVP.
- **Sem notificações:** Continua não havendo envio de e-mail, notificação push ou WebSocket para novas solicitações, respostas ou prazos próximos.
- **Débitos técnicos registrados:** Validações pendentes do contrato da US04 e ausência de teste de integração ponta a ponta, conforme a seção 2.2 de [`docs/entregas/sprint-3.md`](docs/entregas/sprint-3.md).

## 10. Entrega 8: Submissão de Entregas, Avaliação e Ambiente de Staging (Sprint 4)

Nesta etapa, concluímos o fluxo de acompanhamento do TCC previsto para o MVP, com a submissão de entregas por link externo HTTPS pelo aluno (US06) e a avaliação com parecer pelo orientador (US07). A sprint também consolidou oito correções de autorização e sessão, controle de concorrência nas vagas e nos pareceres, atualização da documentação de arquitetura e padrões de projeto, guia de deploy e configuração do ambiente de _staging_.

- **[Relatório de Fechamento da Sprint 4](docs/entregas/sprint-4.md):** Planejamento, correções de bugs, Pull Requests mesclados, retrospectiva, incremento entregue e contribuições individuais.
- **Contratos de Dados da API:** [US06 - Submissão de Entregas](docs/contrato-dados-us06.md) e [US07 - Avaliação e Parecer](docs/contrato-dados-us07.md).
- **[Guia de Deploy e Staging](docs/DEPLOY.md):** Consolidação do `.env.example`, execução em ambiente local e _staging_ (`docker-compose.prod.yml` + Cloudflare Tunnel) e estratégia de _rollback_.
- **[Documento de Arquitetura](docs/ARQUITETURA.md) e [Padrões de Projeto](docs/PADROES-DE-PROJETO.md):** Atualizações da Sprint 4 para o módulo de entregas, pareceres e controle concorrente.
- **[Riscos do Projeto](docs/riscos.md):** Revisão dos riscos ao final da Sprint 4 (incluindo os riscos R11, R12 e R13).
- **[Integração Contínua (CI)](docs/ci.md):** Pipeline atualizado para a `entrega-8` e workflow de build Docker (`docker-build.yml`).
- **[Plano de Medição e Métricas](docs/METRICAS.md):** Fichas técnicas de [M-01](docs/metricas/M-01.md) a [M-06](docs/metricas/M-06.md).
- **[Declaração de Uso de IA](USO-IA.md):** Registro da utilização de ferramentas de IA na Sprint 4.
- **Release:** `v0.4.0` (planejada após a integração da `entrega-8` na `main`).

### 10.1. O que funciona no MVP ao final da Sprint 4

- **Submissão de entregas por link externo (US06):** Alunos com vínculo aceito (`ROLE_ALUNO`) submetem um link HTTPS para `PROJETO_TCC`, `TCC_1` ou `TCC_2` na rota `/minhas-entregas` (`POST /api/v1/entregas`), conforme a [`ADR-0013`](docs/adrs/ADR-0013.md), com detecção automática de envio em atraso e suporte a reenvio quando o parecer solicita ajustes.
- **Acompanhamento do status pelo aluno (US07):** O painel do aluno exibe as entregas submetidas, status (`SUBMETIDO`, `AJUSTES_NECESSARIOS`, `APROVADO`, `REPROVADO`), indicador de atraso e comentário do orientador via `GET /api/v1/entregas/minhas` e `GET /api/v1/entregas/{id}`.
- **Avaliação e parecer pelo orientador (US07):** Professores vinculados (`ROLE_PROFESSOR`) listam as entregas dos orientandos na rota `/avaliar-entregas` (`GET /api/v1/entregas/orientador`), acessam o link do documento externo e registram parecer (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`, com comentário obrigatório nos dois últimos casos) via `POST /api/v1/entregas/{id}/parecer`.
- **Segurança, sessão e concorrência:** Correção de falhas de autorização e isolamento de dados entre usuários (`#199` a `#204`), renovação automática de token (`#205`), controle de concorrência otimista (`@Version`) nas vagas do orientador (`#206`) e trava pessimista (`PESSIMISTIC_WRITE`) no registro de parecer (`#215`).
- **Infraestrutura de staging e testes de integração:** Disponibilização de `SIGTCC/docker-compose.prod.yml` e ampliação dos cenários automatizados de integração no backend e de testes unitários no frontend para US06 e US07.

### 10.2. Endpoints da API adicionados na Sprint 4

| Método | Rota                            | Descrição                                                                                         | Sucesso                  | Quem usa                                |
| :----- | :------------------------------ | :------------------------------------------------------------------------------------------------ | :----------------------- | :-------------------------------------- |
| `POST` | `/api/v1/entregas`              | Submete link HTTPS para etapa do TCC ou reenvia entrega com status `AJUSTES_NECESSARIOS`.         | `201 Created` / `200 OK` | Aluno (`ROLE_ALUNO`)                    |
| `GET`  | `/api/v1/entregas/minhas`       | Lista as entregas submetidas pelo aluno autenticado na turma vigente.                             | `200 OK`                 | Aluno (`ROLE_ALUNO`)                    |
| `GET`  | `/api/v1/entregas/orientador`   | Lista as entregas dos orientandos vinculados ao professor autenticado (filtro opcional `status`). | `200 OK`                 | Orientador (`ROLE_PROFESSOR`)           |
| `GET`  | `/api/v1/entregas/{id}`         | Detalha uma entrega específica (restrito ao aluno autor ou ao orientador vinculado).              | `200 OK`                 | Aluno autor ou Orientador vinculado     |
| `POST` | `/api/v1/entregas/{id}/parecer` | Registra parecer (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`) em uma entrega submetida.     | `200 OK`                 | Orientador vinculado (`ROLE_PROFESSOR`) |

Todas as rotas exigem o cabeçalho `Authorization: Bearer <token>`. Especificação completa de payloads, validações e códigos de erro (`400`, `401`, `403`, `409`, `422`) em [`docs/contrato-dados-us06.md`](docs/contrato-dados-us06.md) e [`docs/contrato-dados-us07.md`](docs/contrato-dados-us07.md).

### 10.3. Limitações Conhecidas e Escopo Postergado

- **US08 (Agendamento de bancas de defesa) postergada:** Conforme registrado na seção 10.4 de [`docs/entregas/sprint-4.md`](docs/entregas/sprint-4.md), a US08 foi mantida no backlog para preservar o prazo e a validação das entregas US06 e US07 e das oito correções de bugs.
- **Reenvio sem histórico de links:** Quando o aluno reenvia uma entrega após parecer `AJUSTES_NECESSARIOS`, o novo `urlDocumento` substitui o anterior; o conteúdo externo hospedado fora do sistema segue sob responsabilidade do autor (`R11` e `R12` em [`docs/riscos.md`](docs/riscos.md)).
- **Sem notificações automáticas:** Não há envio de e-mail, push ou WebSocket quando uma entrega é submetida ou avaliada; o acompanhamento ocorre pelos painéis da aplicação.
- **Autenticação via Direct Access Grant:** Permanece o fluxo descrito na [`ADR-0014`](docs/adrs/ADR-0014.md), agora com renovação de token tratada no cliente HTTP.

## 11. Entrega 9: Release Candidate

- **[Revisão Final do MVP e Retrospectiva do Projeto](docs/RETROSPECTIVA.md):** Comparação "planejado × entregue" de cada funcionalidade da Inception, registro justificado das mudanças de escopo, retrospectiva consolidada das Sprints 1 a 4 (processo, colaboração, riscos, qualidade e previsibilidade), lições aprendidas e o que a equipe faria diferente ([#254](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/254)).
- **[Relatório de Bugs](docs/relatorio-de-bugs.md):** Triagem final dos 22 achados da revisão estática ([#256](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/256)), com o status de cada bug, as issues e os PRs relacionados.
- **[Planejamento e Condução da Release Candidate](docs/entregas/release-candidate.md):** Metas, escopo congelado, cronograma, backlog final, matriz de rastreabilidade, Pull Requests e contribuições individuais da Entrega 9 ([#253](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/253)).
- **[Guia de Deploy e Staging](docs/DEPLOY.md) e [Staging estável](docs/staging.md):** URLs fixas via Tailscale Funnel, restrição de `webOrigins` no Keycloak, procedimento de reinício e _rollback_ ([#260](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/260) e [#284](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/284)).
- **[Roteiro da Demonstração Final do MVP](docs/ROTEIRO-DEMONSTRACAO.md):** Roteiro estruturado passo a passo da versão `v1.0.0-rc.1` (login → catálogo e filtro → solicitação → aceite → prazos → submissão por link → parecer → reenvio após ajustes), usuários e dados de cada passo, limitações residuais e guia de defesa técnica ([#280](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/280) e [#286](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/286)).
- **[Testes de Aceitação do MVP](docs/ACEITACAO.md):** Definição, execução e registro dos resultados dos casos de teste de aceitação `AT-01` a `AT-07` cobrindo as histórias centrais US01 a US07 (cenários positivos e negativos de segurança e regras de negócio), homologados com 100% de aprovação ([#267](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/267)).

### 11.1. Limitações Conhecidas

A triagem final do relatório de bugs teve este resultado:

- **Corrigidos (21):**
  - BUG-01 a BUG-08, todos os críticos e altos, nas issues `#199` a `#206` (Sprint 4). O BUG-10 também foi corrigido, de forma indireta, pelo PR `#211`.
  - Na Entrega 9: BUG-09 (issue `#261`, PR `#289`), BUG-11 (`#273`, PR `#296`), BUG-12 (`#262`, PR `#290`), BUG-13 (`#263`, PR `#293`), BUG-14 (`#264`, PR `#270`), BUG-16 (`#265`, PR `#268`), BUG-17 (`#266`, PR `#295`), BUG-18 (`#275`, PR `#297`), BUG-19 (`#277`, PR `#300`), BUG-20 (`#276`, PR `#299`), BUG-21 (`#278`, PR `#294`) e BUG-22 (`#279`, PR `#300`).
- **Limitações conhecidas (1):** o BUG-15, descrito abaixo.

Nenhum defeito conhecido impede a demonstração do MVP. Detalhes, passos de reprodução e correções estão na seção 4 do relatório.

| Bug    | Severidade | Limitação                                                                                                                                                            |
| :----- | :--------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BUG-15 | Média      | O seed `V5` usa IDs fixos e pode sobrescrever dados de bancos criados antes dele. Não afeta bancos novos (verificado por teste); decisão registrada na issue `#274`. |

## 12. Ambiente de Staging e Execução

### 12.1. Ambiente de Staging Oficial (URLs Fixas até a Defesa)

O ambiente de _staging_ está publicado e acessível publicamente via URLs fixas com HTTPS para avaliação contínua até a apresentação final e defesa técnica:

- **Frontend (Aplicação Web):** [https://sigtcc.cobbler-albacore.ts.net](https://sigtcc.cobbler-albacore.ts.net)
- **Backend (API Spring Boot):** [https://sigtcc.cobbler-albacore.ts.net:8443](https://sigtcc.cobbler-albacore.ts.net:8443)
- **Keycloak (Autenticação):** [https://sigtcc.cobbler-albacore.ts.net:10000](https://sigtcc.cobbler-albacore.ts.net:10000)

> Para detalhes de configuração, procedimentos operacionais de reinício, responsável pela disponibilidade e script de verificação automatizada (_smoke test_), consulte [`docs/staging.md`](docs/staging.md).

### 12.2. Como executar o aplicativo localmente

O projeto pode ser executado inteiramente com Docker Compose (opção A) ou com o banco e o Keycloak no Docker e o backend e o frontend direto na máquina (opção B).

> **Documentação de Deploy e Staging:** Para instruções detalhadas sobre variáveis de ambiente (`.env.example`), execução do ambiente de _staging_ (com `docker-compose.prod.yml`), estratégia de _rollback_ e limitações conhecidas, consulte o **[Guia de Deploy e Staging (`docs/DEPLOY.md`)](docs/DEPLOY.md)**.

### Pré-requisitos

- [Git](https://git-scm.com/)
- [Docker](https://docs.docker.com/get-docker/) com [Docker Compose](https://docs.docker.com/compose/install/) v2
- Apenas para a opção B: **JDK 21** e **Node.js 20+** com npm

Em qualquer opção, clone o repositório e crie o arquivo `SIGTCC/.env` a partir do `.env.example`:

```bash
git clone https://github.com/IFSC-ES2/es2-2026_2-equipe-3.git
cd es2-2026_2-equipe-3
cp .env.example SIGTCC/.env
```

### Opção A: tudo no Docker (recomendado)

1. Na pasta `SIGTCC`, suba os quatro serviços (`db`, `keycloak`, `backend` e `frontend`):

```bash
cd SIGTCC
docker compose up
```

A primeira subida leva alguns minutos: o backend baixa as dependências do Gradle e aplica as migrations do Flyway, e o frontend instala as dependências do npm.

2. Acesse a aplicação no navegador:

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **Backend (API):** [http://localhost:8080](http://localhost:8080)
- **Keycloak (console administrativo):** [http://localhost:8081](http://localhost:8081)

3. Faça login no frontend com um dos usuários de teste do realm `sigtcc`:

| Papel                     | E-mail / Usuário                    | Senha          | Role no Keycloak | Vínculo inicial no Banco                                                                                                               |
| :------------------------ | :---------------------------------- | :------------- | :--------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| **Aluno**                 | `aluno@example.com`                 | `aluno123`     | `ROLE_ALUNO`     | Criado automaticamente no primeiro envio de solicitação (`POST /api/v1/solicitacoes`).                                                 |
| **Professor Orientador**  | `professor.orientador@example.com`  | `professor123` | `ROLE_PROFESSOR` | Orientador ativo (`ID 1`, depto `DAELN`, `3` vagas disponíveis).                                                                       |
| **Professor Responsável** | `professor.responsavel@example.com` | `professor123` | `ROLE_PROFESSOR` | Orientador ativo (`ID 2`, depto `DAELN`, `3` vagas disponíveis) e responsável pela `Turma_TCC` vigente (`2026/2`, `responsavelTurma`). |

> **Nota:** O realm é importado automaticamente na primeira subida do Keycloak. Detalhes da configuração e de como alterá-la em [`docs/keycloak/keycloak.md`](docs/keycloak/keycloak.md).

### Opção B: execução manual do backend e do frontend

1. Suba apenas o banco e o Keycloak:

```bash
cd SIGTCC
docker compose up -d db keycloak
```

2. Em outro terminal, execute o backend apontando para o MySQL exposto em `localhost:3306`. As credenciais são as de `MYSQL_USER` e `MYSQL_PASSWORD` do `SIGTCC/.env`:

- **No Linux / macOS:**

```bash
cd SIGTCC/backend/gestao-tcc
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gestao_tcc \
SPRING_DATASOURCE_USERNAME=tcc_user \
SPRING_DATASOURCE_PASSWORD=tcc_password \
./gradlew bootRun
```

- **No Windows (PowerShell):**

```powershell
cd SIGTCC/backend/gestao-tcc
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/gestao_tcc"
$env:SPRING_DATASOURCE_USERNAME = "tcc_user"
$env:SPRING_DATASOURCE_PASSWORD = "tcc_password"
.\gradlew bootRun
```

3. Em um terceiro terminal, execute o frontend. Sem variáveis definidas, ele usa `http://localhost:8080` para a API e `http://localhost:8081` para o Keycloak:

```bash
cd SIGTCC/frontend
npm install
npm run dev
```

4. Acesse [http://localhost:5173](http://localhost:5173) e faça login com um dos usuários de teste.

### 12.3. Dados, Usuários e Cenário de Demonstração (Reprodutível)

Ao subir o ambiente do zero (`docker compose up`), a migração Flyway `V5__seed_professores_e_turma_vigente.sql` cria os dois orientadores base e a turma vigente `2026/2`. Para popular de forma reprodutível e idempotente as **linhas de pesquisa** dos orientadores e os **três prazos oficiais da turma vigente** (`PROJETO_TCC`, `TCC1` e `TCC2`), execute a partir da raiz do repositório:

```bash
bash SIGTCC/scripts/seed-demo.sh
```

Esse script autentica os três usuários de teste no Keycloak e configura via API REST o seguinte cenário previsível de demonstração:

- **Catálogo de Orientadores (`GET /api/v1/orientadores`):**
  - `Professor Orientador` (`professor.orientador@example.com`): departamento `DAELN`, `3` vagas disponíveis, linhas de pesquisa `Engenharia de Software`, `Arquitetura de Sistemas` e `Qualidade de Software`.
  - `Professor Responsável pela Turma` (`professor.responsavel@example.com`): departamento `DAELN`, `3` vagas disponíveis, linhas de pesquisa `Inteligência Artificial`, `Ciência de Dados` e `Engenharia de Software`.
- **Turma Vigente e Calendário de Prazos (`GET /api/v1/prazos`):**
  - Turma `2026/2` (`vigente = true`), sob responsabilidade de `professor.responsavel@example.com`.
  - Prazos cadastrados em ordem cronológica futura: `PROJETO_TCC` (`+30 dias`), `TCC1` (`+60 dias`) e `TCC2` (`+90 dias`).
- **Aluno (`aluno@example.com`):** sem solicitações ou entregas prévias, permitindo executar do início ao fim o roteiro da demonstração (login → busca e filtro por linha de pesquisa e vagas → envio de solicitação → aceite pelo orientador com decremento de vaga → consulta/edição de prazos → submissão de entrega por link HTTPS → avaliação com parecer `AJUSTES_NECESSARIOS` → reenvio e parecer `APROVADO`).

Para **recriar o cenário limpo do zero** a qualquer momento (por exemplo, antes de uma nova bateria de avaliação ou apresentação):

```bash
cd SIGTCC
docker compose down -v
docker compose up -d
cd ..
bash SIGTCC/scripts/seed-demo.sh
```

### Comandos Úteis

Para acompanhar os logs da aplicação em tempo real:

```bash
docker compose logs -f
```

Para parar a execução e remover os contêineres:

```bash
docker compose down
```

Para recriar o ambiente do zero, apagando também os volumes do banco e do Keycloak:

```bash
docker compose down -v
```

## 13. Como executar os testes unitários

O projeto possui suítes de testes isoladas para as camadas de frontend e backend. Certifique-se de abrir o terminal e navegar para a pasta correspondente antes de executar os comandos.

### Frontend (React + Vitest)

1. Acesse o diretório do frontend a partir da raiz do projeto:

```bash
cd SIGTCC/frontend
```

2. Instale as dependências locais (caso seja a primeira execução fora do Docker):

```bash
npm install
```

3. Execute a suíte de testes:

```bash
npm test
```

4. Para gerar o relatório de cobertura de código (Code Coverage):

```bash
npm run coverage
```

> **Nota:** O relatório de cobertura em HTML é gerado na pasta `SIGTCC/frontend/coverage`. Você pode abrir o arquivo `index.html` diretamente no navegador.

### Backend (Spring Boot + Java)

1. Acesse o diretório base do backend:

```bash
cd SIGTCC/backend/gestao-tcc
```

2. Execute os testes automatizados (unitários e de integração, com H2 em memória) utilizando o wrapper do Gradle:

- **No Linux / macOS:**

```bash
./gradlew test
```

- **No Windows (CMD ou PowerShell):**

```cmd
.\gradlew test
```

> **Nota:** O `./gradlew build` compila e executa os testes, como no CI. O build não gera relatório de cobertura; para medi-la, use a opção "Run with Coverage" da IDE (como o IntelliJ IDEA). Os valores acompanhados pela equipe estão na métrica [`M-01`](docs/metricas/M-01.md).
