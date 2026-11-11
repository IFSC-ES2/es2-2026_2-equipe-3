# Registro da Abordagem de Estimativa

## 1. Técnica Utilizada e Ferramenta

A equipe adotou a técnica de **Planning Poker**, uma abordagem baseada em consenso e estimativa relativa.
Para o dimensionamento das tarefas, foi adotada a escala simplificada de **Fibonacci (1, 2, 3, 5, 8, 13)**, permitindo estimar a complexidade e o esforço relativo entre cada História de Usuário.

## 2. Participantes do Processo

A sessão de estimativa foi realizada com a participação dos seguintes integrantes da equipe:

- **Eduardo Cardoso Oliveira** (Engenheiro de Requisitos / Scrum Master)
- **Talles Souza da Cruz** (Engenheiro de Qualidade - QA)
- **Damares do Socorro Gonçalves Gaia** (Arquiteta de Software)

## 3. Unidade de Medida Adotada

A unidade adotada para o dimensionamento do trabalho foi **Story Points (SP)**.
A escolha por Story Points em vez de horas absolutas visa focar na complexidade, no esforço arquitetural e nos riscos de cada funcionalidade, abstraindo variações individuais de produtividade dos membros.

## 4. Critérios de Comparação e Dimensionamento dos Itens

Para estabelecer a referência de estimativa entre os itens do backlog priorizado, a equipe utilizou os seguintes critérios técnicos:

### **US01 - Autenticação e Perfis de Usuário (5 Story Points)**

- **Justificativa da Pontuação (Complexidade Alta):**
  - Por ser a primeira história a ser implementada, ela carrega o impacto de estabelecer a **fundação arquitetural do backend em Spring Boot e do frontend em React**.
  - Exige a criação do modelo de dados inicial, DTOs, mecanismos de criptografia/segurança, tratamento de sessão e a lógica de controle de acesso para os três perfis distintos do sistema (Aluno, Professor e Coordenador).
  - Requer a estruturação das rotas protegidas no React com TypeScript e a configuração inicial do contrato de APIs.

### **US02 - Cadastro de Perfil de Orientador e Vagas (5 Story Points)**

- **Justificativa da Pontuação (Complexidade Alta):**
  - Envolve o modelo de dados central do produto (mapeamento de linhas de pesquisa, áreas de atuação e disponibilidade de vagas do professor).
  - Exige a implementação das regras de validação de negócio e persistência relacional complexa (relacionamentos Entidade-Relacionamento no MySQL via Spring Data JPA/Hibernate).
  - Requer telas do frontend com formulários reativos para cadastro e edição dinâmica de linhas de pesquisa.

### **US03 - Busca e Filtragem de Orientadores (3 Story Points)**

- **Justificativa da Pontuação (Complexidade Média):**
  - Se trata de uma funcionalidade de **leitura e exibição de dados (Query/Read)**.
  - Reaproveita toda a infraestrutura e os modelos de dados já construídos e consolidados nas tarefas anteriores (US01 e US02).
  - A complexidade está na construção do mecanismo de filtro dinâmico por área de pesquisa no Spring Data JPA e na montagem dos componentes visuais do catálogo no frontend.

### **US04 - Solicitação e Aceite de Orientação (5 Story Points)**

- **Justificativa da Pontuação (Complexidade Alta):**
  - Introduz o fluxo central de "match" entre aluno e orientador, com uma nova entidade (`SolicitacaoOrientacao`) e uma máquina de estados (`PENDENTE`, `ACEITA`, `RECUSADA`).
  - Exige regras de elegibilidade antes da criação da solicitação (orientador ativo, vagas disponíveis e ausência de solicitação pendente duplicada) e o decremento de vagas no aceite, dentro da mesma transação.
  - Envolve telas para os dois perfis: o envio e o acompanhamento pelo aluno e o painel de aceite ou recusa pelo orientador.

### **US05 - Calendário de Prazos (5 Story Points)**

- **Justificativa da Pontuação (Complexidade Alta):**
  - Exige a modelagem da `Turma_TCC` vigente e do professor responsável por ela, que passa a ser o único autorizado a cadastrar e editar prazos.
  - Contém regras de validação de datas: rejeição de prazos no passado e de sequências inválidas entre Projeto de TCC, TCC 1 e TCC 2. Para testar essas regras, a data atual precisa ser controlada nos testes.
  - Envolve telas distintas por perfil: a gestão do calendário pelo professor responsável e a contagem regressiva dos prazos nos painéis de aluno e professor.

### **US06 - Submissão de Entregas do TCC (3 Story Points)**

- **Justificativa da Pontuação (Complexidade Média):**
  - O escopo foi reduzido pela ADR-0013: o aluno informa um link externo HTTPS, sem upload, download ou armazenamento de arquivos no sistema.
  - Reaproveita a autenticação por papel, o vínculo de orientação da US04 e os prazos da US05, usados para marcar a entrega como atrasada.
  - A complexidade está nas regras de negócio: a submissão fora do prazo é aceita e sinalizada, e o reenvio só é permitido quando o último parecer pede ajustes.

### **US07 - Avaliação e Parecer das Entregas (3 Story Points)**

- **Justificativa da Pontuação (Complexidade Média):**
  - Reaproveita a entidade de entrega e o vínculo entre orientador e aluno da US06, acrescentando a entidade `Parecer` com o histórico de avaliações.
  - Exige a consulta das entregas dos orientandos e o registro do parecer (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`), com comentário obrigatório para ajustes e reprovação.
  - A complexidade está no controle de estado da entrega: um parecer final encerra a etapa, e um pedido de ajustes libera o reenvio pelo aluno. Não há envio de notificações por e-mail.

### **US08 - Agendamento e Organização de Banca (5 Story Points)**

- **Justificativa da Pontuação (Complexidade Alta):**
  - Introduz um novo módulo, a gestão de bancas, com novas entidades para a banca e para os professores examinadores convidados.
  - Exige regras de negócio próprias: só pode ser agendada a banca de um TCC já aprovado para defesa, com data, horário, local ou link de videoconferência e dois professores examinadores.
  - Envolve mais de um perfil de professor: o orientador agenda a banca, e os membros convidados precisam visualizar o agendamento em seus painéis.

---

## 5. Limitações e Incertezas Percebidas

Durante o processo de estimativa, a equipe identificou as seguintes limitações, riscos e incertezas que sustentam as pontuações atribuídas:

1. **Curva de Integração Inicial da Stack:** A necessidade de conectar do zero a API Java/Spring Boot com o banco MySQL containerizado e o frontend em React/TypeScript cria incertezas sobre o tempo necessário de _setup_ inicial.
2. **Definição dos Contratos de API:** Ajustes nos DTOs de entrada e saída entre backend e frontend durante o desenvolvimento podem exigir pequenas refatorações não previstas nas USs iniciais.
3. **Disponibilidade e Trabalho Assíncrono:** A restrição de tempo dos integrantes decorrente de jornadas de trabalho em horário comercial impõe a necessidade de um fluxo rigoroso de testes e Code Reviews assíncronos, o que pode diluir o tempo de conclusão de cada SP ao longo da semana.

--- 

## 6. Justificativa de não participação

Durante os três períodos de estimativas de US's, alguns membros do grupo não participaram das estimativas, seguem justificativas abaixo:

## Estimativa 1 (US's 01, 02 e 03)

Os membros Marcus e Willian não participaram da estimativas com planning poker e não apresentaram justificativas para o não comparecimento. Embora não comparecerem, o membro Willian entrou em contato com o resto dos membros do grupo e aprovou a estimativa realizada pelos outros três membros.

## Estimativa 2 (US 04)

O membro Marcus não participou da estimativa com planning poker, não apresentou nenhuma justificativa ou aprovação dos pontos estimados para a história de usuário.

## Estimativa 3 (US's 05, 06, 07 e 08)

O membro Marcus não participou da estimativa com planning poker, não apresentou nenhuma justificativa ou aprovação dos pontos estimados para a história de usuário.
