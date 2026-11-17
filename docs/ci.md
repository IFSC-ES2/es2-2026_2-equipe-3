# Integração Contínua (CI)

Este documento descreve a infraestrutura de Integração Contínua (CI) configurada para o Sistema de Gestão Centralizada de TCC, estabelecendo os critérios mínimos de qualidade que devem ser validados de forma automatizada. A configuração atende aos requisitos definidos para a estabilidade do projeto e mitigação de falhas e regressões.

## 1. Ferramenta de CI

Utilizamos o **GitHub Actions** para a orquestração dos pipelines. Todos os arquivos de configuração (_workflows_) estão versionados no diretório `.github/workflows/`.

## 2. Gatilhos de Execução (Triggers)

Para garantir que códigos problemáticos não cheguem à produção, os _workflows_ são executados de forma automatizada. Cada arquivo possui gatilhos específicos:

- `backend-ci.yml` e `verificacao-docs.yml`: Executados em `push` e `pull_request` apontando para o ramo principal (`main`) e branches de entrega (`entrega-*`).
- `frontend-ci.yml`: Executado em `push` e `pull_request` em `main` e `entrega-*`, mas também em eventos de `push` para `feature/*`.
- `verificar-formatacao.yml`: Executado em `push` e `pull_request` para `main` e `entrega-*`.
- `docker-build.yml`: Executado em `push` para `main`, branches de entrega (`entrega-*`) e tags de versão/release (`v*`). Inclui jobs internos de testes unitários (`needs`) que condicionam o build e publicação das imagens.

## 3. Workflows e Checks Obrigatórios

O pipeline está sendo estruturado de forma incremental. Atualmente, ele garante a integridade da documentação, formatação, validação de sintaxe de workflows, testes, cache e build de imagens.

### 3.1. Validação de Documentação, Estilo e Workflows (Ativos)

- **Arquivos:** `verificacao-docs.yml` e `verificar-formatacao.yml`.
- **O que avalia:**
  - Verifica se o arquivo `README.md` existe no repositório.
  - Valida a sintaxe dos arquivos `SIGTCC/docker-compose.yml` e `SIGTCC/docker-compose.prod.yml`.
  - Executa o **Prettier** para garantir que todos os arquivos do repositório sigam um padrão consistente de formatação (espaçamentos, quebras de linha, etc).
  - Executa o **actionlint** para validar a sintaxe e o esquema dos arquivos YAML de workflow do GitHub Actions (`.github/workflows/*.yml`), checando tipos de eventos, expressões e dependências entre jobs.

### 3.2. Validação do Backend (Ativa)

A validação automatizada da API (Java 21 / Spring Boot) inclui:

- **Build e Cache:** Compilação do projeto via Gradle, com cache das dependências do Gradle via `actions/setup-java` (`cache: gradle`) para otimizar o tempo de execução.
- **Testes:** Execução da suíte de testes unitários e de integração (JUnit 5 / Mockito) utilizando banco de dados em memória H2 configurado em modo de compatibilidade (`MODE=MySQL`).
- **Execução Incondicional para `main` (Issue #259):** Em PRs direcionados à branch `main` ou push direto na `main`, a compilação e todos os testes são obrigatoriamente executados, mesmo que o PR contenha apenas alterações em documentação. Em PRs para branches de entrega (`entrega-*`), o `paths-filter` continua otimizando o pipeline caso não haja alterações no backend.
- **Relatórios:** Upload dos relatórios de testes (`relatorio-testes-gradle`) gerados no build para download via GitHub Actions.

### 3.3. Validação do Frontend (Ativa)

A validação da aplicação web (React / TypeScript / Vite) inclui:

- **Build e Cache:** Compilação do Vite e cache das dependências do NPM via `actions/setup-node` (`cache: npm`, baseado no `package-lock.json`).
- **Linting de Código:** ESLint para garantir as boas práticas do React.
- **Testes:** Execução de testes de componentes com Vitest e Testing Library.
- **Execução Incondicional para `main` (Issue #259):** Similarmente ao backend, em PRs direcionados à `main`, lint, testes e build do frontend rodam incondicionalmente. Em PRs para `entrega-*`, dispara quando houver alterações em `SIGTCC/frontend/**`.

### 3.4. Build de Imagens Docker (Staging)

Para dar suporte à validação em ambiente de staging (a partir da `entrega-8`), o pipeline inclui a geração e publicação de imagens Docker automatizadas.

- **Build e Push:** Utiliza o Docker Buildx para compilar as imagens do Backend e do Frontend simultaneamente (condicionado ao sucesso dos testes).
- **Registry:** As imagens são publicadas no GHCR (GitHub Container Registry).
- **Versionamento:** Cada build gera uma tag `latest` e uma tag com o SHA do commit (ex: `a1b2c3d`), garantindo rastreabilidade para o staging.
- **Cache Docker:** Utiliza a API de cache nativa do GitHub Actions (`type=gha`) para otimizar o tempo de construção das camadas do Docker.

## 4. Política de Bloqueio e Quality Gates (Regras de Proteção de Branch)

A integração atua como a primeira linha de defesa contra bugs e erros (Risco R02 - Falhas de Regressão). As ramificações protegidas (`entrega-9` e `main`) exigem obrigatoriamente:

1. **Revisão humana aprovada:** Pelo menos uma aprovação formal em Pull Request antes da integração por commit de mesclagem.
2. **Checks obrigatórios do GitHub Actions aprovados:**
   - `Compilar e Rodar Testes Unitarios (Gradle)` (`backend-ci.yml`)
   - `Compilar, Testar e Gerar Build (Frontend)` (`frontend-ci.yml`)
   - `Checar Formatação de Código (Prettier)` (`verificar-formatacao.yml`)
   - `Validar Sintaxe dos Workflows (actionlint)` (`verificar-formatacao.yml`)
   - `Checar Arquivos Essenciais` (`verificacao-docs.yml`)

Caso algum _check_ do pipeline falhe, o botão de _Merge_ do GitHub permanecerá bloqueado, exigindo que o desenvolvedor corrija o erro na branch de origem e faça um novo _push_ para disparar o pipeline novamente.

## 5. Atualização para a Entrega 9 (Issue #259)

Na Sprint 5 (Entrega 9 / Release Candidate), o pipeline foi atualizado com as seguintes melhorias:

- **Gatilhos de Build de Imagens Docker:** O workflow `docker-build.yml` foi ampliado para disparar em `main`, `entrega-*` e tags de versão (`v*`), garantindo que tanto as branches ativas quanto os releases oficiais (`v1.0.0-rc.1` e futuras tags) publiquem imagens atualizadas no GitHub Container Registry.
- **Execução Incondicional de Testes para `main`:** Os workflows `backend-ci.yml` e `frontend-ci.yml` agora garantem que qualquer Pull Request direcionado ao ramo principal `main` execute a suíte completa de testes e build, prevenindo que alterações apenas de documentação ou configuração subam sem revalidação do código.
- **Validação de Sintaxe dos Workflows:** Inclusão do job `validar-workflows` utilizando `actionlint` para validar automaticamente o esquema dos arquivos YAML do GitHub Actions.
- **Alinhamento dos Checks Obrigatórios:** Atualização das regras de proteção de branch para `entrega-9` e `main`.

## 6. Evidência de Execução do Pipeline — Sprint 4

Durante a Sprint 4, os workflows de Integração Contínua (**Backend CI**, **Frontend CI**, **Verificação de Formatação** e **Verificação de Documentação**) validaram continuamente os Pull Requests direcionados à ramificação `entrega-8`, incluindo build, testes automatizados, formatação Prettier e validação de sintaxe Docker Compose (`docker-compose.yml` e `docker-compose.prod.yml`).

- **Atualização do pipeline na Sprint 4 (Issue #219):** [Pull Request #238](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/238) (`Docs/219 atualiza ci`), que ajustou os triggers dos workflows para `entrega-8` e configurou o build multi-stage Docker no GHCR.
- **Execução completa com código e testes (Issue #229):** [Pull Request #247](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/247) (`Test/229 cenarios integracao us07`), integrado à `entrega-8` com os checks obrigatórios aprovados.

> Na Sprint 3 (Entrega 7), a evidência anterior de execução do pipeline foi registrada no [Pull Request #182](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/182).
