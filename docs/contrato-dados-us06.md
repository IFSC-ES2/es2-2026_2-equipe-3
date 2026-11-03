# Contrato de Dados da API - US06: Submissão de Entregas do TCC

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 03/11/2026
- **Responsáveis:** Damares Gaia (Arquiteta de Software) e Talles Souza da Cruz (Desenvolvedor Backend)
- **História de Usuário Relacionada:** US06 - Submissão de Entregas do TCC (Sprint 4)
- **Issues Relacionadas:** #13 (US06), #208 (Modelagem Flyway V6), #209 (Contrato de Dados), #216 (Endpoint de Submissão)
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o **Contrato de Dados formal entre o Frontend (React) e o Backend (Spring Boot)** para a funcionalidade de **submissão de entregas parciais e finais do TCC pelo aluno** (US06): `Projeto de TCC`, `TCC 1` e `TCC 2`.

Conforme estabelecido no [relatório de replanejamento da Sprint 3](entregas/sprint-3.md) e formalizado na [ADR-0013](adrs/ADR-0013.md), o SIGTCC adota a **submissão por link externo (`urlDocumento` com protocolo `https://`)** em substituição ao upload físico de arquivos binários. O sistema valida o vínculo ativo de orientação do aluno, verifica o prazo da etapa na `Turma_TCC` vigente ([ADR-0010](adrs/ADR-0010.md)), calcula automaticamente o indicador de atraso (`atrasada`) e controla a máquina de estados da entrega, incluindo o reenvio quando o orientador solicita ajustes (`AJUSTES_NECESSARIOS`).

Este contrato complementa os contratos da [US01](contrato-dados-us01.md), [US04](contrato-dados-us04.md), [US05](contrato-dados-us05.md) e [US07](contrato-dados-us07.md).

---

## 2. Padrões Globais da API

Herdados dos contratos anteriores, sem alterações:

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Nomenclatura dos Campos (JSON):** `camelCase`
- **Datas e Carimbos de Tempo:**
  - `enviadoEm` e `registradoEm` em formato ISO-8601 UTC (`YYYY-MM-DDTHH:mm:ssZ`).
  - A aferição de pontualidade (`atrasada`) compara a data atual no fuso institucional `America/Sao_Paulo` (`ClockConfig.FUSO_INSTITUICAO`) com a `dataLimite` (`YYYY-MM-DD`) do prazo da turma vigente.
- **Formato de Erro:** objeto padrão `ErroResponse` produzido por `ErrorResponseDTO` / `GlobalExceptionHandler`. No `400` de validação de campos, `detalhes` é um array `[{campo, mensagem}]`; nos códigos `403`, `404`, `409` e `422`, `detalhes` é uma `String`.
- **Autenticação e Autorização:** todas as rotas exigem token JWT emitido pelo Keycloak no cabeçalho `Authorization: Bearer <token>` ([ADR-0012](adrs/ADR-0012.md)). O endpoint `POST /api/v1/entregas` exige a role `ROLE_ALUNO`, e o aluno é identificado exclusivamente pela claim `email` do token JWT.

---

## 3. Modelos de Dados

### 3.1. Enumeração `TipoEntrega` (reaproveitada da US05)

| Valor         | `descricao`      | Ordem no Fluxo |
| :------------ | :--------------- | :------------: |
| `PROJETO_TCC` | `Projeto de TCC` |       1ª       |
| `TCC1`        | `TCC 1`          |       2ª       |
| `TCC2`        | `TCC 2`          |       3ª       |

### 3.2. Enumeração `StatusEntrega`

| Valor                 | Significado                                                                             | Definido por                         |
| :-------------------- | :-------------------------------------------------------------------------------------- | :----------------------------------- |
| `SUBMETIDO`           | Entrega enviada (ou reenviada) pelo aluno e aguardando avaliação do orientador.         | Sistema (`POST /api/v1/entregas`)    |
| `AJUSTES_NECESSARIOS` | Orientador avaliou e solicitou correções; habilita o reenvio da mesma etapa pelo aluno. | Orientador (`POST .../parecer` US07) |
| `APROVADO`            | Entrega aprovada pelo orientador (estado final da etapa).                               | Orientador (`POST .../parecer` US07) |
| `REPROVADO`           | Entrega reprovada pelo orientador (estado final da etapa).                              | Orientador (`POST .../parecer` US07) |

### 3.3. `EntregaRequest` (entrada do `POST /api/v1/entregas`)

| Campo          | Tipo          | Obrigatório? | Regras e Restrições                                                                                                           |
| :------------- | :------------ | :----------: | :---------------------------------------------------------------------------------------------------------------------------- |
| `tipo`         | `TipoEntrega` |     Sim      | Um dos valores de `TipoEntrega` (`PROJETO_TCC`, `TCC1`, `TCC2`).                                                              |
| `urlDocumento` | String        |     Sim      | Não pode estar em branco (`@NotBlank`), máx. 2048 caracteres e URL válida com protocolo `https` (`@URL(protocol = "https")`). |

> **Identificação implícita de contexto:** o cliente **não** envia `alunoId`, `orientadorId` nem `turmaId`. O aluno é obtido pela claim `email` do token JWT; o orientador é obtido a partir da `SolicitacaoOrientacao` com status `ACEITA` do aluno; e a turma é sempre a `Turma_TCC` com `vigente = true`.

### 3.4. `EntregaResponse` (saída de sucesso, compartilhada com a US07)

| Campo           | Tipo                  | Descrição                                                                                                                                 |
| :-------------- | :-------------------- | :---------------------------------------------------------------------------------------------------------------------------------------- |
| `id`            | Long                  | Identificador único da entrega.                                                                                                           |
| `semestre`      | String                | Semestre da `Turma_TCC` à qual a entrega pertence (ex.: `"2026/2"`).                                                                      |
| `tipo`          | `TipoEntrega`         | Etapa da entrega (`PROJETO_TCC`, `TCC1` ou `TCC2`).                                                                                       |
| `descricaoTipo` | String                | Nome de exibição do tipo de entrega (ex.: `"Projeto de TCC"`).                                                                            |
| `urlDocumento`  | String                | URL `https://` do documento ou repositório submetido pelo aluno.                                                                          |
| `status`        | `StatusEntrega`       | Estado atual da entrega (`SUBMETIDO`, `AJUSTES_NECESSARIOS`, `APROVADO`, `REPROVADO`).                                                    |
| `atrasada`      | Boolean               | `false` se enviada até a `dataLimite` do prazo da etapa (fuso `America/Sao_Paulo`); `true` se enviada após a `dataLimite`.                |
| `enviadoEm`     | String (ISO-8601 UTC) | Data e hora da última submissão ou reenvio (ex.: `"2026-11-03T14:20:00Z"`).                                                               |
| `aluno`         | Objeto                | `{ id: Long, nome: String, email: String, curso: String }`                                                                                |
| `orientador`    | Objeto                | `{ id: Long, nome: String, email: String }`                                                                                               |
| `parecer`       | Objeto ou `null`      | Último parecer registrado pelo orientador (`{ id: Long, resultado: StatusEntrega, comentario: String, registradoEm: String }`) ou `null`. |

---

## 4. Especificação do Endpoint

### 4.1. Submeter ou Reenviar Entrega de TCC

- **Método HTTP:** `POST`
- **Rota:** `/api/v1/entregas`
- **Permissão:** `ROLE_ALUNO` (usuário autenticado deve possuir solicitação de orientação com status `ACEITA`).
- **Descrição:** Registra a submissão do link externo de uma etapa do TCC na `Turma_TCC` vigente ou realiza o reenvio caso a entrega existente daquela etapa esteja com status `AJUSTES_NECESSARIOS`.
- **Regras de Negócio:**
  1. **Vínculo de orientação ativo (`403`):** O aluno autenticado (claim `email`) deve existir na base e possuir uma `SolicitacaoOrientacao` com `status = ACEITA`. Caso contrário, a operação é negada com `403 Forbidden`.
  2. **Turma vigente e prazo configurado (`422`):** Deve existir uma `Turma_TCC` com `vigente = true` e um `PrazoEntrega` cadastrado para o `tipo` informado nessa turma. Se não houver turma vigente ou se o prazo da etapa ainda não tiver sido cadastrado no calendário, retorna `422 Unprocessable Content`.
  3. **Cálculo da flag `atrasada`:** O backend compara a data atual no fuso `America/Sao_Paulo` (`LocalDate.now(clock)`) com a `dataLimite` do `PrazoEntrega` correspondente:
     - Se `dataAtual <= dataLimite` → `atrasada = false`.
     - Se `dataAtual > dataLimite` → `atrasada = true` (o envio **não é bloqueado**, ficando registrado com a marcação de atraso para ciência do orientador).
  4. **Controle de duplicidade e regra de reenvio (`409` vs. atualização):**
     - **Primeira submissão da etapa:** cria a entidade `Entrega` com `status = SUBMETIDO` e retorna `201 Created` (com cabeçalho `Location: /api/v1/entregas/{id}`).
     - **Reenvio permitido (`status == AJUSTES_NECESSARIOS`):** quando já existe entrega do aluno para a mesma `(turma, tipo)` com status `AJUSTES_NECESSARIOS`, o registro existente é atualizado com o novo `urlDocumento`, o `status` retorna para `SUBMETIDO`, `atrasada` é recalculada em relação à data do reenvio e `enviadoEm` é atualizado, retornando `200 OK` com a `EntregaResponse` atualizada.
     - **Submissão bloqueada (`status` em `SUBMETIDO`, `APROVADO` ou `REPROVADO`):** retorna `409 Conflict`.

#### Cabeçalhos da Requisição

```http
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição

```json
{
  "tipo": "PROJETO_TCC",
  "urlDocumento": "https://docs.google.com/document/d/1aBcDeFgHiJkLmNoPqRsTuVwXyZ/edit?usp=sharing"
}
```

#### Respostas Possíveis

##### Sucesso (Primeira submissão): `201 Created`

- **Cabeçalho:** `Location: /api/v1/entregas/1`
- **Corpo (`EntregaResponse`):**

```json
{
  "id": 1,
  "semestre": "2026/2",
  "tipo": "PROJETO_TCC",
  "descricaoTipo": "Projeto de TCC",
  "urlDocumento": "https://docs.google.com/document/d/1aBcDeFgHiJkLmNoPqRsTuVwXyZ/edit?usp=sharing",
  "status": "SUBMETIDO",
  "atrasada": false,
  "enviadoEm": "2026-11-03T14:20:00Z",
  "aluno": {
    "id": 7,
    "nome": "Aluno de Teste",
    "email": "aluno@example.com",
    "curso": "Análise e Desenvolvimento de Sistemas"
  },
  "orientador": {
    "id": 1,
    "nome": "Professor Orientador",
    "email": "professor.orientador@example.com"
  },
  "parecer": null
}
```

##### Sucesso (Reenvio após `AJUSTES_NECESSARIOS`): `200 OK`

Retorna `EntregaResponse` com `status: "SUBMETIDO"`, `urlDocumento` atualizado, `atrasada` recalculada e `enviadoEm` atualizado (mantendo o último `parecer` registrado até que uma nova avaliação ocorra).

##### Erro de Validação: `400 Bad Request`

Retornado quando `tipo` ou `urlDocumento` estão ausentes, ou quando `urlDocumento` não é uma URL válida com protocolo `https://`:

```json
{
  "timestamp": "2026-11-03T14:25:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/entregas",
  "detalhes": [
    {
      "campo": "urlDocumento",
      "mensagem": "A URL do documento deve ser válida e utilizar o protocolo https."
    }
  ]
}
```

##### Erro de Acesso Negado: `403 Forbidden`

Retornado pelo Spring Security quando o usuário não possui `ROLE_ALUNO`, ou pela camada de serviço (`AcessoNegadoException`) quando o aluno autenticado não possui orientação `ACEITA`:

```json
{
  "timestamp": "2026-11-03T14:25:00Z",
  "status": 403,
  "erro": "Acesso negado",
  "caminho": "/api/v1/entregas",
  "detalhes": "Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas."
}
```

##### Erro de Conflito: `409 Conflict`

Retornado quando já existe entrega para a mesma etapa na turma vigente com status `SUBMETIDO`, `APROVADO` ou `REPROVADO`:

```json
{
  "timestamp": "2026-11-03T14:25:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/entregas",
  "detalhes": "Já existe uma entrega para Projeto de TCC com status SUBMETIDO. O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS."
}
```

##### Erro de Regra de Negócio: `422 Unprocessable Content`

Retornado quando não há turma vigente ou quando o calendário da turma vigente ainda não possui prazo cadastrado para a etapa solicitada:

```json
{
  "timestamp": "2026-11-03T14:25:00Z",
  "status": 422,
  "erro": "Regra de negócio violada",
  "caminho": "/api/v1/entregas",
  "detalhes": "Não há prazo cadastrado para Projeto de TCC na turma vigente."
}
```

---

## 5. Tabela Oficial de Códigos e Mensagens de Erro

| Código | `campo` / Origem            | Condição                                                                     | Mensagem (`detalhes`)                                                                                                               |
| :----: | :-------------------------- | :--------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------- |
| `400`  | `tipo`                      | Ausente (`null`)                                                             | `O tipo de entrega é obrigatório.`                                                                                                  |
| `400`  | `urlDocumento`              | Ausente ou em branco                                                         | `A URL do documento é obrigatória.`                                                                                                 |
| `400`  | `urlDocumento`              | Formato inválido ou protocolo diferente de `https`                           | `A URL do documento deve ser válida e utilizar o protocolo https.`                                                                  |
| `400`  | `urlDocumento`              | Acima de 2048 caracteres                                                     | `A URL do documento deve ter no máximo 2048 caracteres.`                                                                            |
| `400`  | Corpo JSON                  | JSON mal formatado ou valor de `tipo` fora da enumeração `TipoEntrega`       | `O corpo da requisição está ausente, mal formatado ou contém um valor incompatível com o tipo esperado.`                            |
| `401`  | Spring Security             | Token JWT ausente, expirado ou inválido                                      | _(Sem corpo `ErroResponse`)_                                                                                                        |
| `403`  | Spring Security             | Token válido, mas sem a role `ROLE_ALUNO`                                    | _(Sem corpo `ErroResponse`)_                                                                                                        |
| `403`  | `AcessoNegadoException`     | Aluno autenticado não possui solicitação de orientação `ACEITA`              | `Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas.`                                             |
| `409`  | `ConflitoDeEstadoException` | Entrega da etapa já existe com status `SUBMETIDO`, `APROVADO` ou `REPROVADO` | `Já existe uma entrega para {descricaoTipo} com status {status}. O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS.` |
| `422`  | `RegraDeNegocioException`   | Não existe `Turma_TCC` com `vigente = true`                                  | `Não há turma de TCC vigente cadastrada para receber entregas.`                                                                     |
| `422`  | `RegraDeNegocioException`   | Etapa ainda não possui `PrazoEntrega` cadastrado na turma vigente            | `Não há prazo cadastrado para {descricaoTipo} na turma vigente.`                                                                    |

---

## 6. Máquina de Estados da Entrega (Ciclo US06 + US07)

```text
                                 (Reenvio pelo Aluno - POST /api/v1/entregas)
                            ┌────────────────────────────────────────────────────┐
                            ▼                                                    │
 [Início] ──(POST)──► SUBMETIDO ──(Parecer do Orientador)──► AJUSTES_NECESSARIOS ┘
                            │
                            ├─────(Parecer do Orientador)──► APROVADO   [Estado Final]
                            │
                            └─────(Parecer do Orientador)──► REPROVADO  [Estado Final]
```

---

## 7. Decisões Assumidas

| #   | Decisão                                                                                             | Justificativa                                                                                                                                 |
| :-- | :-------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------- |
| D1  | Submissão via JSON com `urlDocumento` (`https://`) em vez de `multipart/form-data`.                 | Cumpre a [ADR-0013](adrs/ADR-0013.md), evitando custódia de binários e volumes extras de storage no Docker.                                   |
| D2  | Validação de `urlDocumento` com `@URL(protocol = "https")` retorna `400 Bad Request`.               | É uma validação sintática de campo (Bean Validation), seguindo o padrão de `400` com array `[{campo, mensagem}]` do `GlobalExceptionHandler`. |
| D3  | Envio após `dataLimite` não é bloqueado; define `atrasada = true`.                                  | Atende ao critério da Issue #216: permite que o aluno entregue com atraso devidamente registrado para avaliação do orientador.                |
| D4  | Reenvio atualiza a `Entrega` existente quando `status == AJUSTES_NECESSARIOS`.                      | Mantém unicidade por `(aluno_id, turma_id, tipo)` na tabela `entregas` (Issue #208), evitando múltiplos registros órfãos para a mesma etapa.  |
| D5  | Exigência de `PrazoEntrega` cadastrado na turma vigente para o `tipo` submetido (`422` se ausente). | O cálculo determinístico da flag `atrasada` depende da existência da `dataLimite` daquela etapa no calendário da turma vigente.               |
