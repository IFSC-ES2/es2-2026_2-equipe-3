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
- **Responsável pelo Acompanhamento:** Scrum Master (`Eduardo Cardoso`)

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
- **Responsável pelo Acompanhamento:** Scrum Master (`Eduardo Cardoso`) / Product Owner (`Adriano Luiz de Souza`)

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

> Matriz original da Entrega 4. Para o status atualizado ao final da
> Sprint 2, incluindo os riscos R06 e R07, ver [seção 4](#4-revisão-ao-final-da-sprint-2-entrega-6).

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

## 4. Revisão ao Final da Sprint 2 (Entrega 6)

Esta seção atualiza o estado dos riscos registrados na Entrega 4 com base na
execução real da Sprint 2 (US03 fatia essencial + US04) e registra os
riscos novos identificados durante a elaboração do
[`docs/contrato-dados-us04.md`](contrato-dados-us04.md).

### 4.1 Status Atualizado dos Riscos Existentes

| ID      | Risco                                                             | Status na Sprint 2              | Observação                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| :------ | :---------------------------------------------------------------- | :------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **R01** | Desistência ou abandono de membro da equipe                       | Concretizado (parcialmente)     | Não houve desistência total, mas um integrante teve baixa participação pontual na sprint, o que **chegou a impactar prazo/redistribuição de pelo menos uma issue**. A mitigação preventiva (documentação constante, sem conhecimento exclusivo) evitou um cenário pior, mas a ação de resposta ("Se R01 ocorrer: repriorizar backlog") deveria ter sido formalmente acionada e não foi registrada como tal no momento.                                                      |
| **R02** | Falhas de regressão por ausência de testes e CI quebrado          | Ativo, parcialmente mitigado    | Houve quebras de CI durante a sprint, corrigidas rapidamente - a mitigação preventiva (bloqueio de merge com CI vermelho) funcionou como rede de segurança, mas o risco de quebra recorrente continua presente. Some-se a isso a divergência já existente no `GlobalExceptionHandler` (issue #90), que mostra lacunas de cobertura em código anterior à Sprint 2.                                                                                                           |
| **R03** | Atraso no cronograma de integração das APIs de backend e frontend | Mitigado nesta sprint           | Ação preventiva aplicada com sucesso: o contrato de dados da US04 foi estabelecido no início do ciclo (`docs/contrato-dados-us04.md`), permitindo que o desenvolvimento de frontend (11.5–11.7) iniciasse em paralelo ao backend, sem esperar os endpoints reais estarem prontos.                                                                                                                                                                                           |
| **R04** | Crescimento não planejado do escopo (scope creep)                 | Mitigado nesta sprint           | O escopo da US03 foi deliberadamente reduzido a uma "fatia essencial" (indicador de vagas + botão de solicitação), adiando filtros avançados. Melhorias identificadas durante a elaboração do contrato de dados (bloqueio de e-mail cruzado, cancelamento, paginação) foram conscientemente registradas como fora de escopo em vez de incorporadas ao sprint.                                                                                                               |
| **R05** | Divergência de ambientes e instabilidade na orquestração (Docker) | Concretizado - gap na mitigação | Um integrante teve falha ao subir o backend via Docker em ambiente Linux, causada por permissão de arquivo. **O problema foi contornado localmente, mas a correção não foi commitada ao repositório** - ou seja, a causa raiz permanece sem correção documentada e deve se repetir para qualquer outro integrante Linux que configure o ambiente do zero. A estratégia de mitigação preventiva (padronizar Dockerfile/compose) existe, mas não cobria este caso específico. |

### 4.2 Riscos Novos Identificados na Sprint 2

### R06 - Ausência de Autenticação Expõe Endpoints da US04 a Uso Indevido

- **Natureza:** Segurança
- **Identificação:** Como a US01 (autenticação/JWT) foi postergada, todos os endpoints de `/api/v1/solicitacoes` são públicos - qualquer cliente pode chamar `PATCH /solicitacoes/{id}/status` como se fosse o orientador dono da solicitação, sem qualquer verificação de identidade.
- **Descrição:** Os identificadores de solicitação (`id`) são sequenciais e enumeráveis, o que facilita a um cliente externo descobrir e manipular solicitações de outros orientadores por tentativa direta, sem necessidade de autenticação ou autorização.
- **Causa:** Decisão consciente de simplificação de escopo (ADR-0007), priorizando a entrega do fluxo funcional de match em detrimento da segurança de acesso, já que a US01 não fazia parte do MVP desta sprint.
- **Consequência / Impacto Esperado:** Um agente mal-intencionado poderia aceitar, recusar ou consultar solicitações de orientação que não lhe pertencem, comprometendo a integridade dos dados de vagas e a confiabilidade do fluxo de match caso o sistema seja exposto fora de um ambiente controlado de demonstração/avaliação.
- **Probabilidade:** Alta (3) - a limitação é garantida, não hipotética, enquanto a US01 não for implementada.
- **Impacto:** Médio (2) - mitigado pelo fato de o sistema ainda não estar em produção real com dados sensíveis; o impacto seria alto em um ambiente de uso real.
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Manter o sistema restrito a ambiente de desenvolvimento/avaliação até a implementação da US01. Documentar a limitação de forma explícita e visível no `README.md` e na release `v0.2.0`, para que ninguém trate o sistema como pronto para uso real nesse estado. Priorizar a US01 no backlog da próxima sprint.
- **Responsável pelo Acompanhamento:** Scrum Master (`Damares Gaia`)

### R07 - Condição de Corrida no Aceite da Última Vaga Disponível

- **Natureza:** Tecnologia / Qualidade
- **Identificação:** Duas requisições `PATCH .../status` (`ACEITA`) concorrentes para o mesmo orientador, no instante em que ele possui apenas 1 vaga disponível, podem ambas passar pela validação de `vagasDisponiveis > 0` antes que o decremento da primeira seja persistido, resultando em `vagasDisponiveis` negativo ou em duas solicitações aceitas para uma única vaga.
- **Descrição:** O `AtualizadorVagasObserver` foi desenhado para rodar de forma síncrona na mesma transação do `PATCH` (decisão registrada no contrato de dados, seção 8.3), o que reduz a janela de corrida, mas não a elimina sem um mecanismo explícito de controle de concorrência (ex.: lock otimista/versionamento na entidade `PerfilOrientador`).
- **Causa:** Ausência de estratégia de controle de concorrência (locking otimista ou pessimista) na primeira versão da implementação, por não ter sido um requisito explícito da issue original antes da revisão do contrato de dados.
- **Consequência / Impacto Esperado:** Inconsistência de dados (vagas negativas) ou aceite de mais solicitações do que a capacidade real do orientador, comprometendo a confiabilidade da funcionalidade core do MVP (o "match").
- **Probabilidade:** Baixa (1) - exige simultaneidade real de requisições no exato instante da última vaga; cenário raro no volume de uso esperado para o projeto acadêmico.
- **Impacto:** Alto (3) - quando ocorre, corrompe diretamente a regra de negócio central da US04.
- **Prioridade:** Média (3)
- **Estratégia de Mitigação:** Cobrir o cenário com teste de concorrência explícito (issue 11.8) simulando duas requisições simultâneas sobre a última vaga. Caso o teste demonstre falha, avaliar a adoção de lock otimista (`@Version` em `PerfilOrientador`) como ação da próxima sprint.
- **Responsável pelo Acompanhamento:** Responsável por Qualidade (`Talles Souza`)

### R08 - Possível Mudança de Direção do Produto (Pivot) Sugerida pelo Professor

- **Natureza:** Escopo / Estratégico
- **Identificação:** O professor orientador sugeriu que a equipe reavalie a forma/abordagem geral do sistema (mantendo o domínio de gestão de TCC, mas potencialmente alterando a estrutura do produto), sem que a equipe tenha ainda decidido se e como essa mudança será incorporada.
- **Descrição:** Diferente do R04 (crescimento não planejado de escopo, isto é, acúmulo de funcionalidades sobre a direção já definida), este risco trata de uma possível mudança na própria direção do produto - uma decisão estratégica que pode alterar premissas já assumidas no MVP, no modelo de domínio (UML) e nas User Stories já implementadas ou planejadas (US02, US03, US04).
- **Causa:** Feedback do professor orientador sobre a adequação da abordagem atual do sistema ao contexto da disciplina/avaliação.
- **Consequência / Impacto Esperado:** Caso confirmado, pode exigir a revisão do MVP, do modelo de domínio e do backlog já priorizado; o grau de reaproveitamento do trabalho já entregue (US02, fatia da US03, US04, ADRs, contrato de dados) ainda é incerto e só poderá ser avaliado quando o novo rumo for definido com mais detalhe.
- **Probabilidade:** Média (2) - ainda não é uma decisão tomada; depende de avaliação da equipe na virada de sprint.
- **Impacto:** Alto (3) - uma mudança de direção do produto pode invalidar parte do trabalho de modelagem e implementação já feito, e força a equipe a reabrir decisões que pareciam fechadas (MVP, ADRs, contrato de dados).
- **Prioridade:** Alta (6)
- **Estratégia de Mitigação:** Reservar um espaço explícito na próxima Sprint Planning para avaliar a sugestão do professor antes de iniciar qualquer nova implementação, evitando decidir o novo rumo "no meio" de uma sprint em andamento. Ao avaliar as opções, mapear explicitamente o que de US02/US03/US04/UML/contrato de dados seria reaproveitável em cada alternativa, para que a decisão considere custo de retrabalho, não só adequação conceitual. Não iniciar a Sprint 3 sem essa decisão fechada, para evitar implementar sobre uma base que pode ser descartada.
- **Responsável pelo Acompanhamento:** Scrum Master (` Damares Gaia`) / Product Owner (`Adriano Luiz de Souza`)

### Matriz de Riscos Atualizada (Entrega 6)

| ID      | Risco                                          | Natureza             | Probabilidade |  Impacto  | Prioridade  | Status                          |
| :------ | :--------------------------------------------- | :------------------- | :-----------: | :-------: | :---------: | :------------------------------ |
| **R03** | Atraso no cronograma das APIs                  | Prazo                |   Alta (3)    | Alto (3)  | Crítica (9) | Mitigado                        |
| **R06** | Ausência de autenticação nos endpoints da US04 | Segurança            |   Alta (3)    | Médio (2) |  Alta (6)   | Novo - ativo                    |
| **R08** | Possível mudança de direção do produto (pivot) | Escopo/Estratégico   |   Média (2)   | Alto (3)  |  Alta (6)   | Novo - em avaliação             |
| **R01** | Desistência de membro da equipe                | Equipe               |   Média (2)   | Alto (3)  |  Alta (6)   | Concretizado (parcial)          |
| **R02** | Falhas por ausência de testes/CI               | Qualidade            |   Alta (3)    | Médio (2) |  Alta (6)   | Ativo                           |
| **R05** | Divergência de ambientes (Docker)              | Tecnologia           |   Alta (3)    | Médio (2) |  Alta (6)   | Concretizado - gap na mitigação |
| **R04** | Crescimento do escopo                          | Escopo               |   Média (2)   | Médio (2) |  Média (4)  | Mitigado                        |
| **R07** | Condição de corrida no aceite da última vaga   | Tecnologia/Qualidade |   Baixa (1)   | Alto (3)  |  Média (3)  | Novo - ativo                    |

### 4.3 Ações de Mitigação para a Próxima Sprint

- **R02:** Auditar handlers de erro existentes de outras funcionalidades (não só o já corrigido na #90) em busca de divergências similares entre contrato documentado e comportamento real antes de escalar novas features sobre eles.
- **R01:** Como a ação de resposta prevista ("repriorizar backlog") não chegou a ser formalmente acionada apesar do impacto real em prazo, formalizar explicitamente na próxima Sprint Planning: identificar a issue redistribuída, registrar o impacto de prazo real, e reforçar o pareamento preventivo do integrante afetado nas próximas tarefas críticas.
- **R05:** Commitar a correção de permissão de arquivo do ambiente Linux (Dockerfile/entrypoint/script de setup) que resolveu o problema localmente, para fechar o gap identificado - sem isso, o risco permanece ativo e vai se repetir para qualquer integrante Linux configurando o ambiente do zero. Adicionar uma nota no README sobre esse ajuste, caso ainda não esteja refletido na imagem/compose.
- **R06:** Priorizar a US01 (autenticação/JWT) no planejamento da próxima sprint; até lá, reforçar no README e na release que o sistema não deve ser exposto fora de ambiente de avaliação.
- **R07:** Executar o teste de concorrência da issue 11.8 como critério de bloqueio para o merge da 11.3; se falhar, tratar a introdução de lock otimista como item de maior prioridade que novas funcionalidades na sprint seguinte.
- **R08:** Marcar um momento dedicado (fora da correria de fechamento da Entrega 6) para avaliar a sugestão do professor com calma, mapeando o reaproveitamento possível de cada alternativa antes de comprometer a Sprint 3 a um novo rumo. Não decidir o pivot sob pressão de prazo.
- **Geral:** Manter a prática de contrato-first (R03) e de escopo revisado explicitamente por sprint (R04) nas próximas sprints, já que ambas se mostraram eficazes nesta iteração.
