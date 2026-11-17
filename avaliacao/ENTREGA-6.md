# Avaliação da Entrega 6 - Sprint 2

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC — SIGTCC
- Entrega: 6 - Sprint 2
- Repositório confirmado pela configuração: `IFSC-ES2/es2-2026_2-equipe-3`
- MVP declarado: autenticação/perfis de alunos, professores e coordenadores; catálogo de orientadores, vagas e vinculação; documentos, prazos e pareceres.
- Escopo da Sprint 2: US04 — solicitação e aceite/recusa de orientação — e fatia operacional da US03 no catálogo.
- Pull request avaliado: [#125](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/125), de `entrega-6` para `main`.
- Versão considerada: `b5c5a3f62406c15f904631707fc7ee5dfeab341b`, tag `v0.2.0`, merge registrado pela API em 01/10/2026 às 21:54:23 (UTC−3).
- Comparação com a Sprint 1: `v0.1.1`, commit `18cd7d5`.
- Release esperada e encontrada: `v0.2.0`, publicada em 01/10/2026 às 21:59:26 (UTC−3).
- Prazo normativo: 01/10/2026.

## Documentos Consultados

Somente documentos e artefatos do repositório da equipe:

- `README.md` e `USO-IA.md`.
- `.github/PULL_REQUEST_TEMPLATE.md` e `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/workflows/backend-ci.yml`, `frontend-ci.yml`, `verificacao-docs.yml` e `verificar-formatacao.yml`.
- `docs/inception.md` e `docs/dod.md`.
- `docs/BASELINE.md`, `docs/ESTIMATIVAS.md`, `docs/METRICAS.md` e `docs/metricas/M-01.md` a `M-06.md`.
- `docs/riscos.md`, `docs/qualidade.md`, `docs/fluxo-de-trabalho.md` e `docs/ci.md`.
- `docs/contrato-dados-us02.md` e `docs/contrato-dados-us04.md`.
- `docs/entregas/sprint-1.md`, `docs/entregas/sprint-2.md` e `docs/PADROES-DE-PROJETO.md`.
- Todos os ADRs, de `docs/adrs/ADR-0001.md` a `ADR-0009.md`.
- Código, testes, migrações, arquivos de build, Dockerfiles e configurações Compose em `SIGTCC/`.
- Issues US04/#11 e tarefas #79, #81–#100; PRs intermediários e final #125, reviews, checks, histórico Git e regras de proteção.
- [Project 31 — equipe 3](https://github.com/orgs/IFSC-ES2/projects/31), milestone `entrega-6` e [release `v0.2.0`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v0.2.0).

## 1. Descrição — Resumo da Entrega

O MVP é uma aplicação web OO para centralizar o fluxo acadêmico de TCC. Bancas, acervo público, aplicativo móvel, assinatura avançada e integrações institucionais ficam fora do recorte atual. A Sprint 2 selecionou US04 (5 SP) e o botão operacional da US03, mantendo autenticação/US01 e documentos para ciclos futuros. Esses adiamentos são explícitos no README, planejamento e ADR-0008; não são tratados como funcionalidades prometidas e esquecidas nesta sprint.

O incremento é concreto: o aluno solicita orientação pelo catálogo e acompanha o status; o orientador lista solicitações, aceita ou recusa com justificativa; o backend persiste alunos/solicitações e publica evento que atualiza vagas. O caminho atravessa React/TypeScript, Spring Boot/Java 21, JPA e MySQL, com Flyway. Strategy e Observer têm realizações úteis. Não se recontam catálogo, CRUD de orientadores e filtragem já entregues na Sprint 1.

Há testes relevantes, revisão efetiva, execução remota de build/testes/lint/validação de Compose e integração por merge commit. Tag e release corretas foram publicadas no prazo: a data UTC de 02/10 corresponde ainda a 01/10 em UTC−3. As principais lacunas são consistência concorrente de vagas/estado, validações inferiores ao contrato, teste integrado central anunciado mas ausente, riscos sem fechamento e divergências entre arquitetura, métricas, CI, release e código.

## 2. Itens Obrigatórios da Entrega

Os itens abaixo são subitens comprovados, não pontuação integral automática nos critérios compostos:

### 2.1 Planejamento da Sprint 2

- **Atendido:** Planejamento com meta, US04 de 5 SP, recorte da US03, velocidade anterior, capacidade de 28h semanais, riscos e adiamentos (`sprint-2.md:13–45`).
- **Parcialmente atendido:** Planejamento forte, mas ampliação do recorte estimado de 13 para 18 SP sem revisão formal correspondente da baseline.

#### A1 — Planejamento e baseline

- **Categoria:** planejamento/MVP.
- **Evidência:** `sprint-2.md:13–45,138–147` relaciona objetivo, capacidade, velocidade, riscos, entrega e replanejamento. M-05 amplia o total estimado de 13 para 18 SP, incluindo US04: 55,6% significa 10/18 SP do recorte estimado, não todo o MVP com documentos/prazos.
- **Impacto:** forte planejamento, comparabilidade global parcial.
- **Recomendação:** versionar revisão de baseline e explicitar o denominador ao comunicar progresso.

### 2.2 Incremento funcional do MVP

- **Atendido:** Incremento novo entre formulário, acompanhamento, painel, API, domínio e persistência, associado a issues/PRs com critérios de aceitação.
- **Atendido:** Criação/reaproveitamento de aluno por e-mail, listagem, consulta individual, aceite e recusa com justificativa implementados.
- **Parcialmente atendido:** Fluxo sequencial funcional, sem proteção suficiente para vagas/decisões concorrentes e sem cumprimento integral das validações e erros contratuais.
- **Não atendido:** Teste concorrente previsto como bloqueante e mecanismo equivalente a locking/versionamento/operação condicional para vagas/estado.
- **Não atendido:** Validação backend integral dos limites de campos e padronização completa dos erros do contrato US04.

Autenticação e documentos continuam no MVP global, mas foram explicitamente replanejados. Endpoints públicos são limitação séria de exposição, não omissão oculta de escopo da Sprint 2.

#### A2 — Incremento real, com exposição pública declarada

- **Categoria:** implementação/escopo.
- **Evidência:** `J/service/SolicitacaoService.java:39–132`, controller, entidades, migrações e telas do aluno/professor conectam o fluxo. README declara ausência de autenticação; o painel usa ID de rota/localStorage ou padrão 1. Sem identidade verificada, qualquer cliente pode consultar dados e responder solicitações por ID.
- **Impacto:** crédito pelo fluxo e limitação a demonstração controlada, sem exigir retroativamente US01 nesta sprint.
- **Recomendação:** autenticação/autorização antes de exposição real.

#### A3 — Transação não elimina concorrência

- **Categoria:** confiabilidade.
- **Evidência:** `SolicitacaoService.java:97–131` lê estado/vagas e publica evento sem exclusão concorrente; `AtualizadorVagasObserver.java:18–22` decrementa em memória, sem `@Version`, lock ou atualização condicional equivalente nas entidades/repositórios. Dois aceites podem ler a última vaga e ambos persistir zero, aceitando duas solicitações para uma vaga mesmo sem saldo negativo. Aceite/recusa da mesma solicitação também podem superar a verificação inicial de `PENDENTE`. A duplicidade de pendências é consultada antes de inserir, sem proteção correspondente no banco.
- **Impacto:** risco alto no núcleo, fundamentado estaticamente e não reproduzido nesta análise. `@Transactional` agrupa alterações, não garante exclusão mútua.
- **Recomendação:** proteger decisões/recursos concorrentes e testar última vaga, duplicidade e mudanças conflitantes de estado.

#### A4 — API valida menos que o contrato

- **Categoria:** contrato/comportamento.
- **Evidência:** `contrato-dados-us04.md:53–61` exige ID positivo, nome 3–100, e-mail até 100, curso 2–100 e mensagem 10–1000. `J/dto/SolicitacaoRequestDTO.java:13–38` não impõe todos esses limites; mensagem não branca de um caractere satisfaz o DTO. Frontend valida adicionalmente (`SolicitacaoForm/index.tsx:38–61`), mas chamadas diretas contornam a UI. Handler de enum/corpo ilegível retorna `detalhes` como string (`GlobalExceptionHandler.java:94–105`), não array por campo; textos de duplicidade/estado também divergem.
- **Impacto:** contrato parcial e possibilidade de erro de persistência em vez de 400 por campo.
- **Recomendação:** harmonizar DTO, regras, handler e testes contratuais, priorizando divergências estruturais sobre diferenças meramente textuais.

#### A5 — Capacidades da interface precisam ser delimitadas

- **Categoria:** integração.
- **Evidência:** `OrientadorService.java:62–73,163–170` pode listar inativos com vagas; `F/components/OrientadorCard/index.tsx:35–43` verifica só vagas, embora o cenário 3 de #11 exija bloqueio também para inativo. Backend rejeita corretamente: não há bypass, mas o aluno entra em um fluxo que terminará em rejeição. Painel consulta apenas pendentes; filtro por status existe na API, não em seletor na tela. Acompanhamento é sob demanda, não tempo real.
- **Impacto:** limitações localizadas, sem negar o fluxo principal.
- **Recomendação:** alinhar elegibilidade da UI e distinguir capacidade da API da oferecida na tela.

### 2.3 Testes automatizados relacionados às funcionalidades implementadas

- **Atendido:** Testes versionados de serviços, controller, validações, Observer, persistência de aluno e componentes frontend, com comandos locais documentados.
- **Parcialmente atendido:** Suíte útil e executável, com integração JPA simples, mas sem integração HTTP–serviço–persistência–Observer e concorrência anunciadas.
- **Não atendido:** Teste automatizado completo HTTP–serviço–banco–Observer declarado no fechamento/release.

#### A6 — Testes reais, integração central superdeclarada

- **Categoria:** testes.
- **Evidência:** `sprint-2.md:106–107`, #89/#118 e release anunciam MockMvc/H2 do ciclo completo. Contudo, `B/src/test/java/.../controller/SolicitacaoControllerTest.java:31–50` usa serviço mockado; `SolicitacaoServiceTest.java:30–54,152–168` simula repositories/publicador e verifica publicação, não entrega do evento com decremento persistido. `AlunoRepositoryTest.java:16–48` é integração JPA real, restrita a aluno. Testes H2 usam `create-drop` e desabilitam Flyway. Não há teste concorrente correspondente ao declarado.
- **Impacto:** crédito unitário e de integração simples, sem atribuir comprovação do ciclo completo nem das migrações MySQL.
- **Recomendação:** testar contexto Spring, HTTP, banco, Observer e concorrência em conjunto, além das migrações.

### 2.4 Integração contínua em uso

- **Não atendido:** Obrigatoriedade de backend, frontend e formatação também na `main`, conforme regras consultadas.

Achado relacionado: A11 (item 2.5).

### 2.5 Fluxo de trabalho evidenciado no repositório

- **Atendido:** PRs de tarefa integrados em `entrega-6`, revisões por colegas, CI final verde e merge commit em `main`.
- **Parcialmente atendido:** Fluxo efetivamente seguido, mas `main` não obriga todos os checks técnicos nem apenas merge; aprovação intermediária anterior ao último push.

#### A11 — CI efetivo; governança com lacunas

- **Categoria:** fluxo/CI.
- **Evidência:** Os 24 PRs mesclados do recorte, incluindo #125, têm aprovação de colega. #80/#108/#115/#116 mostram pedidos técnicos e respostas com mudanças. No final, checks passaram antes das aprovações de Eduardo (21h53min58s) e Talles (21h54min14s) e do merge. [Backend run 36948182015](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/36948182015) executou Gradle build/test com sucesso; [frontend 36948182031](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/36948182031) executou instalação, lint, 43 testes/11 arquivos e build; [documentação 36948182055](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/36948182055) validou README e sintaxe dos dois Compose. Formatação passou. Em PRs intermediários existem passos ignorados por filtros; job verde não prova execução de tudo em cada alteração. #126 recebeu push após aprovação intermediária, mas o head completo foi depois aprovado em #125.
- **Proteção por API/CLI:** a API clássica retorna 404, mas regras aplicáveis/rulesets confirmam proteção. `Merges` (**20534413**), da `main`, exige PR, uma aprovação, invalidação após push, bloqueio de exclusão/force push e somente `Checar Arquivos Essenciais`; permite merge/squash/rebase. `Sprint 2` (**24054134**), de `entrega-6`, exige PR/aprovação, apenas merge e os quatro checks essenciais/backend/frontend/formatação, mas não invalida aprovação antiga. Não há bypass cadastrado nesses rulesets. A consulta é conclusiva: não é necessário perguntar ao professor se existe proteção. A configuração atual não substitui auditoria histórica completa. O método praticado foi merge commit, mas a `main` não impõe integralmente os gates técnicos/política descritos.
- **Impacto:** fluxo majoritariamente adequado, não CI nominal.
- **Recomendação:** revalidar aprovação após alteração e distinguir testes executados de passos ignorados.

### 2.6 Aplicação justificada de padrões OO

- **Atendido:** Dois mecanismos OO identificáveis, com problema, benefícios, consequências e ADR.
- **Parcialmente atendido:** Padrões reais, mas nomes, diagramas, contexto, ordenação e observadores documentados não correspondem integralmente ao código.
- **Não atendido:** Correspondência integral entre classes/diagramas dos padrões e código e entre garantias de CI documentadas e executadas.

#### A7 — Padrões presentes, documentação não consolidada

- **Categoria:** padrões OO.
- **Evidência:** `List<ValidacaoSolicitacaoOrientacao>` e implementações para ativo/vagas/duplicidade (`SolicitacaoService.java:37,54`) desacoplam regras; evento e `AtualizadorVagasObserver` são consumidores reais e síncronos. Executar cumulativamente as regras pede explicação como composição de validações, sem invalidar seu polimorfismo. ADR-0007 e documento de padrões descrevem pacote `strategy`, interface `ValidadorSolicitacaoStrategy`, `SolicitacaoContexto`, quarta estratégia e `@Order` ausentes/renomeados. `LogAuditoriaObserver` não existe; e-mail aparece prospectivamente, enquanto README/contrato reconhecem sua ausência.
- **Impacto:** dois padrões reconhecidos, com desconto pela distância entre design aprovado e realizado.
- **Recomendação:** atualizar nomes/diagramas/consequências e separar extensões futuras de consumidores implementados.

### 2.7 Atualização das métricas

- **Atendido:** Seis fichas de métricas atualizadas e contribuições dos cinco integrantes rastreáveis.
- **Parcialmente atendido:** Métricas/contribuições presentes; cobertura backend/horas não auditáveis nas fontes entregues, riscos ainda intermediários e board divergente.
- **Não atendido:** Mitigação por bloqueio pessimista com `EntityManager.refresh` indicada em M-02: não existe no código entregue.

#### A8 — Métricas com contradição e fontes insuficientes

- **Categoria:** acompanhamento/evidência.
- **Evidência:** As seis fichas têm valores/análise; M-01 reconhece frontend em 67,74%, abaixo da meta. Backend 91% não é reproduzível por JaCoCo/gate ou relatório no CI consultado. M-02:49 registra “Aplicação imediata de Bloqueio Pessimista com `EntityManager.refresh`”, ausente e contradito por `qualidade.md:186–190`, que declara mitigação pendente. M-04 mostra tempos por PR e desvios, mas termina no #119; M-06 registra 53h/56h, 94,6%, sem apontamentos individualizados recuperados.
- **Impacto:** análise útil, não plenamente auditável; M-02 não comprova correção, sem inferir falsificação intencional.
- **Recomendação:** registrar estado real da ação, relatórios/comandos/fontes e memória de horas/PRs finais.

### 2.8 Atualização dos riscos

- **Não atendido:** Fechamento final dos riscos em 01/10, prometido pelo registro intermediário.

#### A9 — Riscos sem fechamento

- **Categoria:** gestão.
- **Evidência:** `riscos.md:153–161,214–240` permanece revisão intermediária de 23/09, exige fechamento em 01/10 e mantém ações “ainda dentro da Sprint 2”. `sprint-2.md:149–153` afirma revisão integral concluída. R05 diz que a correção Linux não foi commitada, apesar de `chmod +x ./gradlew` no Compose e modo executável do wrapper. O teste concorrente bloqueante não se materializou.
- **Impacto:** revisão substancial reconhecida, mas estado final de mitigação não confiável.
- **Recomendação:** encerrar ações com evidência, responsável e risco residual.

### 2.9 Documentação atualizada

- **Atendido:** Registro de IA abrangente, com ferramentas, finalidades, artefatos e declaração de responsabilidade.
- **Parcialmente atendido:** Documentação/release amplas, com garantias excessivas de integração, CI, notificação e tempo real, além de diferenças entre ambientes e links.

#### A12 — Documentação operacional/ambientes divergentes

- **Categoria:** documentação/arquitetura.
- **Evidência:** `docs/ci.md:18–34` cita workflow de links ausente, MySQL no CI e gate de cobertura inexistentes; testes usam H2. Fluxo ainda menciona `entrega-4`; links `file:///c:/Users/...` não são portáveis. ADR-0009 determina `ddl-auto=validate`, mas `SIGTCC/docker-compose.prod.yml:24–28` usa `update`; transição de banco legado para Flyway carece de procedimento. Contrato US04 referencia seção BDD inexistente e diagnóstico parcialmente superado do handler. Pré-requisitos fora do Docker omitem JDK/Node explícitos; há avisos de dependências Node 22 com CI/Compose Node 20, embora os passos observados passem.
- **Impacto:** documentação não representa integralmente o artefato; sintaxe válida não prova boot MySQL/Flyway.
- **Recomendação:** sincronizar configurações, requisitos, contratos e links e testar migração/ambientes.

### 2.10 Release do marco

- **Atendido:** Tag/release exata `v0.2.0`, no prazo e no commit correto.

#### A13 — Release correta, conteúdo superestimado

- **Categoria:** release.
- **Evidência:** `v0.2.0` aponta ao merge correto e foi publicada no prazo local, com funcionalidades, padrões, testes/CI, limitações e próximos passos. Entretanto, anuncia integração completa MockMvc/H2, acompanhamento em tempo real e notificação simulada. O produto consulta sob demanda e atualiza vagas via Observer, não notifica o usuário.
- **Impacto:** crédito forte pelo marco, redução localizada por precisão, sem tratar release como ausente/tardia.
- **Recomendação:** descrever apenas capacidades/verificações comprovadas e distinguir evento interno, notificação e atualização em tempo real.

### 2.11 Registro das contribuições individuais

#### A10 — Board e redistribuição

- **Categoria:** processo/contribuições.
- **Evidência:** Project 31 tem tarefas/responsáveis, porém #11/US04 permanece `Product Backlog`, com campo atualizado em 23/09, apesar do fechamento documental. #79 entrou no Project em 30/09, depois de seu PR; #95/#11 tiveram fechamento administrativo em 02/10, sem que isso torne tardios código/README já integrados. Milestone `entrega-6` está aberta e sem vencimento; campos de datas planejadas dos itens examinados não retornam valores. #82 passou de Willian a Talles em 24/09 às 21h28, antes das entidades/migrações. A transferência é fato, sua causa não foi registrada.
- **Impacto:** acompanhamento parcial, não abandono ou prova de identidade do R01 anônimo.
- **Recomendação:** reconciliar cartões/PRs, registrar cronograma, impedimentos e motivos de redistribuição; não confundir estado atual com histórico integral.

#### Análise das Contribuições Individuais

`18cd7d5..b5c5a3f` contém 102 commits não merge: Damares 13, Eduardo 33, Marcus 4, Talles 23 e Willian 29. Aliases foram reunidos por autoria/conta/conteúdo; autor de PR não recebe automaticamente crédito de todos os commits. Não há novos commits em 18–21/09 nesse recorte, sem que isso prove ausência de trabalho externo. Houve integração incremental em 23–30/09, com testes adicionais, métricas e fechamento concentrados em 30/09–01/10. Contagem e linhas são contexto, não fórmula de nota.

- **Damares do Socorro Gonçalves Gaia:** trabalho em 22, 23, 24, 28/09 e 01/10: planejamento (`aa5ee24`, `dfd287f`), ADRs (`08c0f74`), padrões (`95c5d07`, `831ffe9`), painel/testes (`5b402c2`), M-03/M-06 e fechamento (`bc19190`). Painel integrado em 28/09, com margem para integração. Abriu #125 e publicou release; reviews #115/#116 exigiram formatação, vínculos, CORS e contrato. Forte aderência a arquitetura/frontend/Scrum Master, sem prova integral de todas as cerimônias. Perdas por documentação não reconciliada e fechamento/board/riscos incompletos. Testes do painel e serviço HTTP são compartilhados com outros integrantes.

- **Eduardo Cardoso Oliveira:** atividade em 23, 24, 26, 28, 30/09 e 01/10: contrato (`1c42c56`), exceções/handler/testes, riscos (`b6b705a`), validadores/POST (`154edae`, `af02e28`, `2fbb65e`), consolidação (`061937e`, `2ee12c1`), README, filtros CI e ambiente (`758de77`, `e7824fe`). Reviews substantivos #80/#108/#113 e apoio transversal comprovados. #102/#104 tiveram base incorreta, corrigida sem merge em `main`. Maior amplitude observável de implementação/integração/revisão; perdas por contrato/riscos incompletos e estabilização final próxima ao prazo. Backend #116 é coautoria com Willian; parte de infraestrutura genericamente atribuída a DevOps é de Eduardo.

- **Marcus Jhuan Epifanio Lima:** `ae6ef43` em 29/09 entrega formulário, botão, acompanhamento, rotas, serviço e testes; `f16654e` corrige teste, `37c61f6` atende formatação e `61deb48` atualiza M-05. PR #115 tem conteúdo substancial, apesar de quatro commits. Aprovou #80/#107 e revisou/integrou painel #112. #86/#88 estavam atribuídas desde 23/09; primeiro commit em 29/09 às 13h46, PR às 15h18 e merge em 30/09 às 12h54, cerca de 33h antes do marco. Sem reportes de progresso/impedimentos nas issues #86/#87/#88; contrato de 24/09 permitia paralelismo. Isso não prova seis dias sem trabalho nem atraso contra vencimento individual não recuperado. Painel #87 era compartilhado, implementado por Damares e revisado/integrado por Marcus, sem prova autônoma de abandono.

- **Talles Souza da Cruz:** atividade em 25, 27, 30/09 e 01/10; 16 dos 23 commits no último dia. Assumiu #82, fez entidades (`7d76d77`), Flyway/ADR (`0fc3ab1`, `3e4c668`), testes, qualidade e M-01/M-02. Detectou duplicações no #116 e participou fortemente de aprovações/merges. Domínio/QA centrais e trabalho anterior ao fechamento reconhecidos. Perdas por consolidação final dos testes, integração/concorrência anunciadas e não entregues e mitigação inexistente em M-02; não por inexistência de testes ou quantidade de commits.

- **Willian Ferreira dos Santos:** autoria em 26, 27, 30/09 e 01/10: CI/template/Compose, evento/Observer (`c6834da`, `7c9990a`), listagem/aceite/recusa, consulta (`07783ea`, `ceb117e`), M-04 (`776c14f`) e resposta ao review #108. Onze commits backend têm autoria em 27/09 e confirmação em 29/09: não se presume disponibilidade antecipada aos colegas. #82 foi transferida a Talles sem causa registrada; Eduardo apoiou CI, backend e ambiente. Contribuição de Willian não desaparece por integrar o PR de Eduardo, mas não abrange automaticamente toda infraestrutura/release. Duas aprovações e comunicação restrita limitam a colaboração formal; não se lhe atribui R01 anônimo.

**Contexto informado sobre Marcus:** o registro central em `3-avaliacoes/projeto/avaliacoes/equipe-3/CONTRIBUICOES.md`, seção Método, registra relato posterior da equipe ao professor de baixo envolvimento, poucas responsabilidades, demora e prejuízo às tarefas dependentes. Considerou-se esse relato, não as notas antigas, confrontando-o com intervalo atribuição–primeiro incremento, lote concentrado, falta de reportes e consolidação dos testes após #115. A cronologia é compatível com perda de previsibilidade/compressão das etapas dependentes, mas não prova bloqueio causal exclusivo nem permite quantificar horas de atraso. #87 compartilhada não prova sozinha transferência por abandono; R01 não identifica Marcus sem esse contexto externo.

**Colaboração e limites:** reviews #80/#108/#115/#116 demonstram cooperação real. Redistribuição #82 é fato, causa não. Cerimônias/comunicação externa e extensão de prejuízo coletivo são parcialmente relatos, não fatos integralmente auditáveis. Todos foram avaliados por conteúdo, relevância, oportunidade, periodicidade, integração, comunicação e responsabilidade, não só entrega final ou volume de commits.

## 3. Critérios de Avaliação (10 pts)

### Nota da Entrega

| Critério                                               |   Máximo |    Nota |
| ------------------------------------------------------ | -------: | ------: |
| Planejamento da Sprint 2                               |      1,0 |     0,9 |
| Incremento funcional central, integrado e demonstrável |      2,0 |     1,6 |
| Testes automatizados coerentes e executáveis           |      1,5 |     1,0 |
| Pelo menos dois padrões OO justificados                |      1,5 |     1,2 |
| Métricas, riscos, backlog, board e contribuições       |      1,0 |     0,6 |
| Fluxo de trabalho e CI                                 |      1,5 |     1,3 |
| README, documentação, uso de IA e release              |      1,5 |     1,1 |
| **Total**                                              | **10,0** | **7,7** |

### Justificativa da Nota

**Nota coletiva: 7,7/10,0.** A equipe planejou evolução central do match, implementou fluxo conectado e dois mecanismos OO úteis, manteve suíte executável e praticou revisão/integração incremental com CI real. Marco correto/tempestivo, transparência sobre autenticação e métricas analisadas fortalecem o resultado.

Não há atendimento pleno porque concorrência do núcleo permanece vulnerável, a API não cumpre todos os limites contratuais e a integração anunciada não foi entregue nos testes. Padrões documentados, métricas, riscos, produção, CI e release apresentam divergências. A `main` possui proteção, mas não exige todos os gates, embora o PR final tenha passado por eles: fluxo majoritariamente adequado, não pleno.

Os impactos distinguem funcionamento, alcance dos testes, precisão do design, acompanhamento e documentação, sem desconto integral duplicado pela mesma falha. Não se exige MVP completo, não se presume atraso por data UTC nem ausência de proteção pelo 404 clássico. Contribuição e efeito coletivo são avaliados individualmente, separados do mérito técnico comum.

### Notas Individuais

Notas de contribuição na Sprint 2, independentes da nota técnica coletiva. Não se pressupõe desempenho pleno apenas pelo artefato final: oportunidade, previsibilidade e apoio ao fluxo coletivo também importam.

| Integrante                        | Nota individual |
| --------------------------------- | --------------: |
| Damares do Socorro Gonçalves Gaia |         **8,8** |
| Eduardo Cardoso Oliveira          |         **9,0** |
| Marcus Jhuan Epifanio Lima        |         **6,5** |
| Talles Souza da Cruz              |         **8,0** |
| Willian Ferreira dos Santos       |         **8,0** |

- **Damares — 8,8:** atuação ampla, incremental e central em planejamento, arquitetura, painel, revisão e fechamento; perdas por reconciliação documental/gerencial insuficiente.
- **Eduardo — 9,0:** amplitude de integração, implementação e revisão substantiva, com apoio a infraestrutura; perdas por riscos/contrato incompletos e consolidação final, sem atribuir-lhe toda lacuna coletiva.
- **Marcus — 6,5:** mérito técnico do fluxo do aluno preservado, com testes/reviews e resposta a ajustes. Redução por oportunidade, visibilidade incremental, comunicação/previsibilidade e colaboração, confrontadas com relato de impacto coletivo. Não é punição pelos quatro commits nem imputação de abandono da #87.
- **Talles — 8,0:** domínio, migrações, testes reais e revisão efetiva; perdas pela validação crítica incompleta, concentração final e afirmações de integração/locking/cobertura acima da evidência.
- **Willian — 8,0:** backend/Observer/consulta/CI relevantes; perdas por comunicação/rastreabilidade da redistribuição, menor revisão formal e apoio de outros em parcelas genericamente atribuídas à sua área, sem presumir culpa pela transferência.

Evidências contemporâneas adicionais e esclarecimentos podem fundamentar revisão, sem atribuição retroativa de autoria. As pontuações desta avaliação substituem as notas anteriormente registradas para a Entrega 6, sem reavaliar etapas anteriores.

### Notas Compostas por Integrante

Conforme o modelo de registro solicitado pelo professor, combina-se 30% da coletiva e 70% da contribuição individual; os critérios técnicos continuam exclusivamente os da rubrica normativa.

`nota composta = (nota coletiva × 0,30) + (nota individual × 0,70)`

| Integrante                        | Nota coletiva | Nota individual |                              Cálculo | Nota composta |
| --------------------------------- | ------------: | --------------: | -----------------------------------: | ------------: |
| Damares do Socorro Gonçalves Gaia |           7,7 |             8,8 | `(7,7 × 0,30) + (8,8 × 0,70) = 8,47` |       **8,5** |
| Eduardo Cardoso Oliveira          |           7,7 |             9,0 | `(7,7 × 0,30) + (9,0 × 0,70) = 8,61` |       **8,6** |
| Marcus Jhuan Epifanio Lima        |           7,7 |             6,5 | `(7,7 × 0,30) + (6,5 × 0,70) = 6,86` |       **6,9** |
| Talles Souza da Cruz              |           7,7 |             8,0 | `(7,7 × 0,30) + (8,0 × 0,70) = 7,91` |       **7,9** |
| Willian Ferreira dos Santos       |           7,7 |             8,0 | `(7,7 × 0,30) + (8,0 × 0,70) = 7,91` |       **7,9** |

## 4. Observações

### Observações sobre Uso de IA

`USO-IA.md:65–83` declara Claude/Gemini em planejamento, contrato, README, issues, padrões/diagramas, painel, testes, Flyway/SQL, endpoints e CI/proteção. Os correspondentes existem: documentos/ADRs, classes/listener, migrações V1/V2, componentes/estilos/tipos/serviço, suítes e workflows. O apoio não foi descrito como mera revisão textual: há reconhecimento de implementação e geração de testes, compatível com o material entregue.

Testes do painel incluem aceite, recusa e erros 409/422; CI confirma compilação/testes, e reviews com mudanças demonstram intervenção humana. Existe declaração explícita de leitura, compreensão, adaptação e responsabilidade acadêmica/técnica.

Persistem limites: ferramentas/atividades não são vinculadas uniformemente a responsável, extensão aproveitada e evidência de validação; arquitetura descreve classes/pacotes ausentes ou renomeados; contrato e DTOs/erros divergem; afirmação ampla de tudo testado não comprova integração/concorrência inexistentes nem percentuais sem relatório. Referências históricas requerem manutenção: `verificar-links.yml` existiu e foi removido; `verificacao-formatacao.yml` não é o nome atual `verificar-formatacao.yml`. Isso é imprecisão de rastreabilidade, não prova de artefato inventado.

**Conclusão explícita:** uso declarado de IA **majoritariamente compatível com artefatos e tipos de apoio entregues**, mas **validação alegada apenas parcialmente sustentada**, sobretudo em contrato, padrões, integração/concorrência e garantias de CI. As divergências reduzem confiança documental, não autorizam concluir fraude, plágio ou apoio oculto. Não se encontraram indícios fortes e verificáveis de uso relevante sem declaração; estilo/extensão dos textos não são prova. Compreensão técnica deverá ser verificada na defesa.

### Recomendações para a Equipe

- Proteger concorrência de vagas, decisões e duplicidade e testar operações simultâneas conflitantes.
- Implementar integração HTTP–serviço–banco–Observer e verificar migrações MySQL/transição do banco legado.
- Alinhar validações/erros do contrato, API e elegibilidade da UI.
- Atualizar diagramas/ADRs/classes e separar consumidores futuros dos efetivos.
- Corrigir M-02 e tornar cobertura, horas, progresso e lead time auditáveis; fechar riscos/ações com evidências.
- Reconciliar board/histórias/tarefas/PRs e milestone; comunicar progresso, impedimentos e redistribuição oportunamente.
- Exigir checks técnicos e apenas merge também em `main`; revalidar aprovação após push.
- Sincronizar ambientes `validate`, requisitos de Node/JDK, links e CI; não confundir sintaxe válida com execução funcional comprovada.
- Descrever precisamente testes, notificações e consulta sob demanda na release.
- Priorizar autenticação/autorização antes de uso externo e refinar o registro de IA com validações específicas.
