# Planejamento Inicial e Baseline

## 1. Recorte do Backlog e Priorização

_O planejamento para o primeiro horizonte de desenvolvimento do MVP foca no módulo de "Autenticação e Catálogo de Orientadores". As seguintes User Stories foram priorizadas para a etapa inicial:_

- [x] **Prioridade Alta:** US01 - Autenticação e Perfis de Usuário
- [x] **Prioridade Alta:** US02 - Cadastro de Perfil de Orientador e Vagas
- [x] **Prioridade Alta:** US03 - Busca e Filtragem de Orientadores

_Nota: As US04 (Solicitação e Aceite de Orientação) e as demais histórias do fluxo de entregas (US05, US06 e US07) compõem as próximas etapas do MVP e permanecem no Product Backlog para ciclos futuros._

## 2. Estimativas

_As estimativas serão realizadas utilizando a técnica de Planning Poker, adotando a escala de Fibonacci (1, 2, 3, 5, 8, 13) para representar pontos de história (Story Points). Mais detalhes metodológicos estão registrados no arquivo `ESTIMATIVAS.md`._

- **US01 (Autenticação):** 5 pontos.
- **US02 (Cadastro Orientador):** 5 pontos.
- **US03 (Busca e Filtragem):** 3 pontos.
- **Total estimado para a etapa:** 13 pontos de história.

## 3. Hipóteses Assumidas

Para sustentar este planejamento inicial, a equipe assume as seguintes premissas:

- **Regras de Negócio Adaptadas:** Como o curso de Análise e Desenvolvimento de Sistemas (ADS) no IFSC Câmpus São José não exige TCC, baseamos o fluxo do sistema nas normativas de outros cursos de outras instituições (como a Universidade Federal de Santa Catarina). A hipótese é que essas regras não mudem durante o semestre para não comprometer a arquitetura que construímos.
- **Estabilidade da Stack:** As decisões tecnológicas tomadas na fase de Inception (como Java com Spring Boot, React e MySQL) permanecerão inalteradas neste ciclo, garantindo que o esforço estimado não sofra impacto de curva de aprendizado em novas tecnologias.
- **Disponibilidade Constante:** A equipe manterá a capacidade de 5 integrantes ativos ao longo do ciclo.

## 4. Capacidade Planejada da Equipe

- **Integrantes Ativos:** 5 (Damares, Eduardo, Marcus, Talles, Willian).
- **Papéis:** Arquitetura, Requisitos/Scrum Master, UX/UI, Qualidade (QA) e DevOps/Infra.
- **Disponibilidade Estipulada:** 28 horas semanais para a equipe.
- **Cálculo da Capacidade:**
  - 3 integrantes possuem jornada de trabalho externo de 6h/dia, limitando a dedicação ao projeto a cerca de 4 horas semanais cada (12 horas totais).
  - 2 integrantes possuem maior disponibilidade, dedicando cerca de 8 horas semanais cada (16 horas totais).
- **Restrições, Impedimentos Conhecidos e Fatores de Previsibilidade:**
  - _Impactos do trabalho:_ Parte significativa da equipe trabalha durante o horário comercial (08h às 18h).
  - _Impacto na Previsibilidade:_ O tempo de sobreposição para reuniões síncronas é reduzido. O desenvolvimento ocorrerá majoritariamente no período noturno e aos finais de semana. A equipe dependerá fortemente de comunicação assíncrona e de um fluxo de Pull Requests bem documentado para evitar gargalos. Semanas de provas e avaliações em outras disciplinas também podem reduzir a produtividade temporariamente.
  - _Sobrecarga Acadêmica Simultânea:_ O andamento da produtividade semanal pode sofrer variação dependendo do calendário acadêmico. Semanas com picos de entregas de trabalhos em outras disciplinas semestre (como projetos de Backend, Interface Humano-Computador ou práticas em Sistemas Operacionais) tendem a reduzir as horas reais que os integrantes conseguem focar no projeto de Engenharia de Software.
  - _Curva de Aprendizado e Setup Inicial:_ Por ser a primeira prática de codificação do MVP (e com base nas ADRs definidas), a configuração da infraestrutura inicial (integração contínua no GitHub Actions, organização do repositório e de sua infraestrutura) costuma ocultar gargalos técnicos não previstos, o que pode diminuir a velocidade de entrega (Velocity) nas primeiras semanas.
  - _Imprevistos da Jornada Externa:_ A rotina de trabalho fora da instituição está pode sofrer diversos imprevistos, como necessidade de horas extras pontuais ou fadiga, o que significa que a estimativa de 28 horas semanais da equipe é um teto otimista, com possíveis reduções não programadas ao longo do tempo.

## 5. Previsão Inicial

Com base nas estimativas de 13 pontos de história e na capacidade mapeada de 28 horas semanais (com as devidas reduções de eficiência pela rotina de trabalho externo), a previsão inicial da equipe para o próximo marco do projeto é concluir integralmente a base de acesso e o catálogo de orientadores (US01, US02 e US03). Isso garante uma entrega sólida, realista e de alta qualidade para validar a arquitetura inicial do MVP.

**Data de registro do baseline:** 26 de agosto de 2026.
