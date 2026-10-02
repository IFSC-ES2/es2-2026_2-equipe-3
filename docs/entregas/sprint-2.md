# Sprint 2 - Entrega 6

- **Data de Início:** 18/09/2026
- **Data da Entrega:** 01/10/2026
- **Scrum Master da Sprint 2:** Damares do Socorro Gonçalves Gaia
- **Marco de Release:** `v0.2.0`
- **Branch de Trabalho:** `entrega-6`

---

## 1. Planejamento da Sprint 2

### 1.1 Meta da Sprint (Sprint Goal)

> **"Implementar o fluxo completo de solicitação e aceite de orientação entre aluno e professor (match), integrando a seleção de orientadores com vagas abertas e aplicando padrões de projeto OO para desacoplamento de regras de elegibilidade e notificações."**

### 1.2 Seleção dos Itens do Backlog

Para este ciclo, a equipe selecionou o avanço central do fluxo de vinculação do MVP:

1. **US04 - Solicitação e Aceite de Orientação (5 Story Points):**
   - O aluno pode enviar uma proposta de orientação a um professor disponível (informando seus dados cadastrais, curso, tema pretendido e mensagem/descrição inicial).
   - O envio da solicitação cria ou reaproveita uma entidade `Aluno` (busca prévia por e-mail: se o aluno não existir no banco, cria-se o registro como subtipo de `Usuario` sem credenciais ativas de login, preparando o terreno para a futura US01).
   - O orientador pode consultar as solicitações pendentes recebidas.
   - O orientador pode **Aceitar** (o que decrementa automaticamente suas vagas ofertadas) ou **Recusar** a solicitação (com registro de justificativa).
   - O aluno acompanha o status de sua solicitação (`PENDENTE`, `ACEITA`, `RECUSADA`, `CANCELADA`).

2. **Fatia Essencial da US03 - Busca e Filtragem Operacional (Integrada à US04):**
   - Na vitrine de orientadores construída na Sprint 1 (US02), disponibilizar o botão de ação rápida **"Solicitar Orientação"** condicionado à existência de vagas (`vagasDisponiveis > 0`).
   - Indicador visual claro de status de vagas (disponível vs. esgotado) para guiar o fluxo do aluno.

### 1.3 Justificativa da Escolha do Escopo

A seleção da US04 acompanhada apenas da fatia operacional da US03 fundamenta-se nos seguintes pilares do projeto:

- **Alinhamento com o MVP:** O catálogo estático entregue na Sprint 1 (US02) atua como vitrine. A solicitação e o aceite formalizam a principal proposta de valor inicial do sistema: acabar com o envio informal de solicitações e centralizar o "match" acadêmico de TCC.
- **Capacidade e Velocidade Histórica (Métrica M-03):** Na Sprint 1, a equipe entregou com sucesso 5 Story Points (US02). Manter a meta em 5 Story Points para a US04 respeita o ritmo sustentável do time e a capacidade declarada de 28 horas semanais ([`docs/BASELINE.md`](../BASELINE.md)).
- **Mitigação do Risco R01 (Sobrecarga) e R04 (Crescimento de Escopo):** Adiar filtros avançados e buscas combinadas complexas da US03 impede o inchaço do escopo (_scope creep_) e permite que o time foque na qualidade e robustez da regra de negócio central.
- **Mitigação do Risco R03 (Atraso de Integração):** O contrato de dados entre backend e frontend é definido no início da sprint, permitindo paralelismo no desenvolvimento das telas e das APIs.

### 1.4 Status e Histórico de Escopo

- **Concluído na Sprint 1:** US02 - Cadastro de Perfil de Orientador e Vagas (5 SP concluídos integralmente no marco `v0.1.1`).
- **Planejado para a Sprint 2:** US04 (5 SP) + fatia essencial da US03 (suporte à ação de solicitar no catálogo).
- **Postergado para Sprints Futuras:** US01 (Autenticação e Perfis com Spring Security/JWT) e filtros avançados combinados da US03, além do fluxo de documentos (US05, US06 e US07).

---

## 2. Aplicação Justificada de Padrões de Projeto OO

A equipe identificou problemas reais de arquitetura na evolução do sistema e planejou a aplicação de pelo menos **dois padrões de projeto OO**:

### 2.1 Padrão 1: Strategy (Comportamental)

- **Problema Identificado:** A criação de uma solicitação de orientação exige múltiplas validações de negócio com regras variáveis e expansíveis (ex.: verificar se o orientador possui vagas abertas, verificar se o aluno já possui solicitação pendente ativa para aquele orientador, validar preenchimento e tamanho do tema). Centralizar tudo isso em cadeias de `if/else` no serviço viola o princípio Aberto/Fechado (OCP) e dificulta a escrita de testes de unidade isolados.
- **Solução com Strategy:** Criação da interface `ValidadorSolicitacaoStrategy` e de estratégias concretas (ex.: `ValidacaoVagasDisponiveisStrategy`, `ValidacaoSolicitacaoDuplicadaStrategy`). A estratégia `ValidacaoSolicitacaoDuplicadaStrategy` checa duplicidade comparando a chave estrangeira `aluno_id` (FK) e `orientador_id` em status `PENDENTE`, garantindo integridade relacional sem comparações frágeis de strings. O serviço de criação itera sobre a lista de estratégias injetadas pelo Spring, facilitando a adição de novas regras futuras sem alterar o código do serviço.
- **Classes/Módulos Afetados:** `br.edu.ifsc.gestao_tcc.strategy.*`, `SolicitacaoService`.
- **Benefícios:** Alta extensibilidade, baixo acoplamento e facilidade de testes unitários.
- **Trade-off:** Criação de mais interfaces e classes pequenas no backend.

### 2.2 Padrão 2: Observer (Comportamental)

- **Problema Identificado:** Quando uma solicitação muda de estado (de `PENDENTE` para `ACEITA` ou `RECUSADA`), ações secundárias distintas precisam ser disparadas: atualizar o quantitativo de vagas do orientador, registrar log de auditoria do TCC e emitir notificações/e-mails ao aluno. Vincular essas chamadas diretamente dentro do método de negócio acopla o serviço de solicitações a múltiplos subsistemas.
- **Solução com Observer:** A classe de serviço atua como publicadora do evento de transição de status (`SolicitacaoStatusChangedEvent`), notificando observadores desacoplados (ex.: `AtualizadorVagasObserver`, `NotificadorEmailObserver`).
- **Classes/Módulos Afetados:** `br.edu.ifsc.gestao_tcc.observer.*`, `SolicitacaoService`.
- **Benefícios:** Desacoplamento do fluxo principal em relação às reações colaterais do sistema.
- **Trade-off:** Ordem de execução dos observadores não é estritamente garantida e requer atenção ao gerenciamento de transações de banco de dados.

_(A formalização detalhada dos padrões será consolidada no artefato [`docs/PADROES-DE-PROJETO.md`](../PADROES-DE-PROJETO.md). As decisões arquiteturais correlatas serão formalizadas na `ADR-0007` - Padrões de Projeto na Gestão de Solicitações e na `ADR-0008` - Modelagem da Entidade Aluno sem Autenticação na Sprint 2)._

---

## 3. Incremento Funcional Planejado

### 3.1 Backend (Spring Boot 4 / Java 21)

- **Novas Entidades e Mapeamento JPA:**
  - `Aluno`: Subclasse de `Usuario` (mapeada com `@Inheritance(strategy = InheritanceType.JOINED)` e tabela `alunos`), herdando `nome`, `email`, `matricula` e `senha` (com senha nula/não utilizada nesta sprint), adicionando o campo específico `curso` (String).
  - `SolicitacaoOrientacao`: Entidade com chave primária (`id`), relacionamento `@ManyToOne` com `Aluno` (FK `aluno_id`), relacionamento `@ManyToOne` com `Orientador` (FK `orientador_id`), campos `tema` (String), `mensagem` (String/Text com a proposta/justificativa), status via enum `StatusSolicitacao` (`PENDENTE`, `ACEITA`, `RECUSADA`, `CANCELADA`), justificativa de recusa (opcional) e data de criação (`criadoEm`).
  - _Decisão de Relacionamento:_ A solicitação referencia diretamente a entidade `Orientador` (Raiz de Agregação / Aggregate Root) e **não** `PerfilOrientador`, preservando o modelo OO e o encapsulamento de domínio (o controle de vagas é acessado via `orientador.getPerfil().getVagasDisponiveis()`).
  - **Evolução de Esquema do Banco (Migrações Flyway):** Criação das migrações SQL/tabelas para `alunos` e `solicitacoes_orientacao`, assegurando integridade referencial com o schema já existente de `usuarios`, `orientadores` e `perfis_professores`.
- **Endpoints REST:**
  - `POST /api/v1/solicitacoes`: Envio de nova solicitação pelo aluno. O endpoint implementa a lógica de buscar `Aluno` por e-mail; se não existir, instancia e persiste o registro antes de associá-lo à nova `SolicitacaoOrientacao`.
  - `GET /api/v1/solicitacoes/orientador/{orientadorId}`: Listagem das solicitações recebidas pelo orientador (com filtro opcional por status).
  - `GET /api/v1/solicitacoes/{id}`: Detalhamento de uma solicitação específica.
  - `PATCH /api/v1/solicitacoes/{id}/status`: Atualização do status pelo orientador (`ACEITA` ou `RECUSADA` com motivo). O aceite dispara os observadores para decrementar a vaga no perfil do orientador.

### 3.2 Frontend (React / Vite / TypeScript)

- **Atualização do Catálogo de Orientadores:**
  - Inclusão do botão "Solicitar Orientação" nos cards de orientadores que possuem vagas disponíveis.
  - Bloqueio ou feedback caso as vagas estejam zeradas.
- **Fluxo de Envio de Solicitação:** Modal/Formulário para o aluno informar seus dados (nome, e-mail, curso), tema pretendido e mensagem/proposta para o orientador.
- **Painel de Gestão de Solicitações:** Tela ou seção onde o professor visualiza a fila de solicitações e executa ações de aceite (`ACEITA`) ou recusa (`RECUSADA`) com retorno visual em tempo real.

---

## 4. Estratégia de Testes Automatizados

Seguindo o edital da Entrega 6 (itens 3a, 3b, 3c e 3d):

1. **Testes de Unidade:**
   - Teste unitário de cada estratégia de validação (`ValidadorSolicitacaoStrategy`) isoladamente com JUnit 5 e Mockito.
   - Teste unitário do serviço de solicitações cobrindo cenários de sucesso e falha (ex.: tentativa de aprovar solicitação sem vagas disponíveis).
   - Teste unitário de componentes frontend com Vitest e React Testing Library (validações do formulário de solicitação e exibição condicional de botões).
2. **Testes de Integração Simples:**
   - Testes de integração de API utilizando `MockMvc` com persistência em banco H2 em memória, testando o fluxo completo desde a requisição HTTP até a persistência no banco e disparo dos observadores.
3. **Instruções de Execução Local:**
   - **Backend:** `./gradlew test` (a partir de `SIGTCC/backend/gestao-tcc`).
   - **Frontend:** `npm test` (a partir de `SIGTCC/frontend`).

---

## 5. Governança, CI e Fluxo de Trabalho

- **Ramificação Base:** Todo o trabalho da sprint é integrado a partir de e para a ramificação `entrega-6`.
- **Branches de Funcionalidade:** Padrão `feature/US04-...` ou `task/...` com abertura de Pull Request direcionado para `entrega-6`.
- **Critérios de Merge:**
  - Pelo menos uma aprovação de outro membro da equipe via Code Review.
  - Pipeline de CI (build, testes e formatação) 100% verde.
  - Merge realizado exclusivamente através de **commit de mesclagem** (Merge Commit).
- **Integração Final:** Ao final da sprint, a branch `entrega-6` será integrada à `main` via PR revisado com criação da release e tag `v0.2.0`.

---

## 6. Papéis e Responsabilidades na Sprint 2

- **Damares do Socorro Gonçalves Gaia:** Scrum Master da Sprint 2, Arquiteta de Software e Desenvolvimento Frontend (coordenação da sprint, modelagem, ADRs, padrões OO e componentes React da US04).
- **Eduardo Cardoso Oliveira:** Engenheiro de Requisitos e Apoio Geral / Fullstack (critérios de aceitação BDD, regras de negócio da US04 no backend e apoio na integração).
- **Marcus Jhuan Epifanio Lima:** Designer UX/UI e Desenvolvimento Frontend (design de interação, componentes de solicitação e painel do orientador no React).
- **Talles Souza da Cruz:** Engenheiro de Qualidade - QA (testes unitários, testes de integração de API e acompanhamento das métricas de produto).
- **Willian Ferreira dos Santos:** DevOps / Infra e Desenvolvimento Backend (manutenção do CI para `entrega-6`, Docker e implementação de rotas/persistência no backend).

---

## 7. Fechamento e Resultados da Sprint 2

### 7.1 Escopo Planejado vs. Entregue

| Item do Backlog / Atividade                                | Planejado  |  Entregue  |     Status      | Observações / Justificativa                                                                                         |
| :--------------------------------------------------------- | :--------: | :--------: | :-------------: | :------------------------------------------------------------------------------------------------------------------ |
| **US04 - Solicitação e Aceite de Orientação (5 SP)**       |    5 SP    |    5 SP    |  **Concluído**  | Fluxo ponta a ponta implementado (criação de solicitação, aceite/recusa com Observer e acompanhamento pelo aluno).  |
| **US03 (Fatia Essencial) - Ação de Solicitar no Catálogo** | Integrado  | Integrado  |  **Concluído**  | Botão "Solicitar Orientação" condicionado à existência de vagas (`vagasDisponiveis > 0`) no card do orientador.     |
| **Padrões de Projeto OO (Strategy + Observer)**            | 2 padrões  | 2 padrões  |  **Concluído**  | Strategy aplicado na validação de propostas de orientação e Observer aplicado na atualização reativa de vagas.      |
| **Evolução de Esquema e Persistência (Flyway)**            | Planejado  |  Entregue  |  **Concluído**  | Adotado Flyway para versionamento do banco de dados (V1 e V2), com herança `@Inheritance(JOINED)` para `Aluno`.     |
| **US01 - Autenticação e Perfis (Spring Security / JWT)**   | Postergado | Postergado | **Replanejado** | Mantido para a Sprint 3 conforme baseline; aluno modelado sem credenciais ativas ([ADR-0008](../adrs/ADR-0008.md)). |
| **Filtros Avançados Combinados da US03**                   | Postergado | Postergado | **Replanejado** | Busca avançada por múltiplos critérios mantida no backlog para próximas fases do MVP.                               |

### 7.2 Riscos Revisados

A matriz de riscos foi integralmente revisada na Sprint 2 pela equipe de requisitos e arquitetura. Os riscos técnicos e de processo mapeados na Entrega 4 foram atualizados com novos planos de contingência, com destaque para a mitigação bem-sucedida do risco de integração tardia por meio de contratos de dados prévios.

- O detalhamento completo da revisão, status dos riscos ativos, mitigados e ações preventivas está registrado em [`docs/riscos.md`](../riscos.md) (Issue #93).

### 7.3 Métricas Finais da Sprint

Os indicadores de qualidade de código, fluxo de trabalho e avanço do projeto foram coletados e consolidados ao término da Sprint 2:

- Os relatórios e fichas técnicas com a análise aprofundada dos resultados estão documentados em [`docs/metricas`](../metricas/) ([M-01](../metricas/M-01.md), [M-02](../metricas/M-02.md), [M-03](../metricas/M-03.md), [M-04](../metricas/M-04.md), [M-05](../metricas/M-05.md), [M-06](../metricas/M-06.md)) (Issue #94).

---

## 8. Contribuições Individuais Detalhadas

A atuação de cada integrante manteve estrita coerência com os papéis declarados no planejamento da Sprint 2:

### 8.1 Damares do Socorro Gonçalves Gaia

- **Papel:** Scrum Master da Sprint 2, Arquiteta de Software e Desenvolvimento Frontend.
- **Atividades e Entregas:**
  - **Gestão e Planejamento:** Coordenação das cerimônias da Sprint 2, definição e refinamento da meta da sprint e backlog da US04 (**Issue #79**, **PR #80**).
  - **Arquitetura e Contrato de Dados:** Revisão técnica do contrato de dados da API para a US04 (**Issue #81 / Doc-3**, **PR #103**), especificando contratos REST, DTOs e critérios BDD.
  - **Decisões Arquiteturais (ADRs):** Formalização da **ADR-0007** (Modelagem de Aluno e Padrões OO da Sprint 2) e **ADR-0008** (Modelagem da Entidade Aluno sem Autenticação) (**Issue #91 / Doc-1**, **PR #101**).
  - **Padrões de Projeto OO:** Autoria da documentação completa e diagramas UML dos padrões Strategy e Observer em [`docs/PADROES-DE-PROJETO.md`](../PADROES-DE-PROJETO.md) (**Issue #92 / Doc-2**, **PR #110**).
  - **Desenvolvimento Frontend:** Implementação da interface do Painel de Gestão de Solicitações do Professor em React (`PainelSolicitacoesPage`), com listagem, ações de Aceite/Recusa e feedback visual (**Issue #87**, abertura do **PR #112**).
  - **Fechamento da Sprint:** Elaboração do relatório de fechamento e registro de contribuições no `sprint-2.md` (**Issue #98 / Doc-9**).
  - **Revisão de Código (Code Review):** Revisão e aprovação formal dos PRs #103, #107, #115 e #117.

### 8.2 Eduardo Cardoso Oliveira

- **Papel:** Engenheiro de Requisitos e Apoio Geral / Fullstack.
- **Atividades e Entregas:**
  - **Requisitos e Contrato de Dados:** Elaboração técnica e autoria do contrato de dados da US04 e cenários de aceitação em BDD (**Issue #81 / Doc-3**, **PR #103**).
  - **Tratamento de Exceções:** Criação das classes de exceção de domínio e conflito (HTTP 409, 422) e refatoração do manipulador global `GlobalExceptionHandler` (**Issue #90**, **PR #105**).
  - **Gestão de Riscos:** Revisão e atualização completa do plano de riscos do projeto para a Sprint 2 (**Issue #93 / Doc-4**, **PR #107**).
  - **Desenvolvimento Backend:** Implementação das estratégias de validação de propostas de orientação (`ValidacaoOrientadorAtivo`, `ValidacaoSolicitacaoDuplicada`, `ValidacaoVagasDisponiveis`), DTOs e endpoint `POST /api/v1/solicitacoes` (**Issue #83**, **PR #114**).
  - **Integração Backend/Frontend:** Unificação de DTOs, controllers e services para os fluxos de alteração de status com o Observer (**Issue #84**, **PR #116**).
  - **Documentação do Sistema:** Atualização geral do [`README.md`](../../README.md) com novos endpoints e instruções de execução (**Issue #95 / Doc-6**, PR #120).
  - **Revisão de Código (Code Review):** Revisão e aprovação formal dos PRs #80, #106, #108, #109, #110, #113, #118, #119 e #122.

### 8.3 Marcus Jhuan Epifanio Lima

- **Papel:** Designer UX/UI e Desenvolvimento Frontend.
- **Atividades e Entregas:**
  - **Design de Interação e Vitrine:** Inclusão do botão de ação rápida "Solicitar Orientação" nos cards do catálogo, com tratamento condicional quando não há vagas disponíveis (**Issue #86**, **PR #115**).
  - **Formulário de Solicitação:** Desenvolvimento do componente `SolicitacaoForm` no React com validações de campos obrigatórios e feedback de envio (**Issue #86**, **PR #115**).
  - **Tela de Acompanhamento:** Construção da página `AcompanharSolicitacao` para consulta do status da solicitação pelo aluno via ID/URL (**Issue #88**, **PR #115**).
  - **Revisão e Merge de Frontend:** Revisão técnica, aprovação e merge do Painel de Gestão do Professor (**Issue #87**, **PR #112**).
  - **Revisão de Código (Code Review):** Revisão e aprovação formal do **PR #112**.

### 8.4 Talles Souza da Cruz

- **Papel:** Engenheiro de Qualidade - QA e Desenvolvimento Backend.
- **Atividades e Entregas:**
  - **Modelagem de Domínio e Banco:** Criação das entidades `Aluno` (herança JPA JOINED) e `SolicitacaoOrientacao`, além da introdução da biblioteca Flyway com os scripts de migração `V1` e `V2` (**Issue #82**, **PR #106** e **PR #109**).
  - **Decisão Arquitetural Flyway:** Autoria da **ADR-0009** justificando a adoção do Flyway para controle de versão do banco de dados (**PR #106**).
  - **Testes Automatizados:** Implementação da suíte de testes automatizados unitários e de integração cobrindo a US04 (**Issue #89**, PR #118).
  - **Revisão de Qualidade:** Atualização do documento [`docs/qualidade.md`](../qualidade.md) frente às normas da ISO/IEC 25010 e decisões técnicas da sprint (**Issue #96 / Doc-7**, PR #119).
  - **Revisão de Código (Code Review):** Revisão e aprovação formal dos PRs #101, #105, #107, #111, #114, #116, #120 e #121.

### 8.5 Willian Ferreira dos Santos

- **Papel:** DevOps / Infra e Desenvolvimento Backend.
- **Atividades e Entregas:**
  - **Automação de CI/CD:** Aprimoramento e manutenção dos workflows do GitHub Actions (`backend-ci.yml`, `frontend-ci.yml` e `verificacao-docs.yml`), além do ajuste do template de Pull Request para a branch `entrega-6` (**Issue #99 / CI-1**, **PR #108** e **PR #111**).
  - **Padrão Observer no Backend:** Implementação inicial do evento `SolicitacaoStatusChangedEvent` e do listener `AtualizadorVagasObserver` para reação desacoplada ao aceite de orientações (**Issue #84**, **PR #116**).
  - **Endpoints REST:** Implementação do endpoint `GET /api/v1/solicitacoes/{id}` para permitir o acompanhamento pelo aluno (**Issue #85**, **PR #117**).
  - **Infraestrutura e Release:** Suporte à conteinerização Docker e preparação da integração da release `v0.2.0` (**Issue #100 / REL-1**).
  - **Revisão de Código (Code Review):** Revisão e aprovação formal dos PRs #80 e #107.

---

## 9. Release do Marco

- **Tag:** `v0.2.0`
- **Ramificação Base da Sprint:** `entrega-6`
- **Branch Principal Integrada:** `main` (via commit de mesclagem)
- **Descrição da Release:** Publicação no GitHub contendo o resumo executivo das funcionalidades entregues (US03 fatia essencial e US04 completa), arquitetura com padrões OO Strategy e Observer, suíte de testes automatizados, situação do CI e pendências técnicas para os próximos ciclos.
