# Integração Contínua (CI)

Este documento descreve a infraestrutura de Integração Contínua (CI) configurada para o Sistema de Gestão Centralizada de TCC, estabelecendo os critérios mínimos de qualidade que devem ser validados de forma automatizada. A configuração atende aos requisitos definidos para a estabilidade do projeto e mitigação de falhas e regressões.

## 1. Ferramenta de CI

Utilizamos o **GitHub Actions** para a orquestração dos pipelines. Todos os arquivos de configuração (_workflows_) estão versionados no diretório `.github/workflows/`.

## 2. Gatilhos de Execução (Triggers)

Para garantir que códigos problemáticos não cheguem à produção, os _workflows_ são executados de forma automatizada nas seguintes condições:

- Abertura de um **Pull Request (PR)** apontando para o ramo principal `main` ou para as branches de entrega (`entrega-*`).
- Envio de novos _commits_ para uma ramificação que já possui um PR aberto.

## 3. Workflows e Checks Obrigatórios

O pipeline está estruturado de forma incremental, garantindo a integridade da documentação, formatação e testes automatizados.

### 3.1. Validação de Documentação e Estilo (Ativos)

- **Arquivos:** `verificacao-docs.yml` e `verificar-formatacao.yml`.
- **O que avalia:**
  - Verifica a existência do arquivo `README.md` na raiz do repositório.
  - Valida a sintaxe dos arquivos de infraestrutura `SIGTCC/docker-compose.yml` e `SIGTCC/docker-compose.prod.yml`.
  - Executa o **Prettier** para garantir que todos os arquivos do repositório sigam um padrão consistente de formatação (espaçamentos, quebras de linha, etc).

### 3.2. Validação do Backend (Ativa)

A validação automatizada da API (Java 21 / Spring Boot) inclui:

- **Build:** Compilação do projeto via Gradle.
- **Testes:** Execução da suíte de testes unitários e de integração (JUnit 5 / Mockito) utilizando banco de dados em memória H2 configurado em modo de compatibilidade (`MODE=MySQL`).
- **Relatórios:** Publicação dos resultados dos testes como artefatos disponíveis para download no GitHub Actions (`relatorio-testes-gradle`).

### 3.3. Validação do Frontend (Ativa)

A validação da aplicação web (React / TypeScript / Vite) inclui:

- **Build:** Compilação do Vite para garantir ausência de erros de tipagem estrutural.
- **Linting de Código:** ESLint para garantir as boas práticas do React.
- **Testes:** Execução de testes de componentes com Vitest e Testing Library.

### 3.4. Build de Imagens Docker (Staging)

Para dar suporte à validação em ambiente de staging (a partir da `entrega-8`), o pipeline inclui a geração e publicação de imagens Docker automatizadas.

- **Build e Push:** Utiliza o Docker Buildx para compilar as imagens do Backend e do Frontend simultaneamente.
- **Registry:** As imagens são publicadas de forma privada/pública no GitHub Container Registry (GHCR), vinculadas ao repositório.
- **Versionamento:** Cada build gera uma tag `latest` e uma tag com o SHA do commit (ex: `a1b2c3d`), garantindo rastreabilidade para o staging.
- **Cache:** Utiliza a API de cache nativa do GitHub Actions (`type=gha`) para otimizar o tempo de construção das camadas do Docker.

## 4. Política de Bloqueio e Quality Gates

A integração atua como a primeira linha de defesa contra bugs e erros (Risco R02 - Falhas de Regressão). Para que um Pull Request possa ser mesclado ao ramo principal, ele precisa obrigatoriamente:

1. Passar sem falhas em todas as etapas (checks) do GitHub Actions.
2. Receber a aprovação manual de revisão de código por ao menos um colega da equipe.

Caso algum _check_ do pipeline falhe, o botão de _Merge_ do GitHub ficará bloqueado, exigindo que o desenvolvedor corrija o erro na branch de origem e faça um novo _push_ para disparar o pipeline novamente.

## 5. Evidência de Execução do Pipeline — Sprint 3

Durante a Sprint 3, foi realizada uma execução completa e bem-sucedida do pipeline de Integração Contínua em um Pull Request direcionado à ramificação `entrega-7`.

A execução validou os workflows de **Backend CI** e **Frontend CI**, incluindo build e testes automatizados.

**Pull Request de evidência:** #148 — Atualização do pipeline de CI para a Sprint 3.

**Resultado:** execução concluída com sucesso, com os checks obrigatórios de Backend CI e Frontend CI aprovados.

> [Link para o Pull Request #148](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/182)
