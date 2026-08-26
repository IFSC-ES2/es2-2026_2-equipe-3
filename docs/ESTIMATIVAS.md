# Registro da Abordagem de Estimativa

## 1. Técnica Utilizada e Ferramenta
A equipe adotou a técnica de **Planning Poker**, uma abordagem baseada em consenso e estimativa relativa. 
Para o dimensionamento das tarefas, foi adotada a escala simplificada de **Fibonacci (1, 2, 3, 5, 8, 13)**, permitindo estimar a complexidade e o esforço relativo entre cada História de Usuário.

## 2. Participantes do Processo
A sessão de estimativa foi realizada com a participação dos seguintes integrantes da equipe:
* **Eduardo Cardoso Oliveira** (Engenheiro de Requisitos / Scrum Master)
* **Talles Souza da Cruz** (Engenheiro de Qualidade - QA)
* **Damares do Socorro Gonçalves Gaia** (Arquiteta de Software)

## 3. Unidade de Medida Adotada
A unidade adotada para o dimensionamento do trabalho foi **Story Points (SP)**. 
A escolha por Story Points em vez de horas absolutas visa focar na complexidade, no esforço arquitetural e nos riscos de cada funcionalidade, abstraindo variações individuais de produtividade dos membros.

## 4. Critérios de Comparação e Dimensionamento dos Itens

Para estabelecer a referência de estimativa entre os itens do backlog priorizado, a equipe utilizou os seguintes critérios técnicos:

### **US01 - Autenticação e Perfis de Usuário (5 Story Points)**
* **Justificativa da Pontuação (Complexidade Alta):** 
  * Por ser a primeira história a ser implementada, ela carrega o impacto de estabelecer a **fundação arquitetural do backend em Spring Boot e do frontend em React**.
  * Exige a criação do modelo de dados inicial, DTOs, mecanismos de criptografia/segurança, tratamento de sessão e a lógica de controle de acesso para os três perfis distintos do sistema (Aluno, Professor e Coordenador).
  * Requer a estruturação das rotas protegidas no React com TypeScript e a configuração inicial do contrato de APIs.

### **US02 - Cadastro de Perfil de Orientador e Vagas (5 Story Points)**
* **Justificativa da Pontuação (Complexidade Alta):**
  * Envolve o modelo de dados central do produto (mapeamento de linhas de pesquisa, áreas de atuação e disponibilidade de vagas do professor).
  * Exige a implementação das regras de validação de negócio e persistência relacional complexa (relacionamentos Entidade-Relacionamento no MySQL via Spring Data JPA/Hibernate).
  * Requer telas do frontend com formulários reativos para cadastro e edição dinâmica de linhas de pesquisa.

### **US03 - Busca e Filtragem de Orientadores (3 Story Points)**
* **Justificativa da Pontuação (Complexidade Média):**
  * Se trata de uma funcionalidade de **leitura e exibição de dados (Query/Read)**.
  * Reaproveita toda a infraestrutura e os modelos de dados já construídos e consolidados nas tarefas anteriores (US01 e US02).
  * A complexidade está na construção do mecanismo de filtro dinâmico por área de pesquisa no Spring Data JPA e na montagem dos componentes visuais do catálogo no frontend.

---

## 5. Limitações e Incertezas Percebidas

Durante o processo de estimativa, a equipe identificou as seguintes limitações, riscos e incertezas que sustentam as pontuações atribuídas:

1. **Curva de Integração Inicial da Stack:** A necessidade de conectar do zero a API Java/Spring Boot com o banco MySQL containerizado e o frontend em React/TypeScript cria incertezas sobre o tempo necessário de *setup* inicial.
2. **Definição dos Contratos de API:** Ajustes nos DTOs de entrada e saída entre backend e frontend durante o desenvolvimento podem exigir pequenas refatorações não previstas nas USs iniciais.
3. **Disponibilidade e Trabalho Assíncrono:** A restrição de tempo dos integrantes decorrente de jornadas de trabalho em horário comercial impõe a necessidade de um fluxo rigoroso de testes e Code Reviews assíncronos, o que pode diluir o tempo de conclusão de cada SP ao longo da semana.