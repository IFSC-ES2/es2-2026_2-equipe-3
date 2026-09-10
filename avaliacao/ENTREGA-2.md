# Avaliação da Entrega 2 - Inception

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC
- Entrega: 2 - Inception
- Data limite considerada para avaliação: 15/08/2026
- Pull request avaliado: PR #21, branch `entrega-2` para `main`

## Documentos Consultados

- `README.md`.
- `USO-IA.md`.
- `docs/inception.md`.
- `docs/dod.md`.
- `docs/adrs/ADR-0001.md` a `docs/adrs/ADR-0006.md`.
- `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/PULL_REQUEST_TEMPLATE.md`.
- `.github/workflows/verificacao-docs.yml`.
- Issues GitHub #5 a #20 relacionadas à Entrega 2.
- Pull request GitHub #21 e respectiva aprovação.

## Resumo da Entrega

A equipe entregou os artefatos principais da Inception: visão do produto e MVP em `docs/inception.md`, DoD, seis ADRs iniciais, README atualizado, backlog priorizado em issues e board, além do PR #21 integrado à `main`. O PR teve aprovação de outro integrante e os checks executaram com sucesso.

A reavaliação confirmou também uma ruleset ativa para `main`, criada antes da entrega e atualizada antes da mesclagem do PR #21. A regra exige pull request, uma aprovação e a execução bem-sucedida do check `Checar Arquivos Essenciais`, tornando efetivas partes centrais da DoD declarada pela equipe.

## Critérios Atendidos

- A visão do produto em `docs/inception.md` cobre problema, área de aplicação, usuários e interessados, contexto, proposta de valor, objetivos do semestre e restrições.
- O tema permanece coerente com educação e apoio à aprendizagem.
- O MVP explicita objetivo, funcionalidades essenciais, itens fora do escopo, viabilidade para o semestre e critérios de decisão.
- O backlog foi registrado em issues com títulos claros, descrição, prioridade relativa, relação com o MVP e critérios de aceitação verificáveis.
- As issues do MVP usam formato próximo de história de usuário e critérios observáveis em Dado/Quando/Então.
- A DoD em `docs/dod.md` é objetiva e contempla critérios de aceitação, versionamento, branch, testes, lint, documentação, PR, checks, revisão e merge commit.
- A ruleset ativa de `main` exige PR, ao menos uma aprovação e o check `Checar Arquivos Essenciais` aprovado antes da integração.
- Foram registradas seis ADRs iniciais sobre backend, tipo de aplicação, arquitetura em camadas, persistência, containers e frontend.
- O README referencia visão/MVP, backlog e board, DoD, ADRs e declaração de uso de IA.
- A Entrega 2 foi desenvolvida na branch `entrega-2` e integrada à `main` pelo merge commit do PR #21, dentro do prazo autorizado até 15/08/2026.

## Critérios Parcialmente Atendidos

- Os objetivos do semestre ainda incluem gestão de bancas e acervo público, embora essas funcionalidades estejam fora do escopo do MVP imediato.
- Apesar de mais delimitado que na Entrega 1, o MVP ainda reúne autenticação, catálogo de orientadores, solicitação e aceite de orientação, calendário, upload de documentos e pareceres, constituindo um recorte relevante para o semestre.
- Algumas issues inicialmente vinculadas ao MVP tratam de bancas e acervo, que foram postergados. O board as separa em `Versões Futuras`, mas o texto original dessas issues não foi alinhado ao recorte final.
- As ADRs são completas, mas algumas ainda usam bancas, acervo público ou microsserviços para contextualizar decisões do MVP, embora essas funcionalidades não integrem o escopo imediato.

## Critérios Não Atendidos

- Nenhum item obrigatório da rubrica ficou sem evidência.

## Achados com Evidências

### Aderência aos requisitos da entrega

- A visão do produto está em `docs/inception.md`, linhas 7-49; a definição do MVP está nas linhas 52-95.
- As funcionalidades essenciais estão nas linhas 58-72 e os itens fora do escopo nas linhas 73-83 de `docs/inception.md`.
- A DoD está registrada em `docs/dod.md`, linhas 3-13.
- O README referencia os artefatos da Entrega 2 em `README.md`, linhas 52-60.
- As ADRs iniciais estão em `docs/adrs/ADR-0001.md` a `docs/adrs/ADR-0006.md`, cada uma com contexto, decisão, alternativa e consequências.

### Escopo e coerência do MVP

- `docs/inception.md`, linhas 38-41, mantém bancas e acervo entre os objetivos do semestre; as linhas 77-78 os excluem do MVP.
- Issues #15, #16 e #17 descrevem bancas e acervo como relacionados ao MVP, mas foram fechadas como não planejadas e separadas no board como trabalho futuro.
- A ADR de persistência relaciona a decisão a bancas e acervo em `docs/adrs/ADR-0004.md`, linhas 9-16 e 47-49.

### Processo GitHub

- O backlog do MVP está nas issues #8 a #14, com prioridade, relação com o MVP e critérios de aceitação verificáveis.
- A issue #19 registra a configuração dos checks e da proteção de `main`.
- A ruleset `Merges` está ativa para `main`, com exigência de PR, uma aprovação e o status check obrigatório `Checar Arquivos Essenciais`; foi criada em 07/08/2026 e atualizada em 14/08/2026 às 23:16:54 UTC.
- O PR #21 foi aprovado por `EduCardoso22` em 14/08/2026 às 23:17:24 UTC e mesclado em 14/08/2026 às 23:18:06 UTC.
- O check obrigatório `Checar Arquivos Essenciais` foi concluído com sucesso no commit de cabeça do PR #21; o check adicional `Verificar Links Quebrados` também foi bem-sucedido.
- A integração ocorreu pelo merge commit `78254c6` (`Merge pull request #21 from IFSC-ES2/entrega-2`).

### Uso de IA

- `USO-IA.md`, linhas 13-22, declara uso do Gemini para os workflows de CI, ADRs e README, com validação e responsabilidade assumida pela equipe.
- A declaração é compatível com os workflows, ADRs e README presentes no repositório.

## Recomendações para a Equipe

- Alinhar os objetivos do semestre, as issues e as ADRs ao MVP vigente, removendo referências a bancas e acervo quando se referirem ao escopo imediato.
- Manter a proteção de `main` e ampliar gradualmente os checks obrigatórios para build, testes e lint reais quando houver código da aplicação.
- Configurar a ruleset para permitir exclusivamente merge commit se esse continuar sendo o único método aceito pela DoD.
- Evitar versionar arquivos de configuração de IDE e manter o `.gitignore` adequado nas próximas entregas.

## Nota da Entrega

Nota: 4,5 / 5,0

## Justificativa da Nota

- Visão do produto clara, contextualizada e coerente com o tema aprovado: 0,9 / 1,0. A visão cobre os pontos exigidos; a ressalva é a inclusão de bancas e acervo nos objetivos do semestre, embora estejam fora do MVP.
- MVP definido com escopo viável, funcionalidades essenciais e itens fora do escopo: 0,8 / 1,0. O recorte foi reduzido e justificado, mas ainda concentra um conjunto relevante de funcionalidades.
- Backlog inicial priorizado, registrado em issues/board e com critérios de aceitação: 0,9 / 1,0. Há backlog priorizado e verificável; as histórias de bancas e acervo preservam texto antigo que as vincula ao MVP, apesar de estarem separadas como futuras.
- DoD objetiva e compatível com o fluxo de trabalho da equipe: 1,0 / 1,0. A DoD é objetiva, foi aplicada no PR #21 e seus controles de PR, aprovação e check obrigatório são efetivamente reforçados pela ruleset ativa de `main`.
- ADRs iniciais registradas e README atualizado com referências para os artefatos da etapa: 0,9 / 1,0. Há seis ADRs e README atualizado; permanecem pequenas inconsistências entre o contexto de algumas ADRs e o MVP atual.

## Observações sobre Uso de IA

A equipe declarou uso do Gemini na Entrega 2 para apoio na estruturação de pipelines de CI, refinamento de ADRs e revisão/estruturação do README. A declaração é compatível com os artefatos entregues: há workflows em `.github/workflows/`, ADRs em `docs/adrs/` e README atualizado. O registro declara revisão, adaptação e responsabilidade da equipe, atendendo aos princípios de transparência e responsabilidade do protocolo de uso de IA.
