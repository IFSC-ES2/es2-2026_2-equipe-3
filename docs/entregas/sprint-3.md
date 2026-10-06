# Sprint 3 - Entrega 7

- **Data de Início:** 02/10/2026
- **Data da Entrega:** 22/10/2026
- **Scrum Master da Sprint 3:** Talles Souza da Cruz
- **Marco de Release:** `v0.3.0`
- **Branch de Trabalho:** `entrega-7`


---

# Inspeção e replanejamento da Sprint 3

## 1. Inspeção: Estado do MVP ao Final da Sprint 2

### 1.1 Funcionalidades

| História                                          |  Estimativa   | Estado ao final da Sprint 2 | Observação                                                                                                       |
| :------------------------------------------------ | :-----------: | :-------------------------- |:-----------------------------------------------------------------------------------------------------------------|
| US01 - Autenticação e Perfis                      |     5 SP      | Não iniciada                | Postergada nas Sprints 1 e 2. Todos os endpoints são públicos.                                                   |
| US02 - Cadastro de Perfil de Orientador e Vagas   |     5 SP      | Concluída (Sprint 1)        | CRUD de orientadores, catálogo e edição de perfil.                                                               |
| US03 - Busca e Filtragem de Orientadores          |     3 SP      | Parcial                     | Entregues o filtro textual `?area=` (Sprint 1) e o botão "Solicitar Orientação" condicionado a vagas (Sprint 2). |
| US04 - Solicitação e Aceite de Orientação         |     5 SP      | Concluída (Sprint 2)        | Envio, listagem, aceite/recusa e acompanhamento, com Strategy e Observer.                                        |
| US05 - Calendário de Prazos                       | Não estimada  | Não iniciada                | Depende da US01.                                                                                                 |
| US06 e US07 - Submissão de Documentos e Pareceres | Não estimadas | Não iniciadas               | Afetadas pelo ajuste de escopo (seção 3).                                                                        |

O sistema ao final da Sprint 2 entrega o "match" entre aluno e orientador de ponta a ponta, mas sem identidade de usuário: o painel do professor identifica o orientador pelo `orientadorId` guardado no `localStorage` do navegador (com retorno ao identificador `1` quando ausente) e o aluno acompanha a solicitação por um identificador sequencial na URL.

### 1.2 Métricas

| Métrica                                           | Sprint 1          | Sprint 2           | Situação ao final da Sprint 2                                                                             |
| :------------------------------------------------ | :---------------- | :----------------- | :-------------------------------------------------------------------------------------------------------- |
| [M-01](../metricas/M-01.md) Cobertura (backend)   | 92,00%            | 91,00%             | Meta desejada.                                                                                            |
| [M-01](../metricas/M-01.md) Cobertura (frontend)  | 97,95%            | 67,74%             | **Zona de alerta.** Queda causada pela pasta `utils` (8,53%) e pela página `OrientadorCadastro` (38,88%). |
| [M-02](../metricas/M-02.md) Densidade de defeitos | 0,00 bugs/SP      | 0,20 bugs/SP       | Faixa aceitável. O defeito registrado é a condição de corrida no aceite (R07).                            |
| [M-03](../metricas/M-03.md) Velocidade            | 5 SP              | 5 SP               | Estável: 5 SP por sprint de duas semanas.                                                                 |
| [M-04](../metricas/M-04.md) Lead time de PRs      | 12h               | 0,1h a 59,7h       | Maioria dentro da meta; um PR em zona de alerta (#107, 59,7h) e um na faixa aceitável (#108, 37,3h).      |
| [M-05](../metricas/M-05.md) Conclusão do MVP      | ~38,5% (5 de 13)  | ~55,6% (10 de 18)  | O denominador cobre apenas US01 a US04; US05 a US07 não têm estimativa.                                   |
| [M-06](../metricas/M-06.md) Capacidade            | ~89% (50h de 56h) | 94,6% (53h de 56h) | Meta desejada.                                                                                            |

### 1.3 Riscos

Situação registrada em [`docs/riscos.md`](../riscos.md) na revisão da Sprint 2:

| Risco                                       | Prioridade  | Situação ao final da Sprint 2                                                                |
| :------------------------------------------ | :---------: | :------------------------------------------------------------------------------------------- |
| R01 - Desistência ou baixa participação     |  Alta (6)   | Concretizado parcialmente, com impacto em prazo de ao menos uma issue.                       |
| R02 - Regressão por falta de testes/CI      |  Alta (6)   | Ativo, parcialmente mitigado pelo bloqueio de merge com CI vermelho.                         |
| R03 - Atraso de integração backend/frontend | Crítica (9) | Mitigado pela prática de contrato de dados no início da sprint.                              |
| R04 - Crescimento do escopo                 |  Média (4)  | Mitigado na Sprint 2; **reabre com o ajuste de escopo da seção 3**.                          |
| R05 - Divergência de ambientes (Docker)     |  Alta (6)   | Concretizado na Sprint 2 (falha de permissão do `gradlew` em Linux) e resolvido: a correção foi integrada ao repositório. |
| R06 - Ausência de autenticação              |  Alta (6)   | Ativo. É o principal motivador da priorização da US01.                                       |
| R07 - Condição de corrida no aceite         |  Média (3)  | Ativo. Sem mecanismo de controle de concorrência no código.                                  |
| R08 - Mudança de direção do produto (pivot) |  Alta (6)   | Tratado pela equipe por meio do relatório de refatoração de escopo (seção 3).                |

---

## 2. O que será Mantido, Corrigido, Replanejado e Priorizado

### 2.1 Mantido

- **Funcionalidades das Sprints 1 e 2:** o catálogo de orientadores (US02) e o fluxo de solicitação e aceite (US04) permanecem válidos.
- **Decisões de arquitetura:** arquitetura em camadas (ADR-0003), MySQL com Spring Data JPA (ADR-0004), Docker (ADR-0005), React com TypeScript (ADR-0006), Strategy e Observer (ADR-0007) e Flyway (ADR-0009).
- **Modelagem de `Aluno` como subtipo de `Usuario` (ADR-0008):** o reaproveitamento do aluno pelo e-mail facilita o vínculo com a conta de autenticação na US01.
- **Práticas de processo:** contrato de dados definido no início da sprint (eficaz contra o R03), revisão obrigatória de PR, bloqueio de merge com CI vermelho e integração por commit de mesclagem.

### 2.2 Corrigido

| Correção                                                                                                                                                | Quando                                                           |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------| :--------------------------------------------------------------- |
| Proteger os endpoints e substituir o `localStorage` pela identidade do usuário autenticado.                                                             | Sprint 3 (US01)                                                  |
| Alinhar README, ADR-0007 e `PADROES-DE-PROJETO.md` ao código real; atualizar `fluxo-de-trabalho.md`, `riscos.md` e o template de PR para a `entrega-7`. | Sprint 3 (ADRs consolidados e documentação)                      |
| Candidato à refatoração orientada a design da Entrega 7, com métrica antes/depois (duplicação e cobertura de `utils`).                                  | Sprint 3 (reengenharia)                                          |
| Coletar cobertura no pipeline (JaCoCo no backend e `vitest --coverage` no frontend) e corrigir `docs/ci.md`.                                            | Sprint 3 (testes no pipeline)                                    |
| Decidir o mecanismo de controle de concorrência, com teste que reproduza o cenário.                                                                     | Sprint 3 se houver capacidade; senão, backlog                    |
| Mover o filtro de ativos para o backend ao implementar os filtros combinados.                                                                           | Sprint 3 (US03)                                                  |
| Tratar violação de integridade com `409`; alinhar `docker-compose.prod.yml` à ADR-0009 e externalizar as origens do CORS.                               | Sprint 3 (junto da US01, que altera a configuração de segurança) |
| Completar as validações do contrato da US04 e criar o teste de integração ponta a ponta.                                                                | Backlog (débito técnico registrado)                              |

### 2.3 Replanejado

- **US01:** deixa de ser "login próprio com Spring Security/JWT e três perfis" e passa a ser autenticação via Keycloak com dois papéis (seção 3). A estimativa de 5 SP do baseline foi feita para o desenho anterior e deve ser revista na Sprint Planning.
- **US03:** o que resta são os filtros por vagas disponíveis, a combinação de filtros, o estado de "nenhum resultado" e a ação de limpar filtros.
- **US05:** o ator passa de "Coordenador" para "Professor responsável pela `Turma_TCC`". A história não possui estimativa e deve ser estimada na Sprint Planning.
- **US06 e US07:** permanecem no backlog, com a submissão alterada de upload de arquivo para link (seção 3).
- **M-05:** o total de Story Points do MVP deve ser recalculado após a estimativa das histórias restantes e da história de agendamento de bancas, pois o denominador atual (18 SP) não representa o MVP inteiro.

### 2.4 Priorizado

1. **US01 - Autenticação e Perfis.** Elimina o risco R06 e é pré-requisito das telas por perfil e da US05.
2. **US03 - Busca e Filtragem.** Fecha uma história parcialmente entregue há duas sprints, com baixo risco técnico.
3. **US05 - Calendário de Prazos.** Primeira história do fluxo de entregas; depende da US01.

---

## 3. Ajustes de Escopo

Os ajustes abaixo foram aprovados pela equipe . Eles respondem ao risco R08 (possível mudança de direção do produto) preservando o que foi entregue nas Sprints 1 e 2.

### 3.1 Resumo dos Ajustes

| Tipo       | Ajuste                                                                                                                                | Impacto                                                                                                 |
| :--------- | :------------------------------------------------------------------------------------------------------------------------------------ |---------------------------------------------------------------------------------------------------------|
| Alterado   | Perfis reduzidos a `ROLE_ALUNO` e `ROLE_PROFESSOR`. O "Coordenador" passa a ser o professor responsável pela `Turma_TCC` do semestre. | US01 e US05; nova entidade `Turma_TCC`; interface do professor renderizada conforme a responsabilidade. |
| Alterado   | Submissão de entregas por link de nuvem, em vez de upload de arquivo.                                                                 | US06 e US07; dispensa infraestrutura de armazenamento.                                                  |
| Adicionado | Autenticação com Spring Security e Keycloak, como prioridade máxima da Sprint 3.                                                      | US01; novo contêiner no `docker-compose`; nova ADR.                                                     |
| Adicionado | Agendamento de bancas de defesa, sob responsabilidade do orientador ou do professor da disciplina.                                    | Volta ao backlog do MVP, **fora da Sprint 3**. Exige nova história e estimativa.                        |
| Removido   | Papel de autenticação `Coordenador`.                                                                                                  | US01 e US05.                                                                                            |
| Removido   | Armazenamento de arquivos físicos.                                                                                                    | US06.                                                                                                   |
| Mantido    | Catálogo e "match" (US02 e US04), acervo público fora do escopo e independência do SIGAA.                                             | Nenhum retrabalho nas entregas anteriores.                                                              |

### 3.2 Ajustes nas Histórias da Sprint 3

**US01 - Autenticação e Perfis (issue #8)**

- Login via Keycloak com e-mail e senha, com os papéis `ROLE_ALUNO` e `ROLE_PROFESSOR`.
- A permissão de gestão da turma e do calendário não é um papel de login: é verificada no banco, checando se o professor autenticado é o responsável pela `Turma_TCC` do semestre vigente.
- Critérios de aceitação atualizados para cinco cenários: login de aluno, senha incorreta, professor orientador sem responsabilidade pela turma, professor responsável pela turma e acesso não autenticado a rota protegida.

**US03 - Busca e Filtragem de Orientadores (issue #10)**

- Critérios de aceitação atualizados para cinco cenários: filtro por linha de pesquisa, filtro "somente com vagas disponíveis", combinação dos dois, nenhum resultado e limpar filtros.
- **Ponto a decidir antes da implementação:** no modelo atual não existe um atributo "área de conhecimento" separado. O parâmetro `area` de `GET /api/v1/orientadores` é uma busca textual sobre o nome das linhas de pesquisa. A equipe deve registrar no contrato de dados se os termos são sinônimos ou se um novo atributo será criado.

**US05 - Calendário de Prazos (issue #12)**

- O ator passa a ser o professor responsável pela `Turma_TCC` vigente.
- Critérios de aceitação atualizados para seis cenários: cadastro de prazo, bloqueio de professor sem responsabilidade pela turma, cadastro dos três tipos de entrega (Projeto de TCC, TCC 1 e TCC 2), edição de prazo, validação de data inválida e visibilidade do prazo para o aluno.

---

## 4. Meta da Sprint 3

> **"Tornar o MVP autenticado e orientado a perfis: implementar o login via Keycloak com os papéis de Aluno e Professor (US01), concluir a busca e filtragem do catálogo de orientadores (US03) e disponibilizar o calendário de prazos gerido pelo professor responsável pela turma de TCC (US05), consolidando a documentação da arquitetura, as ADRs e a qualidade interna do código."**

### 4.1 Itens Selecionados

| Item                                     | Issue |             Estimativa             |
| :--------------------------------------- | :---: | :--------------------------------: |
| US01 - Autenticação e Perfis (Keycloak)  |  #8   |    5 SP (baseline; a reestimar)    |
| US03 - Busca e Filtragem de Orientadores |  #10  | 3 SP (baseline; fatia já entregue) |
| US05 - Calendário de Prazos              |  #12  |             A estimar              |

### 4.2 Fundamentação

- **Backlog:** US01 e US03 são as duas histórias de prioridade alta do baseline ([`docs/BASELINE.md`](../BASELINE.md)) ainda não concluídas. A US05 é a próxima do fluxo de entregas e a primeira a depender do perfil do usuário.
- **Métricas:** a velocidade observada é de 5 SP por sprint de duas semanas (M-03) e a capacidade realizada ficou entre 89% e 95% do planejado (M-06). A cobertura do frontend em zona de alerta (M-01) orienta a escolha da refatoração e a inclusão da cobertura no pipeline.
- **Riscos:** o R06 (ausência de autenticação, prioridade alta) deixa de ser aceitável com a entrada do calendário e dos painéis por perfil. O R03 segue mitigado pela definição dos contratos da US01 e da US05 no início da sprint.

---

## 5. Riscos Considerados no Planejamento

A revisão completa será registrada em [`docs/riscos.md`](../riscos.md) no fechamento. No planejamento, a equipe considera:

- **R06 (ausência de autenticação):** deve ser mitigado pela US01.
- **R08 (mudança de direção):** concretizado de forma controlada; as entregas das Sprints 1 e 2 foram preservadas.
- **R04 (crescimento do escopo):** reaberto pela inclusão do agendamento de bancas no MVP; a contenção é manter as bancas fora da Sprint 3.
- **R05 (ambientes Docker):** tende a aumentar com mais um contêiner (Keycloak) e sua configuração de _realm_, que deve ser versionada para subir com um único comando.
- **Novo - Curva de aprendizado do Keycloak:** a integração com Spring Security e com o frontend pode consumir mais tempo que o estimado e atrasar a US05, que depende dela. Mitigação: iniciar a sprint pela infraestrutura e pelo contrato de autenticação.

---

## 6. Governança e Fluxo de Trabalho

- **Ramificação base:** todo o trabalho da sprint parte da `entrega-7` e retorna a ela por Pull Request.
- **Critérios de merge:** ao menos uma aprovação de outro integrante, pipeline de CI verde e integração por commit de mesclagem.
- **Rastreabilidade:** cada PR referencia a issue correspondente; o PR da refatoração é específico e identificável.
- **Integração final:** PR revisado da `entrega-7` para a `main`, seguido da release `v0.3.0`.

