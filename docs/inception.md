# Visão do Produto e Definição do MVP

Este documento consolida a **Visão do Produto** e a **Definição do Produto Mínimo Viável (MVP)** para o **Sistema de Gestão Centralizada de TCC**.

---

## 1. Visão do Produto

### Contexto e Problema Central

A gestão do Trabalho de Conclusão de Curso (TCC) na graduação, inserida na área de **Educação e Apoio à Aprendizagem (Gestão Acadêmica)**, enfrenta gargalos operacionais e de comunicação causados pela descentralização de informações. A carência de uma plataforma integrada gera desafios críticos no fluxo acadêmico:

- **Perda de histórico e prazos**: Utilização de canais informais (como grupos de WhatsApp e trocas de e-mails pessoais) para submissão de versões e acompanhamento de orientações, levando à perda de arquivos, perda de prazos e falta de auditabilidade.
- **Falta de transparência na busca por orientadores**: Alunos têm dificuldade em mapear as áreas de atuação, linhas de pesquisa ativas e disponibilidade de vagas dos professores na etapa inicial do TCC, atrasando a formalização dos pares orientador-orientando.
- **Desorganização na gestão de bancas e acervo**: Ausência de um fluxo estruturado para agendamento de defesas públicas e disponibilização centralizada dos trabalhos finalizados à comunidade acadêmica.

### Público-Alvo e Contexto de Aplicação

O sistema foi desenhado para uso interno em **Instituições de Ensino Superior (IES)**, com foco inicial nos cursos de graduação do **Instituto Federal de Santa Catarina (IFSC - Câmpus São José)**. Ele atende diretamente aos diferentes participantes envolvidos no processo:

- **Alunos de Graduação (Usuários Finais)**: Pesquisam linhas de pesquisa e vagas de orientadores, vinculam-se a um orientador, realizam o envio oficial de documentos dentro dos prazos e submetem a versão final aprovada.
- **Professores / Orientadores (Usuários Finais)**: Cadastram e atualizam suas linhas de pesquisa e vagas disponíveis, avaliam propostas e entregas intermediárias, e organizam as bancas de defesa.
- **Coordenadores de TCC / Gestores Acadêmicos (Usuários Administradores)**: Definem o calendário acadêmico com prazos das entregas, monitoram o progresso global das turmas e validam a homologação final.
- **Comunidade Acadêmica e Público Geral (Interessados)**: Consultam o acervo público de TCCs concluídos e aprovados.

### Proposta de Valor

A plataforma proporciona um ambiente único, oficial e transparente para todo o ciclo de vida do TCC, gerando benefícios para cada envolvido:

- **Para os alunos**: Facilidade no "match" com orientadores e clareza nos prazos e requisitos de entrega.
- **Para os professores**: Centralização de orientandos em um único painel e redução da burocracia de acompanhamento.
- **Para a instituição**: Rastreabilidade completa dos processos, garantia de cumprimento de prazos institucionais e preservação da memória acadêmica através do acervo público.

### Objetivos do Semestre

Como meta de desenvolvimento para o semestre letivo, o produto visa atingir os seguintes objetivos:

1. Estabelecer o fluxo completo de cadastro, busca e vinculação entre aluno e orientador.
2. Permitir o acompanhamento das entregas de documentos conforme os prazos estipulados pela coordenação.
3. Oferecer suporte à gestão e agendamento das bancas examinadoras de TCC.
4. Disponibilizar um acervo público para consulta e download dos TCCs concluídos.

### Premissas, Restrições e Limitações Conhecidas

- **Orientação a Objetos**: A arquitetura do sistema deve seguir estritamente o paradigma Orientado a Objetos.
- **Plataforma Web**: O sistema deve ser uma aplicação Web (acessível via navegadores desktop/mobile), sendo vedado o desenvolvimento de aplicativo nativo mobile.
- **Independência de Sistemas Externos**: O MVP não dependerá de integrações diretas síncronas com sistemas acadêmicos legados (como SIGAA ou ERPs da instituição), mantendo cadastro próprio para o escopo do projeto.
- **Recorte de MVP**: O escopo deve priorizar as regras essenciais de negócio realizáveis durante o período do semestre acadêmico.

---

## 2. Definição do Produto Mínimo Viável (MVP)

### Objetivo do MVP

Validar o fluxo inicial e central de gestão de TCC — focando na resolução do "match" de orientação e na entrega oficial de documentos — garantindo um ciclo enxuto, utilizável e auditável pelas três personas principais (Aluno, Professor e Coordenador).

### Funcionalidades Essenciais (Escopo do MVP)

O escopo do MVP está estruturado em torno das seguintes entregas funcionais de valor:

1. **Autenticação e Controle de Perfis**:
   - Cadastro e login diferenciados para Alunos, Professores e Coordenadores.
2. **Catálogo de Orientadores e Vagas**:
   - Cadastro de perfil do professor contendo áreas de interesse, linhas de pesquisa e número de vagas disponíveis.
   - Busca e filtragem de orientadores por áreas de pesquisa para alunos.
   - Solicitação de orientação pelo aluno e aceite ou recusa pelo professor.
3. **Gestão de Documentos e Prazos**:
   - Definição do calendário acadêmico pela coordenação, incluindo prazos para Projeto de TCC, TCC I e TCC II.
   - Upload de arquivos PDF pelo aluno.
   - Registro de pareceres pelo orientador.

### Funcionalidades Fora do Escopo

As seguintes ideias e recursos foram postergados para focar na validação inicial e não fazem parte do MVP:

- **Gestão de Bancas de Defesa:** Agendamento de data/horário e composição da banca examinadora.
- **Acervo Público de TCCs:** Consulta pública de trabalhos concluídos pela comunidade externa.
- Aplicativo mobile nativo (iOS/Android).
- Integração automatizada de assinatura digital via `gov.br` para atas de defesa.
- Geração automatizada de PDFs complexos (atas formatadas e certificados de participação em banca).
- Integração síncrona com sistemas institucionais de lançamento de notas (ex.: SIGAA).
- Sistema interno de bate-papo em tempo real (chat ao vivo).

### Justificativa de Viabilidade para o Semestre

O recorte proposto concentra-se em operações CRUD estruturadas, regras de validação de datas e prazos, controle de permissões por perfil e gerenciamento de arquivos. Essa abordagem permite uma implementação robusta, testável e compatível com o tempo disponível durante o semestre letivo, evitando dependências de integrações externas complexas ou bibliotecas de terceiros com alta curva de adoção.

### Critérios de Decisão do Escopo

Para definir o que entraria ou ficaria de fora do MVP, a equipe utilizou os seguintes critérios objetivos:

- **Essencialidade para o Fluxo do TCC**: A funcionalidade é mantida no MVP quando é indispensável para conduzir o processo desde a escolha do orientador até a conclusão e publicação do TCC.
- **Complexidade Técnica × Tempo Disponível**: Funcionalidades que exigem integrações externas complexas ou burocráticas (como `gov.br` e APIs restritas do SIGAA) foram postergadas.
- **Foco em Governança e Qualidade**: Priorizou-se um conjunto reduzido de funcionalidades com alta cobertura de testes, boa arquitetura e manutenção facilitada, em vez de um grande volume de telas e recursos inacabados.
