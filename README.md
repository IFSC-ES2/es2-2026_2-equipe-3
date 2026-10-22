# Sistema de Gestão Centralizada de TCC

## 1. Equipe

- **DAMARES DO SOCORRO GONCALVES GAIA** (Matrícula: 202510703590) - Papel: Arquiteta de Software
- **EDUARDO CARDOSO OLIVEIRA** (Matrícula: 202510703625) - Papel: Engenheiro de Requisitos e Scrum Master (Sprint 1)
- **MARCUS JHUAN EPIFANIO LIMA** (Matrícula: 202320003071) - Papel: Designer de UX/UI
- **TALLES SOUZA DA CRUZ** (Matrícula: 202510703745) - Papel: Engenheiro de Qualidade (QA)
- **WILLIAN FERREIRA DOS SANTOS** (Matrícula: 202320002996) - Papel: DevOps/Infra

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

## 10. Como executar o aplicativo

Para facilitar a execução dos ambientes de frontend, backend e banco de dados simultaneamente, o projeto está configurado com Docker Compose.

### Pré-requisitos

Certifique-se de ter instalado em sua máquina:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### Passo a Passo

1. Clone o repositório e acesse a pasta raiz do projeto:

```bash
git clone https://github.com/IFSC-ES2/es2-2026_2-equipe-3.git
cd es2-2026_2-equipe-3/SIGTCC

```

2. Construa as imagens e suba os contêineres:

```bash
docker-compose up

```

3. Acesse a aplicação no seu navegador:

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **Backend (API):** [http://localhost:8080](http://localhost:8080)
- **Keycloak (console administrativo):** [http://localhost:8081](http://localhost:8081)

4. Faça login no frontend com um dos usuários de teste do realm `sigtcc`:

| E-mail                              | Senha          | Perfil                                         |
| :---------------------------------- | :------------- | :--------------------------------------------- |
| `aluno@example.com`                 | `aluno123`     | Aluno                                          |
| `professor.orientador@example.com`  | `professor123` | Professor orientador                           |
| `professor.responsavel@example.com` | `professor123` | Professor responsável pela `Turma_TCC` vigente |

> **Nota:** O realm é importado automaticamente na primeira subida do Keycloak. Detalhes da configuração e de como alterá-la em [`docs/keycloak/keycloak.md`](docs/keycloak/keycloak.md).

### Comandos Úteis

Para acompanhar os logs da aplicação em tempo real:

```bash
docker compose logs -f

```

Para parar a execução e remover os contêineres:

```bash
docker compose down

```

## 11. Como executar os testes unitários

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
   npm run test
```

4. Para gerar o relatório de cobertura de código (Code Coverage):

```bash
   npm run coverage
```

> **Nota:** O relatório de cobertura em HTML será gerado na pasta `frontend/coverage`. Você pode abrir o arquivo `index.html` diretamente no seu navegador.

### Backend (Spring Boot + Java)

1. Acesse o diretório base do backend:

```bash
   cd SIGTCC/backend/gestao-tcc
```

2. Execute os testes automatizados utilizando o wrapper do Gradle:

- **No Linux / macOS:**

```bash
     ./gradlew test
```

- **No Windows (CMD ou PowerShell):**

```cmd
     .\gradlew test
```

> **Nota:** Para visualizar a cobertura de código do backend, recomenda-se executar os testes diretamente pela sua IDE (como o IntelliJ IDEA) utilizando a opção "Run with Coverage".
