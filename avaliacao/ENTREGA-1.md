# Avaliação da Entrega 1 - Kickoff

## Identificação

- Equipe: es2-2026_2-equipe-3
- Projeto: Sistema de Gestão Centralizada de TCC
- Entrega: 1 - Kickoff
- Data limite considerada para avaliação: 07/08/2026

## Documentos Consultados

- `README.md` da equipe.
- `USO-IA.md` da equipe.
- `.github/ISSUE_TEMPLATE/tarefa_padrao.md`.
- `.github/PULL_REQUEST_TEMPLATE.md`.
- Issues GitHub #1, #2 e #3.
- Pull request GitHub #4.
- Histórico Git local e remoto.

## Resumo da Entrega

A equipe entregou um README inicial para um sistema web de gestão centralizada de TCC. A documentação apresenta a equipe, matrículas, papéis, contextualização do tema, usuários, proposta de solução, escopo de MVP e itens fora do escopo. O repositório também contém registro de uso de IA, template de issue, template de pull request, issues de organização e branch `entrega-1` integrada à `main` por pull request.

A entrega está bem estruturada em documentação e rastreabilidade inicial, mas possui lacunas em relação ao escopo do MVP e à governança verificável do pull request, especialmente pela ausência de aprovação formal registrada pela API do GitHub e ausência de checks.

## Critérios Atendidos

- O README apresenta o nome do projeto e uma definição clara do sistema.
- O README lista cinco integrantes, matrículas e papéis iniciais.
- O Scrum Master da primeira sprint está identificado.
- Há papéis de Arquiteta de Software, DevOps/Infra e Engenheiro de Qualidade.
- O tema está associado à linha de educação e apoio à aprendizagem.
- O README responde às perguntas obrigatórias sobre problema, área de aplicação, usuários, contexto de aplicação, relevância e proposta de solução.
- O sistema proposto é uma aplicação web orientada a objetos, em conformidade com as restrições gerais declaradas.
- O README apresenta funcionalidades principais do MVP e itens fora do escopo.
- O repositório possui template de issue com critérios de aceitação.
- O repositório possui template de pull request com checklist de autor e checklist de revisor.
- Há issues criadas para organizar o kickoff, com critérios de aceitação: #1, #2 e #3.
- A entrega foi desenvolvida na branch `entrega-1` e integrada à `main` por merge commit do PR #4.
- O registro de uso de IA declara ferramenta, finalidade e validação pela equipe.
- A entrega foi integrada em 07/08/2026, dentro do prazo estendido considerado para a Entrega 1.

## Critérios Parcialmente Atendidos

- O MVP está bem descrito, mas ainda é relativamente amplo para uma primeira versão, pois inclui autenticação, catálogo de orientadores, gestão de documentos e prazos, gestão de bancas e acervo público acadêmico.
- A issue #3 descreve o fluxo de integração via pull request, mas foi fechada com itens de aceite ainda desmarcados no corpo da issue, embora o PR tenha sido mesclado.
- O PR #4 contém checklist de revisor marcado no corpo do PR, mas a consulta do GitHub retornou `reviews: []`, sem aprovação formal registrada como review.
- O repositório está com Projects habilitado, mas não foi possível verificar o board utilizado porque a consulta a Projects V2 exige escopo `read:project`, indisponível no token atual.
- A consulta de proteção da branch `main` não pôde confirmar regras de proteção por limitação do plano/permissão do GitHub, retornando mensagem de indisponibilidade do recurso.

## Critérios Não Atendidos

- Não há evidência de checks obrigatórios executados no PR #4; a consulta retornou `statusCheckRollup: []`.
- Não há aprovação formal registrada no PR #4 via mecanismo de review do GitHub; a consulta retornou `reviews: []`.
- Não há milestone criada para a entrega; a API de milestones retornou lista vazia.

## Achados com Evidências

- Equipe com cinco integrantes, composição aceita para esta entrega: `README.md`, linhas 3-8.
- Matrículas informadas: `README.md`, linhas 4-8.
- Scrum Master identificado: `README.md`, linha 5.
- Papéis de arquitetura, qualidade e infraestrutura identificados: `README.md`, linhas 4, 7 e 8.
- Tema e contextualização: `README.md`, linhas 11-31.
- Escopo do MVP: `README.md`, linhas 33-42.
- Itens fora do escopo: `README.md`, linhas 44-48.
- Registro de IA com validação: `USO-IA.md`, linhas 1-9.
- Template de issue com critérios de aceitação: `.github/ISSUE_TEMPLATE/tarefa_padrao.md`, linhas 9-15.
- Template de PR com checklists: `.github/PULL_REQUEST_TEMPLATE.md`, linhas 1-15.
- Issues de kickoff com critérios de aceitação: issues #1, #2 e #3.
- Issue #3 fechada com critérios ainda desmarcados no corpo: issue #3, critérios de PR aberto, aprovação e merge permanecem como `- [ ]` na consulta da issue.
- PR de entrega existente e mesclado: PR #4, branch `entrega-1` para `main`, estado `MERGED`, URL `https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/4`.
- PR sem review formal registrado: consulta do PR #4 retornou `reviews: []`.
- PR sem checks registrados: consulta do PR #4 retornou `statusCheckRollup: []`.
- Integração por merge commit: commit `162cd98`, mensagem `Merge pull request #4 from IFSC-ES2/entrega-1`.
- Branch de entrega preservada no remoto: `origin/entrega-1` aponta para o commit `efcbf16`.
- Ausência de milestone: API de milestones retornou `[]`.

## Recomendações para a Equipe

- Priorizar o MVP para uma fatia inicial menor, distinguindo funcionalidades essenciais da validação inicial e funcionalidades candidatas para entregas futuras.
- Garantir que os próximos pull requests tenham aprovação formal registrada via review do GitHub, não apenas checklist marcado no corpo do PR.
- Configurar ou planejar checks obrigatórios para pull requests.
- Manter as issues consistentes com o estado real do trabalho, marcando critérios de aceitação concluídos antes de fechá-las ou justificando a divergência.
- Criar milestones por entrega ou por marco para melhorar a rastreabilidade.
- Evidenciar o board de acompanhamento e vincular as issues ao fluxo utilizado pela equipe.

## Nota da Entrega

Nota: 4,3 / 5,0

## Justificativa da Nota

- Equipe formada dentro do prazo: 1,0 / 1,0. A equipe informa nomes, matrículas e papéis, incluindo Scrum Master, Arquiteta de Software, DevOps/Infra e Engenheiro de Qualidade; a composição com cinco integrantes foi aceita para esta entrega.
- Tema definido de forma clara, contextualizada, relevante e em conformidade com as restrições: 1,8 / 2,0. O tema é claro, relevante e bem contextualizado, mas o MVP proposto ainda é amplo para a orientação de poucas funcionalidades principais.
- README inicial preenchido, incluindo escopo do MVP e informações básicas: 0,9 / 1,0. O README cobre os itens principais, com pequena perda pela necessidade de melhor delimitação do MVP.
- Governança mínima do repositório: 0,6 / 1,0. Há branch de entrega, PR, merge commit, issues, templates e critérios de aceitação, mas faltam aprovação formal registrada em review, checks obrigatórios, milestone e evidência verificável de board.

## Observações sobre Uso de IA

A equipe declarou uso do Gemini para ideação, escopo e estruturação da documentação. A declaração é compatível com os artefatos entregues, pois o README apresenta tema, escopo, MVP e estruturação documental alinhados aos usos declarados. O registro informa validação pela equipe e inclui declaração de responsabilidade acadêmica e técnica, atendendo aos princípios de transparência e responsabilidade do protocolo. Para próximas entregas, recomenda-se detalhar melhor os artefatos específicos impactados e exemplos do que foi aproveitado.
