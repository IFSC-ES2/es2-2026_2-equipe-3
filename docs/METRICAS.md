# Plano de Medição e Acompanhamento de Métricas

## 1. Estratégia e Objetivos do Plano de Medição

O acompanhamento de métricas é uma prática essencial para garantir a visibilidade, a qualidade técnica e a previsibilidade das entregas do Sistema de Gestão Centralizada de TCC. Durante esta fase de planejamento, a equipe estabeleceu um conjunto objetivo de indicadores para orientar as tomadas de decisão nas etapas subsequentes de desenvolvimento do MVP.

O plano de medição busca responder às seguintes perguntas fundamentais do projeto:
* **Qualidade do Software**: O código produzido possui cobertura adequada de testes e baixa incidência de defeitos?
* **Eficiência do Processo**: A equipe mantém um fluxo contínuo de integração e revisões de código sem gargalos?
* **Progresso do Projeto**: O escopo planejado para o MVP está avançando dentro do cronograma e da capacidade estipulada?

---

## 2. Categorização das Métricas

Para cobrir todas as dimensões relevantes do desenvolvimento, as métricas foram organizadas em três pilares estratégicos:

```
                          ┌────────────────────────────────┐
                          │   Plano de Medição do Projeto  │
                          └───────────────┬────────────────┘
                                          │
         ┌────────────────────────────────┼────────────────────────────────┐
         │                                │                                │
┌────────┴─────────┐             ┌────────┴─────────┐             ┌────────┴─────────┐
│Métricas de Produto│             │Métricas de Processo│             │Métricas de Projeto│
│ (Qualidade/Código)│             │(Fluxo/Produtividade)           │(Escopo/Cronograma)│
└──────────────────┘             └──────────────────┘             └──────────────────┘
```

1. **Métricas de Produto**: Avaliam os atributos de qualidade do artefato de software produzido, como cobertura de testes automatizados e densidade de defeitos.
2. **Métricas de Processo**: Medem a eficiência e a agilidade do fluxo de trabalho da equipe, focando na velocidade de entregas e no tempo de ciclo dos Pull Requests.
3. **Métricas de Projeto**: Monitoram a evolução do escopo global do MVP em relação à linha de base (baseline) e o cumprimento da capacidade planejada.

---

## 3. Matriz Geral de Métricas Acompanhadas

A tabela a seguir consolida os 6 indicadores selecionados pela equipe. Cada indicador possui uma ficha detalhada acessível pelo link correspondente na tabela:

| Código | Nome da Métrica | Categoria | Objetivo Estratégico | Responsável (Papel) | Ficha Técnica |
| :---: | :--- | :---: | :--- | :--- | :---: |
| **M-01** | Cobertura de Testes de Código | **Produto** | Garantir que o código do backend e frontend possua validação automatizada contínua. | Engenheiro de QA | [`M-01.md`](metricas/M-01.md) |
| **M-02** | Densidade de Defeitos Abertos | **Produto** | Monitorar e conter o surgimento de bugs em módulos entregues. | Engenheiro de QA | [`M-02.md`](metricas/M-02.md) |
| **M-03** | Velocidade da Equipe em SP | **Processo** | Medir a quantidade de Story Points entregues por ciclo/sprint. | Scrum Master | [`M-03.md`](metricas/M-03.md) |
| **M-04** | Lead Time de Pull Requests | **Processo** | Identificar gargalos no tempo entre a abertura do PR e a mesclagem na branch principal. | Engenheiro de DevOps | [`M-04.md`](metricas/M-04.md) |
| **M-05** | Taxa de Conclusão do MVP | **Projeto** | Acompanhar o percentual de conclusão do escopo do MVP em relação aos Story Points totais planejados. | Designer de UX/UI | [`M-05.md`](metricas/M-05.md) |
| **M-06** | Cumprimento da Capacidade | **Projeto** | Comparar as horas/esforço realizados contra a capacidade de 28h/semana declarada. | Arquiteta de Software | [`M-06.md`](metricas/M-06.md) |

---

## 4. Diretrizes de Coleta, Governança e Atualização

### Periodicidade de Coleta
As métricas serão atualizadas periodicamente ao longo das etapas de execução, seguindo a frequência especificada em cada ficha individual (por Pull Request, semanalmente ou ao final de cada Sprint/Marco).

### Responsabilidade e Transparência
Cada métrica possui um papel responsável pela extração dos dados e atualização da tabela de histórico na respectiva ficha técnica. Os resultados serão compartilhados com toda a equipe durante as reuniões de alinhamento e retrospectivas de final de ciclo.

### Ações Corretivas e Limiares
Caso um indicador apresente valor fora da faixa aceitável definida em sua ficha técnica (por exemplo, queda na cobertura de testes abaixo de 70% ou aumento no Lead Time de PRs acima de 48h), o papel responsável convocará uma ação imediata de alinhamento para ajuste do processo ou refatoração.
