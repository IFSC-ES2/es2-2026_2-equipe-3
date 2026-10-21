# Avaliação da Entrega 5 - Sprint 1

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC
- Entrega: 5 - Sprint 1
- MVP declarado: aplicação web para autenticação e perfis, catálogo de orientadores e gestão de documentos e prazos de TCC.
- Vertical slice declarado: US02 - cadastro de perfil de orientador, vagas e catálogo, integrando React, Spring Boot e MySQL.
- Versão avaliada: tag `v0.1.0`, commit `903d6db`, resultante do PR #76.
- Data prevista: 17/09/2026.

## Documentos Consultados

- `README.md`.
- `USO-IA.md`.
- `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/PULL_REQUEST_TEMPLATE.md`.
- `.github/workflows/backend-ci.yml`.
- `.github/workflows/frontend-ci.yml`.
- `.github/workflows/verificacao-docs.yml`.
- `.github/workflows/verificar-formatacao.yml`.
- `docs/inception.md`.
- `docs/dod.md`.
- `docs/BASELINE.md`.
- `docs/ESTIMATIVAS.md`.
- `docs/METRICAS.md`.
- `docs/metricas/M-01.md` a `docs/metricas/M-06.md`.
- `docs/ci.md`.
- `docs/fluxo-de-trabalho.md`.
- `docs/qualidade.md`.
- `docs/riscos.md`.
- `docs/contrato-dados-us02.md`.
- `docs/entregas/sprint-1.md`.
- `docs/adrs/ADR-0001.md` a `docs/adrs/ADR-0006.md`.
- Issues e pull requests vinculados à milestone `Entrega-5`.
- Histórico Git, código-fonte, testes, rulesets, tag e release do repositório.

## Resumo da Entrega

A equipe implementou um incremento real e coerente com o MVP: cadastro, consulta, edição, desativação e catálogo de orientadores, com interface React, API Spring Boot e persistência MySQL. Em cópia temporária do commit avaliado, os 30 testes do backend e os 22 testes do frontend passaram. Após contornar a falta de permissão de execução do Gradle Wrapper, também foi possível cadastrar, consultar, filtrar, atualizar e desativar um orientador pela API e confirmar os dados diretamente no MySQL.

A tag correta `v0.1.0` aponta para o merge commit `903d6db` do PR #76. O PR teve aprovação e o check obrigatório de documentação passou. Entretanto, os checks de backend, frontend e formatação falharam e não estavam configurados como obrigatórios. O merge ocorreu em 18/09/2026 às 00:03, três minutos após o prazo, e a release foi publicada às 11:32 do mesmo dia.

As principais perdas decorrem da execução não reprodutível em checkout limpo, dos workflows com caminhos incorretos no marco, das divergências entre contrato e implementação, da rastreabilidade incompleta das métricas e das inconsistências do relatório de fechamento. As correções da tag `v0.1.1` são posteriores e não foram creditadas à Entrega 5.

## Critérios Atendidos

- O slice US02 está alinhado ao catálogo de orientadores e às vagas previstos no MVP.
- O incremento atravessa interface, integração HTTP, lógica de aplicação e persistência relacional.
- O backend implementa cadastro, listagem, filtro, consulta por ID, atualização e desativação.
- O frontend oferece formulário, catálogo, filtro, visualização e edição de orientadores.
- Há 52 testes automatizados versionados e relevantes: 30 no backend e 22 no frontend.
- Todos os testes passaram quando executados na cópia temporária com o Gradle Wrapper chamado por `bash`.
- O README descreve o MVP, o incremento e comandos para aplicação e testes.
- As issues da sprint possuem critérios de aceitação e vínculos com branches e pull requests.
- As seis fichas de métricas receberam registros referentes à Sprint 1.
- As contribuições dos cinco integrantes estão registradas e são majoritariamente confirmadas pelo histórico.
- O desenvolvimento foi consolidado na branch `entrega-5` e integrado à `main` por merge commit.
- O PR #76 teve aprovação humana e o check obrigatório `Checar Arquivos Essenciais` passou.
- A `main` estava protegida por ruleset ativo, exigindo PR, uma aprovação e o check documental.
- A tag e a release `v0.1.0` existem e descrevem o incremento entregue.

## Critérios Parcialmente Atendidos

- O planejamento lista o escopo e justifica a US02, mas não formula claramente a meta, não demonstra a seleção pela capacidade de 28 horas semanais e não registra suficientemente as decisões da reunião e os ajustes de escopo.
- A postergação da US01 é citada como restrição técnica, porém sem detalhamento, ADR ou análise das consequências sobre a US02.
- O incremento é funcional após ajuste local, mas o checkout limpo não inicia pelos comandos documentados porque `gradlew` foi versionado sem permissão de execução.
- Os testes são numerosos e relevantes, mas o comando `./gradlew test` falha em checkout limpo, o lint frontend falha e faltam testes para bugs contratuais observados.
- Backlog, métricas e contribuições existem, porém algumas coletas não são reproduzíveis e o relatório de fechamento apresenta estados divergentes.
- O board é referenciado e há eventos vinculando issues, mas sua visão completa não pôde ser consultada porque o token disponível não possui o escopo `read:project`.
- O fluxo utilizou branch, PR, aprovação e merge commit, mas a proteção obrigava apenas o check documental e permitiu o merge com três checks técnicos falhando.
- O README é informativo, mas as instruções não bastam para iniciar o backend sem intervenção manual.
- O registro de IA é amplo e majoritariamente compatível com os artefatos, mas é agregado, pouco rastreável e apresenta validação genérica.

## Critérios Não Atendidos

- A versão `v0.1.0` não possui pipeline técnico estável: os workflows apontam para `src/...`, enquanto o código está em `SIGTCC/...`.
- Os checks de backend, frontend e formatação do PR final #76 falharam.
- O comando documentado `docker-compose up` não inicia o backend em checkout limpo por falta de permissão de execução em `gradlew`.
- `docs/entregas/sprint-1.md` não contém uma retrospectiva efetiva com aprendizados e ações, embora declare relatório de fechamento e retrospectiva.
- A integração final e a publicação da release não ocorreram até o prazo de 17/09/2026.
- O contrato de dados não corresponde integralmente às validações, ao corpo de resposta do `POST` nem ao formato real dos erros de validação.

## Achados com Evidências

- **Planejamento e MVP:** `docs/entregas/sprint-1.md` lista as issues e justifica a US02, mas não apresenta meta explícita nem cálculo de capacidade. O planejamento é majoritariamente coerente, porém incompleto. Recomendação: registrar meta, capacidade utilizada, decisões e replanejamentos na ata da sprint.
- **Priorização:** `docs/BASELINE.md` priorizava US01, US02 e US03; a US01 foi postergada por “restrição técnica” sem detalhamento. A US02 é central ao MVP, mas a mudança da ordem não está plenamente justificada. Recomendação: documentar a decisão, alternativas, riscos e impactos da postergação da autenticação.
- **Vertical slice:** componentes React, `orientadorService.ts`, controller, service, repository e entidades JPA implementam o fluxo da US02, comprovando incremento real entre UI, API e banco. Recomendação: preservar o recorte vertical e corrigir as divergências funcionais encontradas.
- **Reprodutibilidade:** `SIGTCC/backend/gestao-tcc/gradlew` está com modo `100644`; `./gradlew test` e o backend no Compose falham com “Permission denied”. Isso impede execução externa pelos comandos documentados. Recomendação: versionar o wrapper como executável e testar o README em checkout limpo.
- **Funcionalidade:** após ajuste de permissão em cópia temporária, `POST`, `GET`, filtro, `PATCH`, `DELETE` e persistência MySQL funcionaram. Isso sustenta a existência do slice, mas exige contorno não documentado. Recomendação: automatizar um smoke test do fluxo no CI.
- **Regras de negócio:** `PATCH` com e-mail duplicado retorna 500 e expõe stack trace, SQL e detalhes internos. Há falha funcional e risco de segurança. Recomendação: validar unicidade na atualização e padronizar o tratamento de exceções.
- **Contrato:** o `POST` retorna somente `Location`; erros usam `Map`; limites documentados não estão todos no DTO; e inativos continuam na listagem da API. Contrato e implementação divergem em comportamentos relevantes. Recomendação: reconciliar contrato, DTOs, handler e consultas do repository.
- **Testes:** 30 testes backend e 22 frontend passaram; o frontend apresentou 93,33% de statements nos módulos carregados. Há boa verificação automatizada, apesar das lacunas. Recomendação: testar integração HTTP, serviço frontend, inativos, duplicidade no `PATCH` e limites do contrato.
- **Qualidade:** `npm run lint` falhou por `no-explicit-any`, enquanto `npm run build` passou. A tag contém problema estático não detectado como bloqueio. Recomendação: corrigir o lint e torná-lo check obrigatório.
- **CI:** `backend-ci.yml` e `frontend-ci.yml` usam caminhos `src/...`; o PR #76 teve backend, frontend e Prettier com falha. O marco não possui pipeline técnico estável. Recomendação: validar workflows após movimentações de diretório e antes do PR final.
- **Métricas:** M-01 apresenta cobertura sem relatório reproduzível; M-04 agrega dois PRs; e M-06 mantém `[50h]*` e `[~89%]*`. Isso reduz a rastreabilidade e a confiabilidade das coletas. Recomendação: registrar fonte, comando, período, cálculo e evidência de cada valor.
- **Fechamento:** `docs/entregas/sprint-1.md` afirma “21 concluídas”, mas marca #40 a #43 pendentes; no marco, apenas #42 permanecia aberta. O documento não representa corretamente o estado final. Recomendação: revisar o fechamento imediatamente antes de criar a tag.
- **Contribuições:** o relatório diz que Marcus não teve PR, mas ele é autor do PR #71. Há inconsistência no registro individual. Recomendação: conferir autores, PRs, reviews e issues antes de publicar o relatório.
- **Board:** o Project #31 é referenciado, mas a API recusou consulta por falta de `read:project`. O estado do board não pôde ser confirmado. Recomendação: manter evidência exportável ou acesso verificável ao board no marco.
- **Proteção:** o ruleset `Merges` exige PR, uma aprovação e `Checar Arquivos Essenciais`, sem bypass, mas não exige backend, frontend ou Prettier. A proteção existe, mas é insuficiente para garantir qualidade técnica. Recomendação: tornar build, testes e formatação checks obrigatórios.
- **Fluxo e prazo:** o PR #76 foi aprovado e integrado por merge commit às 00:03 de 18/09; a release foi publicada às 11:32. O fluxo estrutural foi atendido, mas com falhas técnicas e atraso. Recomendação: abrir o PR final com antecedência e só integrar com todos os checks verdes.
- **Documentação:** `docs/fluxo-de-trabalho.md` ainda se refere à `entrega-4`; `docs/ci.md` cita workflow de links ausente e bloqueio que não corresponde à configuração. A documentação operacional não reflete o marco. Recomendação: atualizar os documentos afetados na mesma sprint e compará-los com a configuração real.
- **Uso de IA:** Gemini foi declarado para testes, Docker, JPA, CI, cliente HTTP, contrato e backend; os artefatos existem, mas CI e contrato ainda continham falhas. Há compatibilidade material, com validação e rastreabilidade insuficientes. Recomendação: registrar data, artefato exato, trecho aproveitado, responsável e validação específica.

## Análise das Contribuições Individuais

- **Damares do Socorro Gonçalves Gaia:** realizou oito commits não relacionados a merges; foi autora dos PRs #46, #55 e #57; produziu o contrato de dados da US02; implementou consulta por ID e atualização parcial, incluindo testes; atualizou a métrica M-06; e participou da revisão do PR #58. A atuação é coerente com arquitetura e backend. A M-06 manteve valores provisórios sem fonte reproduzível e houve baixa participação em reviews.
- **Eduardo Cardoso Oliveira:** realizou vinte e cinco commits e seis PRs; implementou cadastro, listagem e o componente de linhas de pesquisa; conduziu planejamento, organização das issues, métricas e fechamento documental; e fez doze aprovações e duas solicitações de mudança em PRs. A atuação é ampla e coerente com Scrum Master, requisitos e desenvolvimento full stack. Eduardo integrou o PR final após o prazo e com checks técnicos falhando, e o relatório de fechamento ficou inconsistente.
- **Marcus Jhuan Epifanio Lima:** foi autor do PR #71 e do commit que reúne as telas das issues #64, #65 e #66; implementou as interfaces de cadastro, edição e catálogo de orientadores. A contribuição é tecnicamente relevante e coerente com UX/UI e frontend. O trabalho ficou concentrado em um único commit grande, sem reviews e sem atualização rastreável da métrica M-05 atribuída ao papel; o relatório registra incorretamente que não houve PR.
- **Talles Souza da Cruz:** realizou vinte e quatro commits e seis PRs; fez o setup da aplicação, a estrutura inicial do backend e frontend, o endpoint de desativação e uma ampla suíte de testes; registrou onze aprovações e duas solicitações de mudança; aprovou o PR final; e publicou a release. A atuação é muito coerente com QA e inclui implementação técnica relevante. Talles aprovou o PR final com checks técnicos falhando e publicou a release após o prazo.
- **Willian Ferreira dos Santos:** realizou dezesseis commits e foi autor dos PRs #58 e #73; implementou Docker Compose, healthcheck, variáveis de ambiente, workflows e documentação de CI; e fez duas aprovações em PRs. A atuação é coerente com DevOps/Infra. Os workflows e a execução do backend estavam quebrados no snapshot final e exigiram correções após o marco.
- **Identidades e limitações de autoria:** Damares aparece como `damaresgaia`, com commits também registrados como `unknown` no mesmo e-mail; Eduardo como `EduCardoso22`, `Eduardo Cardoso` e `EduCardoso22`; Marcus como `MarcusJhuan`; Talles como `SouzaTalles`; e Willian como `williansantfer`, com e-mail pessoal e endereço `noreply` do GitHub. As notas consideram conteúdo, autoria, issues, PRs, reviews, documentos e coerência com os papéis, não apenas quantidade de commits.

## Notas Individuais

As notas abaixo avaliam a contribuição individual observável e são independentes da nota coletiva da entrega.

| Integrante | Nota individual |
|---|---:|
| Damares do Socorro Gonçalves Gaia | **9,0** |
| Eduardo Cardoso Oliveira | **9,2** |
| Marcus Jhuan Epifanio Lima | **8,0** |
| Talles Souza da Cruz | **9,2** |
| Willian Ferreira dos Santos | **8,0** |

- **Damares - 9,0:** contribuição forte em arquitetura, contrato, backend, testes e métrica, com baixa participação em reviews e valores provisórios em M-06.
- **Eduardo - 9,2:** atuação ampla em implementação, planejamento, organização, métricas, documentação e reviews; a integração ocorreu após o prazo, com checks falhando e fechamento inconsistente.
- **Marcus - 8,0:** entregou as interfaces centrais do slice, mas concentrou a contribuição em um commit grande, sem reviews e sem atualização rastreável da métrica atribuída.
- **Talles - 9,2:** contribuição ampla em estrutura, implementação, testes, reviews e release; aprovou o PR final com checks falhando e publicou a release após o prazo.
- **Willian - 8,0:** contribuição relevante em infraestrutura e CI, mas os artefatos sob sua responsabilidade estavam quebrados no snapshot final.

## Notas Compostas por Integrante

As notas compostas combinam 30% da nota coletiva da entrega e 70% da nota individual de contribuição:

`nota composta = (nota coletiva × 0,30) + (nota individual × 0,70)`

| Integrante | Nota coletiva | Nota individual | Cálculo | Nota composta |
|---|---:|---:|---:|---:|
| Damares do Socorro Gonçalves Gaia | 7,0 | 9,0 | `(7,0 × 0,30) + (9,0 × 0,70) = 8,40` | **8,4** |
| Eduardo Cardoso Oliveira | 7,0 | 9,2 | `(7,0 × 0,30) + (9,2 × 0,70) = 8,54` | **8,5** |
| Marcus Jhuan Epifanio Lima | 7,0 | 8,0 | `(7,0 × 0,30) + (8,0 × 0,70) = 7,70` | **7,7** |
| Talles Souza da Cruz | 7,0 | 9,2 | `(7,0 × 0,30) + (9,2 × 0,70) = 8,54` | **8,5** |
| Willian Ferreira dos Santos | 7,0 | 8,0 | `(7,0 × 0,30) + (8,0 × 0,70) = 7,70` | **7,7** |

As notas compostas da Entrega 5 são: Damares **8,4**; Eduardo **8,5**; Marcus **7,7**; Talles **8,5**; Willian **7,7**.

Todos os integrantes possuem contribuição rastreável. Eduardo e Talles apresentam a participação mais ampla em implementação, organização e revisão. Damares possui contribuição forte em arquitetura, contrato e backend. Marcus entregou as telas centrais do slice, mas concentrou o trabalho em um único PR e não participou de reviews. Willian produziu a infraestrutura e o CI, porém os artefatos sob sua responsabilidade estavam quebrados no marco final. Essas diferenças fundamentam as notas individuais sem apagar a nota técnica comum da equipe.

## Recomendações para a Equipe

- Testar aplicação, testes e instruções em checkout limpo antes de criar a tag.
- Versionar `gradlew` com permissão de execução e padronizar os comandos do Docker Compose.
- Tornar build, testes, lint e formatação checks obrigatórios no ruleset.
- Corrigir validações, formato de erros, listagem de inativos e tratamento do `PATCH` duplicado.
- Sincronizar contrato de dados e implementação.
- Registrar meta, capacidade, decisões, ajustes, retrospectiva e ações da sprint.
- Produzir métricas reproduzíveis, com fontes, comandos e cálculos.
- Conferir status das issues e contribuições antes do fechamento.
- Atualizar documentação de CI e fluxo sempre que houver mudança de diretório ou etapa.
- Detalhar cada uso de IA com data, ferramenta, artefato, aproveitamento e validação específica.

## Nota da Entrega

| Critério | Máximo | Nota |
|---|---:|---:|
| Planejamento inicial da Sprint 1 | 1,0 | 0,7 |
| Primeiro incremento funcional como vertical slice | 2,0 | 1,4 |
| Testes de unidade automatizados | 1,5 | 1,2 |
| Escopo, backlog, board, métricas e contribuições | 1,5 | 1,0 |
| Fluxo de trabalho | 2,0 | 1,5 |
| README, documentação, uso de IA e release | 2,0 | 1,2 |
| **Total** | **10,0** | **7,0** |

## Justificativa da Nota

A entrega demonstra trabalho técnico substancial: o vertical slice é real, coerente com o MVP e apoiado por 52 testes automatizados. O histórico também evidencia colaboração, aprovação e integração por merge commit. A nota não é maior porque o artefato marcado como `v0.1.0` não é reproduzível diretamente pelas instruções fornecidas, o pipeline técnico do PR final falhou, o contrato possui divergências observáveis, as métricas têm rastreabilidade parcial e a documentação de fechamento não representa integralmente o estado entregue. O atraso foi registrado, sem aplicação de penalidade autônoma além de seus efeitos nos critérios de fluxo e release.

## Observações sobre Uso de IA

O `USO-IA.md` declara Gemini na Sprint 1 para testes, Docker e Compose, modelagem JPA, workflows, correção de lint/build, cliente `apiFetch`, contrato de dados, backend, Git e validação da modelagem. Os testes, Dockerfiles, arquivos Compose, entidades JPA, workflows, `apiFetch`, contrato e código backend existem. Assim, há **compatibilidade material majoritária** entre as áreas declaradas e os artefatos entregues.

Há, contudo, divergências e limitações de rastreabilidade. A validação é genérica e não identifica datas, responsáveis, trechos aproveitados, PRs ou resultados específicos. O CI declarado como resolvido estava quebrado em `v0.1.0`; o teste frontend ainda apresentava erro de lint; o contrato apoiado por IA divergia da implementação; e não foi localizado o diagrama UML mencionado. O GitHub Copilot foi acionado como revisor no PR #71, mas informou impossibilidade de revisar por limite de quota. Como não produziu revisão substancial nem artefato incorporado, isso não constitui evidência forte de conteúdo gerado por IA sem declaração, embora a ferramenta acionada devesse ter sido registrada por transparência.

**Conclusão explícita:** o conteúdo de `USO-IA.md` é **majoritariamente compatível** com os artefatos da Entrega 5, mas há divergência parcial quanto à alegada resolução/validação de CI, lint, contrato e diagrama UML. A declaração atende à transparência em nível geral, porém não oferece rastreabilidade e validação específicas suficientes. Não foram encontrados indícios fortes de artefatos produzidos por IA sem declaração correspondente.
