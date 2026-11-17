# Relatório de Bugs — SIGTCC

## 1. Contexto

Este relatório nasceu de uma revisão estática de todo o código do projeto, feita durante a Sprint 4 (Entrega 8). A revisão cobriu:

- backend: `SIGTCC/backend/gestao-tcc/src/main`
- frontend: `SIGTCC/frontend/src`
- migrations Flyway
- `docker-compose.yml`
- realm do Keycloak

Os 22 achados foram classificados por severidade. Os 8 de severidade crítica e alta viraram as issues [#199](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/199) a [#206](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/206) e foram corrigidos na Sprint 4. A triagem final ([#256](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/256)) revisou cada item contra o código da `main` no congelamento de código da Entrega 9. A seção 3 consolida o que não foi corrigido como **limitação conhecida**.

Os caminhos são relativos a `SIGTCC/`, e o pacote `br/edu/ifsc/gestao_tcc` foi abreviado para `…/`.

**Legenda de status:**

- **Corrigido:** o defeito foi eliminado por um PR mesclado.
- **Corrigido indiretamente:** o defeito deixou de ocorrer por efeito do PR de outro bug.
- **Limitação conhecida:** o defeito permanece e está documentado. Alguns foram mitigados em parte.
- **Não reproduzível:** o defeito não ocorre mais no código atual. Nenhum item ficou nesse status.

## 2. Resumo da triagem final

| Severidade | Quantidade | Corrigidos | Limitações conhecidas |
| ---------- | ---------- | ---------- | --------------------- |
| Crítica    | 4          | 4          | 0                     |
| Alta       | 4          | 4          | 0                     |
| Média      | 7          | 5          | 2                     |
| Baixa      | 7          | 0          | 7                     |
| **Total**  | **22**     | **13**     | **9**                 |

| Bug    | Título resumido                                    | Severidade | Status final                   | Issue                                                              | PR                                                                          |
| ------ | -------------------------------------------------- | ---------- | ------------------------------ | ------------------------------------------------------------------ | --------------------------------------------------------------------------- |
| BUG-01 | Aceite/recusa de qualquer solicitação              | Crítica    | Corrigido                      | [#199](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/199) | [#212](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/212)            |
| BUG-02 | CRUD de orientadores aberto                        | Crítica    | Corrigido                      | [#200](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/200) | [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211)            |
| BUG-03 | Solicitação com e-mail do corpo                    | Crítica    | Corrigido                      | [#201](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/201) | [#213](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/213)            |
| BUG-04 | Leitura de qualquer solicitação (IDOR)             | Crítica    | Corrigido                      | [#202](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/202) | [#224](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/224)            |
| BUG-05 | Painel mostra o orientador errado                  | Alta       | Corrigido                      | [#203](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/203) | [#214](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/214)            |
| BUG-06 | "Meu perfil" depende do `localStorage`             | Alta       | Corrigido                      | [#204](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/204) | [#217](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/217)            |
| BUG-07 | Sessão expira em ~5 minutos                        | Alta       | Corrigido                      | [#205](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/205) | [#225](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/225)            |
| BUG-08 | Condição de corrida nas vagas                      | Alta       | Corrigido                      | [#206](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/206) | [#241](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/241)            |
| BUG-09 | Conflito de e-mail aluno/orientador gera 500       | Média      | Corrigido                      | [#261](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/261) | [#289](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/289)            |
| BUG-10 | PATCH de orientador sem checar e-mail duplicado    | Média      | Corrigido indiretamente        | [#200](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/200) | [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211)            |
| BUG-11 | Faltam limites de tamanho nos DTOs                 | Média      | Corrigido                      | [#273](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/273) | PR da issue #273                                                            |
| BUG-12 | Sem handler genérico de exceções                   | Média      | Corrigido                      | [#262](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/262) | PR da issue #262                                                            |
| BUG-13 | Orientador desativado continua listado e operando  | Média      | Corrigido                      | [#263](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/263) | PR da issue #263                                                            |
| BUG-14 | Filtro por linha de pesquisa compara por substring | Média      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-15 | Seed V5 pode sobrescrever dados                    | Média      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-16 | Erro de digitação em `application.properties`      | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-17 | Contador de vagas mostra "—"                       | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-18 | Redirecionamento após o login não acontece         | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-19 | Inputs controlados recebem `null`                  | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-20 | Calendário abre com a data vazia                   | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-21 | `ErrorResponseDTO` usa `Instant.now()`             | Baixa      | Limitação conhecida            | —                                                                  | —                                                                           |
| BUG-22 | Professor cadastra orientador com qualquer e-mail  | Baixa      | Limitação conhecida (mitigada) | —                                                                  | Mitigação: [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211) |

**Impacto no MVP:** nenhum defeito crítico ou alto permanece aberto. As limitações restantes são de severidade média ou baixa: afetam mensagens de erro, chamadas feitas direto à API ou detalhes de interface. Nenhuma impede a demonstração dos fluxos do MVP (login, catálogo, solicitação e aceite de orientação, calendário de prazos, submissão e avaliação de entregas).

## 3. Limitações conhecidas

Defeitos não corrigidos até o congelamento de código, com o efeito observável para quem usa o sistema. Esta lista é replicada no `README.md` e na descrição da release.

| Bug    | Limitação                                                                                                                                                   | Contorno                                                                            |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| BUG-14 | O filtro por linha de pesquisa usa "contém": filtrar por "IA" também traz "Mídias Digitais".                                                                | —                                                                                   |
| BUG-15 | O seed `V5` usa IDs fixos e pode sobrescrever registros de bancos criados antes dele.                                                                       | Não ocorre em banco novo, como o de _staging_ ou o criado pelo `docker compose up`. |
| BUG-16 | A chave `xspring.application.name` está com um "x" a mais, e o arquivo `application.properties` não está em UTF-8.                                          | Sem efeito funcional.                                                               |
| BUG-17 | O painel do professor mostra "—" em vagas disponíveis quando não há solicitações pendentes.                                                                 | Consultar as vagas em "Meu perfil".                                                 |
| BUG-18 | Ao abrir um link protegido sem estar logado, o usuário vai para a página inicial do seu papel após o login, e não para o link original.                     | Navegar de novo até a página desejada.                                              |
| BUG-19 | Na edição do perfil, campos opcionais vazios (`departamento`, `biografia`) geram aviso de input não controlado no console, e o PATCH reenvia campos extras. | Sem efeito visível para o usuário.                                                  |
| BUG-20 | O calendário de prazos abre em "Editar prazo" com a data em branco, mesmo quando o prazo já existe.                                                         | Selecionar outro tipo de entrega e voltar, ou consultar a lista de prazos.          |
| BUG-21 | O `timestamp` das respostas de erro usa `Instant.now()`, e não o `Clock` do projeto.                                                                        | Sem efeito funcional.                                                               |
| BUG-22 | Qualquer professor pode cadastrar um perfil de orientador com um e-mail digitado livremente, sem vínculo com a conta do Keycloak.                           | A edição e a remoção já são restritas ao dono do perfil (PR #211).                  |

## 4. Detalhamento dos bugs

### 4.1. Severidade crítica (segurança e autorização)

#### BUG-01: Qualquer usuário autenticado aceita ou recusa qualquer solicitação

- **Status:** Corrigido. Issue [#199](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/199), PR [#212](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/212).
- **Local:**
  - `backend/.../controller/SolicitacaoController.java` (`atualizarStatus`)
  - `backend/.../service/SolicitacaoService.java` (`atualizarStatus`)
  - `backend/.../config/SecurityConfig.java`
- **Descrição:** O `SecurityConfig` só exigia papel em `/api/v1/professores/**`. As demais rotas exigiam apenas autenticação. O endpoint `PATCH /api/v1/solicitacoes/{id}/status` não recebia o JWT e não conferia se quem chamava era o orientador da solicitação.
- **Impacto:** Um aluno, ou um professor qualquer, podia aceitar ou recusar solicitações de outros orientadores. Cada aceite indevido também consumia uma vaga do orientador real, via `AtualizadorVagasObserver`.
- **Como reproduzir (antes da correção):**
  1. Faça login como `aluno@example.com`.
  2. Envie `PATCH /api/v1/solicitacoes/1/status` com `{"status":"ACEITA"}`.
  3. A resposta é 200 e a vaga é decrementada.
- **Correção:** A rota passou a exigir `ROLE_PROFESSOR`, e o service compara o e-mail do JWT com o do orientador da solicitação, lançando `AcessoNegadoException` (403).

#### BUG-02: CRUD de orientadores aberto a qualquer usuário autenticado

- **Status:** Corrigido. Issue [#200](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/200), PR [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211).
- **Local:**
  - `backend/.../controller/OrientadorController.java` (`POST`, `PATCH /{id}`, `DELETE /{id}`)
  - `backend/.../service/OrientadorService.java`
- **Descrição:** Não havia restrição de papel nem verificação de dono. Qualquer token válido conseguia cadastrar, editar ou desativar (soft delete) qualquer orientador.
- **Impacto:**
  - Um aluno podia desativar todos os orientadores ou alterar o número de vagas deles.
  - Um professor podia editar o perfil de outro professor, inclusive o e-mail, que identifica o usuário.
- **Como reproduzir (antes da correção):** Faça login como aluno e envie `DELETE /api/v1/orientadores/1`. A resposta é 204 e o orientador fica inativo.
- **Correção:** A escrita passou a exigir `ROLE_PROFESSOR`. `PATCH` e `DELETE` só são aceitos quando o e-mail do JWT é o do próprio orientador, e o e-mail se tornou imutável no `PATCH`.

#### BUG-03: A solicitação é criada com o e-mail informado no corpo, não com o do usuário logado

- **Status:** Corrigido. Issue [#201](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/201), PR [#213](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/213).
- **Local:**
  - `backend/.../service/SolicitacaoService.java` (`criarSolicitacao`)
  - `frontend/src/components/SolicitacaoForm/index.tsx`
- **Descrição:** O aluno era buscado ou criado a partir de `dto.getAluno().getEmail()`. No frontend, esse e-mail era um campo de texto livre. O JWT era ignorado.
- **Impacto:**
  - Qualquer usuário criava solicitações em nome de outro aluno, ou de um e-mail inexistente, que gerava um novo registro em `alunos`.
  - Professores também conseguiam criar solicitações.
  - A regra de duplicidade (`ValidacaoSolicitacaoDuplicada`) era contornada trocando o e-mail.
- **Como reproduzir (antes da correção):** Logado como `professor.orientador@example.com`, envie uma solicitação com `aluno.email = "outro@aluno.com"`. Ela é criada.
- **Correção:** O aluno passou a ser identificado pelo claim `email` do JWT, a rota exige `ROLE_ALUNO` e o bloco `aluno` saiu do corpo da requisição.

#### BUG-04: Qualquer usuário logado lê qualquer solicitação (IDOR)

- **Status:** Corrigido. Issue [#202](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/202), PR [#224](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/224).
- **Local:** `backend/.../controller/SolicitacaoController.java` (`GET /{id}` e `GET /orientador/{orientadorId}`)
- **Descrição:** Nenhuma das rotas verificava se quem consultava era o aluno autor ou o orientador da solicitação. Os IDs são sequenciais (`AUTO_INCREMENT`).
- **Impacto:** Vazamento de dados pessoais de todos os alunos: nome, e-mail, curso, tema, mensagem e justificativa de recusa. Bastava iterar os IDs.
- **Como reproduzir (antes da correção):** Logado como qualquer usuário, chame `GET /api/v1/solicitacoes/1`, `/2`, `/3` e assim por diante, ou `GET /api/v1/solicitacoes/orientador/1`.
- **Correção:** `GET /{id}` é liberado apenas ao aluno autor e ao orientador da solicitação, e a listagem por orientador apenas ao próprio orientador. Os demais recebem 403.

### 4.2. Severidade alta (funcionalidade quebrada)

#### BUG-05: O painel do professor mostra as solicitações do orientador errado

- **Status:** Corrigido. Issue [#203](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/203), PR [#214](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/214).
- **Local:** `frontend/src/paginas/PainelSolicitacoes/index.tsx`
- **Descrição:** O ID do orientador vinha de `useParams`, depois de `localStorage.getItem("orientadorId")` e, por último, do valor fixo **`1`**. A página já carregava `obterPerfilProfessor()` (`/professores/me`), mas não usava o `perfil.id` desse retorno.
- **Impacto:**
  - `professor.responsavel@example.com` (id 2) via, aceitava e recusava as solicitações do professor 1.
  - Nenhum professor que não fosse o de id 1 conseguia ver as próprias solicitações pelo menu.
- **Como reproduzir (antes da correção):** Faça login como `professor.responsavel@example.com` em um navegador sem `localStorage`. O painel lista as solicitações do orientador 1.
- **Correção:** O painel usa o `perfil.id` retornado por `/professores/me`, sem fallback fixo nem `localStorage`.

#### BUG-06: "Meu perfil" depende do `localStorage` e pode editar o perfil de outra pessoa

- **Status:** Corrigido. Issue [#204](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/204), PR [#217](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/217).
- **Local:**
  - `frontend/src/paginas/OrientadorEdicao/index.tsx`
  - `frontend/src/paginas/OrientadorCadastro/index.tsx`
- **Descrição:** A edição usava `localStorage.orientadorId`, que só era gravado quando um orientador era cadastrado naquele navegador. Nada ligava esse valor ao usuário logado.
- **Impacto:**
  - Professores do seed, ou que faziam login em outro navegador, recebiam "Você não está autenticado. Faça o cadastro primeiro." Se tentassem se cadastrar, recebiam 409 (e-mail duplicado).
  - Se outra pessoa tivesse cadastrado um orientador no mesmo navegador, o professor logado passava a editar o perfil **dela**.
  - O valor também sobrevivia ao logout.
- **Como reproduzir (antes da correção):** Faça login como `professor.orientador@example.com` em um navegador limpo e abra "Meu perfil". A tela mostra erro.
- **Correção:** "Meu perfil" obtém o ID por `/professores/me`, e o uso de `localStorage` foi eliminado.

#### BUG-07: A sessão expira em cerca de 5 minutos e todas as chamadas passam a falhar com 401

- **Status:** Corrigido. Issue [#205](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/205), PR [#225](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/225).
- **Local:**
  - `frontend/src/utils/api.ts`
  - `frontend/src/auth/authService.ts`
  - `frontend/src/auth/AuthContext.tsx`
- **Descrição:** Quando o access token vencia, `obterAccessTokenAtual()` retornava `null` e o `apiFetch` enviava a requisição **sem** o header `Authorization`. `renovarToken()` só era chamado no carregamento inicial da aplicação. Não havia refresh automático nem tratamento de 401, e o `AuthContext` mantinha a sessão como logada. O realm não define `accessTokenLifespan`, então vale o padrão de 300 s do Keycloak.
- **Impacto:** Após cerca de 5 minutos de uso, todas as telas mostravam erros genéricos ("Unauthorized"). O usuário continuava vendo o menu como logado e precisava recarregar a página ou sair e entrar de novo.
- **Como reproduzir (antes da correção):** Faça login, espere mais de 5 minutos e tente aceitar uma solicitação ou abrir o catálogo.
- **Correção:** O `apiFetch` renova o token quando ele está vencido ou após um 401 e repete a requisição. Se a renovação falhar, a sessão é limpa e o usuário volta ao `/login`.

#### BUG-08: Condição de corrida no controle de vagas (lost update)

- **Status:** Corrigido. Issue [#206](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/206), PR [#241](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/241).
- **Local:**
  - `backend/.../service/SolicitacaoService.java` (`atualizarStatus` e `validarVagasDisponiveis`)
  - `backend/.../observer/AtualizadorVagasObserver.java`
  - `backend/.../model/PerfilOrientador.java`
- **Descrição:** A sequência era: ler as vagas, validar se havia vaga e gravar `vagas - 1`, sem lock pessimista nem `@Version` (lock otimista). Dois aceites simultâneos liam o mesmo valor.
- **Impacto:** Com 1 vaga, dois aceites concorrentes passavam pela validação e ambos gravavam `0`. O orientador ficava com mais orientandos aceitos do que vagas, e o contador perdia a consistência.
- **Como reproduzir (antes da correção):** Deixe o orientador com 1 vaga e 2 solicitações pendentes. Dispare os dois `PATCH .../status` com `ACEITA` em paralelo. Ambos retornam 200.
- **Correção:** `PerfilOrientador` ganhou `@Version` (migration `V8__adicionar_versao_perfil_orientador.sql`), e `OptimisticLockingFailureException` é mapeada para 409. Com 1 vaga e 2 aceites em paralelo, um recebe 200 e o outro 409, e as vagas terminam em `0` (`SolicitacaoConcorrenciaIntegrationTest`).

### 4.3. Severidade média (erros 500 e integridade de dados)

#### BUG-09: Conflito de e-mail entre aluno e orientador gera erro 500

- **Status:** Corrigido. Issue [#261](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/261), PR [#289](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/289). Antes, o PR [#213](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/213) e a issue [#262](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/262) (PR [#290](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/290)) já haviam mitigado o defeito.
- **Local:**
  - `backend/.../service/OrientadorService.java` (`cadastrar`)
  - `backend/.../service/SolicitacaoService.java` (`criarSolicitacao`)
  - `backend/.../repository/UsuarioRepository.java` (`existsByEmailIgnoreCase`)
  - `db/migration/V1__criar_tabelas_iniciais.sql` (`uk_usuario_email`)
- **Descrição:** O e-mail é `UNIQUE` na tabela `usuarios`, que é compartilhada entre alunos e orientadores (herança `JOINED`). Mas `OrientadorService.cadastrar` verificava apenas orientadores (`OrientadorRepository.existsByEmail`) e `SolicitacaoService.criarSolicitacao` verificava apenas alunos (`AlunoRepository.findByEmail`).
- **Impacto (antes da correção):** Cadastrar um orientador com o e-mail de um aluno (ou criar solicitação com e-mail já pertencente a um orientador) fazia o banco lançar `DataIntegrityViolationException`, que antes do PR #290 virava **500** quando o contrato esperava 409.
- **Correção:** Criado `UsuarioRepository.existsByEmailIgnoreCase`, utilizado tanto em `OrientadorService.cadastrar` quanto em `SolicitacaoService.criarSolicitacao` para verificar a existência do e-mail em toda a tabela `usuarios` e lançar `EmailDuplicadoException` (**409** / `Conflito de dados` com a mensagem `"Já existe um usuário cadastrado com o e-mail: {email}"` no formato `ErrorResponseDTO`), coberto por `OrientadorControllerIntegrationTest` e `SolicitacaoControllerIntegrationTest`.

#### BUG-10: O PATCH de orientador não verifica duplicidade de e-mail

- **Status:** Corrigido indiretamente. Issue [#200](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/200), PR [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211).
- **Local:** `backend/.../service/OrientadorService.java` (`atualizar`)
- **Descrição:** O e-mail era alterado sem checar se já pertencia a outro usuário.
- **Impacto:** Erro 500 no flush, por violação da unique key, em vez de 409 com mensagem amigável.
- **Correção:** Ao corrigir o BUG-02, o PR #211 tornou o e-mail imutável no `PATCH`. Qualquer valor diferente do atual é rejeitado com `RegraDeNegocioException` ("O e-mail do orientador não pode ser alterado."), então a violação de unique key deixou de ser alcançável.

#### BUG-11: Faltam limites de tamanho nos DTOs (o banco rejeita com 500)

- **Status:** Corrigido. Issue [#273](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/273). Antes, o PR [#213](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/213) havia mitigado o defeito.
- **Local:**
  - `backend/.../dto/OrientadorRequestDTO.java`
  - `backend/.../dto/OrientadorUpdateDTO.java`
- **Descrição (antes da correção):**
  - `OrientadorRequestDTO`: não havia `@Size` em `nome` (coluna de 100), `departamento` (100) e `biografia` (500).
  - Itens de `linhasDePesquisa` (coluna `VARCHAR(80)`): não tinham `@Size` nem `@NotBlank`, nem no POST nem no PATCH.
  - `OrientadorUpdateDTO`: aceitava lista vazia de linhas, enquanto o cadastro exige ao menos uma.
- **Mitigação anterior:** O `AlunoDTO` saiu do `SolicitacaoRequestDTO` no PR #213. O nome e o e-mail do aluno passaram a vir do JWT, e o `curso` ganhou `@Size(max = 100)`.
- **Impacto (antes da correção):** Requisições feitas direto à API, sem passar pela validação do frontend, geravam "Data too long" no MySQL. Linhas de pesquisa em branco eram aceitas.
- **Correção:**
  - `OrientadorRequestDTO`: `nome` 3–100, `email` ≤ 100, `departamento` ≤ 100 e `biografia` ≤ 500.
  - Itens de `linhasDePesquisa`: `List<@NotBlank @Size(min = 2, max = 80) String>`, no POST e no PATCH. O mesmo limite já é aplicado pelo frontend.
  - `OrientadorUpdateDTO`: `@Size(min = 1)` rejeita lista vazia. Omitir o campo continua mantendo as linhas atuais.
  - Campos acima do limite retornam `400` com `detalhes` em `[{campo, mensagem}]`. Nos itens da lista, o `campo` traz o índice, como em `linhasDePesquisa[0]`.

#### BUG-12: Não há handler genérico de exceções

- **Status:** Corrigido. Issue [#262](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/262). Antes, o PR [#241](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/241) já havia adicionado o handler de `OptimisticLockingFailureException` (409), como consequência da correção do BUG-08.
- **Local:** `backend/.../exception/GlobalExceptionHandler.java`
- **Descrição:** Não existiam handlers para `Exception` nem para `DataIntegrityViolationException`.
- **Impacto (antes da correção):** Erros inesperados (BUG-09 e BUG-11) saíam no formato padrão do Spring, fora do `ErrorResponseDTO` previsto em `docs/contrato-dados-*.md`. O `utils/apiErro.ts` do frontend caía na mensagem genérica.
- **Correção:**
  - `DataIntegrityViolationException` → **409** / `Conflito de dados`, com mensagem genérica. A mensagem do banco vai só para o log.
  - `Exception` → **500** / `Erro interno do servidor`, com mensagem fixa e sem stack trace. A exceção é registrada no log.
  - Exceções do Spring MVC que já carregam status (rota inexistente, método não suportado, parâmetro ausente) mantêm o status original, no formato `ErrorResponseDTO`.
  - Os handlers específicos continuam com precedência, verificado em `GlobalExceptionHandlerTest.testPrecedenciaDosHandlers`.

#### BUG-13: Orientador desativado continua listado e operando

- **Status:** Corrigido. Issue [#263](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/issues/263).
- **Local:**
  - `backend/.../repository/OrientadorRepository.java` (`findByFiltros`)
  - `backend/.../service/OrientadorService.java` (`deletar`)
  - `backend/.../service/SolicitacaoService.java` (`atualizarStatus`)
- **Descrição (antes da correção):**
  - A consulta do catálogo não filtrava `ativo = true`. Só o frontend filtrava, em `OrientadorList`.
  - O `deletar` (soft delete) não cancelava as solicitações `PENDENTE` do orientador.
  - O `atualizarStatus` permitia aceitar solicitações de um orientador inativo.
- **Impacto (antes da correção):** A API expunha orientadores desativados para outros clientes. Alunos ficavam com solicitações pendentes "presas", que nunca seriam respondidas.
- **Correção:**
  - `findByFiltros` filtra `o.ativo = true`. A consulta por ID continua retornando o orientador desativado.
  - Ao desativar, as solicitações `PENDENTE` passam para `CANCELADA`, com um `SolicitacaoStatusChangedEvent` publicado para cada uma. As vagas e as solicitações `ACEITA` não mudam.
  - Aceitar uma solicitação de orientador inativo retorna `422` (`OrientadorInativoException`). A recusa continua permitida.
  - A migration `V9` cancela as pendentes de orientadores desativados antes da correção.

#### BUG-14: O filtro por linha de pesquisa compara por substring

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:**
  - `backend/.../repository/OrientadorRepository.java` (`LIKE CONCAT('%', :area, '%')`)
  - `frontend/src/components/OrientadorList/index.tsx`
- **Descrição:** O frontend oferece um `<select>` com nomes exatos de linhas de pesquisa, mas o backend faz uma busca por "contém". Os caracteres `%` e `_` também não são escapados.
- **Impacto:** Ao escolher a linha "IA", o catálogo também mostra orientadores de "Mídias Digitais", "Engenharia" e outros. O resultado não corresponde ao filtro escolhido.
- **Como reproduzir:** Cadastre linhas "IA" e "Mídias Digitais" em orientadores diferentes e filtre por "IA".
- **Sugestão:** Usar igualdade sem diferenciar maiúsculas (`LOWER(lp.nome) = LOWER(:area)`) quando o filtro vier do seletor.

#### BUG-15: O seed V5 pode sobrescrever dados existentes

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `backend/gestao-tcc/src/main/resources/db/migration/V5__seed_professores_e_turma_vigente.sql`
- **Descrição:** A migration insere IDs fixos (1 e 2) em `usuarios`, `orientadores`, `perfis_professores` e `turmas_tcc`, com `ON DUPLICATE KEY UPDATE`.
- **Impacto:** Em um banco que já tinha registros com esses IDs (ambiente de desenvolvimento com dados anteriores à V5):
  - o e-mail do usuário 1 ou 2 é sobrescrito;
  - um aluno com id 1 ou 2 passa a ser também orientador;
  - perfis com id 1 ou 2 têm as vagas alteradas.
- **Observação:** Não ocorre em bancos novos, como o de _staging_ e o criado pelo `docker compose up`, em que a V5 roda sobre tabelas vazias.
- **Sugestão:**
  - Inserir sem ID fixo, usando `INSERT ... SELECT` com `WHERE NOT EXISTS` por e-mail.
  - Como a V5 já foi aplicada, a correção deve vir em uma nova migration, e não editando a V5.

### 4.4. Severidade baixa (UX e problemas menores)

#### BUG-16: Erro de digitação e codificação em `application.properties`

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `backend/gestao-tcc/src/main/resources/application.properties`, linha 1
- **Descrição:**
  - A chave está escrita `xspring.application.name`, com um "x" a mais, então o nome da aplicação nunca é definido.
  - O arquivo não está em UTF-8: os comentários aparecem como `Configura��es`.
- **Sugestão:** Corrigir a chave e converter o arquivo para UTF-8.

#### BUG-17: O contador de vagas do painel mostra "—" quando não há solicitações pendentes

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `frontend/src/paginas/PainelSolicitacoes/index.tsx` (linhas 49 a 51 e 208 a 211)
- **Descrição:** As vagas são lidas de `dados[0].orientador.vagasDisponiveis`, ou seja, só existem quando há pelo menos uma solicitação pendente.
- **Impacto:** O professor sem pedidos pendentes não vê quantas vagas tem.
- **Sugestão:** Obter as vagas do perfil do orientador, via `/orientadores/{id}` ou incluindo-as em `/professores/me`.

#### BUG-18: O redirecionamento de volta após o login nunca acontece

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:**
  - `frontend/src/auth/ProtectedRoute.tsx`, linha 23
  - `frontend/src/paginas/Login/index.tsx`, linha 29
- **Descrição:** O `LoginPage` lê `location.state.from`, mas o `<Navigate to="/login" replace />` não envia esse `state`.
- **Impacto:** Quem abre um link direto (por exemplo, `/acompanhar/15`) sem estar logado é levado à rota inicial do seu papel depois do login, e não à página que tentou acessar.
- **Sugestão:** Usar `<Navigate to="/login" replace state={{ from: location }} />`.

#### BUG-19: Inputs controlados recebem `null` na edição de perfil

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `frontend/src/components/OrientadorForm/index.tsx`
- **Descrição:** O estado é inicializado com o objeto do backend, em que `departamento` e `biografia` podem ser `null`. Isso resulta em `value={null}` nos inputs.
- **Impacto:**
  - Aviso do React sobre a troca entre input não controlado e controlado.
  - O PATCH reenvia campos extras (`id`, `ativo`).
  - Um campo esvaziado é enviado como `""`, sem ser tratado como "remover".
- **Sugestão:** Normalizar os valores (`departamento ?? ""`, `biografia ?? ""`) e montar o payload só com os campos editáveis.

#### BUG-20: O calendário abre em modo de edição com a data vazia

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `frontend/src/components/GestaoCalendario/index.tsx` (linhas 27 a 29)
- **Descrição:** O tipo `PROJETO_TCC` já vem selecionado e `dataLimite` começa como `""`. Se o prazo desse tipo já existe, o formulário mostra "Editar prazo" com o campo de data em branco.
- **Impacto:** O usuário pode achar que o prazo não está cadastrado. Ao clicar em salvar, recebe "A data limite é obrigatória."
- **Sugestão:** Depois de carregar os prazos, chamar `trocarTipoSelecionado(tipoSelecionado)` para preencher a data.

#### BUG-21: `ErrorResponseDTO` usa `Instant.now()` em vez do `Clock`

- **Status:** Limitação conhecida. Sem issue nem PR.
- **Local:** `backend/.../dto/ErrorResponseDTO.java`
- **Descrição:** O projeto define que a lógica de tempo deve usar o bean `Clock` (`config/ClockConfig`), mas o timestamp do erro usa `Instant.now()` diretamente.
- **Impacto:** O timestamp não pode ser fixado em testes, o que é inconsistente com a convenção do projeto.
- **Sugestão:** Injetar o `Clock` no `GlobalExceptionHandler` e passar `Instant.now(clock)` para `ErrorResponseDTO.of`.

#### BUG-22: Qualquer professor cadastra orientadores com qualquer e-mail

- **Status:** Limitação conhecida (mitigada). Sem issue própria. A mitigação veio com o PR [#211](https://github.com/IFSC-ES2/es2-2026_2-equipe-3/pull/211).
- **Local:**
  - `backend/.../controller/OrientadorController.java` (`POST`)
  - `frontend/src/App.tsx` (rota `/`)
  - `frontend/src/paginas/OrientadorCadastro/index.tsx`
- **Descrição:** A rota `/` (cadastro de orientador) fica disponível para todo `ROLE_PROFESSOR`, e o e-mail é digitado livremente. O `POST /orientadores` não compara esse e-mail com o do JWT, então nada liga o cadastro ao usuário do Keycloak.
- **Mitigação:** Após o PR #211, só professores cadastram, e apenas o dono do perfil (e-mail do JWT) pode editá-lo ou desativá-lo. Um perfil criado em nome de um colega não fica sob controle de quem o criou.
- **Impacto:** Surgem perfis "fantasmas" ou duplicados, sem conta no Keycloak correspondente.
- **Sugestão:**
  - Fazer o "primeiro acesso" criar o perfil com o e-mail do JWT, sem permitir edição desse campo.
  - Restringir o cadastro de terceiros a um perfil administrativo.
