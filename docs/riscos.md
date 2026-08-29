# Registro de Riscos

Este documento apresenta o levantamento de riscos do projeto, contemplando a identificação, análise, priorização e o plano de resposta para mitigar impactos na execução e entrega do produto.

Este documento deve ser revisitado e atualizado ao final de cada sprint.

---

## 1. Detalhamento dos riscos

### R01 - Desistência ou Abandono de Membro da Equipe
* **Natureza:** Equipe
* **Identificação:** Um ou mais integrantes podem trancar a matrícula ou deixar o projeto durante o semestre.
* **Descrição:** A perda de um membro da equipe reduz a força de trabalho e pode causar a perda de conhecimento específico de partes do sistema.
* **Causa:** Dificuldade de adaptação, sobrecarga acadêmica com outras disciplinas ou problemas pessoais.
* **Consequência / Impacto Esperado:** Sobrecarga dos membros restantes, atraso nas entregas das sprints e necessidade de redistribuição abrupta de tarefas.
* **Probabilidade:** Média (2)
* **Impacto:** Alto (3)
* **Prioridade:** Alta (6)
* **Estratégia de Mitigação:** Adotar pareamento esporádico e garantir que todo o código e infraestrutura estejam devidamente documentados no repositório. Nenhuma parte do sistema deve ser de conhecimento exclusivo de apenas uma pessoa.
* **Responsável pelo Acompanhamento:** Scrum Master (Eduardo Cardoso)

---

## 2. Análise e Priorização dos Riscos

### 2.1 Critérios Adotados

A classificação dos riscos utiliza uma matriz baseada em duas dimensões:
* **Probabilidade (P):** Baixa (1), Média (2), Alta (3)
* **Impacto (I):** Baixo (1), Médio (2), Alto (3)

**Fórmula de Exposição ao Risco (Prioridade):** `P × I`
* **Baixa:** 1 a 2
* **Média:** 3 a 4
* **Alta:** 6
* **Crítica:** 9

### Matriz de Prioridade (Probabilidade × Impacto)

| | Impacto Baixo (1) | Impacto Médio (2) | Impacto Alto (3) |
| :--- | :---: | :---: | :---: |
| **Prob. Alta (3)** | Média (3) | Alta (6) | Crítica (9) |
| **Prob. Média (2)** | Baixa (2) | Média (4) | Alta (6) |
| **Prob. Baixa (1)** | Baixa (1) | Baixa (2) | Média (3) |

### 2.2 Matriz de Riscos

| ID | Risco | Natureza | Probabilidade | Impacto | Prioridade |
|:---|:---|:---|:---:|:---:|:---:|
| **R01** | Desistência de membro da equipe | Equipe | Média (2) | Alto (3) | **Alta (6)** |

### 2.3 Justificativa das Prioridades 

O risco **R01** recebe prioridade alta porque a perda de um integrante reduz a capacidade produtiva, o que pode inviabilizar a entrega de todas as funcionalidades dentro do cronograma da disciplina.

---

## 3. Plano de Resposta aos Riscos

### 3.1 Ações Preventivas (Mitigação)
* **R01:** Compartilhar o conhecimento técnico por meio de programação em pares e documentação constante.

### 3.2 Ações Contingenciais (Caso o risco ocorra)
* **Se R01 ocorrer:** Repriorizar o backlog, cortando funcionalidades não essenciais para adequar o escopo à nova capacidade da equipe.

### 3.3 Acompanhamento da Evolução
