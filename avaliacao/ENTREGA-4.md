# Avaliação da Entrega 4 - Riscos e Consolidação

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC
- Entrega: 4 - Riscos e Consolidação
- Data prevista da entrega: 03/09/2026
- Pull request avaliado: PR #34, branch `entrega-4` para `main`
- Versão considerada: `origin/main` no commit `27e8e64`

## Documentos Consultados

- `README.md` da equipe.
- `USO-IA.md` da equipe.
- `.github/PULL_REQUEST_TEMPLATE.md`.
- `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/workflows/verificacao-docs.yml`.
- `.github/workflows/verificar-formatacao.yml`.
- `docs/inception.md`.
- `docs/dod.md`.
- `docs/riscos.md`.
- `docs/fluxo-de-trabalho.md`.
- `docs/ci.md`.
- `docs/qualidade.md`.
- Issues GitHub #28, #29, #30, #31, #32 e #33.
- Pull request GitHub #34.
- Histórico Git local e remoto.

## Resumo da Entrega

A equipe entregou os principais artefatos esperados para a Entrega 4: registro de riscos, análise e priorização, plano de resposta, documentação de fluxo de trabalho, template de Pull Request, documentação de CI, critérios de qualidade e atualização do README e do `USO-IA.md`. O escopo do MVP permanece coerente com o Sistema de Gestão Centralizada de TCC, priorizando autenticação, perfis, catálogo de orientadores e gestão de documentos e prazos.

O registro de riscos é consistente e cobre naturezas diferentes, com probabilidade, impacto, prioridade, responsáveis e respostas. A documentação de qualidade conecta atributos da ISO/IEC 25010 aos riscos do projeto e define formas preliminares de observação. O PR #34 foi aprovado por outro integrante e teve checks executados com sucesso.

As principais ressalvas são a ausência de proteção da branch `main`, a falta de comprovação de enforcement obrigatório dos checks antes do merge, e inconsistências entre a documentação de CI/fluxo e os workflows existentes, especialmente a referência a `verificar-links.yml`, arquivo removido na própria entrega.

## Critérios Atendidos

- O arquivo `docs/riscos.md` foi criado com cinco riscos relevantes de naturezas distintas: equipe, qualidade/processo, prazo, escopo e tecnologia.
- Cada risco possui causa, consequência/impacto esperado, probabilidade, impacto, prioridade, estratégia de mitigação e responsável pelo acompanhamento.
- A matriz de probabilidade e impacto foi definida e aplicada para priorizar os riscos.
- O plano de resposta inclui ações preventivas, ações caso o risco ocorra e rotina de acompanhamento por cerimônias ágeis.
- O documento `docs/qualidade.md` seleciona quatro atributos de qualidade prioritários para o MVP e relaciona esses atributos aos riscos mapeados.
- O fluxo de trabalho foi documentado em `docs/fluxo-de-trabalho.md`, incluindo cerimônias, branches, commits, PRs, revisão e uso de CI.
- O template de Pull Request foi configurado com checklist de autor e revisor.
- O README foi atualizado com links para riscos, fluxo de trabalho, CI, qualidade e evidência de integração.
- O `USO-IA.md` contém seção específica para a Entrega 4.
- A entrega foi consolidada na branch `entrega-4` e integrada à `main` por merge commit no PR #34.
- O PR #34 teve aprovação formal de integrante diferente do autor.
- Os checks do PR #34 foram executados com sucesso.

## Critérios Parcialmente Atendidos

- A documentação de CI existe e descreve política de bloqueio, mas a branch `main` não está protegida; portanto, não há evidência de que os checks sejam obrigatórios para merge.
- O pipeline mínimo foi configurado, mas as verificações observáveis ainda são predominantemente documentais e de formatação. A expansão para build, testes e cobertura foi planejada para a Entrega 5, o que é aceitável para o estágio do projeto, mas ainda limita a mitigação efetiva dos riscos R02 e R03.
- O fluxo de trabalho define checklist mais amplo do que o template real do PR. O documento menciona verificação de links, Docker e validações de segurança, mas o template em `.github/PULL_REQUEST_TEMPLATE.md` é mais simples.
- O README afirma que a CI cobre verificação de links, mas o workflow `verificar-links.yml` foi removido no PR #34.
- O documento `docs/ci.md` lista `verificar-links.yml` como workflow ativo, embora esse arquivo não exista mais no estado final avaliado.
- A gestão por GitHub Projects é referenciada, mas não pôde ser verificada diretamente porque o token atual não possui o escopo `read:project`.

## Critérios Não Atendidos

- A branch `main` não possui proteção configurada, conforme retorno da API do GitHub: `Branch not protected`.
- Não foi comprovado bloqueio obrigatório de merge por checks ou aprovação na configuração do repositório, apesar de o processo declarado exigir esses controles.
- A documentação do fluxo contém link incorreto para o documento de CI: `docs/ci.md` apontado como `(cd.md)` em `docs/fluxo-de-trabalho.md`.

## Achados com Evidências

- Equipe e papéis registrados no README: `README.md`, linhas 3-9.
- Escopo do MVP no README: `README.md`, linhas 33-50.
- README atualizado com artefatos da Entrega 4: `README.md`, linhas 77-85.
- Registro de riscos com orientação de revisão ao final de cada sprint: `docs/riscos.md`, linhas 1-5.
- Riscos R01 a R05 documentados com natureza, causa, impacto, probabilidade, prioridade, mitigação e responsável: `docs/riscos.md`, linhas 11-74.
- Critérios de probabilidade, impacto e matriz de riscos: `docs/riscos.md`, linhas 78-100.
- Matriz de riscos priorizada com R03 como crítico: `docs/riscos.md`, linhas 102-118.
- Plano de resposta aos riscos: `docs/riscos.md`, linhas 122-146.
- Atributos de qualidade priorizados: `docs/qualidade.md`, linhas 7-50.
- Relação entre qualidade e riscos: `docs/qualidade.md`, linhas 53-75.
- Critérios preliminares de avaliação da qualidade: `docs/qualidade.md`, linhas 78-161.
- Fluxo de trabalho com cerimônias, sprints, board e colunas: `docs/fluxo-de-trabalho.md`, linhas 7-30.
- Regras de branches e proibição de push direto para `main`: `docs/fluxo-de-trabalho.md`, linhas 33-62.
- Regras de PR e aprovação mínima: `docs/fluxo-de-trabalho.md`, linhas 85-94.
- Checklist documentado para PRs: `docs/fluxo-de-trabalho.md`, linhas 95-118.
- Link incorreto para CI em `docs/fluxo-de-trabalho.md`: linha 124 usa `(cd.md)` em vez de `(ci.md)`.
- `docs/fluxo-de-trabalho.md` declara `verificar-links.yml` como check obrigatório: linhas 122-128.
- `docs/ci.md` declara workflows ativos `verificacao-docs.yml`, `verificar-links.yml` e `verificar-formatacao.yml`: linhas 16-27.
- `docs/ci.md` declara bloqueio de merge por checks e aprovação: linhas 44-51.
- Workflow `verificacao-docs.yml` verifica apenas existência do README: `.github/workflows/verificacao-docs.yml`, linhas 21-30.
- Workflow `verificar-formatacao.yml` executa Prettier em PR para `main`: `.github/workflows/verificar-formatacao.yml`, linhas 1-23.
- PR #34 removeu `.github/workflows/verificar-links.yml`, conforme lista de arquivos alterados do GitHub.
- Template real de PR contém checklist de autor e revisor, mas é mais simples que o checklist documentado em `docs/fluxo-de-trabalho.md`: `.github/PULL_REQUEST_TEMPLATE.md`, linhas 11-21.
- Issues #28, #29, #30, #31, #32 e #33 registram as tarefas da Entrega 4 e foram fechadas.
- PR #34 integrou a branch `entrega-4` à `main`, estado `MERGED`, URL `https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/34`.
- PR #34 foi criado em 04/09/2026 02:25:44 UTC e mesclado em 04/09/2026 02:44:06 UTC. Considerando horário local do Brasil, a integração ocorreu ainda em 03/09/2026.
- PR #34 possui aprovação formal de `EduCardoso22` em 04/09/2026 02:43:38 UTC.
- PR #34 teve checks com sucesso: dois checks `Checar Arquivos Essenciais` e um check `Checar Formatação de Código`.
- Integração por merge commit: commit `27e8e64`, mensagem `Merge pull request #34 from IFSC-ES2/entrega-4`.
- Branch remota `origin/entrega-4` preservada no commit `687b3cb`.
- Consulta da proteção da branch `main` retornou `Branch not protected`.
- Consulta ao GitHub Project #31 não pôde ser realizada por falta do escopo `read:project` no token atual.

## Recomendações para a Equipe

- Configurar proteção da branch `main`, exigindo PR, pelo menos uma aprovação e checks obrigatórios antes do merge.
- Corrigir a documentação de CI e fluxo para refletir exatamente os workflows ativos no repositório.
- Restaurar ou remover das referências o workflow de verificação de links, evitando declarar checks inexistentes.
- Corrigir o link `cd.md` para `ci.md` em `docs/fluxo-de-trabalho.md`.
- Aproximar o template real de PR do checklist definido na documentação de fluxo, especialmente nos itens de CI, testes, ambiente e documentação.
- Expandir o CI na próxima etapa para build, testes e validações de backend/frontend, alinhando a automação aos riscos R02 e R03.
- Registrar evidências de acompanhamento dos riscos nas próximas retrospectivas, mostrando alterações de probabilidade, impacto ou ações de mitigação realizadas.

## Observações Sobre Uso de IA

O `USO-IA.md` declara uso do Gemini na Entrega 4 para apoio na elaboração dos documentos de riscos, fluxo de trabalho e governança, além de apoio na infraestrutura de CI. Essa declaração é compatível com os artefatos entregues, que são majoritariamente documentação estruturada em Markdown e configuração simples de GitHub Actions.

Há uma pequena inconsistência textual: o `USO-IA.md` menciona `verificacao-formatacao.yml`, enquanto o arquivo real é `.github/workflows/verificar-formatacao.yml`. A diferença não compromete a transparência geral do registro, mas deve ser revisada para manter rastreabilidade precisa.

## Nota da Entrega

- Nota: 4,2 / 5,0

## Justificativa da Nota

A entrega atende aos objetivos centrais de riscos e consolidação, com documentação robusta, riscos relevantes e priorizados, plano de resposta, critérios de qualidade coerentes com o MVP, issues organizadas, PR integrado por merge commit, aprovação formal e checks executados com sucesso.

A nota não é maior por causa de inconsistências importantes entre o processo declarado e a configuração verificável: a `main` não está protegida, não há enforcement comprovado de checks obrigatórios, a documentação declara workflow de links inexistente, há link incorreto para o documento de CI e o pipeline atual ainda tem validações limitadas. Mesmo assim, considerando o estágio do projeto e a qualidade geral dos artefatos, a entrega demonstra boa aderência aos critérios da etapa.
