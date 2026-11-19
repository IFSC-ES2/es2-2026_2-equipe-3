# Avaliação da Entrega 8 - Sprint 4

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: SIGTCC — Sistema de Gestão Centralizada de TCC
- Entrega: 8 - Sprint 4
- Repositório confirmado pela configuração: `IFSC-ES2/es2-2026_2-equipe-3`
- Pull request avaliado: [#252](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/252), `entrega-8` → `main`.
- Versão avaliada: `7df1b25`, merge do PR #252 em 11/11/2026 às 23h44 (UTC−3).
- Prazo: 11/11/2026.
- Release esperada: `v0.4.0`

## Documentos Consultados

- `README.md`, `USO-IA.md` e `.env.example`.
- `.github/PULL_REQUEST_TEMPLATE.md` e workflows em `.github/workflows/`.
- `docs/entregas/sprint-4.md`, `docs/DEPLOY.md`, `docs/staging.md` e `docs/ci.md`.
- `docs/ARQUITETURA.md`, `docs/PADROES-DE-PROJETO.md`, `docs/adrs/ADR-0013.md`, `ADR-0014.md`, contratos US06/US07 e diagramas.
- `docs/METRICAS.md`, fichas de métricas e `docs/riscos.md`.
- `docs/CONTRIBUICOES-S4.md`.
- `SIGTCC/docker-compose.yml`, `SIGTCC/docker-compose.prod.yml`, `SIGTCC/scripts/smoke-test.sh`, código, migrations, testes e configurações do backend/frontend/Keycloak.
- Issues, milestone, PRs da Sprint 4, Project 31, checks, histórico Git e [release `v0.4.0`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v0.4.0).

## 1. Descrição — Resumo da Entrega

O MVP declarado é uma aplicação web para gestão do ciclo de TCC, com autenticação e perfis, catálogo/match de orientadores, solicitações de orientação, calendário de prazos, submissão de entregas e pareceres. A Sprint 4 implementa US06 — submissão por link HTTPS, atraso e reenvio — e US07 — consulta, avaliação e parecer do orientador — além de correções de autorização, renovação de token, concorrência, staging e documentação. US08, agendamento de bancas, foi conscientemente postergada (`docs/entregas/sprint-4.md:272–292`).

Há evidência técnica forte de implementação, testes de integração, Docker Compose de produção, script de smoke test, workflow de build/publicação de imagens no GHCR, documentação de deploy e release `v0.4.0`.

O staging, entretanto, depende de máquina da equipe e de três Cloudflare Quick Tunnels efêmeros; não há URL persistente nem deployment registrado na API do GitHub. O merge final e a release ocorreram dentro do prazo de 11/11.

## 2. Itens Obrigatórios da Entrega

### 2.1 Planejamento e incremento funcional da Sprint 4

- **Atendido:** `docs/entregas/sprint-4.md` registra inspeção da Sprint 3, meta, riscos, issues, responsabilidades, andamento, retrospectiva e escopo postergado (US08).
- **Atendido:** US06 e US07 possuem entidades, migrations, endpoints, telas, contratos e testes. A documentação descreve submissão HTTPS, atraso, reenvio após ajustes, pareceres e autorização por vínculo. Arquitetura e padrões foram atualizados para US06/US07, links externos, refresh de token e concorrência.
- **Parcialmente atendido:** divergência pontual de isolamento por turma na consulta de entregas (A1) e riscos reconhecidos, mas não resolvidos, da submissão por link externo (A6).

#### A1 — Contrato de consulta de entregas não filtra explicitamente a turma vigente

- **Categoria:** implementação e coerência funcional.
- **Relevância:** média.
- **Evidência:** o README promete entregas do aluno na turma vigente (`README.md:225–228`), mas a análise do serviço indica consulta por aluno sem filtro explícito da turma vigente em `EntregaService.java:132–141`.
- **Impacto:** em contexto com mais de uma turma, pode haver exposição de entregas fora da turma atual, contrariando o isolamento descrito.
- **Recomendação:** aplicar o filtro de `Turma_TCC` vigente na consulta e criar teste de isolamento entre turmas.

### 2.2 Ambiente de staging ou equivalente acessível

- **Parcialmente atendido:** há roteiro de ambiente externo via Cloudflare Quick Tunnels, credenciais de teste e documentação de validação, mas as URLs são geradas a cada execução, dependem de máquina da equipe e não são acessíveis de forma persistente ao avaliador. A API de deployments retornou lista vazia.
- **Não atendido:** staging persistentemente acessível não demonstrado; não há URL fixa, evidência independente no commit ou registro de deployment.

#### A2 — Staging externo é efêmero e não verificável independentemente

- **Categoria:** aderência aos requisitos e implantação.
- **Relevância:** alta.
- **Evidência:** `docs/DEPLOY.md:110–173` exige três comandos `cloudflared`, gera URLs `trycloudflare.com` novas a cada execução e requer editar `.env` antes do build. `docs/staging.md` descreve a máquina da equipe e túneis temporários. A API de deployments retornou `[]`.
- **Impacto:** o ambiente não está acessível de modo persistente para avaliação e não há evidência operacional independente de que o staging estava disponível no commit avaliado.
- **Recomendação:** manter uma URL de staging estável ou versionar evidências datadas de execução, logs, SHA implantado e smoke test; separar claramente staging remoto temporário de deploy reproduzível local.

### 2.3 Deploy automatizado ou reprodutível

- **Atendido:** `.github/workflows/docker-build.yml` compila e publica imagens com tags `latest` e SHA no GHCR, depois dos jobs de teste; há Compose de produção e script de smoke test.
- **Atendido:** o processo é reprodutível e documentado em `docs/DEPLOY.md`: túneis, variáveis, build, subida do Compose e smoke test.
- **Parcialmente atendido:** a reprodução exige passos manuais repetidos a cada execução (iniciar três túneis, editar `.env` com URLs novas e reconstruir), mais sujeitos a erro (A3).
- **Observação:** CD remoto não é exigido nesta entrega; o enunciado aceita deploy automatizado **ou** reprodutível.

#### A3 — Deploy reprodutível depende de passos manuais a cada execução

- **Categoria:** deploy e reprodutibilidade.
- **Relevância:** baixa/média.
- **Evidência:** `.github/workflows/docker-build.yml` publica imagens no GHCR após testes; `docs/DEPLOY.md:116–214` informa que o staging não usa essas imagens diretamente, pois o Compose faz `build:` local para embutir URLs efêmeras. A implantação exige túneis, edição de ambiente e `docker compose ... up -d --build`.
- **Impacto:** o deploy é reprodutível, como exige o enunciado, mas a sequência manual de túneis, edição de ambiente e build aumenta o risco de erro e não aproveita as imagens publicadas no GHCR.
- **Recomendação:** reunir túneis, ambiente, build e smoke test em um único script; com URL estável, usar as imagens do GHCR no Compose de staging.

### 2.4 Manutenção e atualização da integração contínua

- **Atendido:** há testes unitários de backend/frontend e de integração para submissão, parecer, autorização, estados e concorrência. Os checks de backend/frontend são exigidos pelo ruleset da `entrega-8`, e o workflow de Docker depende de testes antes da publicação das imagens.
- **Parcialmente atendido:** o build Docker passou no PR, mas não aparece como check obrigatório no ruleset consultado. A política documental em `docs/ci.md` é mais ampla que a regra efetiva.

### 2.5 Documentação de deploy

- **Atendido:** `docs/DEPLOY.md` e `docs/staging.md` cobrem variáveis, execução local, staging, Cloudflare, smoke test, credenciais de teste e rollback.
- **Parcialmente atendido:** a documentação não identifica um único ambiente oficial; túneis efêmeros e domínio fixo aparecem em fontes diferentes (A4).

#### A4 — Documentação do risco de staging é inconsistente

- **Categoria:** documentação e riscos.
- **Relevância:** média.
- **Evidência:** `docs/staging.md` e `DEPLOY.md` descrevem Quick Tunnels efêmeros; `docs/riscos.md:449` menciona domínio fixo `staging.sigtcc.online`. A release e o guia não apresentam uma URL operacional estável para reconciliar as duas descrições.
- **Impacto:** não é possível determinar, apenas pelos artefatos avaliados, qual ambiente corresponde efetivamente à versão entregue.
- **Recomendação:** declarar um único ambiente oficial, sua URL, provedor, data/SHA implantado e procedimento de validação; manter o túnel efêmero apenas como alternativa de teste.

### 2.6 Atualização das métricas e riscos do projeto

- **Atendido:** métricas e riscos foram atualizados e há memória de cálculo.
- **Parcialmente atendido:** algumas conclusões dependem de relatos de execução no staging que não têm URL persistente, logs versionados ou deployment registrado; o risco de staging é descrito de forma inconsistente (A4). O registro de IA de Marcus é menos rastreável (A5).

#### A5 — Uso de IA de Marcus não é rastreável

- **Categoria:** uso de IA e autoria.
- **Relevância:** média.
- **Evidência:** os registros dos demais integrantes detalham ferramenta, prompts, artefatos, adaptação e validação; o bloco de Marcus (`USO-IA.md:463–505`, commit `9735bdd`) registra “Data: Durante a Sprint 4” nos três usos, artefatos genéricos (“Funcionalidades de submissão do aluno e avaliação do orientador”, “Componentes, serviços e integração com a API”) e validações genéricas, sem arquivos, commits/PRs ou resultados identificáveis.
- **Impacto:** não é possível identificar em quais arquivos e momentos a IA atuou nem o que foi aproveitado, embora os artefatos frontend e o PR #242 existam e sejam compatíveis com a contribuição declarada. O protocolo de uso de IA prevê impacto na avaliação por falta de rastreabilidade; a penalização incide na contribuição individual de Marcus e, em menor grau, no critério documental coletivo.
- **Recomendação:** vincular cada uso de IA de Marcus a arquivos, commits/PRs, sugestões aproveitadas, descarte e resultado dos testes.

### 2.7 Qualidade, segurança e configuração do ambiente

- **Atendido:** `.env.example` versionado; correções de autorização e isolamento de dados, renovação de token, `@Version` para concorrência de vagas e `PESSIMISTIC_WRITE` para pareceres, confrontadas com código, migrations e testes citados.
- **Parcialmente atendido:** a correspondência entre ambiente implantado e versão entregue não é verificável (A2); a submissão por link externo preserva riscos reconhecidos (A6).

#### A6 — Links externos preservam riscos reconhecidos, mas não resolvidos

- **Categoria:** requisitos, segurança e riscos.
- **Relevância:** média.
- **Evidência:** `docs/entregas/sprint-4.md:64–67` e `docs/DEPLOY.md:209–215` registram que o arquivo não é armazenado, que a URL pode exigir permissões e que reenvio substitui o link anterior. `docs/riscos.md` mantém riscos de conteúdo externo privado/modificado.
- **Impacto:** o sistema registra o link e o parecer, mas não garante disponibilidade, imutabilidade ou histórico do conteúdo avaliado.
- **Recomendação:** manter links somente leitura/versionados quando possível, preservar histórico de URLs e registrar a decisão de aceite com identificador de versão do documento.

### 2.8 Fluxo de trabalho evidenciado no repositório

- **Atendido:** trabalho em branches de feature integrado à `entrega-8`, PR final #252 para `main`, aprovações de Willian e Talles, checks concluídos e merge commit. A API confirmou rulesets ativos; o endpoint clássico 404 não significa ausência de proteção efetiva.
- **Atendido:** merge às 23h44 e release às 23h46 de 11/11 (UTC−3), dentro do prazo.
- **Observação:** concentração reconhecida das atividades no fechamento, com o PR final integrado poucos minutos após sua abertura.

#### A7 — Validação e integração concentradas no fechamento

- **Categoria:** processo GitHub e colaboração.
- **Relevância:** baixa/média.
- **Evidência:** o prazo coincide com o planejamento interno (03–11/11). O PR #252 foi aberto às 23h41 e integrado às 23h44 de 11/11, e `v0.4.0` foi publicada às 23h46 (UTC−3). O próprio planejamento registra concentração das atividades entre 09 e 11/11 (`docs/entregas/sprint-4.md:111–114,213–224`).
- **Impacto:** o marco foi cumprido, mas a concentração reduz a oportunidade de revisão e de validação do staging antes da integração final; a revisão do PR final se apoia nas revisões dos PRs intermediários.
- **Recomendação:** antecipar infraestrutura e QA, registrar impedimentos e integrar incrementos ao longo da sprint.

### 2.9 Release/tag do marco

- **Atendido:** a API do GitHub confirmou `v0.4.0`, publicada às 23h46 de 11/11/2026 (UTC−3), dentro do prazo, e apontando para `7df1b25`, apesar de inspeções locais não terem trazido a tag sem atualização do clone.
- **Parcialmente atendido:** a release não apresenta URL de staging estável (A4).

### 2.10 Registro de contribuição individual

- **Atendido:** `docs/CONTRIBUICOES-S4.md` registra atividades por integrante, com correspondência em commits, PRs e revisões.
- **Observação:** a própria retrospectiva registra concentração da validação final nos últimos dias.

#### Análise das Contribuições Individuais

Notas de contribuição são distintas da nota coletiva. Foram considerados conteúdo, relevância, periodicidade, responsabilidade, revisão e integração — não apenas contagem de commits.

- **Damares do Socorro Gonçalves Gaia:** modelou migrations V6/V7, contratos, arquitetura, ADRs, refresh de token e concorrência; participou dos PRs #233, #237 e #245 e realizou revisões técnicas com `CHANGES_REQUESTED`. A atividade foi distribuída e alinhada ao papel, com forte integração entre backend, persistência e documentação.

- **Eduardo Cardoso Oliveira:** conduziu pipeline Docker/GHCR, `.env.example`, Compose de staging, Cloudflare, DEPLOY, métricas e riscos; participou de diversos reviews e foi autor da consolidação final. É contribuição central e relevante para o requisito operacional, embora o deploy ainda dependa de passos manuais e a maior intensidade tenha ocorrido perto do encerramento.

- **Marcus Jhuan Epifanio Lima:** implementou as telas e serviços frontend de submissão e avaliação no PR #242, com testes e rodadas de revisão. A contribuição funcional é substancial e coerente com o papel, mas foi concentrada em 03, 09 e 10/11, com menor periodicidade rastreável. O registro de uso de IA não é rastreável (A5), o que reduz sua nota individual.

- **Talles Souza da Cruz:** implementou APIs, serviços, autorização, pareceres, concorrência e testes de US06/US07; participou de vários PRs e revisões, incluindo o PR final. A contribuição foi ampla, incremental e diretamente integrada ao domínio.

- **Willian Ferreira dos Santos:** conduziu planejamento, governança, QA, testes de integração, riscos e revisão final; os PRs #246 e #247 cobrem os fluxos novos e houve aprovação do PR #252. A própria retrospectiva registra concentração da validação final nos últimos dias, reduzindo a nota máxima.

## 3. Critérios de Avaliação (10 pts)

### Nota da Entrega

| Critério                                                         |   Máximo |    Nota |
| ---------------------------------------------------------------- | -------: | ------: |
| Planejamento e incremento da Sprint 4                            |      1,0 |     0,9 |
| Staging ou equivalente acessível, validável e associado à versão |      2,0 |     1,1 |
| Deploy automatizado ou reprodutível e `docs/DEPLOY.md`           |      1,5 |     1,3 |
| CI mantido e atualizado, com testes e checks nos PRs             |      1,5 |     1,3 |
| Métricas, riscos, documentação, README e IA                      |      1,0 |     0,7 |
| Configuração e segurança                                         |      1,0 |     0,8 |
| Fluxo de trabalho, checks, merge e release `v0.4.0`              |      2,0 |     2,0 |
| **Total**                                                        | **10,0** | **8,1** |

### Justificativa da Nota

**Nota coletiva: 8,1/10,0.** A entrega apresenta um incremento funcional amplo e tecnicamente consistente: US06 e US07 implementadas, autorizações corrigidas, concorrência tratada, testes de integração, documentação de arquitetura/deploy, workflow de build e publicação de imagens, PRs revisados por ruleset e release `v0.4.0` confirmada pela API. O trabalho de backend, frontend, QA e infraestrutura é rastreável e relevante.

A nota é reduzida porque o staging não é persistentemente acessível (A2), o deploy depende de passos manuais a cada execução, sem aproveitar as imagens publicadas (A3), e há inconsistência entre domínio fixo e túneis efêmeros (A4). Também houve concentração reconhecida no fechamento (A7), uma divergência pontual de isolamento por turma (A1), riscos de links externos não resolvidos (A6) e registro de IA não rastreável de um integrante (A5), penalizado conforme o protocolo de uso de IA.

### Notas Individuais

Escala 0–10, considerando conteúdo, relevância, aderência às responsabilidades, periodicidade/oportunidade, integração, revisão, comunicação e apoio aos colegas; não são proporcionais à contagem de commits.

| Integrante                        | Nota individual | Justificativa resumida                                                                                                                                     |
| --------------------------------- | --------------: | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Damares do Socorro Gonçalves Gaia |         **8,8** | Migrations, contratos, arquitetura, ADRs, refresh de token e concorrência, com revisões técnicas; atividade distribuída e alinhada ao papel.               |
| Eduardo Cardoso Oliveira          |         **9,0** | Pipeline Docker/GHCR, staging, DEPLOY, métricas e riscos, com reviews e consolidação final; deploy ainda com passos manuais e intensidade no encerramento. |
| Marcus Jhuan Epifanio Lima        |         **7,3** | Telas e serviços frontend de submissão e avaliação no PR #242, com testes; menor periodicidade e uso de IA não rastreável.                                 |
| Talles Souza da Cruz              |         **9,0** | APIs, autorização, pareceres, concorrência e testes de US06/US07; contribuição ampla, incremental e integrada.                                             |
| Willian Ferreira dos Santos       |         **8,6** | Planejamento, QA, testes de integração, riscos e revisão final; validação concentrada nos últimos dias.                                                    |

### Notas Compostas por Integrante

A nota composta da Entrega 8 combina 30% da nota coletiva e 70% da contribuição individual, com peso 10 para a entrega, em continuidade ao registro das Entregas 5–7. A média parcial está registrada em `NOTAS.md`.

`nota composta = (nota coletiva × 0,30) + (nota individual × 0,70)`

| Integrante                        | Coletiva | Individual |                              Cálculo | Nota composta |
| --------------------------------- | -------: | ---------: | -----------------------------------: | ------------: |
| Damares do Socorro Gonçalves Gaia |      8,1 |        8,8 | `(8,1 × 0,30) + (8,8 × 0,70) = 8,59` |       **8,6** |
| Eduardo Cardoso Oliveira          |      8,1 |        9,0 | `(8,1 × 0,30) + (9,0 × 0,70) = 8,73` |       **8,7** |
| Marcus Jhuan Epifanio Lima        |      8,1 |        7,3 | `(8,1 × 0,30) + (7,3 × 0,70) = 7,54` |       **7,5** |
| Talles Souza da Cruz              |      8,1 |        9,0 | `(8,1 × 0,30) + (9,0 × 0,70) = 8,73` |       **8,7** |
| Willian Ferreira dos Santos       |      8,1 |        8,6 | `(8,1 × 0,30) + (8,6 × 0,70) = 8,45` |       **8,5** |

## 4. Observações

### Observações sobre Uso de IA

O uso declarado não foi penalizado por si só; a falta de rastreabilidade, sim. O registro de Marcus está descrito em A5.

**Conclusão explícita:** `USO-IA.md` é **majoritariamente compatível** com os artefatos entregues. Os registros detalhados de Damares, Eduardo, Talles e Willian correspondem a código, migrations, workflows, documentação, testes e PRs existentes, com ferramentas, prompts, adaptação e validação descritos. O registro de Marcus é compatível em termos gerais com o frontend do PR #242, mas é menos específico e não vincula usos a arquivos/commits/PRs e resultados concretos. As validações de staging declaradas são plausíveis e há script/documentação correspondentes, mas não puderam ser confirmadas independentemente por URL persistente, logs versionados ou deployment na API. Não foi encontrada evidência forte de uso de IA não declarado, plágio ou falsificação.

### Recomendações para a Equipe

- Tornar o staging persistente e acessível por URL estável, ou registrar de modo verificável cada execução temporária.
- Reunir o deploy do staging em um único script e, com URL estável, consumir as imagens do GHCR; automatizar a promoção quando houver infraestrutura disponível.
- Reconciliar `staging.sigtcc.online` com Quick Tunnels em DEPLOY, riscos, README e release.
- Filtrar consultas de entregas pela turma vigente e testar isolamento entre turmas.
- Preservar histórico/versionamento de links externos ou explicitar a limitação no contrato do produto.
- Tornar o build Docker um check obrigatório se essa for a política declarada, e manter `docs/ci.md` coerente com os rulesets.
- Registrar o uso de IA de Marcus com data, arquivos, commits/PRs, aproveitamento e validação, no mesmo nível dos demais integrantes.
- Antecipar QA, staging e release para evitar concentração no fechamento.
