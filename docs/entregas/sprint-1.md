# Sprint 1 - Entrega 5

Data da entrega: 17/09/2026

## Escopo da Sprint 1

### Issues planejadas (epic e desdobramentos)

- US02 (epic) - Cadastro de Perfil de Orientador e Vagas
- #35 - Setup do Repositório, .gitignore e Ambientes de Desenvolvimento
- #36 - Planejamento inicial e definição do escopo da Sprint 1
- #37 - Implementação da API de Cadastro de Orientador e Vagas (Backend)
- #38 - Interface de Cadastro de Perfil de Orientador (Frontend)
- #39 - Implementação de Testes de Unidade Automatizados (Sprint 1)
- #40 - Expansão do CI para Builds e Testes Automatizados
- #41 - Coleta de Métricas, Registro de Contribuições e Atualização de README
- #42 - Homologação e Publicação da Release v0.1.0
- #43 - Relatório de Fechamento e Retrospectiva da Sprint 1

### Status

- Concluídas: [preencher]
- Parciais/Replanejadas: [preencher]

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

- Board e backlog: https://github.com/orgs/IFSC-ES2/projects/31
- Issues com critérios de aceitação no GitHub (referências: #35, #36, #37, #38, #39, #40, #41, #42, #43)

## Fluxo de trabalho

- Desenvolvimento realizado na ramificação `entrega-5` e sub-branches via Pull Requests
- Revisão e aprovação de PRs com checks obrigatórios (build, lint, formatação e testes) executados no GitHub Actions
- Integração ao ramo principal realizada exclusivamente por commit de mesclagem

## Contribuições individuais

- **Marcus Jhuan Epifanio Lima:** descrever contribuições(Issues:)
- **Damares do Socorro Goncalves Gaia:** descrever contribuições(Issues:)
- **Eduardo Cardoso Oliveira:** descrever contribuições(Issues:)
- **Talles Souza da Cruz:** descrever contribuições(Issues:)
- **Willian Ferreira dos Santos:** descrever contribuições(Issues:)

## Release

- Tag e release: v0.1.0
