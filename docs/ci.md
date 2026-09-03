# Integração Contínua (CI)

Este documento descreve a infraestrutura de Integração Contínua (CI) configurada para o Sistema de Gestão Centralizada de TCC, estabelecendo os critérios mínimos de qualidade que devem ser validados de forma automatizada. A configuração atende aos requisitos definidos para a estabilidade do projeto e mitigação de falhas e regressões.

## 1. Ferramenta de CI
Utilizamos o **GitHub Actions** para a orquestração dos pipelines. Todos os arquivos de configuração (*workflows*) estão versionados no diretório `.github/workflows/`.

## 2. Gatilhos de Execução (Triggers)
Para garantir que códigos problemáticos não cheguem à produção, os *workflows* são executados de forma automatizada nas seguintes condições[ci]:
* Abertura de um **Pull Request (PR)** apontando para o ramo principal `main`.
* Envio de novos *commits* para uma ramificação que já possui um PR aberto.

## 3. Workflows e Checks Obrigatórios

O pipeline está sendo estruturado de forma incremental. Atualmente, ele garante a integridade da documentação e formatação, com a fundação preparada para as validações de software nas próximas etapas.

### 3.1. Validação de Documentação e Estilo (Ativos)
* **Arquivos:** `verificacao-docs.yml`, `verificar-links.yml` e `verificar-formatacao.yml`.
* **O que avalia:** 
  * Verifica se todos os links internos e externos da documentação estão válidos.
  * Garante que os arquivos Markdown cumprem a sintaxe correta.
  * Executa o **Prettier** para garantir que todos os arquivos do repositório sigam um padrão consistente de formatação (espaçamentos, quebras de linha, etc).

### 3.2. Validação do Backend (Planejado para a Entrega 5 - Sprint 1)
A partir do início da codificação (Java 21 / Spring Boot), o CI será expandido para incluir:
* **Build:** Compilação do projeto via Gradle.
* **Testes:** Execução da suíte de testes unitários (JUnit).
* **Cobertura:** Validação da métrica M-01.

### 3.3. Validação do Frontend (Planejado para a Entrega 5 - Sprint 1)
Com o início da construção das interfaces (React / TypeScript), o CI passará a avaliar:
* **Build:** Compilação do Vite para garantir ausência de erros de tipagem estrutural.
* **Linting de Código:** ESLint para garantir as boas práticas do React.
* **Testes:** Suíte inicial de testes de interface.

## 4. Política de Bloqueio e Quality Gates
A integração atua como a primeira linha de defesa contra bugs e erros (Risco R02 - Falhas de Regressão). Para que um Pull Request possa ser mesclado ao ramo principal, ele precisa obrigatoriamente:
1. Passar sem falhas em todas as etapas (checks) do GitHub Actions.
2. Receber a aprovação manual de revisão de código por ao menos um colega da equipe.

Caso algum *check* do pipeline falhe, o botão de *Merge* do GitHub ficará bloqueado, exigindo que o desenvolvedor corrija o erro na branch de origem e faça um novo *push* para disparar o pipeline novamente.