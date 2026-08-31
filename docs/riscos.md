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
* **Estratégia de Mitigação:** Adotar pareamentos eventuais e garantir que todo o código e infraestrutura estejam devidamente documentados no repositório. Nenhuma parte do sistema deve ser de conhecimento exclusivo de apenas uma pessoa.
* **Responsável pelo Acompanhamento:** Scrum Master (`Eduardo Cardoso`)

### R02 - Falhas de Regressão por Ausência de Testes e CI Quebrado
* **Natureza:** Qualidade / Processo
* **Identificação:** Inserção de bugs no código principal por falta de validações automatizadas em Pull Requests.
* **Descrição:** Sem um pipeline de CI rigoroso, a equipe pode integrar código com erros de sintaxe ou que quebram funcionalidades.
* **Causa:** Pressa na entrega e falta de cultura de testes automatizados.
* **Consequência / Impacto Esperado:** Instabilidade do sistema e tempo desperdiçado corrigindo problemas na branch `main`.
* **Probabilidade:** Alta (3)
* **Impacto:** Médio (2)
* **Prioridade:** Alta (6)
* **Estratégia de Mitigação:** Configurar o CI mínimo com bloqueio de merge em caso de falha de lint ou build. Exigir revisão de código obrigatória.
* **Responsável pelo Acompanhamento:** Responsável por Qualidade (`Talles Souza`)

### R03 - Atraso no Cronograma de Integração das APIs de Backend e Frontend
* **Natureza:** Prazo
* **Identificação:** Atraso na disponibilização dos endpoints da API REST, gerando dependência técnica e bloqueando o fluxo de desenvolvimento das interfaces de usuário.
* **Descrição:** O tempo de implementação do backend pode ultrapassar as estimativas iniciais, criando um efeito cascata que impede a equipe de frontend de integrar, consumir os dados e testar as telas dentro do prazo planejado para a sprint.
* **Causa:** Subestimativa de esforço na modelagem relacional do banco de dados, tratamento de exceções não previstas ou alta complexidade na implementação das regras de negócio nas rotas.
* **Consequência / Impacto Esperado:** Sobrecarga da equipe nas vésperas da entrega, integração feita às pressas, aumento considerável no risco de bugs funcionais e potencial comprometimento da validação final do MVP.
* **Probabilidade:** Alta (3)
* **Impacto:** Alto (3)
* **Prioridade:** Crítica (9)
* **Estratégia de Mitigação:** Priorizar o desenvolvimento das rotas críticas e utilizar mocks de API no frontend enquanto o backend real não estiver concluído.
* **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

### R04 - Crescimento Não Planejado do Escopo (Scope Creep)
* **Natureza:** Escopo
* **Identificação:** Inclusão contínua de novas funcionalidades no backlog sem avaliação criteriosa de impacto no prazo.
* **Descrição:** Novas funcionalidades podem ser sugeridas ao longo do semestre, inflando o escopo além da capacidade real de entrega da equipe e desviando o foco dos requisitos essenciais estabelecidos inicialmente.
* **Causa:** Entusiasmo natural com a evolução do produto e tentativa de absorver todo tipo de feedback, levando a decisões de adição de escopo sem a devida análise de custo e tempo.
* **Consequência / Impacto Esperado:** MVP incompleto ao final do semestre, com estouro do prazo e o risco de comprometer as funcionalidades centrais e essenciais do sistema em favor de itens secundários adicionados no meio do caminho.
* **Probabilidade:** Média (2)
* **Impacto:** Médio (2)
* **Prioridade:** Média (4)
* **Estratégia de Mitigação:** O escopo principal deve ser congelado, utilizando os critérios definidos na documentação do MVP como filtro rigoroso. Ideias não essenciais devem ser obrigatoriamente movidas para um backlog de futuro.
* **Responsável pelo Acompanhamento:** Scrum Master (`Eduardo Cardoso`) / Product Owner (`Adriano Luiz de Souza`)

### R05 - Divergência de Ambientes e Instabilidade na Orquestração (Docker)
* **Natureza:** Tecnologia
* **Identificação:** O sistema apresenta comportamentos inesperados ou falhas de execução dependendo da máquina do desenvolvedor ou do ambiente de deploy.
* **Descrição:** A falta de alinhamento na configuração da infraestrutura local pode fazer com que serviços (como o banco de dados SQL ou a API) rodem perfeitamente para um membro, mas falhem para outro ou no servidor.
* **Causa:** Uso de versões diferentes de dependências, falta de padronização nas imagens dos contêineres e variáveis de ambiente não versionadas ou mal documentadas.
* **Consequência / Impacto Esperado:** Perda severa de tempo em sessões de depuração, bloqueio de tarefas de integração e atrasos no pipeline de entrega.
* **Probabilidade:** Alta (3)
* **Impacto:** Médio (2)
* **Prioridade:** Alta (6)
* **Estratégia de Mitigação:** Padronizar rigorosamente os arquivos `Dockerfile` e `docker-compose.yml`, exigindo que todo o desenvolvimento de rotas e testes seja executado exclusivamente via contêineres padronizados.
* **Responsável pelo Acompanhamento:** DevOps/Infra (`Willian Ferreira`)

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
| **R03** | Atraso no cronograma das APIs | Prazo | Alta (3) | Alto (3) | **Crítica (9)** |
| **R01** | Desistência de membro da equipe | Equipe | Média (2) | Alto (3) | **Alta (6)** |
| **R02** | Falhas por ausência de testes/CI | Qualidade | Alta (3) | Médio (2) | **Alta (6)** |
| **R05** | Divergência de ambientes (Docker) | Tecnologia | Alta (3) | Médio (2) | **Alta (6)** |
| **R04** | Crescimento do escopo | Escopo | Média (2) | Médio (2) | **Média (4)** |

### 2.3 Justificativa das Prioridades 

- O risco **R03** é crítico pois paralisa o avanço geral do sistema. 

- O risco **R01** recebe prioridade alta porque a perda de um integrante reduz a capacidade produtiva, o que pode inviabilizar a entrega de todas as funcionalidades dentro do cronograma da disciplina.

---

## 3. Plano de Resposta aos Riscos

### 3.1 Ações Preventivas

* **R01:** Compartilhar o conhecimento técnico por meio de programação em pares e documentação constante.
* **R02:** Configurar a Integração Contínua (CI) com bloqueios antes de escalar o volume de código.
* **R03:** Estabelecer o contrato de dados no início do ciclo.
* **R04:** Congelar o escopo principal do MVP e submeter qualquer sugestão de nova funcionalidade a um filtro rigoroso, movendo ideias não essenciais para um backlog futuro.
* **R05:** Versionar todas as configurações de infraestrutura e dependências no repositório, garantindo que o setup inicial seja feito com um único comando de orquestração.

### 3.2 Ações caso o risco ocorra

* **Se R01 ocorrer:** Repriorizar o backlog, cortando funcionalidades não essenciais para adequar o escopo à nova capacidade da equipe.
* **Se R02 ocorrer:** Paralisar a aprovação de novos Pull Requests. A equipe deve realizar a correção de todos os erros/bugs adicionados na branch *main*, priorizando a estabilidade do sistema antes de continuar o desenvolvimento de novas funcionalidades.
* **Se R03 ocorrer:** O frontend passa a consumir dados estáticos (mocks) temporariamente para não travar a evolução das telas.
* **Se R04 ocorrer:** Realizar imediatamente uma reunião de repriorização de emergência. Congelar o desenvolvimento de qualquer funcionalidade secundária, mesmo que já esteja em andamento, e redirecionar 100% da força produtiva da equipe exclusivamente para finalizar os requisitos essenciais do MVP aprovados no planejamento inicial.
* **Se R05 ocorrer:** O desenvolvedor afetado deve interromper a codificação e recriar seu ambiente do zero a partir de uma imagem limpa. Caso o erro persista, o responsável por DevOps deve priorizar o pareamento com este membro para corrigir o script de orquestração antes que afete o restante da equipe.

### 3.3 Acompanhamento da Evolução
