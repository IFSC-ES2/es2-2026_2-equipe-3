# Avaliação da Entrega 7 - Sprint 3

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC — SIGTCC
- Entrega: 7 - Sprint 3
- Repositório confirmado pela configuração: `IFSC-ES2/es2-2026_2-equipe-3`
- MVP declarado: autenticação/perfis, catálogo e vinculação aluno–orientador e gestão de documentos/prazos de TCC.
- Recorte da Sprint 3: US01 (Keycloak e perfis), US03 (filtros combinados), US05 (calendário), arquitetura/ADRs, reengenharia e testes/CI.
- Pull request final: [#196](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/196), `entrega-7` → `main`, head `a637fb3`.
- Versão avaliada: `88288f7`, merge registrado pela API em 22/10/2026 às 23:11:41 (UTC−3).
- Base de comparação: `b5c5a3f`, marco da Sprint 2.
- Tag e release: `v0.3.0`, apontando ao merge; publicação em 22/10/2026 às 23:28:31 (UTC−3).
- Prazo normativo: 22/10/2026.

## Documentos Consultados

Somente documentos e artefatos do repositório avaliado:

- `README.md` e `USO-IA.md`.
- `.github/PULL_REQUEST_TEMPLATE.md` e `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- Workflows `.github/workflows/backend-ci.yml`, `frontend-ci.yml`, `verificacao-docs.yml` e `verificar-formatacao.yml`.
- `docs/entregas/sprint-3.md` e documentos anteriores de escopo/contrato pertinentes à comparação.
- `docs/ARQUITETURA.md`, `docs/diagramas/c1-contexto.md` e `c2-conteineres.md`.
- Todos os ADRs `docs/adrs/ADR-0001.md` a `ADR-0014.md`.
- `docs/REENGENHARIA.md`.
- `docs/contrato-dados-us01.md`, `us02.md`, `us04.md` e `us05.md`.
- `docs/keycloak/keycloak.md`.
- `docs/METRICAS.md` e `docs/metricas/M-01.md` a `M-06.md`.
- `docs/riscos.md`, `docs/qualidade.md`, `docs/ci.md`, `docs/fluxo-de-trabalho.md` e `docs/dod.md`.
- `docs/inception.md`, `docs/BASELINE.md`, `docs/ESTIMATIVAS.md` e `docs/PADROES-DE-PROJETO.md`.
- Código, testes, migrações, arquivos Gradle/npm, Dockerfiles, Composes e realm Keycloak em `SIGTCC/`.
- Issues #8/#10/#12, tarefas da Sprint 3 #127–#159 e #189, PRs correspondentes, especialmente #160/#161/#165/#167/#168/#171–#175/#180–#187/#190–#196.
- [Project 31](https://github.com/orgs/IFSC-ES2/projects/31), milestone Entrega-7, histórico Git, reviews/checks, regras de proteção e [release `v0.3.0`](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/releases/tag/v0.3.0).

## 1. Descrição — Resumo da Entrega

O MVP continua voltado ao fluxo acadêmico de TCC. A Sprint 3 replanejou Coordenador como professor responsável pela `TurmaTCC`, autenticação própria como Keycloak e upload como futura submissão por link. US06/US07 e bancas ficaram fora da sprint; bancas voltaram ao backlog global. Esses ajustes possuem justificativas e ADRs, não sendo tratados como funcionalidades prometidas e esquecidas nesta entrega.

O incremento é substancial: login e sessão no frontend, Resource Server no backend, roles, perfil do professor, filtros combinados e calendário com regras temporais e responsabilidade consultada no banco. Há C1/C2, revisão dos ADRs, extração do tratamento HTTP em REF-1, comparação antes/depois e testes efetivamente executados em PR. A release correta foi publicada dentro de 22/10 no horário local: a data UTC de 23/10 não caracteriza atraso.

Entretanto, a identidade autenticada não substituiu inteiramente IDs do navegador nem controla papel/titularidade em todas as operações do match. Arquitetura mistura capacidades futuras e atuais; contrato de prazos e cálculo entregue divergem; riscos/métricas/CI contêm afirmações não sustentadas. A reengenharia é real e útil, mas sua contagem manual e os percentuais de cobertura não têm reprodução integral nas evidências entregues.

## 2. Itens Obrigatórios da Entrega

### 2.1 Inspeção e replanejamento da Sprint 3

- **Atendido:** `docs/entregas/sprint-3.md:15–53` analisa o estado real de funcionalidades, métricas e riscos da Sprint 2; `:57–147` distingue manter, corrigir, replanejar e priorizar. Identifica identidade insegura, cobertura frontend baixa, concorrência e denominador incompleto do MVP.
- **Parcialmente atendido:** US01 ainda consta “a reestimar” e US05 “a estimar” no fechamento, embora fichas posteriores lhes atribuam pontos. Correções planejadas não têm balanço final completo.

#### A1 — Inspeção fundamentada, fechamento do compromisso parcial

- **Categoria:** planejamento/requisitos.
- **Evidência:** `sprint-3.md:68–85,137–147` promete trocar identidade por `localStorage`, mover filtro de ativos para backend, coletar cobertura no pipeline e revisar o denominador do MVP. Parte permaneceu pendente ou contraditória sem consolidação final; US01/US05 continuam sem estimativa atualizada no planejamento.
- **Impacto:** atendimento majoritário da inspeção, não apenas nominal, mas sem evidência de execução integral do plano de correções. A falta de estimativas não é convertida em exigência de ferramenta/unidade adicional à norma.
- **Recomendação:** fechar planejado versus executado, justificar correções não concluídas e sincronizar estimativas e recorte vigente.

### 2.2 Incremento funcional da Sprint 3

- **Atendido:** US01/US03/US05 possuem implementação real, issues e PRs; o calendário valida datas, sequência e pertencimento à turma vigente. Autenticação anônima é efetivamente barrada, e filtros combinados funcionam nas camadas repository/service/controller e UI.
- **Parcialmente atendido:** autorização por perfil/titularidade não foi integrada ao núcleo anterior; identidade do painel ainda depende do navegador. Não se recontam catálogo/match/Strategy/Observer da Sprint 2 como novos incrementos.
- **Não atendido:** substituição integral da identificação insegura, prevista para a própria Sprint 3.

#### A2 — Autenticação não garante autorização do match e do perfil

- **Categoria:** implementação/segurança. 
- **Severidade:** alta.
- **Evidência:** `J/config/SecurityConfig.java:28–32` restringe por papel somente `/api/v1/professores/**`; demais operações exigem apenas autenticação. `OrientadorController.java:25–62`, `OrientadorService.java:79–126`, `SolicitacaoController.java:23–53` e `SolicitacaoService.java:39–99` aceitam IDs/dados sem conferir principal, papel ou propriedade. O e-mail do orientador é alterável (`OrientadorService.java:88–90`).
- **Impacto:** pelo fluxo estático, token de aluno não é barrado por papel ao editar/desativar orientadores ou aceitar/recusar solicitações. Dados de propostas de terceiros também ficam acessíveis por IDs. A gestão de prazos verifica responsabilidade por e-mail (`PrazoEntregaService.java:91–102`), mas esse vínculo depende de um cadastro alterável, criando ainda risco de escalada em estados de dados que permitam a alteração. Não se executou exploração; não se afirma comprometimento real. A barreira para anônimos existe, portanto não se classifica a segurança como ausente, mas a US01 não está plenamente integrada ao MVP.
- **Recomendação:** exigir papel e propriedade no servidor, vincular operações ao principal e proteger os atributos usados para mapear identidade; testar acessos cruzados e tentativas de alteração de identidade.

#### A3 — Painel e “Meu perfil” ainda usam identidade do navegador

- **Categoria:** comportamento/integração.
- **Evidência:** `F/paginas/PainelSolicitacoes/index.tsx:19–22,59–62` seleciona orientador por URL, `localStorage` ou fallback `1`. `/professores/me` controla a aba da turma, não o ID usado para listar solicitações. `OrientadorEdicao/index.tsx:20–31` também depende de identificador local; logout não o reconcilia com a nova conta.
- **Impacto:** mesmo sem ataque, um professor pode visualizar o painel do ID errado no ambiente inicial ou após troca de conta. Contradiz a correção planejada em `sprint-3.md:70`.
- **Recomendação:** obter ID/perfil do servidor a partir da sessão e eliminar fallback operacional e seleção arbitrária para “meu” recurso.

#### A4 — Contratos e filtros parcialmente divergentes

- **Categoria:** comportamento/contrato.
- **Evidência:** `contrato-dados-us05.md:59–66,240,257` promete `diasRestantes` no backend, com fuso `America/Sao_Paulo`; `PrazoEntregaResponseDTO.java:7–14` e service não devolvem o campo. `F/utils/contagemRegressiva.ts:18–37` calcula pelo dia local do navegador. O filtro por ativos permanece no frontend (`OrientadorList/index.tsx:27–28`), sem cláusula correspondente no repository, apesar da movimentação para backend ter sido planejada.
- **Impacto:** calendário/contagem e filtros existem; a divergência é de responsabilidade, semântica temporal e contrato, não ausência completa das funcionalidades.
- **Recomendação:** escolher contrato único de data/fuso e filtragem e verificá-lo com testes de resposta e cenários de fronteira.

#### A5 — Migração de seed não distingue instalação limpa de atualização

- **Categoria:** persistência/compatibilidade.
- **Evidência:** `B/src/main/resources/db/migration/V5__seed_professores_e_turma_vigente.sql:3–24` fixa IDs e usa `ON DUPLICATE KEY UPDATE`, modificando e-mails, atividade e vagas de registros que podem existir da Sprint 2.
- **Impacto:** risco de sobrescrever identidade/dados ou falhar em base previamente usada; não é falha de boot reproduzida em banco vazio. Os testes H2 com Flyway desabilitado não verificam esse upgrade.
- **Recomendação:** separar dados demonstrativos de migrações estruturais e testar instalação limpa e evolução de volume legado.

### 2.3 Documentação da arquitetura

- **Atendido:** C1/C2, documento textual, responsabilidades, dependências, relação com MVP e comparação planejado/real estão localizáveis. SPA, API Resource Server, MySQL/Flyway e Keycloak correspondem às unidades entregues.
- **Parcialmente atendido:** diagramas misturam futuro e presente e representam protocolos/garantias diferentes do ambiente efetivo.

#### A6 — Visões amplas, mas não integralmente fiéis ao sistema atual

- **Categoria:** arquitetura/documentação.
- **Evidência:** `c1-contexto.md:29,46,60` apresenta submissão/armazenamento de links como interação existente, embora `ARQUITETURA.md:200` e README reconheçam US06 futura. `c2-conteineres.md:45–49,78` indica HTTPS, mas Compose/clientes usam HTTP; a descrição de busca JWKS na inicialização diverge da inicialização adiada registrada na ADR-0012. `ARQUITETURA.md:195` afirma mitigação integral de R06, incompatível com A2.
- **Impacto:** as visões mínimas e a arquitetura principal foram entregues, mas não recebem pontuação integral de correspondência ao sistema real. Não se exige TLS de produção antecipadamente; exige-se representar corretamente o ambiente atual.
- **Recomendação:** marcar integrações futuras, ajustar protocolos e distinguir autenticação, autorização e condições de inicialização.

### 2.4 ADRs consolidados

- **Atendido:** os nove ADRs anteriores receberam revisão e decisões substituídas foram identificadas; ADR-0010 a 0014 documentam turma, Keycloak, Resource Server, links e Direct Access Grant com contexto, alternativas e consequências.
- **Parcialmente atendido:** notas de revisão convivem com trechos/diagramas antigos e configurações que continuam contradizendo a decisão.

#### A7 — Revisão real, consolidação derivada incompleta

- **Categoria:** decisões arquiteturais.
- **Evidência:** ADR-0007 reconhece nomes/observadores divergentes em nota, mas o corpo e `PADROES-DE-PROJETO.md` mantêm `SolicitacaoContexto`, pacote `strategy`, ordenação e classes inexistentes. ADR-0009 reconhece `ddl-auto=update` de produção, sem resolução no Compose. O guia Keycloak orienta PKCE/redirecionamento e arquivo `sigtcc-realm.json`, mas o artefato real é `SIGTCC/keycloak/realm-import.json` com password grant.
- **Impacto:** não há ausência de ADRs; há revisão e justificativas fortes, porém documentação operacional/derivada não integralmente reconciliada.
- **Recomendação:** explicitar o caráter histórico dos trechos substituídos e alinhar diagramas, produção e guia aos ADRs atuais.

### 2.5 Manutenção/reengenharia

- **Atendido:** problema real de duplicação/acoplamento no tratamento de erros HTTP, diagnóstico #166, implementação específica #180 e registro comparativo #184. O diff confirma extração de `ApiError`, leitura/normalização/status, migração de quatro serviços e correção do merge de headers. Há testes diretos de `apiErro` e `apiFetch`.
- **Parcialmente atendido:** testes sustentam preservação dos comportamentos exercitados, não ausência universal de regressões; serviços de telas são frequentemente simulados.

### 2.6 Comparação de métrica antes/depois da reengenharia

- **Atendido:** `REENGENHARIA.md:127–238` apresenta baseline `23efc6c`, valores antes/depois e relação com manutenibilidade/confiabilidade da Entrega 4. O recorte da duplicação e o boilerplate residual são explicitados, assim como mudanças intencionais de comportamento.
- **Parcialmente atendido:** a contagem exata manual e cobertura não são plenamente reproduzíveis pelos artefatos/logs disponíveis.

#### A8 — REF-1 materialmente demonstrada; precisão de métrica/atribuição parcial

- **Categoria:** reengenharia/mensuração.
- **Evidência:** commits `1ca665b`, `630fb80`, `71c0fc7`, `701e964` e merge `c285e08`/#180 confirmam mudança substancial. O [CI da refatoração](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/39533684215) executou 85 testes/16 arquivos, lint e build com sucesso. A medida 140→0 refere-se aos blocos selecionados, não a toda duplicação do projeto. Faltam intervalos/algoritmo suficientemente precisos para reproduzir todas as linhas manuais; os percentuais de cobertura não têm relatório preservado no CI consultado.
- **Impacto:** crédito substancial pelo problema, PR, solução, testes e comparação; não se declara a reengenharia nominal ou inexistente. A contribuição de `db03a97` fora da REF-1 é reconhecida pela equipe, mas sua explicação temporal é incorreta: o commit veio após o início da extração. A alegação de zero cobertura de todos os serviços ficou superada pelo teste de `prazoEntregaService`; “100% no módulo” também precisa distinguir linhas de 89,47% de branches.
- **Recomendação:** preservar script/relatório e limites de contagem, isolar efeitos de commits externos e declarar preservação dos cenários realmente testados. Não criar ADR novo foi justificado como extração interna sem mudança arquitetural/contratual; isso não é ausência automática de decisão exigida.

### 2.7 Testes automatizados integrados ao pipeline

- **Atendido:** testes versionados, gatilhos de PR, dependências e propagação de falha ao check. Há execução verde real, não somente passos ignorados. PR #187 teve Vitest reprovado, build subsequente pulado e nova execução bem-sucedida antes de integrar.
- **Parcialmente atendido:** faltam cenários críticos de papel/ownership e os filtros de CI não cobrem integralmente alterações isoladas de Compose/realm.

#### A9 — CI efetivo; limites de cobertura funcional e ambiente

- **Categoria:** testes/CI.
- **Evidência:** no PR #196, [backend run 39885770798](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/39885770798) executou build e `:test`; o `test` seguinte ficou legitimamente `UP-TO-DATE`. [Frontend run 39885769913](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/39885769913) executou instalação, lint, 105 testes/19 arquivos e build. PR #180/#182 também tiveram execução técnica real; em PR documental, skips não são contados como nova execução. Não há `continue-on-error` mascarando teste.
- **Impacto:** requisito principal de teste automático no pipeline atendido. Testes de calendário atravessam HTTP/persistência H2 e responsabilidade, mas tokens positivos via `jwt()` do Spring injetam autenticação validada, não verificam um JWT Keycloak real. Não cobrem aluno alterando orientador, respondendo proposta alheia ou cadeia de alteração de identidade. Não se atribui ausência de issuer/audience: configuração e realm os definem. Cobertura percentual não é exigência adicional inventada para este critério.
- **Recomendação:** acrescentar testes de autorização cruzada e ciclo real de token, migração/realm e filtros de caminhos apropriados. Avisos Node 20 com dependências que exigem Node 22 e avisos `act` são riscos de suporte/sincronização, não falha dos runs aprovados.

### 2.8 Atualização de métricas, riscos e documentação

- **Atendido:** seis fichas com valores/comparações, revisão de riscos, README atual, arquitetura/ADRs/reengenharia localizáveis e IA detalhada por integrante.
- **Parcialmente atendido:** várias fontes/conclusões e instruções divergem dos artefatos; dados declarados não são automaticamente medições independentes.
- **Não atendido:** consistência do estado de R07 e reprodução das garantias de cobertura no pipeline descrito.

#### A10 — R07 mitigado no registro, mas não no produto

- **Categoria:** confiabilidade da evidência. 
- **Severidade:** alta.
- **Evidência:** `riscos.md:335` afirma `@Version`/lock otimista validado por testes concorrentes. Entidade `PerfilOrientador`, serviço, repositórios e testes entregues não apresentam esse mecanismo/teste. README:204, M-02 e release corretamente mantêm concorrência pendente.
- **Impacto:** a ficha de riscos não retrata o marco e contradiz outras fontes da própria equipe. O débito herdado foi explicitamente replanejado; o problema avaliativo adicional desta sprint é declarar mitigação inexistente. Não se infere falsificação intencional nem se desconta novamente a mesma causa integralmente em vários critérios.
- **Recomendação:** corrigir status/ação executada e anexar evidência antes de declarar mitigação.

#### A11 — Progresso, esforço e documentação sem reconciliação final

- **Categoria:** métricas/documentação.
- **Evidência:** M-05 declara 21/21 SP = 100%, mas mede US01–05, não todo o MVP com US06/07/bancas pendentes. M-03 usa 11 SP e M-02 usa 8 SP; planejamento/riscos ainda mandam estimar US05. M-06 soma corretamente 73/84h, mas usa commits como comprovação de horas e declara 162 commits por um comando `origin/main..origin/entrega-7` que não reproduz a janela após o merge. O recorte fixo, sem merges/arquivos de avaliação, retornou 151 commits de produto/documentação com outra distribuição. Isso não prova falsificação: horas são apontamento da equipe, não inferíveis de commits.
- **Impacto:** acompanhamento existe, mas a confiança em conclusão global, horas e fechamento quantitativo é parcial. `docs/ci.md` promete MySQL, links/Markdown e cobertura não realizados pelos workflows; usa “PR #148” em link para #182. Qualidade encerra na revisão S2, fluxo ainda cita `entrega-4` e há links/arquivos incorretos no guia Keycloak. Abertura do README/Inception mantém coordenador/upload enquanto o replanejamento alterou o recorte.
- **Recomendação:** publicar fonte/corte/comando válido, separar apontamento de medição, consolidar SP/escopo e revisar documentos vivos contra o marco real.

### 2.9 Fluxo de trabalho evidenciado no repositório

- **Atendido:** branches de tarefa → PR → `entrega-7` → PR #196 → `main`, revisões efetivas e merge commit. Não se observou desenvolvimento direto em `main` na janela. PRs recebidos de avaliação foram segregados do produto.
- **Parcialmente atendido:** proteção da branch de entrega é mais forte que a da `main`; fechamento do board ocorreu parcialmente após o prazo.

#### A12 — Proteção conclusiva e fluxo real, com prevenção incompleta na `main`

- **Categoria:** governança GitHub.
- **Evidência:** proteção clássica retorna 404, mas `rules/branches/main` confirma ruleset **20534413 — Merges**: PR, uma aprovação descartada após push, sem bypass, bloqueio de exclusão/force push e somente `Checar Arquivos Essenciais`; permite merge/squash/rebase. Ruleset **24612019** de `entrega-7` exige backend/frontend, branch atualizada, uma aprovação invalidada após push, resolução de discussões e apenas merge, sem bypass. Foi criado em 06/10 e atualizado em 19/10; os parâmetros atuais não provam vigência integral desde o primeiro dia.
- **Impacto:** O PR final respeitou método e execução técnica; a lacuna é preventiva, pois a `main` não obriga todos os gates. Não se caracteriza integração com testes quebrados: o rollup retém falha antiga de Prettier em arquivos de avaliação, sucedida pelo [run verde 39885769890](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/actions/runs/39885769890) às 22h45, antes das três aprovações/merge. A correção de avaliação não é creditada como trabalho de produto.
- **Recomendação:** alinhar checks/métodos da `main` à política da entrega, manter registro histórico e distinguir falha superada de estado final do pipeline.

### 2.10 Release/tag do marco

- **Atendido:** tag `v0.3.0` correta, merge em 22/10 às 23h11min41s e publicação às 23h28min31s (UTC−3), no prazo. Release descreve funcionalidades, decisões, REF-1, testes/CI, documentação e limitações.
- **Parcialmente atendido:** percentuais de cobertura e números da refatoração dependem das ressalvas de medição, e a descrição não esclarece integralmente a autorização incompleta do match.

Não se aplica desconto por atraso: registros UTC de 23/10 correspondem à noite de 22/10 local. A release reconhece concorrência, ambiente de desenvolvimento, ROPC, ausência de notificações e documentos/pareceres ainda não entregues; esses limites não são ocultados globalmente, embora contraditos em alguns documentos.

### 2.11 Registro de contribuição individual

- **Atendido:** registro específico por integrante, issues/PRs e histórico que permitem distinguir implementação, revisão, testes, documentação e absorção de tarefas.
- **Parcialmente atendido:** memória de horas/quantidade e autoria de todos os ADRs excedem o verificável; implementação do frontend concentra-se na última semana, contextualizada pela redistribuição.
- **Não comprovado:** contribuição de Marcus nesta sprint, além de sua associação nominal a tarefas realizadas por outros.

#### A13 — Board real, mas estados consolidados tardiamente

- **Categoria:** acompanhamento/colaboração.
- **Evidência:** Project 31 recuperado integralmente, com issues, responsáveis e vínculos. Histórias #8/#10/#12 foram fechadas em 21–22/10, mas somente passaram a `Done` em 01/11; #159 passou a `Done` em 31/10 e milestone fechou em 03/11. Arquitetura estava versionada em 16/10, com cartões movidos a andamento somente em 20/10. Há comunicação de dependências por Eduardo na #136 e PR #187.
- **Impacto:** não se trata de board abandonado, mas o estado atual não prova fechamento fiel no prazo. Movimentos de novembro não são crédito retroativo nem tornam tardio o código integrado em outubro.
- **Recomendação:** atualizar estado enquanto o trabalho ocorre e reconciliar histórias, subtarefas, PRs e marco antes do fechamento.

#### Análise das Contribuições Individuais

O recorte `b5c5a3f..88288f7`, excluindo merges e arquivos de `avaliacao/`, contém **151 commits**: Damares 13, Eduardo 78, Marcus 0, Talles 31 e Willian 29. Aliases foram reunidos; contagem inclui documentação/formatação e não mede isoladamente relevância ou horas. A contribuição foi julgada apenas nesta sprint, sem transportar relatos/notas da Sprint 2.

- **Damares do Socorro Gonçalves Gaia:** atividade em 06, 13, 16, 21 e 22/10. Entidade/migração/repository/testes de turma (`3af8799`, `a7d9351`, `c300de9`, PR #160) foram entregues cedo; arquitetura/C4 em `f238048`, revisada em `7d1d850`/`3a9317f` após apontamentos técnicos de Talles; ADRs, M-06 e revisões de #184/#185/#187 e fechamento. Contribuição de domínio, arquitetura e revisão distribuída nas três semanas. Autoria de todos os ADRs não é exclusiva: ADR-0011/12 são de Talles e 0014 de Eduardo; reconhece-se consolidação/revisão, não autoria integral. Perdas por arquitetura ainda parcialmente futura, afirmações de segurança e auditabilidade de horas acima da evidência.

- **Eduardo Cardoso Oliveira:** organização/atribuição/revisão desde 04–05/10; commits em 12 e 16–22/10. Perfil do professor/contrato (#168), frontend de login, dashboard, filtros, calendário, REF-1 (#180), comparação (#184), testes (#181/#187), métricas/riscos e integração. Assumiu tarefas originalmente atribuídas a Marcus, com atribuições/commits/PRs, não apenas relato: #130 em 16/10, #139 em 18/10 e #140 em 21/10. Comunicação de dependências em #136/#187 e revisão/integração efetivas. 72 dos 78 commits são a partir de 16/10, mas havia organização anterior e incrementos sucessivos; a concentração é contextualizada pela absorção do frontend. Perdas por testes finais e limitações da refatoração/mensuração e integração da identidade, não por usar IA declarada. A geração integral de parte do código não elimina a contribuição de revisão, integração, adaptação de testes e responsabilidade, nem prova domínio técnico sem defesa.

- **Marcus Jhuan Epifanio Lima:** nenhum commit, PR ou review localizado no recorte e nenhum comentário de progresso/impedimento nas timelines examinadas. Recebeu tarefas em 05/10 (#130/#131/#134/#135/#139/#140/#146); parte foi posteriormente atribuída a Eduardo e implementada por ele. #147 foi retirada de Marcus e atribuída a Eduardo em 09/10. `sprint-3.md:254–256` registra expressamente ausência de contribuição; a seção de IA está reservada. Fatos demonstram responsabilidade atribuída sem entrega rastreável e absorção por colegas. “Sem comunicação” é relato da equipe, corroborado pela ausência nos canais consultados, não prova de silêncio em toda comunicação privada. A nota é pela contribuição demonstrável na Sprint 3, não reincidência presumida de outra etapa.

- **Talles Souza da Cruz:** atividade em 06, 07, 09, 15, 21 e 22/10. Inspeção/replanejamento (`dd522f2`, #161), Keycloak/realm (#162/#164), Resource Server (`bc32294`, #167), diagnóstico REF-1 (#166), calendário/contrato/validações/testes (#171/#172), revisão de ADRs (#185), correção de rotas/botões (#190), revisões substantivas de arquitetura (#173) e reengenharia (#180), PR final e release. Assumiu #143/#144 de Willian em 06/10 e #127/#137/#138 inicialmente de Eduardo em 09/10; entregas habilitaram etapas dependentes. Forte amplitude e periodicidade nas três semanas. Perdas por completude da autorização de servidor/calendário e consolidação documental/board, sem atribuir-lhe responsabilidade exclusiva por cada falha coletiva.

- **Willian Ferreira dos Santos:** atividade em 06–09, 18–20 e 22/10. Filtros backend e testes (`d726f7f`, `aeea14d`, `7e80643`, #165), integrados em 10/10; CI (#182), evidência (#183), M-04, riscos/IA e aprovações #191/#193/#195/#196. Houve intervalo sem commits entre 10 e 17/10, não prova de ausência de trabalho. A entrega antecipada de filtros permitiu dependências; CI foi consolidado na última semana. #143/#144 foram redistribuídas cedo a Talles, sem evidência suficiente para chamar isso de abandono/atraso pessoal. Perdas por precisão do documento de CI, mensuração/risco e menor abrangência de revisão técnica, sem negar mérito de backend/infra.

**Colaboração e limites:** reviews e mudanças em #173/#180 e falhas corrigidas de testes mostram colaboração real. A absorção de frontend por Eduardo e redistribuições para Talles são rastreáveis; causas pessoais e horas offline não foram auditadas. Contagem elevada não garante nota plena, e nenhuma participação é atribuída por assignee isoladamente. Avaliação entre pares/defesa e evidências contemporâneas adicionais podem fundamentar revisão, sem criação retroativa de autoria.

## 3. Critérios de Avaliação (10 pts)

### Nota da Entrega

| Critério | Máximo | Nota |
|---|---:|---:|
| Inspeção e replanejamento | 1,0 | 0,9 |
| Incremento funcional da Sprint 3 | 1,5 | 1,0 |
| Arquitetura real documentada | 1,5 | 1,2 |
| ADRs revisados e consolidados | 1,0 | 0,8 |
| Reengenharia, PR e métrica antes/depois | 1,5 | 1,2 |
| Testes automatizados integrados ao CI | 1,0 | 0,9 |
| Métricas, riscos, documentação, README e IA | 1,0 | 0,5 |
| Fluxo de trabalho, checks, merge e release | 1,5 | 1,3 |
| **Total** | **10,0** | **7,8** |

### Justificativa da Nota

**Nota coletiva: 7,8/10,0.** A entrega atende majoritariamente à consolidação técnica: inspeção efetiva, incremento central real, duas visões arquiteturais, decisões revisadas, refatoração orientada a problema concreto e testes executados em PR com falhas corretamente propagadas. A integração e a release estão no prazo e há revisões com efeito técnico demonstrado.

A nota não é plena por autorização/identidade incompletas no núcleo, riscos de migração, diferenças de contrato/arquitetura, contagem antes/depois apenas parcialmente reproduzível e afirmações contraditórias de riscos, conclusão do MVP, cobertura e esforço. Documentos atualizados não equivalem automaticamente a documentos coerentes. A reengenharia recebe crédito pelo efeito real e pelos testes, não apenas por existir arquivo/PR; o CI recebe crédito por execução demonstrada, não pela cor de jobs com passos ignorados.

Não se exige US06/07 completas nem se penaliza ROPC apenas por não usar PKCE: a escolha está explicitada e não é vedada pela norma. O débito de concorrência herdado e replanejado é distinguido da afirmação nova e incorreta de mitigação. Descontos são alocados por efeito — produto, fidelidade arquitetural, refatoração/mensuração, testes, acompanhamento e governança — sem perda integral duplicada pelo mesmo problema. A contribuição individual não é confundida com o mérito técnico coletivo.

### Notas Individuais

Escala 0–10, considerando conteúdo, relevância, aderência às responsabilidades, periodicidade/oportunidade, integração, revisão, comunicação e apoio aos colegas; não são proporcionais à contagem de commits.

| Integrante | Nota individual | Justificativa resumida |
|---|---:|---|
| Damares do Socorro Gonçalves Gaia | **8,5** | Base de domínio antecipada, arquitetura, ADRs e revisões substanciais; limitações de fidelidade arquitetural e auditabilidade gerencial. |
| Eduardo Cardoso Oliveira | **9,0** | Absorção de frontend, QA, reengenharia e integração com comunicação de dependências; perdas por fechamento/testes finais e precisão de validação/métrica. |
| Marcus Jhuan Epifanio Lima | **0,0** | Sem contribuição rastreável nesta sprint; tarefas implementadas por colegas, coerentemente com o registro explícito da equipe. |
| Talles Souza da Cruz | **9,0** | Planejamento, identidade, calendário, diagnóstico, revisões efetivas e release, distribuídos na janela; autorização e fechamento documental não integralmente consolidados. |
| Willian Ferreira dos Santos | **8,3** | Filtros/testes antecipados, CI e apoio em revisão/fechamento; perdas de documentação, rastreabilidade de garantias e consolidação tardia do CI. |

O 0,0 de Marcus representa falta de evidência para pontuar a Sprint 3, não uma afirmação de inexistência absoluta de esforço privado. A nota pode ser revista diante de artefatos/ações contemporâneos verificáveis; associação a issues concluídas por colegas não substitui contribuição efetiva. Não foram usadas suas notas ou relatos da Sprint 2 para produzir essa conclusão.

### Notas Compostas por Integrante

A nota composta combina 30% da coletiva e 70% da contribuição individual. Os critérios técnicos continuam exclusivamente os da rubrica normativa.

`nota composta = (nota coletiva × 0,30) + (nota individual × 0,70)`

| Integrante | Coletiva | Individual | Cálculo | Nota composta |
|---|---:|---:|---:|---:|
| Damares do Socorro Gonçalves Gaia | 7,8 | 8,5 | `(7,8 × 0,30) + (8,5 × 0,70) = 8,29` | **8,3** |
| Eduardo Cardoso Oliveira | 7,8 | 9,0 | `(7,8 × 0,30) + (9,0 × 0,70) = 8,64` | **8,6** |
| Marcus Jhuan Epifanio Lima | 7,8 | 0,0 | `(7,8 × 0,30) + (0,0 × 0,70) = 2,34` | **2,3** |
| Talles Souza da Cruz | 7,8 | 9,0 | `(7,8 × 0,30) + (9,0 × 0,70) = 8,64` | **8,6** |
| Willian Ferreira dos Santos | 7,8 | 8,3 | `(7,8 × 0,30) + (8,3 × 0,70) = 8,15` | **8,2** |

## 4. Observações

### Observações sobre Uso de IA

`USO-IA.md` registra a Entrega 7 por integrante, com ferramentas, datas, finalidades, artefatos, aproveitamento, adaptação e validação. Gemini, Claude e ChatGPT são declarados para domínio/ADRs/C4, frontend, testes, REF-1, Keycloak, calendário, filtros, CI, métricas e riscos. Os artefatos correspondentes existem e o tipo geral de apoio é compatível com commits, componentes, configurações, testes e PRs. Há responsabilidade técnica explícita.

Eduardo declara geração integral de parte do código de produção, sem edição manual, com revisão/integração e adaptação de testes. Isso não é infração automática: o protocolo permite geração assistida com compreensão, revisão, validação e responsabilidade. Não se presume escrita manual exclusiva nem se infere falta de domínio apenas pela ferramenta; compreensão deve ser verificada na defesa.

As declarações têm divergências verificáveis:

- O relato de sprint atribui o frontend emergencial ao Gemini, enquanto o detalhamento das mesmas issues em `USO-IA.md:143–155` indica Claude; a associação precisa ser reconciliada, sem concluir que nenhuma das ferramentas foi usada.
- `JwtAuthConverter.java` citado na validação não é o nome da classe entregue (`KeycloakRealmRoleConverter`); REF-2 aparece sem segunda intervenção técnica claramente distinguida da comparação da REF-1.
- O período “16 a 20/10” não cobre toda a atividade verificável de Eduardo em 12 e 21–22/10. Autoria agregada de todos os novos ADRs também excede a autoria direta, embora exista revisão/consolidação.
- Revisão contra código e validação integral alegadas não eliminam R07 incorretamente mitigado, contrato de dias restantes divergente, autorização incompleta ou garantias de cobertura/MySQL ausentes no CI.
- Teste com nome de redirecionamento pode verificar sessão/ausência de alerta sem comprovar destino; teste de UI que mostra 403 simulado não prova autorização real. Há testes backend de calendário, mas não os cenários cruzados destacados em A2.
- A seção reservada a Marcus é coerente com a ausência de contribuição registrada; sem artefato atribuível, não constitui por si prova de uso oculto de IA.

**Conclusão explícita:** uso declarado **majoritariamente compatível com os artefatos e tipos de apoio**, mas **validação integral apenas parcialmente sustentada**. A transparência é positiva e os reviews/testes confirmam parte da intervenção humana; as inconsistências exigem correção técnica/documental. Não há evidência concreta suficiente para afirmar apoio oculto relevante, plágio, fraude ou falsificação intencional; estilo textual e geração integral declarada não foram usados como prova de autoria indevida.

### Recomendações para a Equipe

- Integrar papéis e titularidade no backend de orientadores/solicitações/prazos, proteger os atributos que vinculam identidade e adicionar testes cruzados.
- Substituir IDs de URL/localStorage/fallback por identidade resolvida no servidor, inclusive após logout/troca de conta.
- Testar V5 e Flyway sobre banco legado, separando seed demonstrativo de migração estrutural.
- Reconciliar contrato de calendário, fuso institucional, cálculo de dias e filtro de ativos com a implementação.
- Marcar arquitetura futura, corrigir protocolos/guia Keycloak e consolidar ADRs e documentos derivados.
- Preservar baseline/script/relatórios da métrica de refatoração, delimitar mudanças intencionais e isolar influência de commits externos.
- Corrigir o status de R07 e os denominadores de progresso/velocidade; não apresentar commits como prova de horas.
- Atualizar CI/documentação para o que efetivamente executa, ampliar testes de segurança/realm/migrações e alinhar versões Node.
- Tornar os gates técnicos e método de merge obrigatórios também na `main` e sincronizar board/milestone no encerramento real.
- Manter redistribuição e comunicação de progresso/impedimentos rastreáveis, com responsabilidades acompanhadas durante toda a janela.
- Reconciliar ferramentas, nomes de artefatos, períodos e limites de validação no registro de IA e preparar todos os integrantes para explicar os artefatos que assumiram.
