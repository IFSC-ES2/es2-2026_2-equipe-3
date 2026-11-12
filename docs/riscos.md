# Registro de Riscos

Este documento apresenta o levantamento de riscos do projeto, contemplando a identificação, análise, priorização e o plano de resposta para mitigar impactos na execução e entrega do produto.

Este documento deve ser revisitado e atualizado ao final de cada sprint.

---

## 1. Detalhamento dos riscos

### R01 - Desistência ou Abandono de Membro da Equipe

- **Natureza:** Equipe
- **Identificação:** Um ou mais integrantes podem trancar a matrícula ou deixar o projeto durante o semestre.
- **Descrição:** A perda de um membro da equipe reduz a força de trabalho e pode causar a perda de conhecimento específico de partes do sistema.
- **Causa:** Dificuldade de adaptação, sobrecarga acadêmica com outras disciplinas ou problemas pessoais.
- **Consequência / Impacto Esperado:** Sobrecarga dos membros restantes, atraso nas entregas das sprints e necessidade de redistribuição abrupta de tarefas.
- **Probabilidade:** Média (2)
- **Impacto:** Alto (3)
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Adotar pareamentos eventuais e garantir que todo o código e infraestrutura estejam devidamente documentados no repositório. Nenhuma parte do sistema deve ser de conhecimento exclusivo de apenas uma pessoa.
- **Responsável pelo Acompanhamento:** Scrum Master (Sprint 1: `Eduardo Cardoso`, Sprint 2: `Damares Gaia`, Sprint 3: `Talles Souza`)

### R02 - Falhas de Regressão por Ausência de Testes e CI Quebrado

- **Natureza:** Qualidade / Processo
- **Identificação:** Inserção de bugs no código principal por falta de validações automatizadas em Pull Requests.
- **Descrição:** Sem um pipeline de CI rigoroso, a equipe pode integrar código com erros de sintaxe ou que quebram funcionalidades.
- **Causa:** Pressa na entrega e falta de cultura de testes automatizados.
- **Consequência / Impacto Esperado:** Instabilidade do sistema e tempo desperdiçado corrigindo problemas na branch `main`.
- **Probabilidade:** Alta (3)
- **Impacto:** Médio (2)
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Configurar o CI mínimo com bloqueio de merge em caso de falha de lint ou build. Exigir revisão de código obrigatória.
- **Responsável pelo Acompanhamento:** Responsável por Qualidade (`Talles Souza`)

### R03 - Atraso no Cronograma de Integração das APIs de Backend e Frontend

- **Natureza:** Prazo
- **Identificação:** Atraso na disponibilização dos endpoints da API REST, gerando dependência técnica e bloqueando o fluxo de desenvolvimento das interfaces de usuário.
- **Descrição:** O tempo de implementação do backend pode ultrapassar as estimativas iniciais, criando um efeito cascata que impede a equipe de frontend de integrar, consumir os dados e testar as telas dentro do prazo planejado para a sprint.
- **Causa:** Subestimativa de esforço na modelagem relacional do banco de dados, tratamento de exceções não previstas ou alta complexidade na implementação das regras de negócio nas rotas.
- **Consequência / Impacto Esperado:** Sobrecarga da equipe nas vésperas da entrega, integração feita às pressas, aumento considerável no risco de bugs funcionais e potencial comprometimento da validação final do MVP.
- **Probabilidade:** Alta (3)
- **Impacto:** Alto (3)
- **Prioridade:** Crítica (9)
- **Estratégia de Mitigação:** Priorizar o desenvolvimento das rotas críticas e utilizar mocks de API no frontend enquanto o backend real não estiver concluído.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

### R04 - Crescimento Não Planejado do Escopo (Scope Creep)

- **Natureza:** Escopo
- **Identificação:** Inclusão contínua de novas funcionalidades no backlog sem avaliação criteriosa de impacto no prazo.
- **Descrição:** Novas funcionalidades podem ser sugeridas ao longo do semestre, inflando o escopo além da capacidade real de entrega da equipe e desviando o foco dos requisitos essenciais estabelecidos inicialmente.
- **Causa:** Entusiasmo natural com a evolução do produto e tentativa de absorver todo tipo de feedback, levando a decisões de adição de escopo sem a devida análise de custo e tempo.
- **Consequência / Impacto Esperado:** MVP incompleto ao final do semestre, com estouro do prazo e o risco de comprometer as funcionalidades centrais e essenciais do sistema em favor de itens secundários adicionados no meio do caminho.
- **Probabilidade:** Média (2)
- **Impacto:** Médio (2)
- **Prioridade:** Média (4)
- **Estratégia de Mitigação:** O escopo principal deve ser congelado, utilizando os critérios definidos na documentação do MVP como filtro rigoroso. Ideias não essenciais devem ser obrigatoriamente movidas para um backlog de futuro.
- **Responsável pelo Acompanhamento:** Scrum Master (Sprint 1: `Eduardo Cardoso`, Sprint 2: `Damares Gaia`, Sprint 3: `Talles Souza`) / Product Owner (`Adriano Luiz de Souza`)

### R05 - Divergência de Ambientes e Instabilidade na Orquestração (Docker)

- **Natureza:** Tecnologia
- **Identificação:** O sistema apresenta comportamentos inesperados ou falhas de execução dependendo da máquina do desenvolvedor ou do ambiente de deploy.
- **Descrição:** A falta de alinhamento na configuração da infraestrutura local pode fazer com que serviços (como o banco de dados SQL ou a API) rodem perfeitamente para um membro, mas falhem para outro ou no servidor.
- **Causa:** Uso de versões diferentes de dependências, falta de padronização nas imagens dos contêineres e variáveis de ambiente não versionadas ou mal documentadas.
- **Consequência / Impacto Esperado:** Perda severa de tempo em sessões de depuração, bloqueio de tarefas de integração e atrasos no pipeline de entrega.
- **Probabilidade:** Alta (3)
- **Impacto:** Médio (2)
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Padronizar rigorosamente os arquivos `Dockerfile` e `docker-compose.yml`, exigindo que todo o desenvolvimento de rotas e testes seja executado exclusivamente via contêineres padronizados.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

---

## 2. Análise e Priorização dos Riscos

### 2.1 Critérios Adotados

A classificação dos riscos utiliza uma matriz baseada em duas dimensões:

- **Probabilidade (P):** Baixa (1), Média (2), Alta (3)
- **Impacto (I):** Baixo (1), Médio (2), Alto (3)

**Fórmula de Exposição ao Risco (Prioridade):** `P × I`

- **Baixa:** 1 a 2
- **Média:** 3 a 4
- **Alta:** 6
- **Crítica:** 9

### Matriz de Prioridade (Probabilidade × Impacto)

|                     | Impacto Baixo (1) | Impacto Médio (2) | Impacto Alto (3) |
| :------------------ | :---------------: | :---------------: | :--------------: |
| **Prob. Alta (3)**  |     Média (3)     |     Alta (6)      |   Crítica (9)    |
| **Prob. Média (2)** |     Baixa (2)     |     Média (4)     |     Alta (6)     |
| **Prob. Baixa (1)** |     Baixa (1)     |     Baixa (2)     |    Média (3)     |

### 2.2 Matriz de Riscos

Matriz original da Entrega 4. Para o status atualizado ao final da
Sprint 2, incluindo os riscos R06 a R08, ver [seção 4](#4-atualização-intermediária-sprint-2-em-andamento-entrega-6-até-0110).
Para a revisão de R01 e R04 ao final da Sprint 3, ver [seção 5](#5-revisão-ao-final-da-sprint-3-entrega-7-0210-a-2210).

| ID      | Risco                             | Natureza   | Probabilidade |  Impacto  |   Prioridade    |
| :------ | :-------------------------------- | :--------- | :-----------: | :-------: | :-------------: |
| **R03** | Atraso no cronograma das APIs     | Prazo      |   Alta (3)    | Alto (3)  | **Crítica (9)** |
| **R01** | Desistência de membro da equipe   | Equipe     |   Média (2)   | Alto (3)  |  **Alta (6)**   |
| **R02** | Falhas por ausência de testes/CI  | Qualidade  |   Alta (3)    | Médio (2) |  **Alta (6)**   |
| **R05** | Divergência de ambientes (Docker) | Tecnologia |   Alta (3)    | Médio (2) |  **Alta (6)**   |
| **R04** | Crescimento do escopo             | Escopo     |   Média (2)   | Médio (2) |  **Média (4)**  |

### 2.3 Justificativa das Prioridades

- O risco **R03** é crítico pois paralisa o avanço geral do sistema, criando um gargalo onde o frontend fica totalmente bloqueado aguardando o backend, o que inviabiliza a validação do MVP.
- O risco **R01** recebe prioridade alta porque a perda de um integrante reduz drasticamente a capacidade produtiva, o que pode inviabilizar a entrega de todas as funcionalidades dentro do cronograma da disciplina.
- O risco **R02** possui prioridade alta pois a quebra frequente do código na branch principal afeta a estabilidade do produto e gera desperdício de tempo com retrabalho e correção de bugs evitáveis.
- O risco **R05** também é de alta prioridade pois problemas de infraestrutura local e orquestração consomem tempo útil de desenvolvimento e atrasam o pipeline de integração de toda a equipe.
- O risco **R04** recebe prioridade média porque, apesar de ameaçar o prazo, é um risco puramente gerencial e controlável, que pode ser contido imediatamente pela aplicação rigorosa do filtro de backlog e congelamento do MVP.

---

## 3. Plano de Resposta aos Riscos

### 3.1 Ações Preventivas

- **R01:** Compartilhar o conhecimento técnico por meio de programação em pares e documentação constante.
- **R02:** Configurar a Integração Contínua (CI) com bloqueios antes de escalar o volume de código.
- **R03:** Estabelecer o contrato de dados no início do ciclo.
- **R04:** Congelar o escopo principal do MVP e submeter qualquer sugestão de nova funcionalidade a um filtro rigoroso, movendo ideias não essenciais para um backlog futuro.
- **R05:** Versionar todas as configurações de infraestrutura e dependências no repositório, garantindo que o setup inicial seja feito com um único comando de orquestração.

### 3.2 Ações caso o risco ocorra

- **Se R01 ocorrer:** Repriorizar o backlog, cortando funcionalidades não essenciais para adequar o escopo à nova capacidade da equipe.
- **Se R02 ocorrer:** Paralisar a aprovação de novos Pull Requests. A equipe deve realizar a correção de todos os erros/bugs adicionados na branch _main_, priorizando a estabilidade do sistema antes de continuar o desenvolvimento de novas funcionalidades.
- **Se R03 ocorrer:** O frontend passa a consumir dados estáticos (mocks) temporariamente para não travar a evolução das telas.
- **Se R04 ocorrer:** Realizar imediatamente uma reunião de repriorização de emergência. Congelar o desenvolvimento de qualquer funcionalidade secundária, mesmo que já esteja em andamento, e redirecionar 100% da força produtiva da equipe exclusivamente para finalizar os requisitos essenciais do MVP aprovados no planejamento inicial.
- **Se R05 ocorrer:** O desenvolvedor afetado deve interromper a codificação e recriar seu ambiente do zero a partir de uma imagem limpa. Caso o erro persista, o responsável por DevOps deve priorizar o pareamento com este membro para corrigir o script de orquestração antes que afete o restante da equipe.

### 3.3 Acompanhamento da Evolução

O monitoramento dos riscos será contínuo, utilizando os ritos ágeis do projeto para garantir que nenhuma ameaça passe despercebida:

- **Daily Scrum:** Acompanhamento diário para identificar rapidamente bloqueios técnicos ou processuais, como divergências de ambiente (R05), quebras no pipeline de integração (R02) ou dificuldades de comunicação e entrega nas APIs (R03).
- **Sprint Planning:** Avaliação da disponibilidade e capacidade real da equipe para a sprint (monitorando o R01) e aplicação rigorosa do filtro de requisitos para barrar o crescimento do escopo (R04).
- **Sprint Retrospective:** Revisão formal e atualização deste documento de Riscos. A equipe debaterá a eficácia das estratégias de mitigação, reavaliará a probabilidade e o impacto dos riscos ativos e registrará novos riscos que possam ter surgido durante a iteração.

---

## 4. Atualização Intermediária Sprint 2 em Andamento (Entrega 6, até 01/10)

Esta seção atualiza o estado dos riscos registrados na Entrega 4 com base
na execução da Sprint 2 **até o momento** (23/09/2026) a Sprint 2 segue
em andamento até 01/10/2026, com as sub-issues 11.1 a 11.9 ainda em
implementação. Esta é uma revisão intermediária, não o fechamento da
sprint; o estado aqui registrado pode mudar até 01/10, quando esta seção
deve ser revisada novamente como parte do fechamento formal (Doc-9,
`sprint-2.md`).

### 4.1 Status Atualizado dos Riscos Existentes

| ID      | Risco                                                             | Status na Sprint 2            | Observação                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| :------ | :---------------------------------------------------------------- | :---------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **R01** | Desistência ou abandono de membro da equipe                       | Concretizado (parcialmente)   | Não houve desistência total, mas um integrante teve baixa participação pontual na sprint, o que **chegou a impactar prazo/redistribuição de pelo menos uma issue**. A mitigação preventiva (documentação constante, sem conhecimento exclusivo) evitou um cenário pior, mas a ação de resposta ("Se R01 ocorrer: repriorizar backlog") deveria ter sido formalmente acionada e não foi registrada como tal no momento.                                                    |
| **R02** | Falhas de regressão por ausência de testes e CI quebrado          | Ativo, parcialmente mitigado  | Houve quebras de CI durante a sprint, corrigidas rapidamente a mitigação preventiva (bloqueio de merge com CI vermelho) funcionou como rede de segurança, mas o risco de quebra recorrente continua presente. Some-se a isso a divergência já existente no `GlobalExceptionHandler` (issue #90), que mostra lacunas de cobertura em código anterior à Sprint 2.                                                                                                           |
| **R03** | Atraso no cronograma de integração das APIs de backend e frontend | Mitigado até o momento        | Ação preventiva aplicada com sucesso: o contrato de dados da US04 foi estabelecido no início do ciclo (`docs/contrato-dados-us04.md`), permitindo que o desenvolvimento de frontend (11.5–11.7) inicie em paralelo ao backend, sem esperar os endpoints reais estarem prontos. A sprint ainda está em andamento; este status será confirmado no fechamento.                                                                                                               |
| **R04** | Crescimento não planejado do escopo (scope creep)                 | Mitigado até o momento        | O escopo da US03 foi deliberadamente reduzido a uma "fatia essencial" (indicador de vagas + botão de solicitação), adiando filtros avançados. Melhorias identificadas durante a elaboração do contrato de dados (bloqueio de e-mail cruzado, cancelamento, paginação) foram conscientemente registradas como fora de escopo em vez de incorporadas à sprint.                                                                                                              |
| **R05** | Divergência de ambientes e instabilidade na orquestração (Docker) | Concretizado gap na mitigação | Um integrante teve falha ao subir o backend via Docker em ambiente Linux, causada por permissão de arquivo. **O problema foi contornado localmente, mas a correção não foi commitada ao repositório** ou seja, a causa raiz permanece sem correção documentada e deve se repetir para qualquer outro integrante Linux que configure o ambiente do zero. A estratégia de mitigação preventiva (padronizar Dockerfile/compose) existe, mas não cobria este caso específico. |

### 4.2 Riscos Novos Identificados na Sprint 2

### R06 - Ausência de Autenticação Expõe Endpoints da US04 a Uso Indevido

- **Natureza:** Segurança
- **Identificação:** Como a US01 (autenticação/JWT) foi postergada, todos os endpoints de `/api/v1/solicitacoes` são públicos qualquer cliente pode chamar `PATCH /solicitacoes/{id}/status` como se fosse o orientador dono da solicitação, sem qualquer verificação de identidade.
- **Descrição:** Os identificadores de solicitação (`id`) são sequenciais e enumeráveis, o que facilita a um cliente externo descobrir e manipular solicitações de outros orientadores por tentativa direta, sem necessidade de autenticação ou autorização.
- **Causa:** Decisão consciente de simplificação de escopo (ADR-0007), priorizando a entrega do fluxo funcional de match em detrimento da segurança de acesso, já que a US01 não fazia parte do MVP desta sprint.
- **Consequência / Impacto Esperado:** Um agente mal-intencionado poderia aceitar, recusar ou consultar solicitações de orientação que não lhe pertencem, comprometendo a integridade dos dados de vagas e a confiabilidade do fluxo de match caso o sistema seja exposto fora de um ambiente controlado de demonstração/avaliação.
- **Probabilidade:** Alta (3) a limitação é garantida, não hipotética, enquanto a US01 não for implementada.
- **Impacto:** Médio (2) mitigado pelo fato de o sistema ainda não estar em produção real com dados sensíveis; o impacto seria alto em um ambiente de uso real.
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Manter o sistema restrito a ambiente de desenvolvimento/avaliação até a implementação da US01. Documentar a limitação de forma explícita e visível no `README.md` e na release `v0.2.0`, para que ninguém trate o sistema como pronto para uso real nesse estado. Priorizar a US01 no backlog da próxima sprint.
- **Responsável pelo Acompanhamento:** Scrum Master (Sprint 1: `Eduardo Cardoso`, Sprint 2: `Damares Gaia`, Sprint 3: `Talles Souza`)

### R07 - Condição de Corrida no Aceite da Última Vaga Disponível

- **Natureza:** Tecnologia / Qualidade
- **Identificação:** Duas requisições `PATCH .../status` (`ACEITA`) concorrentes para o mesmo orientador, no instante em que ele possui apenas 1 vaga disponível, podem ambas passar pela validação de `vagasDisponiveis > 0` antes que o decremento da primeira seja persistido, resultando em `vagasDisponiveis` negativo ou em duas solicitações aceitas para uma única vaga.
- **Descrição:** O `AtualizadorVagasObserver` foi desenhado para rodar de forma síncrona na mesma transação do `PATCH` (decisão registrada no contrato de dados, seção 8.3), o que reduz a janela de corrida, mas não a elimina sem um mecanismo explícito de controle de concorrência (ex.: lock otimista/versionamento na entidade `PerfilOrientador`).
- **Causa:** Ausência de estratégia de controle de concorrência (locking otimista ou pessimista) na primeira versão da implementação, por não ter sido um requisito explícito da issue original antes da revisão do contrato de dados.
- **Consequência / Impacto Esperado:** Inconsistência de dados (vagas negativas) ou aceite de mais solicitações do que a capacidade real do orientador, comprometendo a confiabilidade da funcionalidade core do MVP (o "match").
- **Probabilidade:** Baixa (1) exige simultaneidade real de requisições no exato instante da última vaga; cenário raro no volume de uso esperado para o projeto acadêmico.
- **Impacto:** Alto (3) quando ocorre, corrompe diretamente a regra de negócio central da US04.
- **Prioridade:** Média (3)
- **Estratégia de Mitigação:** Cobrir o cenário com teste de concorrência explícito (issue 11.8) simulando duas requisições simultâneas sobre a última vaga. Caso o teste demonstre falha, avaliar a adoção de lock otimista (`@Version` em `PerfilOrientador`) como ação da próxima sprint.
- **Responsável pelo Acompanhamento:** Responsável por Qualidade (`Talles Souza`)

### R08 - Possível Mudança de Direção do Produto (Pivot) Sugerida pelo Professor

- **Natureza:** Escopo / Estratégico
- **Identificação:** O professor orientador sugeriu que a equipe reavalie a forma/abordagem geral do sistema (mantendo o domínio de gestão de TCC, mas potencialmente alterando a estrutura do produto), sem que a equipe tenha ainda decidido se e como essa mudança será incorporada.
- **Descrição:** Diferente do R04 (crescimento não planejado de escopo, isto é, acúmulo de funcionalidades sobre a direção já definida), este risco trata de uma possível mudança na própria direção do produto uma decisão estratégica que pode alterar premissas já assumidas no MVP, no modelo de domínio (UML) e nas User Stories já implementadas ou planejadas (US02, US03, US04).
- **Causa:** Feedback do professor orientador sobre a adequação da abordagem atual do sistema ao contexto da disciplina/avaliação.
- **Consequência / Impacto Esperado:** Caso confirmado, pode exigir a revisão do MVP, do modelo de domínio e do backlog já priorizado; o grau de reaproveitamento do trabalho já entregue (US02, fatia da US03, US04, ADRs, contrato de dados) ainda é incerto e só poderá ser avaliado quando o novo rumo for definido com mais detalhe.
- **Probabilidade:** Média (2) ainda não é uma decisão tomada; depende de avaliação da equipe na virada de sprint.
- **Impacto:** Alto (3) uma mudança de direção do produto pode invalidar parte do trabalho de modelagem e implementação já feito, e força a equipe a reabrir decisões que pareciam fechadas (MVP, ADRs, contrato de dados).
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Reservar um espaço explícito na próxima Sprint Planning para avaliar a sugestão do professor antes de iniciar qualquer nova implementação, evitando decidir o novo rumo "no meio" de uma sprint em andamento. Ao avaliar as opções, mapear explicitamente o que de US02/US03/US04/UML/contrato de dados seria reaproveitável em cada alternativa, para que a decisão considere custo de retrabalho, não só adequação conceitual. Não iniciar a Sprint 3 sem essa decisão fechada, para evitar implementar sobre uma base que pode ser descartada.
- **Responsável pelo Acompanhamento:** Scrum Master (Sprint 1: `Eduardo Cardoso`, Sprint 2: `Damares Gaia`, Sprint 3: `Talles Souza`) / Product Owner (`Adriano Luiz de Souza`)

### Matriz de Riscos Atualizada (Entrega 6, situação em 23/09/2026)

| ID      | Risco                                          | Natureza             | Probabilidade |  Impacto  | Prioridade  | Status                        |
| :------ | :--------------------------------------------- | :------------------- | :-----------: | :-------: | :---------: | :---------------------------- |
| **R03** | Atraso no cronograma das APIs                  | Prazo                |   Alta (3)    | Alto (3)  | Crítica (9) | Mitigado até o momento        |
| **R06** | Ausência de autenticação nos endpoints da US04 | Segurança            |   Alta (3)    | Médio (2) |  Alta (6)   | Novo ativo                    |
| **R08** | Possível mudança de direção do produto (pivot) | Escopo/Estratégico   |   Média (2)   | Alto (3)  |  Alta (6)   | Novo em avaliação             |
| **R01** | Desistência de membro da equipe                | Equipe               |   Média (2)   | Alto (3)  |  Alta (6)   | Concretizado (parcial)        |
| **R02** | Falhas por ausência de testes/CI               | Qualidade            |   Alta (3)    | Médio (2) |  Alta (6)   | Ativo                         |
| **R05** | Divergência de ambientes (Docker)              | Tecnologia           |   Alta (3)    | Médio (2) |  Alta (6)   | Concretizado gap na mitigação |
| **R04** | Crescimento do escopo                          | Escopo               |   Média (2)   | Médio (2) |  Média (4)  | Mitigado até o momento        |
| **R07** | Condição de corrida no aceite da última vaga   | Tecnologia/Qualidade |   Baixa (1)   | Alto (3)  |  Média (3)  | Novo ativo                    |

### 4.3 Ações de Mitigação para o Restante da Sprint 2 e para a Sprint 3

**Ainda dentro da Sprint 2 (até 01/10):**

- **R05:** Commitar o quanto antes a correção de permissão de arquivo do ambiente Linux (Dockerfile/entrypoint/script de setup) que resolveu o problema localmente enquanto não for commitada, o risco permanece ativo para qualquer outro integrante Linux que precise configurar o ambiente do zero durante o restante da sprint.
- **R02:** Auditar handlers de erro existentes de outras funcionalidades (não só o já corrigido na #90) em busca de divergências similares entre contrato documentado e comportamento real, antes de novas issues (11.1–11.9) escalarem sobre eles.
- **R07:** Executar o teste de concorrência da issue 11.8 como critério de bloqueio para o merge da 11.3, ainda dentro desta sprint; se falhar, tratar a introdução de lock otimista como bloqueante antes do fechamento da Entrega 6.
- **R01:** Como a ação de resposta prevista ("repriorizar backlog") não chegou a ser formalmente acionada apesar do impacto real em prazo, formalizar isso ainda durante a sprint: identificar a issue redistribuída, registrar o impacto de prazo real, e reforçar o pareamento preventivo do integrante afetado nas tarefas críticas restantes (11.3, 11.8).

**Para a Sprint 3, a partir de 02/10:**

- **R06:** Priorizar a US01 (autenticação/JWT) no planejamento da próxima sprint; até lá, reforçar no README e na release `v0.2.0` que o sistema não deve ser exposto fora de ambiente de avaliação.
- **R08:** Reservar um momento dedicado na próxima Sprint Planning para avaliar a sugestão do professor com calma, mapeando o reaproveitamento possível de cada alternativa antes de comprometer a Sprint 3 a um novo rumo. Não decidir o pivot sob pressão do fechamento da Entrega 6.
- **Geral:** Manter a prática de contrato-first (R03) e de escopo revisado explicitamente por sprint (R04) nas próximas sprints, já que ambas se mostraram eficazes até o momento nesta iteração.

---

## 5. Revisão ao Final da Sprint 3 (Entrega 7, 02/10 a 22/10)

Esta seção registra a reavaliação dos riscos da Sprint 3.

### 5.1 R01 - Desistência ou Abandono de Membro da Equipe

- **Status na Sprint 3:** Concretizado (abandono de fato, sem desligamento formal).
- **O que ocorreu:** Diferente da Sprint 2, em que houve baixa participação
  pontual, nesta sprint um integrante **não realizou nenhuma atividade**.
  Ele não deixou formalmente a equipe nem trancou a disciplina, mas, na
  prática, a equipe trabalhou com um membro a menos durante toda a
  iteração. As tarefas que caberiam a ele foram absorvidas pelos demais
  integrantes.
- **Reavaliação:**
  - **Probabilidade:** Média (2) → **Alta (3)**. O risco se concretizou em
    duas sprints seguidas, com agravamento da Sprint 2 (participação
    reduzida) para a Sprint 3 (nenhuma participação). Não há indício de
    que a situação se reverta sozinha.
  - **Impacto:** Alto (3), mantido. A perda de um integrante reduz
    diretamente a capacidade da equipe (M-06) e sobrecarrega os demais
    em uma sprint que já tinha o escopo ampliado.
  - **Prioridade:** Alta (6) → **Crítica (9)**.
- **Eficácia da mitigação:** A mitigação preventiva (documentação no
  repositório, nenhum conhecimento exclusivo de uma só pessoa) funcionou:
  as tarefas puderam ser redistribuídas sem perda de conhecimento. A
  falha está na detecção tardia. Como o integrante não saiu formalmente,
  a ausência só ficou evidente com a sprint em andamento, e não houve
  um gatilho claro para acionar a ação de resposta.
- **Ações para a próxima sprint:**
  - Planejar a Sprint 4 com a capacidade real da equipe, sem contar com
    o integrante ausente até que ele volte a contribuir, e repriorizar o
    backlog conforme a ação de resposta prevista para o R01.
  - Definir um gatilho objetivo para o risco: um integrante sem
    atividade registrada (commit, PR, revisão ou issue movida) por mais
    de uma semana deve ser sinalizado na Daily e tratado pelo Scrum
    Master.
  - Conversar diretamente com o integrante para entender a situação e
    combinar se ele continua no projeto e com quais tarefas.
  - Comunicar a situação ao professor da disciplina, já que afeta a
    capacidade da equipe e a distribuição das entregas.
- **Responsável pelo Acompanhamento:** Scrum Master da sprint (`Talles Souza da Cruz`)

### 5.2 R04 - Crescimento Não Planejado do Escopo (Scope Creep)

- **Status na Sprint 3:** Concretizado (aumento de escopo aprovado pela equipe).
- **O que ocorreu:** O ajuste de escopo da Sprint 3, feito em resposta ao
  R08 (seção 3 de [`sprint-3.md`](entregas/sprint-3.md)), aumentou o
  tamanho do MVP:
  - **Autenticação com Keycloak (US01):** substituiu o login próprio
    previsto no baseline e adicionou um novo contêiner ao
    `docker-compose`, a configuração versionada do _realm_ e uma nova ADR.
    A estimativa de 5 SP foi feita para o desenho anterior.
  - **Nova entidade `Turma_TCC`:** a permissão de gestão do calendário
    passou a ser verificada no banco (professor responsável pela turma
    do semestre), o que afeta a US01 e a US05.
  - **Agendamento de bancas de defesa:** voltou ao backlog do MVP como
    nova história, ainda sem estimativa.
  - **US05 entrou na sprint sem estimativa**, com seis cenários de
    aceitação.
  - Houve também reduções (perfil `Coordenador` e armazenamento de
    arquivos removidos), mas elas não compensam as adições.
- **Reavaliação:**
  - **Probabilidade:** Média (2) → **Alta (3)**. O escopo cresceu de
    fato, e o total de Story Points do MVP (M-05) ainda precisa ser
    recalculado, então novos aumentos ainda podem aparecer na estimativa
    das histórias restantes.
  - **Impacto:** Médio (2), mantido. As entregas das Sprints 1 e 2 foram
    preservadas e as bancas ficaram fora da Sprint 3. O impacto deve ser
    reavaliado para Alto (3) se a recontagem do M-05 mostrar que o MVP
    não cabe nas sprints restantes com a capacidade atual (ver R01).
  - **Prioridade:** Média (4) → **Alta (6)**.
- **Eficácia da mitigação:** A contenção funcionou parcialmente. O aumento
  foi uma decisão consciente da equipe, registrada no planejamento, e o
  agendamento de bancas foi mantido fora da sprint. Porém, as histórias
  entraram na sprint sem reestimativa (US01) ou sem estimativa (US05),
  o que dificulta medir o crescimento real.
- **Ações para a próxima sprint:**
  - Estimar a US05 e a história de agendamento de bancas e recalcular o
    denominador do M-05 com o MVP completo.
  - Confrontar o novo total do MVP com a velocidade observada (M-03) e a
    capacidade real da equipe, já considerando o R01. Se não couber,
    cortar ou adiar itens secundários, conforme a ação de resposta do R04.
  - Não iniciar nenhuma história sem estimativa na Sprint Planning.
- **Responsável pelo Acompanhamento:** Scrum Master (`Talles Souza da Cruz`) / Product Owner (`Adriano Luiz de Souza`)

### 5.3 Status Atualizado dos Riscos Existentes (QA)

| ID      | Risco                                                    | Status na Sprint 3 | Observação                                                                                                                                                                                                                                                                                                                                                                                   |
| :------ | :------------------------------------------------------- | :----------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **R02** | Falhas de regressão por ausência de testes e CI quebrado | Mitigado           | A equipe consolidou uma forte cultura de testes automatizados nesta sprint. No frontend, foram implementadas extensas suítes com Vitest e React Testing Library (ex: `SolicitacaoForm.test.tsx`, `PainelSolicitacoes.test.tsx`). No backend, testes unitários robustos com JUnit e Mockito para as regras de negócio e serviços. O risco de regressão silenciosa foi drasticamente reduzido. |
| **R07** | Condição de corrida no aceite da última vaga             | Mitigado           | Bloqueio otimista (`@Version` / lock otimista) na entidade `PerfilOrientador` foi validado com sucesso através de testes de concorrência, evitando definitivamente a inconsistência de dados (vagas negativas) identificada anteriormente.                                                                                                                                                   |

### 5.4 Riscos Novos Identificados na Sprint 3

### R09 - Complexidade e Instabilidade na Integração com Keycloak (Autenticação)

- **Natureza:** Segurança / Tecnologia
- **Identificação:** A introdução do Keycloak como provedor de identidade adiciona sobrecarga na infraestrutura local e no tratamento assíncrono de sessões no frontend.
- **Descrição:** A gestão de tokens JWT (access/refresh) e a proteção de rotas (`ProtectedRoute.tsx`) exigem sincronização rigorosa. Falhas na renovação do token (`authService.ts`) ou instabilidade no contêiner do Keycloak (`docker-compose.yml`) podem bloquear o desenvolvimento e teste das rotas protegidas por toda a equipe.
- **Causa:** Adição de um novo componente arquitetural complexo (Identity and Access Management) para suprir a funcionalidade de autenticação (US01) introduzida nesta sprint.
- **Consequência / Impacto Esperado:** Erros frequentes de HTTP 401 (Não Autorizado) para usuários ativos, bloqueio do ambiente local de desenvolvedores e atraso nas entregas devido ao tempo gasto depurando permissões e escopos.
- **Probabilidade:** Alta (3)
- **Impacto:** Alto (3)
- **Prioridade:** Crítica (9)
- **Estratégia de Mitigação:** Manter os scripts de orquestração do Docker e o artefato `realm-import.json` estritamente sincronizados no repositório. Criar testes E2E automatizados específicos para o ciclo de vida da autenticação e exigir que todo erro de login gere logs explícitos para facilitar a depuração.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`), QA (sprint 1 e 2: `Talles Souza`; sprint 3: `Eduardo Cardoso`) e Scrum Master (`Talles Souza`)

### 5.5 Matriz Atualizada dos Riscos Revisados (Entrega 7)

| ID      | Risco                                 | Natureza   |  Probabilidade   |  Impacto  |     Prioridade      | Status                      |
| :------ | :------------------------------------ | :--------- | :--------------: | :-------: | :-----------------: | :-------------------------- |
| **R01** | Desistência de membro da equipe       | Equipe     | Média → Alta (3) | Alto (3)  | Alta → Crítica (9)  | Concretizado (abandono)     |
| **R04** | Crescimento do escopo                 | Escopo     | Média → Alta (3) | Médio (2) |  Média → Alta (6)   | Concretizado (escopo maior) |
| **R03** | Atraso de integração backend/frontend | Prazo      | Alta → Baixa (1) | Alto (3)  | Crítica → Média (3) | Mitigado                    |
| **R05** | Divergência de ambientes (Docker)     | Tecnologia | Alta → Média (2) | Médio (2) |  Alta → Média (4)   | Mitigado, com atenção       |

Os dois riscos se reforçam: a equipe ficou com menos capacidade (R01) na
mesma sprint em que o escopo do MVP aumentou (R04). Por isso, a Sprint
Planning da Sprint 4 deve tratá-los juntos, ajustando o escopo à
capacidade real da equipe.

### 5.6 R03 - Atraso no Cronograma de Integração das APIs de Backend e Frontend

- **Status na Sprint 3:** Mitigado.
- **O que ocorreu:** A estratégia de definir os contratos de dados antes da implementação continuou sendo utilizada durante a Sprint 3. Os contratos das funcionalidades de autenticação e calendário foram considerados no planejamento inicial, permitindo que a equipe trabalhasse de forma mais coordenada. A implementação e integração das funcionalidades da sprint ocorreram sem um bloqueio generalizado do frontend pela indisponibilidade das APIs.
- **Reavaliação:**
  - **Probabilidade:** Alta (3) → **Baixa (1)**. A prática de contrato-first adotada nas sprints anteriores mostrou-se eficaz para reduzir dependências entre frontend e backend.
  - **Impacto:** Alto (3), mantido. Caso ocorra, um atraso significativo na API ainda pode bloquear telas e testes do frontend e comprometer o cronograma da sprint.
  - **Prioridade:** Crítica (9) → **Média (3)**.
- **Eficácia da mitigação:** A definição antecipada dos contratos de dados mostrou-se eficaz novamente. A equipe conseguiu desenvolver e integrar funcionalidades sem depender exclusivamente da conclusão prévia de toda a implementação do backend.
- **Ações para a próxima sprint:**
  - Continuar definindo ou revisando os contratos de API antes do início da implementação das telas dependentes.
  - Manter mocks ou dados estáticos disponíveis quando uma API ainda estiver em desenvolvimento.
  - Registrar alterações de contrato por meio de documentação e revisão nos Pull Requests.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

### 5.7 R05 - Divergência de Ambientes e Instabilidade na Orquestração (Docker)

- **Status na Sprint 3:** Mitigado, com atenção.
- **O que ocorreu:** O problema de permissão do _gradlew_ identificado na Sprint 2 em ambiente Linux foi corrigido e a correção foi integrada ao repositório. Na Sprint 3, a infraestrutura Docker foi ampliada com a inclusão do Keycloak como novo serviço, aumentando a quantidade de componentes e dependências necessárias para executar o sistema localmente.
- **Reavaliação:**
  - **Probabilidade:** Alta (3) → **Média (2)**. A padronização do ambiente e a correção do problema identificado na Sprint 2 reduziram a probabilidade de divergências, mas a adição do Keycloak e de novas dependências mantém a possibilidade de problemas de configuração entre ambientes.
  - **Impacto:** Médio (2), mantido. Uma falha na orquestração pode impedir o desenvolvimento ou os testes locais e consumir tempo da equipe, mas a infraestrutura está versionada e pode ser reproduzida a partir dos arquivos do projeto.
  - **Prioridade:** Alta (6) → **Média (4)**.
- **Eficácia da mitigação:** A estratégia de versionar a infraestrutura mostrou-se eficaz. O problema de permissão identificado na Sprint 2 foi corrigido no repositório. Na Sprint 3, o `docker-compose.yml` passou a orquestrar também o Keycloak, com healthchecks e dependências entre os serviços, enquanto a configuração do realm foi versionada para permitir a reprodução do ambiente.
- **Ações para a próxima sprint:**
  - Manter `Dockerfile`, `docker-compose.yml`, `docker-compose.prod.yml` e arquivos de configuração necessários versionados.
  - Evitar configurações feitas exclusivamente dentro dos contêineres ou volumes locais dos desenvolvedores.
  - Validar o ambiente completo após alterações na infraestrutura.
  - Manter documentado o procedimento para subir o ambiente de desenvolvimento a partir de uma configuração limpa.
  - Diferenciar problemas gerais de orquestração Docker de problemas específicos da integração com Keycloak, mantendo estes últimos acompanhados pelo R09.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

---

## 6. Revisão ao Final da Sprint 4 (Entrega 8 / Release v0.4.0, 01/11 a 11/11/2026)

Esta seção consolida a revisão dos riscos do projeto no encerramento da Sprint 4 (marco de release `v0.4.0`).

### 6.1 Status Geral dos Riscos na Sprint 4 (Scrum Master / QA)

|   ID    | Risco                                                             | Natureza               |            Status na Sprint 4            | Observação / Evidência                                                                                                                                             | Responsável pelo Acompanhamento                                              |
| :-----: | :---------------------------------------------------------------- | :--------------------- | :--------------------------------------: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------- |
| **R01** | Desistência ou abandono de membro da equipe                       | Equipe                 |               **Mitigado**               | Todos os 5 integrantes contribuíram ativamente na Sprint 4; Marcus implementou telas de US06/US07 (PR #242) e trabalho foi fatiado em issues menores.              | Scrum Master (`Willian Ferreira`)                                            |
| **R02** | Falhas de regressão por ausência de testes e CI quebrado          | Qualidade / Processo   |               **Mitigado**               | 8 bugs pré-existentes corrigidos (#199–#206); suítes de integração US06/US07 adicionadas (PRs #246, #247) e CI `docker-build.yml` condicionado aos testes.         | QA (`Willian Ferreira`)                                                      |
| **R03** | Atraso no cronograma de integração das APIs Backend/Frontend      | Prazo                  |               **Mitigado**               | Contratos integrados em 04/11 (PR #234) e APIs entregues até 06/11, antes do previsto (PRs #236, #240 e #237).                                                     | Backend / QA (`Talles Souza`, `Willian Ferreira`)                            |
| **R04** | Crescimento não planejado do escopo (Scope Creep)                 | Escopo                 |               **Mitigado**               | Regras de US06/US07 congeladas no planejamento (`sprint-4.md`); US08 (#15) formalmente postergada para o backlog após absorção dos 8 bugs (#199–#206).             | Scrum Master (`Willian Ferreira`) / PO (`Adriano Luiz de Souza`)             |
| **R05** | Divergência de ambientes e instabilidade na orquestração (Docker) | Tecnologia             |               **Mitigado**               | Padronização integral via `docker-compose.yml`, `docker-compose.prod.yml`, `.env.example` e `docs/DEPLOY.md` (PR #243).                                            | DevOps / Infra (`Eduardo Cardoso`)                                           |
| **R06** | Ausência de autenticação expõe endpoints                          | Segurança              |        **Resolvido / Encerrado**         | Autenticação Keycloak ativa em `/api/**` desde a Sprint 3 e autorização granular corrigida na Sprint 4 (#199–#204, PRs #211–#214, #217, #224).                     | Arquiteta / Backend (`Damares Gaia`)                                         |
| **R07** | Condição de corrida / concorrência de entregas                    | Qualidade / Dados      |      **Mitigado (risco residual)**       | Lock otimista no aceite de vagas (`@Version`, migração `V8`, PR #241) e trava pessimista no registro de parecer (PR #237).                                         | Backend (`Talles Souza`)                                                     |
| **R08** | Mudança de direção do produto (Pivot)                             | Escopo / Estratégico   |         **Resolvido / Obsoleto**         | Direção consolidada na Sprint 3 (ADRs 0010, 0011 e 0013); nenhuma alteração estratégica ocorreu na Sprint 4.                                                       | Scrum Master (`Willian Ferreira`)                                            |
| **R09** | Complexidade e instabilidade na integração com Keycloak           | Segurança / Tecnologia |               **Mitigado**               | Expiração de sessão com HTTP 401 corrigida via fila e refresh automático de token (Issue #205 / PR #225); staging validado com `KEYCLOAK_PUBLIC_URL` (#220).       | DevOps / QA (`Eduardo Cardoso`, `Willian Ferreira`)                          |
| **R10** | Indisponibilidade do ambiente de staging compartilhado            | Infraestrutura         |               **Mitigado**               | Tunelamento seguro via Cloudflare Tunnels (`staging.sigtcc.online`), smoke tests em `docs/staging.md` e compose local (PR #244).                                   | DevOps / Infra (`Eduardo Cardoso`)                                           |
| **R11** | Submissão de links externos privados ou inacessíveis (US06/US07)  | Negócio / Usabilidade  |        **Mitigado parcialmente**         | Orientação de permissão de leitura na tela de submissão (PR #242) e reenvio após parecer de ajustes (PR #237).                                                     | Backend / Frontend / QA (`Talles Souza`, `Marcus Jhuan`, `Willian Ferreira`) |
| **R12** | Modificação posterior do trabalho submetido fora do prazo (US06)  | Integridade / Negócio  |                **Aceito**                | Data de envio (`enviadoEm`) e flag `atrasada` registradas pelo servidor (PR #236); o conteúdo externo continua editável (ADR-0013).                                | Arquiteta / Backend (`Damares Gaia`, `Talles Souza`)                         |
| **R13** | Atraso em cadeia entre Backend, Frontend e QA na US07             | Prazo / Processo       | **Concretizado parcialmente / Mitigado** | Backend US07 entregue em 06/11 (#210, #215), mas frontend integrado em 10/11 (PR #242) concentrou QA no último dia (PRs #246, #247 em 11/11), sem impedir entrega. | Scrum Master / QA / Dev (`Willian Ferreira`, `Talles Souza`, `Marcus Jhuan`) |

---

### 6.2 Detalhamento dos Riscos de Responsabilidade de DevOps / Infraestrutura (`Eduardo Cardoso`)

#### R05 — Divergência de Ambientes e Instabilidade na Orquestração (Docker)

- **Status na Sprint 4:** Mitigado.
- **O que ocorreu:** A infraestrutura de desenvolvimento e deploy foi totalmente estabilizada e padronizada ao longo da Sprint 4:
  - O arquivo `SIGTCC/docker-compose.yml` manteve-se consistente para execução local de todos os componentes (MySQL 8.0, Keycloak 24 e backend Spring Boot), sem ocorrência de divergências entre os integrantes.
  - Para a homologação e produção da Entrega 8, foi criado e testado o `SIGTCC/docker-compose.prod.yml`, orquestrando contêineres a partir de imagens multi-stage otimizadas, compiladas e publicadas automaticamente no GHCR pelo pipeline de CI (`docker-build.yml` - Issue #219 / PR #238).
  - Todas as variáveis de ambiente necessárias foram mapeadas, testadas e consolidadas no arquivo `.env.example`, eliminando lacunas de configuração entre ambientes locais e remotos.
  - Foi elaborado o guia oficial de deploy [`docs/DEPLOY.md`](DEPLOY.md) (Issue #221 / PR #243), documentando todo o passo a passo de inicialização a partir do zero, configuração de variáveis e procedimentos de rollback.
- **Reavaliação:**
  - **Probabilidade:** Média (2) → **Baixa (1)**. A padronização dos arquivos compose, o versionamento estrito de variáveis em `.env.example` e a publicação automatizada de imagens no registry do GitHub eliminaram a probabilidade de divergência de ambientes.
  - **Impacto:** Médio (2) → **Baixo (1)**. Qualquer novo ambiente pode ser provisionado ou recuperado de forma determinística e reproduzível com um único comando de orquestração a partir do repositório.
  - **Prioridade:** Média (4) → **Baixa (1)**.
- **Eficácia da mitigação:** A mitigação preventiva mostrou-se plenamente eficaz. Não houve relatos de inconsistência de portas, variáveis ou dependências entre as máquinas dos membros da equipe ou no servidor de staging durante todo o ciclo da Sprint 4.
- **Ações para a sustentação pós-MVP:**
  - Manter as tags de versão das imagens Docker no GHCR sincronizadas com as releases do GitHub (`v0.4.0`).
  - Executar os smoke tests documentados em `docs/DEPLOY.md` e `docs/staging.md` antes de qualquer atualização em ambiente de produção.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Eduardo Cardoso`)

#### R10 — Indisponibilidade ou Falha de Acesso ao Ambiente de Staging Compartilhado (Novo Risco de Infraestrutura)

- **Natureza:** Infraestrutura / Operações
- **Status na Sprint 4:** Mitigado.
- **O que ocorreu:** Conforme levantado no planejamento da Sprint 4 ([`docs/entregas/sprint-4.md`](entregas/sprint-4.md)), a introdução de um ambiente de staging compartilhado na nuvem pela primeira vez trazia o risco de indisponibilidade externa (quedas de rede, portas fechadas ou falhas de DNS) que poderiam travar a validação docente e a homologação integrada das histórias US06 e US07.
  - Como resposta, o DevOps implementou o tunelamento seguro via **Cloudflare Tunnels** apontando para os contêineres Docker da VPS (Issue #220 / PR #244), disponibilizando a aplicação em domínio seguro HTTPS (`staging.sigtcc.online`) sem necessidade de expor portas de roteador ou IPs públicos.
  - Foi elaborado o documento [`docs/staging.md`](staging.md) contendo o roteiro de _Smoke Test_ para verificação de integridade dos serviços após o deploy (MySQL, Keycloak, Backend API e Frontend SPA).
  - O ambiente local (`SIGTCC/docker-compose.yml`) permaneceu disponível como contingência imediata, garantindo que nenhum integrante ficasse bloqueado caso houvesse instabilidade de rede externa.
- **Reavaliação:**
  - **Probabilidade:** Média (2) → **Baixa (1)**. O tunnel do Cloudflare operou de forma estável e resiliente a reinicializações com reconexão automática.
  - **Impacto:** Alto (3) → **Baixo (1)**. A existência de procedimentos claros de smoke test e contingência via Docker Compose local neutralizou o impacto sobre a homologação.
  - **Prioridade:** Alta (6) → **Baixa (1)**.
- **Eficácia da mitigação:** O ambiente de staging permaneceu acessível e operacional durante os testes de integração e validação final da entrega.
- **Ações para a sustentação pós-MVP:**
  - Manter o monitoramento básico de saúde dos contêineres via `docker compose ps` e healthchecks configurados.
  - Realizar smoke test periódico dos serviços no endpoint público antes das bancas de avaliação.
- **Responsável pelo Acompanhamento:** DevOps/Infra (`Eduardo Cardoso`)

---

### 6.3 Detalhamento dos Riscos com Ocorrência, Ação Técnica ou Novos na Sprint 4

> **Nota sobre riscos sem ocorrência ou encerrados:** **R01** (todos os 5 integrantes ativos; Marcus no PR #242) não ocorreu na Sprint 4 e segue registrado nas tabelas `6.1` e `6.4`. **R06** (autenticação/autorização concluída com Keycloak e fixes `#199`–`#204`), **R08** (direção do produto consolidada desde a Sprint 3) e eventuais riscos de **storage local / upload binário** (substituídos por link externo HTTPS na [`ADR-0013`](adrs/ADR-0013.md)) foram encerrados como **resolvidos/obsoletos**.

#### R02 — Falhas de Regressão por Ausência de Testes e CI Quebrado

- **Status na Sprint 4:** Mitigado.
- **O que ocorreu e evidências:** Foram corrigidos 8 bugs pré-existentes (`#199`–`#206`, PRs `#211`–`#214`, `#217`, `#224`, `#225`, `#241`) e adicionados cenários de integração da US06 (`#228` / PR `#246`: `EntregaControllerIntegrationTest`, `SubmissaoEntrega.test.tsx`), US07 (`#229` / PR `#247`: `EntregaParecerIntegrationTest`, `AvaliacaoEntrega.test.tsx`), concorrência (`SolicitacaoConcorrenciaIntegrationTest`) e repositórios (`EntregaRepositoryTest`, `ParecerRepositoryTest`). O CI foi atualizado em `entrega-8` (`#219` / PR `#238`) com `docker-build.yml` condicionado aos testes.
- **Reavaliação e risco residual:** Probabilidade **Baixa (1)**, Impacto **Médio (2)**, Prioridade **Baixa (2)**. **Risco residual:** baixo, mantido sob controle pelos checks obrigatórios de CI.
- **Responsável pelo Acompanhamento:** QA (`Willian Ferreira`)

#### R03 — Atraso no Cronograma de Integração das APIs Backend/Frontend

- **Status na Sprint 4:** Mitigado.
- **O que ocorreu:** A equipe manteve a prática de definir os contratos de dados antes da implementação. Os contratos da US06 e da US07 foram integrados em 04/11 (Issue #209 / PR #234), com as roles e as respostas de erro detalhadas por endpoint. A partir deles, as APIs do backend foram entregues antes do previsto no cronograma da seção 3.3 do [`sprint-4.md`](entregas/sprint-4.md):
  - Submissão por link (Issue #216 / PR #236): integrada em 04/11, dentro da janela planejada (04/11 a 06/11).
  - Consulta das submissões (Issue #210 / PR #240): integrada em 06/11, antes do fim previsto (09/11).
  - Registro de parecer (Issue #215 / PR #237): integrado em 06/11, antes do início previsto (09/11).
  - Com as APIs disponíveis, as telas de submissão e de avaliação (Issues #226 e #227 / PR #242) foram integradas em 10/11, dentro da janela planejada, sem bloqueio do frontend por falta de backend.
- **Reavaliação:**
  - **Probabilidade:** Baixa (1), mantida. O contrato antecipado e a entrega adiantada das APIs eliminaram a dependência do frontend em relação ao backend nesta sprint.
  - **Impacto:** Alto (3), mantido. Um atraso nas APIs ainda bloquearia as telas e os testes de integração no fim da sprint, que concentrou as entregas entre 09/11 e 11/11.
  - **Prioridade:** Média (3), mantida.
- **Eficácia da mitigação:** Eficaz. O backend foi concluído com folga de três dias em relação ao planejado, o que deu ao frontend e à QA tempo para integrar as telas e escrever os cenários de integração da US06 e da US07 (PRs #246 e #247).
- **Ações para o fechamento:**
  - Manter os contratos de dados como pré-requisito das issues de backend e frontend em funcionalidades futuras, como a US08.
  - Registrar nos contratos qualquer mudança de comportamento feita durante a implementação, como na correção do R07 (contratos da US02 e da US04 atualizados no PR #241).
- **Responsável pelo Acompanhamento:** Backend / QA (`Talles Souza`, `Willian Ferreira`)

#### R04 — Crescimento Não Planejado do Escopo (Scope Creep)

- **Status na Sprint 4:** Mitigado.
- **O que ocorreu e evidências:** As regras de US06/US07 foram congeladas antes da implementação ([`docs/entregas/sprint-4.md`](entregas/sprint-4.md), seção 3.2). Diante do esforço extra com os 8 bugs (`#199`–`#206`), a equipe acionou a contenção prevista e **postergou a US08 (`#15`)** para o backlog futuro, preservando prazo e qualidade da release `v0.4.0`.
- **Reavaliação e risco residual:** Probabilidade Alta (3) → **Baixa (1)**, Impacto **Médio (2)**, Prioridade Alta (6) → **Baixa (2)**. **Risco residual:** baixo no escopo entregue; US08 permanece rastreada na Issue `#15`.
- **Responsável pelo Acompanhamento:** Scrum Master (`Willian Ferreira`) / PO (`Adriano Luiz de Souza`)

#### R07 — Condição de Corrida no Aceite / Concorrência de Entregas

- **Status na Sprint 4:** Mitigado, com risco residual.
- **O que ocorreu:** O risco foi confirmado e tratado nos dois fluxos sujeitos a requisições simultâneas:
  - **Aceite da última vaga (Issue #206 / PR #241):** um teste de integração reproduziu a falha antes da correção: com 1 vaga, dois aceites simultâneos retornavam `200` e o orientador ficava com mais orientandos do que vagas. A correção adotou lock otimista (`@Version` em `PerfilOrientador`, migração `V8`), com resposta `409` para o aceite em conflito. No teste manual com MySQL real, de 5 aceites simultâneos com 1 vaga, 1 foi aprovado, 4 receberam `409` e as vagas terminaram em 0. A mesma proteção impede que uma edição do perfil sobrescreva as vagas com um valor desatualizado.
  - **Registro de parecer (Issue #215 / PR #237):** para impedir que dois pareceres simultâneos sobrescrevam uma decisão final, a entrega é carregada com trava pessimista (`PESSIMISTIC_WRITE`). No teste manual com MySQL real, de 8 pareceres simultâneos sobre a mesma entrega, 1 foi gravado e 7 receberam `409`.
- **Reavaliação:**
  - **Probabilidade:** Baixa (1), mantida. O cenário continua exigindo requisições no mesmo instante, e agora é bloqueado pelo controle de concorrência.
  - **Impacto:** Alto (3) → **Médio (2)**. Os conflitos passaram a ser rejeitados com `409` e o usuário pode repetir a ação. O caso residual (ver abaixo) afeta apenas o contador de vagas, sem aceitar mais orientandos do que a capacidade.
  - **Prioridade:** Média (3) → **Baixa (2)**.
- **Eficácia da mitigação:** Eficaz. A estratégia definida na Entrega 6 (teste de concorrência e, em caso de falha, `@Version` em `PerfilOrientador`) foi executada como planejado, e os testes automatizados (`SolicitacaoConcorrenciaIntegrationTest`) passaram a proteger o cenário contra regressões.
- **Risco residual:** aceitar e recusar a mesma solicitação ao mesmo tempo não gera conflito de versão, porque a recusa não altera o perfil do orientador. Nesse caso, a solicitação pode terminar `RECUSADA` com a vaga já consumida. O cenário é raro, pois só o próprio orientador responde à solicitação.
- **Ações para o fechamento:**
  - Abrir uma issue para adicionar `@Version` em `SolicitacaoOrientacao`, cobrindo o caso de aceite e recusa simultâneos.
  - Aplicar o mesmo cuidado de concorrência às novas funcionalidades com recursos limitados, como o agendamento de bancas (US08).
- **Responsável pelo Acompanhamento:** Backend (`Talles Souza`)

#### R09 — Complexidade e Instabilidade na Integração com Keycloak

- **Status na Sprint 4:** Mitigado.
- **O que ocorreu e evidências:** Ocorreu expiração de sessão em ~5 minutos gerando erros `401` no frontend (Issue `#205`), corrigida no PR `#225` com fila de requisições e refresh automático de token no `api.ts`. Em staging (`#220` / PR `#244`), `KEYCLOAK_PUBLIC_URL` e `webOrigins` foram ajustados e validados por smoke test (`docs/staging.md`).
- **Reavaliação e risco residual:** Probabilidade Alta (3) → **Baixa (1)**, Impacto Alto (3) → **Médio (2)**, Prioridade Crítica (9) → **Baixa (2)**. **Risco residual:** baixo no MVP, mantida a limitação conhecida de `grant_type=password` ([`ADR-0014`](adrs/ADR-0014.md)) para futura adoção de PKCE.
- **Responsável pelo Acompanhamento:** DevOps / QA (`Eduardo Cardoso`, `Willian Ferreira`)

#### R11 — Submissão de Links Externos Privados ou Inacessíveis (US06 / US07)

- **Natureza:** Negócio / Usabilidade
- **Status na Sprint 4:** Mitigado parcialmente; em monitoramento.
- **O que ocorreu:** Como previsto na ADR-0013, o sistema não acessa o conteúdo do link e, por isso, não consegue verificar se o orientador tem permissão para abri-lo. O risco foi tratado com orientação ao aluno e com um caminho de correção:
  - O backend aceita apenas URLs HTTPS (Issue #216 / PR #236).
  - A tela de submissão orienta o aluno a "compartilhar um link HTTPS com permissão de leitura para seu orientador" e a conferir se o acesso está liberado (PR #242).
  - Se o orientador não conseguir abrir o link, ele pode registrar o parecer `AJUSTES_NECESSARIOS` com um comentário, o que libera o reenvio de um novo link pelo aluno (Issue #215 / PR #237).
- **Reavaliação:**
  - **Probabilidade:** Média (2). A configuração de compartilhamento depende do aluno e do serviço externo, e o sistema não tem como validá-la no envio.
  - **Impacto:** Baixo (1). Um link inacessível atrasa a avaliação, mas não a impede: o fluxo de ajustes e reenvio permite corrigir o problema sem intervenção técnica.
  - **Prioridade:** **Baixa (2)**.
- **Eficácia da mitigação:** Parcial. A orientação na tela e o fluxo de ajustes reduzem o impacto, mas não evitam que o problema ocorra. A variável `APP_SUBMISSAO_DOMINIOS_SUGERIDOS`, configurada para sugerir serviços de hospedagem conhecidos, ainda não é exibida na interface.
- **Ações para o fechamento:**
  - Exibir na tela de submissão os domínios sugeridos configurados em `APP_SUBMISSAO_DOMINIOS_SUGERIDOS`.
  - Acompanhar, nos testes com usuários, quantos pareceres de ajustes foram motivados por link inacessível.
- **Responsável pelo Acompanhamento:** Backend / Frontend / QA (`Talles Souza`, `Marcus Jhuan`, `Willian Ferreira`)

#### R12 — Modificação Posterior do Trabalho Submetido Após o Prazo (US06)

- **Natureza:** Integridade / Negócio
- **Status na Sprint 4:** Aceito (risco residual da ADR-0013).
- **O que ocorreu:** O conteúdo fica hospedado em serviços externos e pode ser alterado depois do envio sem que o sistema perceba. A implementação da US06 (Issue #216 / PR #236) registra o que está sob controle do sistema:
  - O momento do envio (`enviadoEm`) é registrado pelo servidor, com o relógio injetável (`Clock`), e não pode ser informado pelo aluno.
  - A flag `atrasada` é calculada no envio, comparando a data com o prazo da etapa, e é exibida ao aluno e ao orientador como "Entregue com atraso".
  - Após um parecer `APROVADO` ou `REPROVADO`, a etapa é encerrada e o link não pode mais ser trocado. O reenvio só é permitido quando o orientador pede ajustes, e nesse caso o link anterior é substituído.
- **Reavaliação:**
  - **Probabilidade:** Média (2). Nada impede tecnicamente a edição do documento externo após o envio.
  - **Impacto:** Médio (2). A alteração pode fazer o orientador avaliar uma versão diferente da enviada no prazo, mas não altera o registro de data e de atraso mantido pelo sistema.
  - **Prioridade:** **Média (4)**.
- **Eficácia da mitigação:** Parcial. O registro de data e de atraso no servidor garante a rastreabilidade do envio, mas não do conteúdo. A orientação para usar links somente leitura ou de versão imutável, prevista no planejamento, não foi incluída na tela de submissão.
- **Ações para o fechamento:**
  - Incluir na tela de submissão a orientação de usar links que identifiquem uma versão fixa do documento, como uma tag ou um commit no GitHub.
  - Avaliar o registro do histórico de links enviados em cada reenvio, já que hoje o link anterior é substituído.
- **Responsável pelo Acompanhamento:** Arquiteta de Software / Backend (`Damares Gaia`, `Talles Souza`)

#### R13 — Atraso em Cadeia entre Backend, Frontend e QA na US07

- **Natureza:** Prazo / Processo
- **Mitigação e ocorrido:** Havia dependência sequencial concentrada entre `09/11` e `11/11`. O backend da US07 (`#210`, `#215` — PRs `#240`, `#237`) e os contratos (`#209` — PR `#234`) foram entregues antecipadamente (`04/11` e `06/11`) e a US08 foi postergada. Como o frontend (`#226`/`#227` — PR `#242`) foi integrado em `10/11`, os testes de integração de QA (`#228` — PR `#246` e `#229` — PR `#247`) concentraram-se no último dia (`11/11/2026`), sendo concluídos com CI verde antes do encerramento.
- **Reavaliação e risco residual:** Probabilidade **Média (2)**, Impacto **Médio (2)**, Prioridade **Média (4)**. **Risco residual:** controlado para `v0.4.0`; recomendação registrada em `sprint-4.md` (seção 9.3) para antecipar a integração de frontend em ciclos futuros.
- **Responsável pelo Acompanhamento:** Scrum Master / QA / Dev (`Willian Ferreira`, `Talles Souza`, `Marcus Jhuan`)

---

### 6.4 Matriz Consolidada de Riscos ao Término da Sprint 4

|   ID    | Risco                                            | Natureza            | Probabilidade |  Impacto  | Prioridade | Status Final                                            |
| :-----: | :----------------------------------------------- | :------------------ | :-----------: | :-------: | :--------: | :------------------------------------------------------ |
| **R01** | Desistência de membro da equipe                  | Equipe              |   Média (2)   | Alto (3)  |  Alta (6)  | **Mitigado (equipe completa ativa; issues fatiadas)**   |
| **R02** | Falhas por ausência de testes/CI                 | Qualidade           |   Baixa (1)   | Médio (2) | Baixa (2)  | **Mitigado (CI completo + testes US06/US07 + 8 fixes)** |
| **R03** | Atraso no cronograma das APIs                    | Prazo               |   Baixa (1)   | Alto (3)  | Média (3)  | **Mitigado (contratos antecipados)**                    |
| **R04** | Crescimento do escopo                            | Escopo              |   Baixa (1)   | Médio (2) | Baixa (2)  | **Mitigado (US08 postergada para manter qualidade)**    |
| **R05** | Divergência de ambientes (Docker)                | Tecnologia          |   Baixa (1)   | Baixo (1) | Baixa (1)  | **Mitigado (Docker Compose, `.env.example`, DEPLOY)**   |
| **R06** | Ausência de autenticação nos endpoints           | Segurança           |   Baixa (1)   | Baixo (1) | Baixa (1)  | **Resolvido / Encerrado (Keycloak + fixes #199–#204)**  |
| **R07** | Condição de corrida / concorrência               | Qualidade/Dados     |   Baixa (1)   | Médio (2) | Baixa (2)  | **Mitigado (`@Version` e trava pessimista)**            |
| **R08** | Mudança de direção do produto (Pivot)            | Estratégico         |   Baixa (1)   | Baixo (1) | Baixa (1)  | **Resolvido / Obsoleto (direção consolidada)**          |
| **R09** | Instabilidade na integração Keycloak             | Segurança           |   Baixa (1)   | Médio (2) | Baixa (2)  | **Mitigado (refresh automático #205 + staging)**        |
| **R10** | Indisponibilidade de Staging                     | Infraestrutura      |   Baixa (1)   | Baixo (1) | Baixa (1)  | **Mitigado (Cloudflare Tunnels + compose local)**       |
| **R11** | Links externos privados/inacessíveis (US06/US07) | Negócio/Usabilidade |   Média (2)   | Baixo (1) | Baixa (2)  | **Mitigado parcialmente**                               |
| **R12** | Alteração de trabalho após o prazo (US06)        | Integridade/Negócio |   Média (2)   | Médio (2) | Média (4)  | **Aceito (residual da ADR-0013)**                       |
| **R13** | Atraso em cadeia Backend, Frontend e QA na US07  | Prazo/Processo      |   Média (2)   | Médio (2) | Média (4)  | **Mitigado no fechamento (PRs #246 e #247 integrados)** |

> **Riscos obsoletos após a `ADR-0013`:** riscos ligados a **storage local e custódia de arquivos binários** (espaço em disco, volumes Docker de mídia e validação de upload `MultipartFile`) deixaram de ser aplicáveis após a adoção de links externos HTTPS ([`docs/adrs/ADR-0013.md`](adrs/ADR-0013.md)), sendo substituídos por **R11** e **R12**.
