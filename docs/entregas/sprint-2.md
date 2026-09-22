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
   - O aluno pode enviar uma proposta de orientação a um professor disponível (com tema e descrição inicial).
   - O orientador pode consultar as solicitações pendentes recebidas.
   - O orientador pode **Aprovar** (o que decrementa automaticamente suas vagas ofertadas) ou **Recusar** a solicitação (com registro de justificativa).
   - O aluno acompanha o status de sua solicitação (`PENDENTE`, `APROVADA`, `RECUSADA`).

2. **Fatia Essencial da US03 - Busca e Filtragem Operacional (Integrada à US04):**
   - Na vitrine de orientadores construída na Sprint 1 (US02), disponibilizar o botão de ação rápida **"Solicitar Orientação"** condicionado à existência de vagas (`vagasDisponiveis > 0`).
   - Indicador visual claro de status de vagas (disponível vs. esgotado) para guiar o fluxo do aluno.

### 1.3 Justificativa da Escolha do Escopo
A seleção da US04 acompanhada apenas da fatia operacional da US03 fundamenta-se nos seguintes pilares do projeto:

- **Alinhamento com o MVP:** O catálogo estático entregue na Sprint 1 (US02) atua como vitrine. A solicitação e o aceite formalizam a principal proposta de valor inicial do sistema: acabar com o envio informal de solicitações e centralizar o "match" acadêmico de TCC.
- **Capacidade e Velocidade Histórica (Métrica M-03):** Na Sprint 1, a equipe entregou com sucesso 5 Story Points (US02). Manter a meta em 5 Story Points para a US04 respeita o ritmo sustentável do time e a capacidade declarada de 28 horas semanais ([`docs/BASELINE.md`](../BASELINE.md)).
- **Mitigação do Risco R01 (Sobrecarga) e R04 (Crescimento de Escopo):** Adiar filtros avançados e buscas combinadas complexas da US03 impede o inchaço do escopo (*scope creep*) e permite que o time foque na qualidade e robustez da regra de negócio central.
- **Mitigação do Risco R03 (Atraso de Integração):** O contrato de dados entre backend e frontend é definido no início da sprint, permitindo paralelismo no desenvolvimento das telas e das APIs.

### 1.4 Status e Histórico de Escopo
- **Concluído na Sprint 1:** US02 - Cadastro de Perfil de Orientador e Vagas (5 SP concluídos integralmente no marco `v0.1.1`).
- **Planejado para a Sprint 2:** US04 (5 SP) + fatia essencial da US03 (suporte à ação de solicitar no catálogo).
- **Postergado para Sprints Futuras:** US01 (Autenticação e Perfis com Spring Security/JWT) e filtros avançados combinados da US03, além do fluxo de documentos (US05, US06 e US07).

---

## 2. Aplicação Justificada de Padrões de Projeto OO

A equipe identificou problemas reais de arquitetura na evolução do sistema e planejou a aplicação de pelo menos **dois padrões de projeto OO**:

### 2.1 Padrão 1: Strategy (Comportamental)
- **Problema Identificado:** A criação de uma solicitação de orientação exige múltiplas validações de negócio com regras variáveis e expansíveis (ex.: verificar se o orientador possui vagas abertas, verificar se o aluno já possui solicitação pendente ativa, validar formato do tema). Centralizar tudo isso em cadeias de `if/else` no serviço viola o princípio Aberto/Fechado (OCP) e dificulta a escrita de testes de unidade isolados.
- **Solução com Strategy:** Criação da interface `ValidadorSolicitacaoStrategy` e de estratégias concretas (ex.: `ValidacaoVagasDisponiveisStrategy`, `ValidacaoSolicitacaoDuplicadaStrategy`). O serviço de criação itera sobre uma lista de estratégias injetadas pelo Spring, facilitando a adição de novas regras futuras sem alterar o código do serviço.
- **Classes/Módulos Afetados:** `br.edu.ifsc.gestao_tcc.strategy.*`, `SolicitacaoService`.
- **Benefícios:** Alta extensibilidade, baixo acoplamento e facilidade de testes unitários.
- **Trade-off:** Criação de mais interfaces e classes pequenas no backend.

### 2.2 Padrão 2: Observer (Comportamental)
- **Problema Identificado:** Quando uma solicitação muda de estado (de `PENDENTE` para `APROVADA` ou `RECUSADA`), ações secundárias distintas precisam ser disparadas: atualizar o quantitativo de vagas do orientador, registrar log de auditoria do TCC e emitir notificações/e-mails ao aluno. Vincular essas chamadas diretamente dentro do método de negócio acopla o serviço de solicitações a múltiplos subsistemas.
- **Solução com Observer:** A classe de serviço atua como publicadora do evento de transição de status (`SolicitacaoStatusChangedEvent`), notificando observadores desacoplados (ex.: `AtualizadorVagasObserver`, `NotificadorEmailObserver`).
- **Classes/Módulos Afetados:** `br.edu.ifsc.gestao_tcc.observer.*`, `SolicitacaoService`.
- **Benefícios:** Desacoplamento do fluxo principal em relação às reações colaterais do sistema.
- **Trade-off:** Ordem de execução dos observadores não é estritamente garantida e requer atenção ao gerenciamento de transações de banco de dados.

*(A formalização detalhada e as decisões arquiteturais serão consolidadas na `ADR-0007` e no artefato [`docs/PADROES-DE-PROJETO.md`](../PADROES-DE-PROJETO.md)).*

---

## 3. Incremento Funcional Planejado

### 3.1 Backend (Spring Boot 4 / Java 21)
- **Nova Entidade e Migração Flyway:** `SolicitacaoOrientacao` com chave primária, relacionamento com `Orientador`, nome e e-mail do aluno, tema proposto, descrição/justificativa, status (`PENDENTE`, `APROVADA`, `RECUSADA`, `CANCELADA`) e data de criação.
- **Endpoints REST:**
  - `POST /api/v1/solicitacoes`: Envio de nova solicitação pelo aluno.
  - `GET /api/v1/solicitacoes/orientador/{orientadorId}`: Listagem das solicitações recebidas pelo orientador (com filtro opcional por status).
  - `GET /api/v1/solicitacoes/{id}`: Detalhamento de uma solicitação específica.
  - `PATCH /api/v1/solicitacoes/{id}/status`: Atualização do status pelo orientador (aprovação ou recusa com motivo).

### 3.2 Frontend (React / Vite / TypeScript)
- **Atualização do Catálogo de Orientadores:**
  - Inclusão do botão "Solicitar Orientação" nos cards de orientadores que possuem vagas disponíveis.
  - Bloqueio ou feedback caso as vagas estejam zeradas.
- **Fluxo de Envio de Solicitação:** Modal/Formulário para o aluno informar seus dados, tema pretendido e mensagem para o orientador.
- **Painel de Gestão de Solicitações:** Tela ou seção onde o professor visualiza a fila de solicitações e executa ações de aceite ou recusa com retorno visual em tempo real.

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

## 7. Próximos Passos e Acompanhamento
*(Esta seção será atualizada continuamente durante a execução da sprint até o fechamento com as métricas observadas, matriz de riscos revisada e registro detalhado de contribuições individuais).*
