# Sprint 1 - Entrega 5

Data da entrega: 17/09/2026

## Escopo da Sprint 1

### Issues planejadas (epic e desdobramentos)

**Epic: Implementação da API de Cadastro de Orientador e Vagas (Backend) [#37]**

- #48 Esqueleto da Camada de API e Estrutura Base (Controller, Service, Repository e Entidade)
- #49 Implementar Endpoint de Cadastro de Orientador (POST)
- #50 Implementar Endpoint de Listagem de Orientadores (GET)
- #51 Implementar Endpoint de Consulta por ID (GET /id)
- #52 Implementar Endpoint de Atualização Parcial (PATCH /id)
- #53 Implementar Endpoint de Remoção/Desativação (DELETE /id)
- #69 Implementar Testes Unitários para a API de Orientadores

**Epic: Interface de Cadastro de Perfil de Orientador (Frontend) [#38]**

- #59 Configuração Base, API e Interfaces
- #63 Componente Dinâmico de "Linhas de Pesquisa"
- #64 Tela de Cadastro de Orientador (POST)
- #65 Tela de Edição de Perfil (GET e PATCH)
- #66 Tela de Catálogo/Listagem de Orientadores (GET)

**Setup, Testes e Planejamento**

- #35 Setup do Repositório, .gitignore e Ambientes de Desenvolvimento
- #36 Planejamento inicial e definição do escopo da Sprint 1
- #39 Implementação de Testes de Unidade Automatizados (Sprint 1)

**DevOps, Métricas e Fechamento**

- #40 Expansão do CI para Builds e Testes Automatizados
- #41 Coleta de Métricas, Registro de Contribuições e Atualização de README
- #42 Homologação e Publicação da Release v0.1.0
- #43 Relatório de Fechamento e Retrospectiva da Sprint 1

### Status

**Fase Atual:** Em Fechamento
**Progresso das Issues:** 17 concluídas / 4 abertas

- **Concluído:** Todo o desenvolvimento do _vertical slice_ planejado para a sprint, incluindo o esqueleto da API (Backend), endpoints do CRUD de Orientadores, interface de usuário (Frontend), além da configuração do repositório e testes unitários.
- **Em Andamento / Pendente:** Tarefas relacionadas à governança, métricas e DevOps (Issues #40 a #43). O time está focado na expansão do CI para testes automatizados, coleta de métricas, publicação da Release v0.1.0 e no relatório de retrospectiva.

## Justificativa do vertical slice

O vertical slice escolhido para a Sprint 1 foi o Cadastro de Perfil de Orientador e Vagas (US02). A escolha se justifica por sua prioridade alta no backlog e pela restrição técnica de não implementar o sistema de login (US01) neste primeiro momento. O cadastro de orientador é uma funcionalidade central e de alto valor para o MVP, funcionando como a base para o futuro "match" entre alunos e professores. Implementar esse fluxo através de interface, lógica e persistência permitiu validar a integração completa da stack (React, Spring Boot e MySQL), ajudando a mitigar os riscos técnicos mapeados.

## Incremento funcional entregue

- Fluxo completo de cadastro e visualização de perfil de orientador e vagas
- Camadas envolvidas: interface, lógica de aplicação e persistência
- Endpoints: POST e GET para o perfil de orientador

## Testes de unidade automatizados

- Testes da camada de serviço e regras de negócio do cadastro cobrindo cenários de sucesso e falha
- Execução: comandos padrão via CLI (ex: `./mvnw test` para o backend)

## Backlog e board

- **Board e backlog:** https://github.com/orgs/IFSC-ES2/projects/31
- **Issues com critérios de aceitação no GitHub:**
  - Tarefas de planejamento, gestão e CI/CD (referências: #35, #36, #39, #40, #41, #42, #43).
  - Epics e sub-issues de desenvolvimento (referências: Epics #37 e #38; desdobramentos #48 a #53, #59, #63 a #66, e #69).

## Fluxo de trabalho

- Desenvolvimento realizado na ramificação `entrega-5` e sub-branches via Pull Requests
- Revisão e aprovação de PRs com checks obrigatórios (build, lint, formatação e testes) executados no GitHub Actions
- Integração ao ramo principal realizada exclusivamente por commit de mesclagem

## Contribuições individuais

- **Marcus Jhuan Epifanio Lima:**
  - **Atuação principal:** Desenvolvimento Frontend (Telas e Integração).
  - **Epic de contribuição:** #38 Interface de Cadastro de Perfil de Orientador (Frontend).
  - **Issue #64 Tela de Cadastro de Orientador (POST):** Construção do formulário de cadastro com validações, integração do endpoint POST para a criação do perfil, redirecionamento e tratamento de respostas de erro da API (como 400 Bad Request e 409 Conflict).
  - **Issue #65 Tela de Edição de Perfil (GET e PATCH):** Desenvolvimento da interface de edição, incluindo o preenchimento automático dos dados via GET ao carregar a página e o envio do payload de atualização via PATCH, lidando com cenários de sucesso e mensagens de erro nos campos inválidos.
  - **Issue #66 Tela de Catálogo/Listagem de Orientadores (GET):** Implementação da vitrine de exibição dos perfis cadastrados, criação do componente de _Card_ para os professores e integração da busca por área de atuação (parâmetro `?area=texto`), incluindo as renderizações de fallback em caso de erro ou lista vazia.

- **Damares do Socorro Goncalves Gaia:**
  - **Atuação principal:** Desenvolvimento Backend (API REST) e Documentação de Arquitetura.
  - **Epic de contribuição:** #37 Implementação da API de Cadastro de Orientador e Vagas (Backend).
  - **Issue #51 Implementar Endpoint de Consulta por ID (GET /id):** Desenvolvimento da lógica na camada de serviço para buscar os detalhes de um orientador específico, garantindo o retorno de status HTTP 200 OK com o objeto completo para buscas com sucesso, e HTTP 404 Not Found caso o ID seja inválido ou inexistente no banco de dados.
  - **Issue #52 Implementar Endpoint de Atualização Parcial (PATCH /id):** Implementação do endpoint e da regra de negócio para receber alterações opcionais no cadastro do orientador (como linhas de pesquisa e vagas disponíveis), incluindo validações para impedir que as vagas fiquem negativas e tratamento de erros.
  - **PR #46 docs: especifica contrato de dados da api para a us02:** Elaboração do documento de especificação técnica e arquitetura de software da Sprint 1 (`docs/contrato-dados-us02.md`). O artefato estabeleceu a padronização dos endpoints REST, a estrutura dos payloads JSON, o dicionário de dados com regras de _Bean Validation_, códigos HTTP e critérios de aceitação em formato BDD.

- **Eduardo Cardoso Oliveira:**
  - **Atuação principal:** Desenvolvimento Fullstack (Backend e Frontend) e Gestão da Sprint (Planejamento e Métricas).
  - **Epics de contribuição:** #37 Implementação da API (Backend) e #38 Interface de Cadastro (Frontend).
  - **Issue #36 Planejamento inicial e definição do escopo:** Elaboração do documento de planejamento definindo a meta da sprint, justificando a escolha da US02 como o _vertical slice_ inicial e organizando o acompanhamento no GitHub Projects.
  - **Issue #49 Implementar Endpoint de Cadastro de Orientador (POST):** Desenvolvimento da lógica de negócio, persistência e endpoint REST, implementando o `OrientadorRequestDTO` com anotações de _Bean Validation_ e regra na `OrientadorService` para bloquear e-mails duplicados (HTTP 409 Conflict).
  - **Issue #50 Implementar Endpoint de Listagem de Orientadores (GET):** Construção do endpoint de consulta pública para alimentar o catálogo do frontend, com conversão de dados para DTOs e suporte à filtragem opcional por linha de pesquisa via _query param_.
  - **Issue #63 Componente Dinâmico de "Linhas de Pesquisa":** Criação do componente isolado no React permitindo a adição e remoção de _tags_ (pílulas), com validação ativa de caracteres (2 a 80).
  - **Issues em andamento (#41 e #43):** Responsável por compilar as métricas finais, registrar contribuições, atualizar o README com instruções de execução e redigir o relatório de fechamento e retrospectiva da sprint.

- **Talles Souza da Cruz:**
  - **Atuação principal:** Configuração de Arquitetura Base (Setup), Qualidade (Testes Unitários) e Desenvolvimento Fullstack.
  - **Epics de contribuição:** #37 Implementação da API (Backend) e #38 Interface de Cadastro (Frontend).
  - **Issue #35 Setup do Repositório, .gitignore e Ambientes:** Configuração inicial da estrutura do projeto, gerando o `.gitignore`, inicializando os projetos base (Spring Boot e React/Vite + TypeScript) e configurando o `docker-compose.yml` para o MySQL local.
  - **Issue #48 Esqueleto da Camada de API e Estrutura Base:** Fundação do recurso no Spring Boot, estruturando as entidades JPA/Hibernate, repositórios, `OrientadorService` e o controlador base mapeado em `/api/v1/orientadores`.
  - **Issue #53 Implementar Endpoint de Remoção/Desativação (DELETE /id):** Desenvolvimento do endpoint de exclusão lógica/física com retornos padronizados.
  - **Issue #59 Configuração Base, API e Interfaces:** Fundação do projeto React criando tipagens (`src/types/Orientador.ts`), cliente HTTP apontado para `VITE_API_URL` e estruturando o roteamento primário via React Router DOM.
  - **Issues #39 e #69 Implementação de Testes de Unidade Automatizados:** Desenvolvimento da suíte completa de testes da API, incluindo testes de Controller (MockMvc), Serviço (Mockito), Repositório (`@DataJpaTest`) e tratamentos de erro globais.

- **Willian Ferreira dos Santos:**
  - **Atuação principal:** DevOps e Integração Contínua (CI/CD).
  - **Issue em andamento (#40 Expansão do CI para Builds e Testes Automatizados):** Responsável por aprimorar o pipeline de Integração Contínua utilizando GitHub Actions. A tarefa exige a configuração de workflows automatizados para executar a rotina de _build_ do Backend e do Frontend, além de rodar a suíte de testes de unidade a cada novo código submetido, garantindo que os _checks_ sejam obrigatórios nos Pull Requests direcionados às branches `entrega-5` e `main`.

## Release

- Tag e release: v0.1.0
