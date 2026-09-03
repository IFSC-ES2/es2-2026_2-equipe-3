# Fluxo de Trabalho e Governança do Projeto

Este documento descreve as diretrizes, convenções e rotinas adotadas pela equipe para o desenvolvimento e manutenção do **Sistema de Gestão Centralizada de TCC**. Ele estabelece as normas operacionais para versionamento de código, gerenciamento de tarefas, cerimônias ágeis, gestão de qualidade e automações no pipeline de integração contínua (CI).

---

## 1. Organização das Cerimônias Ágeis

Com base na capacidade estipulada em [`docs/BASELINE.md`](BASELINE.md) (28 horas semanais compartilhadas entre os 5 integrantes), a equipe adota uma abordagem ágil adaptada à rotina de trabalhos no horário comercial e às demandas acadêmicas simultâneas.

### 1.1 Sprints e Marcos (Milestones)
- **Duração da Sprint:** Sprints com duração de **1 semana** (ou alinhadas aos marcos de entregas da disciplina).
- **Quadro de Gestão:** Acompanhamento via **GitHub Projects (Board Kanban)**, estruturado nas colunas:
    - `Product Backlog`: Mapeamento geral de todas as User Stories e tarefas do MVP.
    - `To-Do`: Tarefas refinadas, estimadas e selecionadas para execução na sprint atual.
    - `In Progress`: Tarefas em desenvolvimento ativo.
    - `In Review`: Pull Requests abertos aguardando revisão de código.
    - `Done`: Itens concluídos que atendem integralmente à [Definição de Pronto (DoD)](dod.md).
    - `Versões Futuras`: Ideias e funcionalidades identificadas fora do escopo do MVP (utilizada como mecanismo de contenção do Risco R04 - Scope Creep).

### 1.2 Encontros e Cerimônias da equipe
- **Daily Scrum Assíncrona:** Devido às restrições de horário comercial dos integrantes, o alinhamento diário é realizado de forma **assíncrona** no canal de comunicação do grupo. Cada membro reporta brevemente:
    1. O que foi realizado desde o último reporte;
    2. O que pretende realizar a seguir;
    3. Existência de impedimentos, dúvidas de arquitetura ou bloqueios nas APIs.
- **Sprint Planning & Review:** Cerimônias realizadas semanalmente de forma síncrona/híbrida para alinhamento de metas, redistribuição de tarefas e atualização do backlog.
- **Sprint Retrospective:** Cerimônia dedicada à revisão dos processos e atualização do documento [`docs/riscos.md`](riscos.md) e das Fichas de Métricas (`docs/metricas/`).

---

## 2. Estratégia de Versionamento e Regras de Branches

A equipe adota um modelo de ramificação baseado no **GitHub Flow** adaptado para entregas acadêmicas, garantindo isolamento de contexto e estabilidade da branch principal.


```

main (estável / produção)
│
├─► entrega-2
├─► entrega-3
└─► entrega-4
│
├──► feature/US01-autenticacao
├──► feature/US02-cadastro-orientador
├──► fix/ajuste-validacao-vagas
└──► docs/fluxo-de-trabalho

```

### 2.1 Regras de Branches
- **`entrega-4`:** É a branch base obrigatória para todo o desenvolvimento da etapa atual. Nenhuma `feature` ou `fix` da etapa 4 deve ser apontada direto para a `main`.
- **Proibição de Envio Direto para a Main:** É **estritamente proibido** fazer `push` direto para o ramo principal (`main`). Todas as alterações devem obrigatoriamente passar por Pull Request.
- **Branches de Funcionalidade (`feature/`)**: Criadas a partir de `entrega-4` para desenvolvimento de histórias ou tarefas específicas.
    - *Nomenclatura:* `feature/<codigo-us>-<descricao-curta>` ou `feature/<escopo>`.
- **Branches de Correção (`fix/`)**: Criadas para correção de defeitos reportados.
    - *Nomenclatura:* `fix/<issue-id>-<descricao-curta>`.
- **Branches de Documentação (`docs/`)**: Dedicadas à elaboração ou atualização de documentos Markdown.
    - *Nomenclatura:* `docs/<nome-do-documento>`.

---

## 3. Padrões de Commits

A equipe adota o padrão **Conventional Commits** para manter o histórico claro, rastreável e legível.

### 3.1 Formato da Mensagem

`<tipo>: <descrição sucinta em português no imperativo>`


### 3.2 Tipos Permitidos
- `feat`: Introdução de uma nova funcionalidade
- `fix`: Correção de um erro/bug
- `docs`: Alterações na documentação
- `style`: Formatação, ajuste de espaçamentos ou regras de lint/Prettier.
- `refactor`: Refatoração de código sem alteração do comportamento final.
- `test`: Adição ou ajuste de testes automatizados (JUnit 5, Mockito).
- `chore`: Atualização de scripts de build, dependências ou configurações de infraestrutura (Docker, GitHub Actions).

---

## 4. Integração via Pull Request (PR) e Processo de Revisão

Para mitigar os riscos de regressão (**R02**) e divergência de ambientes (**R05**), todas as integrações de código ocorrem exclusivamente via Pull Requests.

### 4.1 Regras de Revisão e Aprovação
- **Integração Exclusiva via PR:** Nenhuma mudança entra na branch de entrega ou na `main` sem um PR associado.
- **Revisores e Aprovação Mínima:** Exige-se o parecer e a aprovação formal de **no mínimo 1 (um) integrante da equipe** (diferente do autor da alteração) para que o PR possa ser integrado.
- **Integração Apenas por Commit de Mesclagem:** A integração na branch alvo ocorre estritamente por **commit de mesclagem**, mantendo o histórico de ramificação explícito e auditável.

### 4.2 Template e Checklist Mínimo de Revisão para PRs
Todo Pull Request aberto deve utilizar a seguinte estrutura na descrição:

```markdown
## Descrição da Alteração
[Descreva sucintamente o que foi implementado ou corrigido neste PR]

## Issue / US Relacionada
Closes #[número da issue]

## Checklist Mínimo de Revisão
- [ ] O código/documentação atende aos requisitos descritos na tarefa.
- [ ] Foi desenvolvida e testada a partir da branch `entrega-4`.
- [ ] Passou com sucesso em todos os checks automatizados de CI (build, lint, formatação Prettier e verificação de links).
- [ ] Foi testado em ambiente containerizado via Docker (quando aplicável ao código).
- [ ] O controle de acesso e permissões por perfil (Aluno/Professor/Coordenador) foi validado (Segurança.)
- [ ] A documentação do projeto foi atualizada, se necessário.
- [ ] Não contém conflitos com a branch de destino.
- [ ] Recebeu ao menos 1 aprovação de outro membro da equipe.

```
---

## 5. Checks Obrigatórios Executados nos Pull Requests (CI/CD)

Cada Pull Request aberto no repositório dispara automaticamente o pipeline de Integração Contínua (CI) orquestrado via GitHub Actions, conforme detalhado no documento [docs/ci.md](cd.md). O merge é bloqueado caso qualquer um desses checks falhe:

- **`verificacao-docs.yml`**: Valida a sintaxe, formatação e estrutura dos arquivos Markdown (`.md`).
- **`verificar-links.yml`**: Executa a varredura rigorosa de links internos e externos em busca de links quebrados ou referências incorretas entre a documentação de planejamento (`BASELINE.md`, `METRICAS.md`) e as fichas de métricas (`M-01.md` a `M-06.md`).
- **`verificar-formatacao.yml`**: Executa a verificação automatizada via **Prettier** para garantir que arquivos de código e documentação (`.md`, `.json`, `.yml`, etc.) sigam padrões consistentes de espaçamento e indentação.

---

## 6. Garantia de Qualidade e Definição de Pronto (DoD)

A aceitação de cada entrega é regida pela [Definição de Pronto (DoD)](dod.md), pelas políticas de CI em [docs/ci.md](ci.md) e pelos atributos de qualidade da norma ISO/IEC 25010 priorizados no documento [Qualidade do Software](qualidade.md):

A qualidade é monitorada continuamente através das Fichas de Métricas:

| Métrica | Nome | Meta / Limiar | Papel Responsável |
| --- | --- | --- | --- |
| **M-01** | Cobertura de Testes de Código | $\ge 80\%$ no Backend / $\ge 70\%$ no Frontend | Engenheiro de QA |
| **M-02** | Densidade de Defeitos Abertos | $0$ defeitos críticos e $\le 2$ defeitos médios | Engenheiro de QA |
| **M-03** | Velocidade da Equipe em SP | Estabilidade em relação aos $13 \text{ SP}$ previstos | Scrum Master |
| **M-04** | Lead Time de Pull Requests | $\le 24 \text{ horas}$ | Engenheiro de DevOps |
| **M-05** | Taxa de Conclusão do MVP | Desvio $\le 10\%$ do cronograma | Designer de UX/UI |
| **M-06** | Cumprimento da Capacidade | $90\%$ a $110\%$ das $28\text{h/semana}$ planejadas | Arquiteta de Software |
