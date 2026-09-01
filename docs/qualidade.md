# Qualidade do Software para o MVP

Este documento define os atributos de qualidade prioritários para o Produto Mínimo Viável (MVP) do Sistema de Gestão Centralizada de TCC, com base na norma ISO/IEC 25010. A proposta considera o escopo do MVP, os riscos mapeados no projeto e a necessidade de validar as funcionalidades centrais com rapidez, baixo retrabalho e ambiente estável.

---

## 1. Atributos de qualidade priorizados para o MVP

Para o contexto do projeto, os quatro atributos de qualidade mais relevantes são:

### 1.1 Adequação Funcional

A adequação funcional mede o quão bem o sistema atende às necessidades e requisitos esperados pelos usuários. No MVP, esse atributo é prioritário porque o produto precisa validar o fluxo principal de TCC: autenticação, cadastro e busca de orientadores, solicitação de orientação, controle de prazos e upload de documentos.

**Justificativa de prioridade**
- O MVP foi definido para validar as funcionalidades essenciais do negócio.
- Qualquer falha funcional no fluxo principal compromete a própria proposta de valor do sistema.
- O produto precisa demonstrar valor real para alunos, professores e coordenadores.

### 1.2 Confiabilidade

A confiabilidade representa a capacidade do sistema de operar sem falhas críticas, recuperar-se de problemas e manter comportamento consistente em uso real. Para o MVP, isso envolve estabilidade de rotas, processamento de arquivos, regras de negócio e comportamento em integração entre frontend e backend.

**Justificativa de prioridade**
- O projeto possui riscos de regressão por ausência de testes e CI quebrado (R02).
- O atraso na integração entre backend e frontend pode gerar falhas operacionais e retrabalho (R03).
- A estabilidade do sistema é crítica para validar o MVP com usuários e membros da equipe.

### 1.3 Segurança

A segurança avalia se o sistema protege informações, restringe acessos indevidos e garante a integridade das operações por perfil de usuário. No contexto acadêmico, esse atributo é essencial porque há diferenciação entre aluno, professor e coordenador, além de documentos e informações sensíveis.

**Justificativa de prioridade**
- O MVP inclui autenticação por perfis e controle de permissões.
- A falta de controle de acesso pode expor dados e quebrar a confiança no sistema.
- O fluxo de documentos e gerenciamento de orientações exige autorização correta em cada etapa.

### 1.4 Manutenibilidade

A manutenibilidade diz respeito à facilidade de alterar, evoluir e corrigir o sistema sem introduzir regressões. Esse atributo é especialmente importante em um projeto acadêmico com curva de aprendizagem e time multidisciplinar.

**Justificativa de prioridade**
- O projeto tem risco de divergência de ambiente e infraestrutura (R05), o que exige manutenção mais simples e previsível.
- A arquitetura foi escolhida para permitir entregas rápidas, mas precisa manter clareza e organização técnica.
- É essencial preservar entendimento do código e facilitar a evolução no próximo ciclo do produto.

---

## 2. Relação com os riscos mapeados

A tabela abaixo conecta os atributos de qualidade prioritários com os riscos mais relevantes do projeto.

| Atributo de qualidade | Risco relevante | Relação | Como a qualidade mitiga o risco |
| :--- | :--- | :--- | :--- |
| Adequação Funcional | R03 - Atraso na integração das APIs | Fortemente relacionado | A priorização de requisitos centrais e validações funcionais reduz a chance de entregar um MVP incompleto ou com comportamento inconsistente. |
| Confiabilidade | R02 - Falhas por ausência de testes/CI | Fortemente relacionado | Testes automatizados, pipeline de integração e validação contínua aumentam a estabilidade e reduzem regressões. |
| Confiabilidade | R05 - Divergência de ambientes e instabilidade em Docker | Direto | Ambientes padronizados e verificações de build ajudam a manter o software funcionando de forma previsível entre desenvolvedores e deploy. |
| Segurança | R02 e R03 | Indireto, mas crítico | A validação de regras de acesso e contratos de API minimiza falhas de autorização e inconsistências de dados em integração. |
| Manutenibilidade | R05 - Divergência de ambientes | Direto | Documentação e padronização da infraestrutura reduzem o custo de manutenção e conflitos entre ambientes. |
| Manutenibilidade | R01 - Desistência ou abandono de membro da equipe | Indireto | Código bem organizado e compartilhado reduz dependência de conhecimento individual e facilita a continuidade do trabalho. |
| Adequação Funcional | R04 - Crescimento não planejado do escopo | Direto | O foco em funcionalidades essenciais e critérios claros de aceitação ajuda a evitar expansão indevida do MVP. |

### Interpretação da relação

Os riscos mais críticos do projeto estão diretamente ligados a qualidade de entrega e estabilidade operacional. Em especial:
- R02 e R05 demandam foco em confiabilidade e manutenibilidade;
- R03 exige atenção à adequação funcional e à capacidade de integração robusta;
- R04 reforça a necessidade de manter o escopo funcional enxuto e bem delimitado;
- R01 reforça a importância de reduzir dependência de conhecimento individual, favorecendo a manutenibilidade.

---

## 3. Definição preliminar de avaliação da qualidade

A avaliação da qualidade será feita de forma incremental, com foco em critérios objetivos e evidências observáveis ao final de cada sprint.

### 3.1 Critérios de evidência por atributo

#### Adequação Funcional
- Validação de histórias de usuário por critérios de aceitação.
- Testes automatizados de cenários críticos: login, cadastro de professor, busca de orientadores, solicitação de orientação e upload de arquivos.
- Verificação manual de fluxo principal em ambiente funcional.

**Métricas preliminares**
- Percentual de critérios de aceitação atendidos.
- Taxa de testes automatizados passando nos fluxos principais.
- Número de defeitos funcionais reportados por sprint.

#### Confiabilidade
- Execução de testes de integração e regressão no pipeline.
- Monitoramento do número de falhas em build, testes e execução da aplicação.
- Observação do comportamento em cenários de falha e recuperação de endpoints.

**Métricas preliminares**
- Percentual de builds e testes com sucesso no CI.
- Taxa de falhas de integração entre frontend e backend.
- Defeitos críticos encontrados após merge.

#### Segurança
- Revisão de permissões por perfil.
- Testes de acesso indevido e autenticação.
- Verificação do fluxo de autorização em rotas e ações sensíveis.

**Métricas preliminares**
- Percentual de rotas protegidas por autenticação/autorização.
- Número de tentativas de acesso indevido bloqueadas.
- Ocorrência de falhas de autenticação ou autorização em testes.

#### Manutenibilidade
- Revisão da estrutura do código e organização em camadas.
- Cobertura de testes automatizados em módulos chave.
- Uso de padrões de nomenclatura, documentação e desacoplamento.

**Métricas preliminares**
- Cobertura de testes por módulo.
- Índice de complexidade de código em áreas críticas.
- Tempo necessário para corrigir falhas ou evoluir uma funcionalidade.

### 3.2 Formas de observação e avaliação ao longo do projeto

A qualidade será avaliada por meio de quatro fontes de evidência:

1. Testes automatizados
   - Unitários, de integração e de API.
   - Foco em autenticação, regras de negócio e fluxo principal do usuário.

2. Pipeline de Integração Contínua
   - Execução de build, lint e testes em cada pull request.
   - Bloqueio de merge quando houver falha crítica.

3. Verificação manual de cenários críticos
   - Testes de ponta a ponta no ambiente local padronizado.
   - Validação de fluxo principal de aluno e professor.

4. Revisão de qualidade em ritos ágeis
   - Sprint review e retrospective para analisar falhas, riscos e oportunidades de melhora.
   - Registro de problemas e decisões de mitigação.

### 3.3 Níveis de expectativa para o MVP

Para o MVP, o objetivo não é alcançar a máxima maturidade de qualidade de um produto em produção, mas sim garantir que:
- os casos de uso essenciais funcionem de forma consistente;
- o sistema seja estável o suficiente para validar a proposta com usuários;
- a infraestrutura seja reproducível e a manutenção seja viável;
- os riscos mais críticos sejam mitigados antes da entrega final.

Portanto, a avaliação preliminar da qualidade será considerada positiva quando o MVP entregar os fluxos centrais atendendo aos critérios de aceitação e mantendo um nível adequado de confiabilidade, segurança, estabilidade e capacidade de evolução.

---

## 4. Conclusão

Os atributos de qualidade escolhidos para o MVP — adequação funcional, confiabilidade, segurança e manutenibilidade — refletem diretamente o contexto do projeto e os riscos identificados. Eles orientam tanto a implementação quanto a validação do produto, assegurando que o sistema entregue seja funcional, estável, controlado e sustentável para evoluir após a fase inicial.

A qualidade não será avaliada apenas como um conjunto de normas técnicas, mas como uma condição necessária para a entrega do valor do produto: um sistema que realmente apoia o fluxo acadêmico de TCC e é capaz de evoluir com segurança no próximo ciclo de desenvolvimento.
