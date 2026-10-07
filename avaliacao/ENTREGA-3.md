# Avaliação da Entrega 3 - Estimativas e Métricas (Baseline)

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC
- Entrega: 3 - Estimativas e Métricas (Baseline)
- Data prevista da entrega: 28/08/2026
- Pull request avaliado: PR #27, branch `entrega-3` para `main`
- Versão considerada: `origin/main` no commit `9ccb72c`

## Documentos Consultados

- `README.md` da equipe.
- `USO-IA.md` da equipe.
- `.github/PULL_REQUEST_TEMPLATE.md`.
- `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/workflows/verificacao-docs.yml`.
- `.github/workflows/verificar-formatacao.yml`.
- `docs/inception.md`.
- `docs/dod.md`.
- `docs/BASELINE.md`.
- `docs/ESTIMATIVAS.md`.
- `docs/METRICAS.md`.
- `docs/metricas/M-01.md`.
- `docs/metricas/M-02.md`.
- `docs/metricas/M-03.md`.
- `docs/metricas/M-04.md`.
- `docs/metricas/M-05.md`.
- `docs/metricas/M-06.md`.
- `docs/adrs/ADR-0001.md`.
- `docs/adrs/ADR-0002.md`.
- `docs/adrs/ADR-0003.md`.
- `docs/adrs/ADR-0004.md`.
- `docs/adrs/ADR-0005.md`.
- `docs/adrs/ADR-0006.md`.
- Issues GitHub #8, #9, #10, #11, #22, #23, #24, #25 e #26.
- Pull request GitHub #27.
- Histórico Git local e remoto.

## Resumo da Entrega

A equipe entregou os artefatos centrais da Entrega 3: baseline de planejamento, registro da abordagem de estimativa, plano de métricas, seis fichas individuais de métricas, README atualizado e registro de uso de IA. O escopo do MVP declarado permanece o Sistema de Gestão Centralizada de TCC, com funcionalidades principais de autenticação e perfis, catálogo de orientadores e gestão de documentos e prazos.

O recorte de curto prazo do baseline seleciona US01, US02 e US03, relacionadas a autenticação, cadastro de perfil de orientador e busca/filtragem de orientadores. As estimativas usam Planning Poker com escala de Fibonacci e Story Points. A capacidade foi registrada como 28 horas semanais para cinco integrantes, com restrições de trabalho, comunicação assíncrona, sobrecarga acadêmica e curva de aprendizado.

A entrega é forte em documentação e rastreabilidade interna dos artefatos. As principais ressalvas são a ambiguidade entre proteção clássica de branch e ruleset ativa, a definição apenas parcial de checks obrigatórios, e inconsistências na operacionalização/classificação de métricas.

## Critérios Atendidos

- O baseline foi registrado em `docs/BASELINE.md` com recorte do backlog, priorização, estimativas, hipóteses, capacidade planejada, previsão inicial e data de registro.
- O recorte do baseline usa histórias reais do backlog, especialmente US01, US02 e US03, representadas pelas issues #8, #9 e #10.
- O MVP declarado é coerente com o tema e com o recorte de curto prazo da Entrega 3.
- A abordagem de estimativa está registrada em `docs/ESTIMATIVAS.md`, com técnica, unidade, participantes, critérios de dimensionamento e limitações.
- A capacidade planejada da equipe foi documentada com integrantes ativos, papéis, disponibilidade total semanal, restrições conhecidas e fatores de previsibilidade.
- O plano de métricas foi registrado em `docs/METRICAS.md`, com métricas de produto, processo e projeto.
- Foram criadas seis fichas individuais de métricas em `docs/metricas/`, com objetivo, fórmula, fonte dos dados, frequência, responsável, histórico de acompanhamento e forma de interpretação.
- O README foi atualizado com links para baseline, estimativas, plano de métricas e fichas técnicas.
- O `USO-IA.md` foi atualizado com declaração específica da Entrega 3.
- A entrega foi desenvolvida na branch `entrega-3` e integrada à `main` por merge commit.
- O PR #27 teve aprovação formal de integrante da equipe.
- O PR #27 teve checks executados com sucesso para documentação e links.
- A `main` possui ruleset ativa exigindo pull request, uma aprovação e o status check `Checar Arquivos Essenciais`.

## Critérios Parcialmente Atendidos

- A estimativa informa participantes, mas apenas três dos cinco integrantes aparecem na sessão de estimativa, sem explicação sobre a ausência dos demais.
- A capacidade de 28 horas semanais é plausível e acompanhada de restrições, mas o documento não identifica quais integrantes correspondem às faixas de 4h e 8h semanais.
- O conjunto de métricas é relevante, porém a métrica M-02 aparece como Produto no README e em `docs/METRICAS.md`, enquanto sua ficha individual a classifica como Processo.
- A métrica M-05 trata a taxa de conclusão do MVP com base em 13 Story Points do recorte inicial, embora o MVP declarado inclua também solicitação/aceite de orientação e gestão de documentos/prazos ainda não estimadas no baseline.
- As métricas têm histórico inicial de baseline, mas alguns valores ainda são metas ou placeholders, o que é aceitável para esta etapa, desde que sejam efetivamente coletados nas próximas entregas.
- A proteção clássica de branch não está configurada no endpoint antigo, mas há ruleset aplicável à `main`; essa ruleset exige `Checar Arquivos Essenciais`, enquanto o check `Verificar Links Quebrados` foi executado com sucesso, mas não aparece como obrigatório.

## Critérios Não Atendidos

- Não há critério completamente não atendido após considerar o prazo atualizado de 28/08/2026; as perdas remanescentes são parciais.

## Achados com Evidências

- Equipe e papéis registrados no README: `README.md`, linhas 3-9.
- Escopo do MVP no README: `README.md`, linhas 33-50.
- README atualizado com artefatos da Entrega 3: `README.md`, linhas 62-75.
- MVP detalhado em documento interno: `docs/inception.md`, linhas 52-96.
- DoD com PR, checks, aprovação e merge commit: `docs/dod.md`, linhas 3-13.
- Baseline com recorte e priorização de US01, US02 e US03: `docs/BASELINE.md`, linhas 3-10.
- Estimativas em Story Points: `docs/BASELINE.md`, linhas 11-16.
- Hipóteses assumidas: `docs/BASELINE.md`, linhas 18-23.
- Capacidade planejada e restrições: `docs/BASELINE.md`, linhas 24-37.
- Previsão inicial e data de registro do baseline: `docs/BASELINE.md`, linhas 38-41.
- Técnica Planning Poker e escala Fibonacci: `docs/ESTIMATIVAS.md`, linhas 3-6.
- Participantes da estimativa: `docs/ESTIMATIVAS.md`, linhas 7-12.
- Unidade Story Points e justificativa: `docs/ESTIMATIVAS.md`, linhas 13-15.
- Critérios de dimensionamento das histórias: `docs/ESTIMATIVAS.md`, linhas 17-38.
- Limitações e incertezas: `docs/ESTIMATIVAS.md`, linhas 41-47.
- Estratégia e objetivos do plano de medição: `docs/METRICAS.md`, linhas 3-11.
- Métricas organizadas por produto, processo e projeto: `docs/METRICAS.md`, linhas 14-33.
- Matriz geral com seis métricas e responsáveis: `docs/METRICAS.md`, linhas 37-48.
- Diretrizes de coleta, governança e limiares: `docs/METRICAS.md`, linhas 52-61.
- Ficha M-01 define cobertura de testes como métrica de produto: `docs/metricas/M-01.md`, linhas 1-50.
- Ficha M-02 define densidade de defeitos, mas classifica a métrica como processo: `docs/metricas/M-02.md`, linhas 1-49.
- README e `docs/METRICAS.md` classificam M-02 como produto: `README.md`, linha 71, e `docs/METRICAS.md`, linha 44.
- Fichas M-03 e M-04 registram métricas de processo: `docs/metricas/M-03.md`, linhas 1-73, e `docs/metricas/M-04.md`, linhas 1-68.
- Fichas M-05 e M-06 registram métricas de projeto: `docs/metricas/M-05.md`, linhas 1-49, e `docs/metricas/M-06.md`, linhas 1-51.
- M-05 mede “taxa de conclusão do MVP” usando os 13 SP do baseline inicial, mas o MVP em `docs/inception.md`, linhas 64-72, inclui funcionalidades ainda não estimadas no baseline.
- Registro de uso de IA para Entrega 3: `USO-IA.md`, linhas 24-34.
- Issues #22, #23, #24, #25 e #26 registram tarefas da Entrega 3 com critérios de aceitação.
- Issues #8, #9, #10 e #11 registram histórias do backlog do MVP com critérios de aceitação.
- PR #27 integrou a branch `entrega-3` à `main`, estado `MERGED`, URL `https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/27`.
- PR #27 foi criado em 27/08/2026 17:38:39 UTC e mesclado em 27/08/2026 17:39:51 UTC, dentro do prazo atualizado da entrega.
- PR #27 possui aprovação formal de `EduCardoso22` em 27/08/2026 17:39:31 UTC.
- PR #27 teve checks com sucesso: `Verificar Links Quebrados` e `Checar Arquivos Essenciais`.
- Integração por merge commit: commit `9ccb72c`, mensagem `Merge pull request #27 from IFSC-ES2/entrega-3`.
- Branch remota `origin/entrega-3` preservada no commit `0884bf4c04cf567a93ae1371bb98bf0d36f09197`.
- Consulta da proteção clássica da branch `main` retornou `Branch not protected`, mas a consulta de rulesets para `main` retornou regra ativa exigindo pull request, uma aprovação e o status check `Checar Arquivos Essenciais`.
- Consulta ao GitHub Project #31 não pôde ser realizada por falta do escopo `read:project` no token atual.

## Recomendações para a Equipe

- Manter a ruleset da `main` ativa e, se possível, alinhar também a proteção clássica ou documentar que a governança é aplicada por ruleset.
- Incluir como obrigatório também o check de links, caso ele seja considerado parte do critério de qualidade da equipe.
- Manter os checks existentes e evoluí-los, nas próximas entregas com código, para incluir build, testes e lint reais da aplicação.
- Corrigir a inconsistência de classificação da M-02 entre README, plano de métricas e ficha individual.
- Ajustar a M-05 para deixar claro se mede o recorte inicial da Entrega 3 ou o MVP completo; se medir o MVP completo, estimar também as demais histórias essenciais.
- Indicar nominalmente a disponibilidade semanal de cada integrante ou justificar por que a capacidade foi agrupada por faixas.
- Registrar se todos os integrantes participaram da estimativa ou justificar a participação parcial.
- Manter o baseline atualizado nas próximas etapas, comparando previsão, capacidade planejada e resultados reais.
- Revisar ADRs que ainda citam acervo público ou bancas como parte relevante da solução, pois esses itens foram colocados fora do MVP imediato.

## Nota da Entrega

Nota: 4,3 / 5,0

## Justificativa da Nota

- Planejamento inicial e baseline coerentes com o MVP e o backlog priorizado: 0,9 / 1,0. O baseline é claro, prioriza US01, US02 e US03, dialoga com o MVP e foi registrado dentro do prazo atualizado; a pequena perda decorre de o registro ocorrer próximo ao marco final da entrega.
- Estimativas registradas com técnica, unidade, participantes, hipóteses e limitações: 0,9 / 1,0. A técnica Planning Poker, a escala Fibonacci, os Story Points e as justificativas estão bem documentados; a ressalva é a participação explícita de apenas três integrantes.
- Capacidade planejada da equipe declarada de forma realista e justificada: 0,8 / 1,0. A capacidade total é plausível e há boas restrições documentadas, mas faltou detalhamento nominal da disponibilidade individual.
- Métricas de produto, processo e projeto definidas com objetivo, fórmula, fonte, frequência e interpretação: 0,8 / 1,0. O conjunto é útil e as fichas são completas para esta etapa, mas há perda pela inconsistência de classificação da M-02 e pela M-05 usar o recorte inicial de 13 SP como base para conclusão do MVP completo.
- Evidências no repositório: README atualizado, PR revisado, checks obrigatórios e ramificação `entrega-3` integrada por commit de mesclagem: 0,9 / 1,0. Há README atualizado, PR aprovado, checks bem-sucedidos, merge commit e ruleset exigindo uma aprovação e o check `Checar Arquivos Essenciais`; a perda remanescente ocorre pela ausência de proteção clássica no endpoint antigo e porque o check de links executado não aparece como obrigatório na ruleset.

## Observações sobre Uso de IA

A equipe declarou uso do Gemini na Entrega 3 para estruturação e padronização das fichas de métricas, harmonização do plano de medição, formatação dos documentos de planejamento e apoio na configuração de verificações automatizadas. A declaração é compatível com os artefatos entregues, pois há fichas de métricas em `docs/metricas/`, plano de métricas, baseline, estimativas, README atualizado e alteração no workflow `verificacao-docs.yml`. O registro inclui validação e responsabilidade da equipe, mas poderia ser mais preciso quanto aos trechos efetivamente aproveitados da IA em cada documento. A menção a “testes automatizados” deve ser lida com cautela nesta etapa, pois as evidências do PR #27 mostram checks de documentação e links, não testes automatizados do produto.
