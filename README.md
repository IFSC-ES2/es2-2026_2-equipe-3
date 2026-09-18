# Sistema de Gestão Centralizada de TCC

## 1. Equipe

- **DAMARES DO SOCORRO GONCALVES GAIA** (Matrícula: 202510703590) - Papel: Arquiteta de Software
- **EDUARDO CARDOSO OLIVEIRA** (Matrícula: 202510703625) - Papel: Engenheiro de Requisitos e Scrum Master (Sprint 1)
- **MARCUS JHUAN EPIFANIO LIMA** (Matrícula: 202320003071) - Papel: Designer de UX/UI
- **TALLES SOUZA DA CRUZ** (Matrícula: 202510703745) - Papel: Engenheiro de Qualidade (QA)
- **WILLIAN FERREIRA DOS SANTOS** (Matrícula: 202320002996) - Papel: DevOps/Infra

## 2. Definição e Contextualização do Tema

- **Qual problema o sistema pretende resolver?**
  A gestão do Trabalho de Conclusão de Curso (TCC) sofre com a descentralização (uso de WhatsApp e e-mails para envio de arquivos) e com a falta de controle de prazos. Além disso, os alunos enfrentam grande dificuldade na fase inicial para descobrir quais são as áreas de atuação e as linhas de pesquisa de cada professor, dificultando o processo de encontrar um orientador compatível.

- **Em qual área de aplicação o problema está situado?**
  Educação e apoio à aprendizagem (Gestão Acadêmica).

- **Quem são os usuários?**
  1. **Alunos** (que buscam orientadores, desenvolvem o TCC e submetem arquivos).
  2. **Professores / Orientadores** (que divulgam suas linhas de pesquisa, avaliam entregas e participam de bancas).
  3. **Coordenadores de TCC** (que estipulam prazos no calendário acadêmico e gerenciam o fluxo).

- **Em qual local, organização, comunidade ou contexto o sistema poderia ser aplicado?**
  Uso interno em Instituições de Ensino Superior, focado em coordenadorias de curso da nossa própria instituição.

- **Por que o tema é relevante?**
  Ele moderniza e formaliza todo o ciclo do TCC. O sistema elimina a perda de histórico de comunicação, facilita o "match" entre aluno e orientador por meio do catálogo de pesquisa, e garante que o processo burocrático (prazos, envio de arquivos, agendamento de bancas) seja feito em um ambiente único, seguro e institucional.

- **Qual é a proposta do sistema para resolver ou apoiar a solução do problema?**
  Desenvolver uma aplicação Web de gestão centralizada de TCC. O sistema terá um catálogo onde os professores expõem suas linhas de pesquisa e disponibilidade de vagas. Após o vínculo, o sistema gerenciará todo o fluxo do trabalho: submissão oficial de documentos, controle do calendário de defesas e a publicação do trabalho final em um acervo para consulta pública.

## 3. Escopo do Produto Mínimo Viável (MVP)

O sistema será construído como uma aplicação Web Orientada a Objetos, com foco estrito nas regras de negócio iniciais e essenciais do fluxo acadêmico, garantindo viabilidade para o semestre letivo.

**O que o MVP fará (Funcionalidades Principais):**

1. **Autenticação e Perfis:** Acesso diferenciado para Alunos, Professores e Coordenadores.
2. **Catálogo de Orientadores (Match):** Vitrine onde os professores cadastram suas linhas de pesquisa, áreas de interesse e quantidade de vagas disponíveis, permitindo a vinculação inicial do aluno.
3. **Gestão de Documentos e Prazos:** Interface para o aluno fazer o upload das entregas obrigatórias (ex: Projeto de TCC, TCC 1, Versão Final) respeitando o calendário do Coordenador, com espaço para pareceres do orientador.

**O que ficará FORA do escopo neste momento (Candidatos para entregas/versões futuras):**

- Gestão de Bancas (Agendamento de defesas e registro de avaliadores convidados).
- Acervo Público Acadêmico (Página pública de busca de TCCs concluídos).
- Aplicativo Mobile.
- Assinatura digital avançada (gov.br) nas atas de defesa.
- Geração automática de PDFs complexos (atas e formulários formatados).
- Integração direta com o sistema acadêmico oficial de notas da instituição (ex: SIGAA).

## 4. Entrega 2: Inception e Planejamento

Durante a etapa de Inception, definimos o escopo técnico e de negócios do projeto, além de estabelecermos nossas regras de qualidade e arquitetura. Todos os artefatos desta fase podem ser acessados nos links abaixo:

- **[Visão do Produto e Escopo do MVP](docs/inception.md):** Definição do problema, proposta de valor, funcionalidades essenciais e o que está fora do escopo.
- **[Backlog Inicial e Board](https://github.com/orgs/IFSC-ES2/projects/31):** Quadro de acompanhamento de tarefas (Kanban) contendo as _User Stories_ priorizadas para o MVP.
- **[Definition of Done - DoD](docs/dod.md):** Nosso acordo de qualidade e critérios mínimos para considerar uma tarefa concluída.
- **[Decisões Arquiteturais - ADRs](docs/adrs/):** Registro das escolhas iniciais de stack tecnológica e infraestrutura.
- **[Declaração de Uso de IA](USO-IA.md):** Registro de uso de ferramentas de Inteligência Artificial durante o desenvolvimento do projeto.

## 5. Entrega 3: Estimativas e Métricas (Baseline)

Nesta etapa, formalizamos as estimativas, a capacidade planejada da equipe e a definição das métricas de produto, processo e projeto:

- **[Planejamento Inicial e Baseline](docs/BASELINE.md):** Recorte do backlog, estimativas, capacidade declarada e premissas do projeto.
- **[Registro da Abordagem de Estimativa](docs/ESTIMATIVAS.md):** Técnica de Planning Poker, Fibonacci (1, 2, 3, 5, 8, 13), participantes e justificativas.
- **[Plano de Medição e Métricas](docs/METRICAS.md):** Matriz geral e categorização dos 6 indicadores acompanhados.
- **Fichas Técnicas de Métricas:**
  - [`M-01.md`](docs/metricas/M-01.md): Cobertura de Testes de Código (Métrica de Produto)
  - [`M-02.md`](docs/metricas/M-02.md): Densidade de Defeitos Abertos (Métrica de Produto)
  - [`M-03.md`](docs/metricas/M-03.md): Velocidade da Equipe em SP (Métrica de Processo)
  - [`M-04.md`](docs/metricas/M-04.md): Lead Time de Pull Requests (Métrica de Processo)
  - [`M-05.md`](docs/metricas/M-05.md): Taxa de Conclusão do MVP (Métrica de Projeto)
  - [`M-06.md`](docs/metricas/M-06.md): Cumprimento da Capacidade (Métrica de Projeto)

## 6. Entrega 4: Riscos e consolidação

Nesta etapa, estabelecemos o registro e priorização de riscos do projeto, consolidamos o fluxo de trabalho colaborativo via Pull Requests e definimos a base de avaliação da qualidade do software:

- **[Riscos do Projeto](docs/riscos.md):** Registro inicial dos riscos identificados, contendo análise, priorização e plano de respostas aos ricos.
- **[Fluxo de Trabalho e Governança](docs/fluxo-de-trabalho.md):** Regras de branches, padrão de commits, cerimônias ágeis e checklist obrigatório para Pull Requests.
- **[Integração Contínua (CI)](docs/ci.md):** Configuração do pipeline de Integração Contínua (Github Actions), cobrindo verificação de docs, links e formatação (Prettier).
- **[Qualidade do Software](docs/qualidade.md):** Mapeamento dos atributos da norma ISO/IEC 25010 priorizados para o MVP.
- **Evidência de Integração:** O desenvolvimento desta etapa foi consolidado através do [Pull Request #34](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/34), que executou os checks de CI obrigatórios e passou pela revisão da equipe.

## 7. Entrega 5: Primeiro Incremento Funcional (Sprint 1)

Nesta etapa, implementamos o primeiro _vertical slice_ funcional do MVP (US02: Cadastro de Perfil de Orientador e Vagas), integrando interface Web em React, lógica de negócio em Spring Boot e persistência relacional com MySQL, além da automação de testes de unidade e pipeline de CI.

- **[Relatório de Fechamento da Sprint 1](docs/entregas/sprint-1.md):** Escopo planejado vs. executado, retrospectiva e registro detalhado de contribuições individuais.
- **[Contrato de Dados da API (US02)](docs/contrato-dados-us02.md):** Especificação formal dos endpoints REST e formatos de dados.
- **[Declaração de Uso de IA](USO-IA.md):** Registro da utilização de ferramentas de IA na Sprint 1.

### 7.1. O que já funciona no MVP (US02)

- **Cadastro de Orientador:** Formulário com validações em tempo real (nome, e-mail institucional único, departamento, número de vagas e linhas de pesquisa) via `POST /api/v1/orientadores`.
- **Vitrine / Catálogo de Orientadores:** Listagem dos orientadores com disponibilidade de vagas e filtragem dinâmica por linha de pesquisa via `GET /api/v1/orientadores?area=...`.
- **Consulta de Perfil:** Visualização detalhada dos dados de um orientador específico por ID via `GET /api/v1/orientadores/{id}`.
- **Edição de Perfil:** Atualização de dados cadastrais, biografia e ajuste no quantitativo de vagas ofertadas via `PATCH /api/v1/orientadores/{id}`.
- **Remoção de Perfil:** Desativação/exclusão de cadastro via `DELETE /api/v1/orientadores/{id}`.

## 8. Como executar o aplicativo

Para facilitar a execução dos ambientes de frontend, backend e banco de dados simultaneamente, o projeto está configurado com Docker Compose.

### Pré-requisitos

Certifique-se de ter instalado em sua máquina:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### Passo a Passo

1. Clone o repositório e acesse a pasta raiz do projeto:

```bash
git clone https://github.com/IFSC-ES2/es2-2026_2-equipe-3.git
cd es2-2026_2-equipe-3/SIGTCC

```

2. Construa as imagens e suba os contêineres:

```bash
docker-compose up

```

3. Acesse a aplicação no seu navegador:

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **Backend (API):** [http://localhost:8080](http://localhost:8080)

### Comandos Úteis

Para acompanhar os logs da aplicação em tempo real:

```bash
docker compose logs -f

```

Para parar a execução e remover os contêineres:

```bash
docker compose down

```

## 9. Como executar os testes unitários

O projeto possui suítes de testes isoladas para as camadas de frontend e backend. Certifique-se de abrir o terminal e navegar para a pasta correspondente antes de executar os comandos.

### Frontend (React + Vitest)

1. Acesse o diretório do frontend a partir da raiz do projeto:

```bash
   cd SIGTCC/frontend
```

2. Instale as dependências locais (caso seja a primeira execução fora do Docker):

```bash
   npm install
```

3. Execute a suíte de testes:

```bash
   npm run test
```

4. Para gerar o relatório de cobertura de código (Code Coverage):

```bash
   npm run coverage
```

> **Nota:** O relatório de cobertura em HTML será gerado na pasta `frontend/coverage`. Você pode abrir o arquivo `index.html` diretamente no seu navegador.

### Backend (Spring Boot + Java)

1. Acesse o diretório base do backend:

```bash
   cd SIGTCC/backend/gestao-tcc
```

2. Execute os testes automatizados utilizando o wrapper do Gradle:

  - **No Linux / macOS:**

```bash
     ./gradlew test
```

- **No Windows (CMD ou PowerShell):**

```cmd
     .\gradlew test
```

> **Nota:** Para visualizar a cobertura de código do backend, recomenda-se executar os testes diretamente pela sua IDE (como o IntelliJ IDEA) utilizando a opção "Run with Coverage".
