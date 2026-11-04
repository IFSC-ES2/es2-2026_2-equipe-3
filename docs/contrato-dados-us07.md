# Contrato de Dados da API - US07: Avaliação, Consulta e Parecer das Entregas

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 03/11/2026
- **Responsáveis:** Damares Gaia (Arquiteta de Software) e Talles Souza da Cruz (Desenvolvedor Backend)
- **História de Usuário Relacionada:** US07 - Avaliação e Parecer das Entregas (Sprint 4)
- **Issues Relacionadas:** #14 (US07), #209 (Contrato de Dados), #210 (Endpoints de Consulta/Acesso ao Documento), #215 (Migração Flyway V7 e Endpoint de Registro de Parecer)
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o **Contrato de Dados formal entre o Frontend (React) e o Backend (Spring Boot)** para a **consulta das entregas submetidas (acesso ao link externo do documento)** e para o **registro de parecer de avaliação pelo orientador** (US07).

De acordo com a [ADR-0013](adrs/ADR-0013.md), o "download/acesso ao documento" pelo orientador consiste em consultar os metadados da submissão e acessar a URL segura (`urlDocumento`) hospedada em nuvem. Após analisar o trabalho, o orientador vinculado registra seu parecer formal (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`), atualizando o status da entrega e disponibilizando o feedback para acompanhamento do aluno.

Este contrato utiliza o modelo `EntregaResponse` e as enumerações `TipoEntrega` e `StatusEntrega` definidos no [contrato da US06](contrato-dados-us06.md).

---

## 2. Padrões Globais da API

Herdados dos contratos da [US01](contrato-dados-us01.md), [US04](contrato-dados-us04.md), [US05](contrato-dados-us05.md) e [US06](contrato-dados-us06.md):

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Nomenclatura dos Campos (JSON):** `camelCase`
- **Datas e Carimbos de Tempo:** ISO-8601 em UTC (`YYYY-MM-DDTHH:mm:ssZ`).
- **Formato de Erro:** objeto padrão `ErroResponse` (`ErrorResponseDTO` / `GlobalExceptionHandler`). No `400` de validação de campos, `detalhes` é um array `[{campo, mensagem}]`; nos demais códigos (`403`, `404`, `409`, `422`), `detalhes` é uma `String`.
- **Autenticação e Autorização:** todas as rotas exigem `Authorization: Bearer <token>` emitido pelo Keycloak ([ADR-0012](adrs/ADR-0012.md)). A identidade do usuário autenticado é extraída da claim `email` do token JWT.

---

## 3. Modelos de Dados

### 3.1. `ParecerRequest` (entrada do `POST /api/v1/entregas/{id}/parecer`)

| Campo        | Tipo            |                           Obrigatório?                           | Regras e Restrições                                                                                                                                      |
| :----------- | :-------------- | :--------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `resultado`  | `StatusEntrega` |                               Sim                                | Deve ser `APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`. O valor `SUBMETIDO` não é permitido como resultado de parecer (`422`).                        |
| `comentario` | String          | Sim, quando `resultado` for `AJUSTES_NECESSARIOS` ou `REPROVADO` | Até 2000 caracteres. Quando `resultado` for `AJUSTES_NECESSARIOS` ou `REPROVADO`, não pode ser nulo nem estar em branco (`422`). Opcional se `APROVADO`. |

### 3.2. `ParecerResumoResponse` (aninhado no campo `parecer` de `EntregaResponse`)

| Campo          | Tipo                  | Descrição                                                                              |
| :------------- | :-------------------- | :------------------------------------------------------------------------------------- |
| `id`           | Long                  | Identificador único do parecer registrado.                                             |
| `resultado`    | `StatusEntrega`       | Decisão registrada pelo orientador (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`). |
| `comentario`   | String ou `null`      | Observações, ressalvas ou justificativa técnica registrada pelo orientador.            |
| `registradoEm` | String (ISO-8601 UTC) | Data e hora em que o parecer foi registrado (ex.: `"2026-11-03T16:00:00Z"`).           |

### 3.3. `EntregaResponse` (saída de todos os endpoints de consulta e parecer)

Mesma estrutura definida na seção 3.4 do [contrato da US06](contrato-dados-us06.md), contendo `id`, `semestre`, `tipo`, `descricaoTipo`, `urlDocumento`, `status`, `atrasada`, `enviadoEm`, `aluno`, `orientador` e `parecer`.

---

## 4. Sumário dos Endpoints

| Método | Rota                            | Descrição                                                                                      | Sucesso  | Quem usa                                                    |
| :----- | :------------------------------ | :--------------------------------------------------------------------------------------------- | :------- | :---------------------------------------------------------- |
| `GET`  | `/api/v1/entregas/orientador`   | Lista as entregas dos orientandos vinculados ao professor autenticado (inclui `urlDocumento`). | `200 OK` | Orientador (`ROLE_PROFESSOR`)                               |
| `GET`  | `/api/v1/entregas/minhas`       | Lista as entregas submetidas pelo aluno autenticado para acompanhamento no painel.             | `200 OK` | Aluno (`ROLE_ALUNO`)                                        |
| `GET`  | `/api/v1/entregas/{id}`         | Detalha uma entrega específica (restrito ao aluno autor ou ao orientador vinculado).           | `200 OK` | Aluno autor (`ROLE_ALUNO`) ou Orientador (`ROLE_PROFESSOR`) |
| `POST` | `/api/v1/entregas/{id}/parecer` | Orientador vinculado registra parecer (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`).      | `200 OK` | Orientador vinculado (`ROLE_PROFESSOR`)                     |

---

## 5. Especificação Detalhada dos Endpoints

### 5.1. Listar Entregas dos Orientandos do Professor Autenticado

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/entregas/orientador`
- **Permissão:** `ROLE_PROFESSOR`.
- **Descrição:** Retorna todas as entregas pertencentes aos alunos orientados pelo professor autenticado (identificado pela claim `email` do JWT), ordenadas por `enviadoEm` decrescente (mais recentes primeiro). É por meio deste endpoint que o orientador obtém o `urlDocumento` para acessar o trabalho submetido pelo orientando.
- **Parâmetros de Query (opcionais):**

| Parâmetro | Local | Tipo            | Obrigatório? | Descrição                                                                             |
| :-------- | :---- | :-------------- | :----------: | :------------------------------------------------------------------------------------ |
| `status`  | Query | `StatusEntrega` |     Não      | Filtra as entregas pelo status (ex.: `?status=SUBMETIDO`). Sem filtro, retorna todas. |

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna um array de `EntregaResponse` (ou `[]` caso nenhum orientando tenha submetido entregas):

```json
[
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
]
```

##### Erro: `400 Bad Request` (parâmetro `status` inválido)

```json
{
  "timestamp": "2026-11-03T15:00:00Z",
  "status": 400,
  "erro": "Parâmetro de requisição inválido",
  "caminho": "/api/v1/entregas/orientador",
  "detalhes": "Valor inválido para o parâmetro 'status'. Valores aceitos: SUBMETIDO, AJUSTES_NECESSARIOS, APROVADO, REPROVADO."
}
```

##### Erro: `403 Forbidden`

Token sem a role `ROLE_PROFESSOR` (gerado pelo Spring Security, sem corpo `ErroResponse`).

##### Erro: `404 Not Found`

O e-mail do token JWT não corresponde a nenhum orientador cadastrado:

```json
{
  "timestamp": "2026-11-03T15:00:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/entregas/orientador",
  "detalhes": "Orientador com e-mail desconhecido@example.com não foi encontrado."
}
```

---

### 5.2. Listar Entregas do Próprio Aluno Autenticado

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/entregas/minhas`
- **Permissão:** `ROLE_ALUNO`.
- **Descrição:** Retorna as entregas submetidas pelo aluno autenticado (identificado pela claim `email` do JWT), ordenadas pela sequência da etapa (`PROJETO_TCC`, `TCC1`, `TCC2`), permitindo que o estudante acompanhe no painel o status de cada etapa e leia o parecer do orientador. Caso o aluno ainda não possua entregas registradas, retorna lista vazia `[]` (`200 OK`). O mesmo vale quando o e-mail do token ainda não corresponde a nenhum aluno cadastrado: a resposta é `[]`, e **não** `404` (ver decisão D6).

#### Respostas Possíveis

##### Sucesso: `200 OK`

Array de `EntregaResponse` (mesmo formato da seção 5.1).

##### Erro: `403 Forbidden`

Token sem a role `ROLE_ALUNO` (gerado pelo Spring Security, sem corpo `ErroResponse`).

---

### 5.3. Consultar Detalhes de uma Entrega por ID

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/entregas/{id}`
- **Permissão:** `ROLE_ALUNO` ou `ROLE_PROFESSOR`, **restrito ao próprio aluno autor da entrega ou ao orientador vinculado à entrega**.
- **Descrição:** Retorna os detalhes completos de uma entrega específica, incluindo `urlDocumento` para leitura/acesso externo e o último `parecer`.

#### Respostas Possíveis

##### Sucesso: `200 OK`

Corpo `EntregaResponse`.

##### Erro: `400 Bad Request` (identificador inválido)

Retornado quando `{id}` não é um número inteiro (ex.: `/api/v1/entregas/abc`), pelo handler de `MethodArgumentTypeMismatchException`:

```json
{
  "timestamp": "2026-11-03T15:10:00Z",
  "status": 400,
  "erro": "Parâmetro de requisição inválido",
  "caminho": "/api/v1/entregas/abc",
  "detalhes": "Valor inválido para o parâmetro 'id'."
}
```

##### Erro: `403 Forbidden`

Há duas origens distintas para o `403`:

- **Spring Security (sem corpo `ErroResponse`):** token válido, mas sem `ROLE_ALUNO` nem `ROLE_PROFESSOR`.
- **Camada de serviço (`AcessoNegadoException`, com corpo `ErroResponse`):** outro aluno ou outro professor (que não é o orientador da entrega) tenta consultar a entrega:

```json
{
  "timestamp": "2026-11-03T15:10:00Z",
  "status": 403,
  "erro": "Acesso negado",
  "caminho": "/api/v1/entregas/1",
  "detalhes": "Acesso permitido apenas ao aluno autor ou ao orientador vinculado a esta entrega."
}
```

##### Erro: `404 Not Found`

```json
{
  "timestamp": "2026-11-03T15:10:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/entregas/99",
  "detalhes": "Entrega com identificador 99 não foi encontrada."
}
```

---

### 5.4. Registrar Parecer do Orientador sobre uma Entrega

- **Método HTTP:** `POST`
- **Rota:** `/api/v1/entregas/{id}/parecer`
- **Permissão:** `ROLE_PROFESSOR` **e** ser o orientador vinculado à `Entrega`.
- **Descrição:** Registra o parecer de avaliação do orientador sobre uma entrega com status `SUBMETIDO` e atualiza o status da entrega para o `resultado` informado (`APROVADO`, `AJUSTES_NECESSARIOS` ou `REPROVADO`), sem envio de e-mail ou notificação externa.
  - Se `resultado == AJUSTES_NECESSARIOS`: exige `comentario` obrigatório e libera a etapa para reenvio pelo aluno (`POST /api/v1/entregas`).
  - Se `resultado == REPROVADO`: exige `comentario` obrigatório e encerra a etapa como reprovada.
  - Se `resultado == APROVADO`: `comentario` é opcional e encerra a etapa como aprovada.

#### Cabeçalhos da Requisição

```http
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição (Exemplo: Solicitação de Ajustes)

```json
{
  "resultado": "AJUSTES_NECESSARIOS",
  "comentario": "Detalhar melhor a seção de metodologia e incluir o cronograma de execução."
}
```

#### Corpo da Requisição (Exemplo: Aprovação)

```json
{
  "resultado": "APROVADO",
  "comentario": "Escopo e fundamentação teórica adequados para a etapa."
}
```

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna a `EntregaResponse` atualizada com o novo `status` e o objeto `parecer` preenchido:

```json
{
  "id": 1,
  "semestre": "2026/2",
  "tipo": "PROJETO_TCC",
  "descricaoTipo": "Projeto de TCC",
  "urlDocumento": "https://docs.google.com/document/d/1aBcDeFgHiJkLmNoPqRsTuVwXyZ/edit?usp=sharing",
  "status": "AJUSTES_NECESSARIOS",
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
  "parecer": {
    "id": 1,
    "resultado": "AJUSTES_NECESSARIOS",
    "comentario": "Detalhar melhor a seção de metodologia e incluir o cronograma de execução.",
    "registradoEm": "2026-11-03T16:00:00Z"
  }
}
```

##### Erro de Validação: `400 Bad Request`

Retornado quando `resultado` está ausente ou quando `comentario` ultrapassa 2000 caracteres (validação de campos, `detalhes` em array):

```json
{
  "timestamp": "2026-11-03T16:05:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/entregas/1/parecer",
  "detalhes": [
    {
      "campo": "resultado",
      "mensagem": "O resultado do parecer é obrigatório."
    }
  ]
}
```

Também retorna `400`, com `detalhes` em `String`, quando o corpo JSON está mal formatado ou `resultado` contém um valor fora de `StatusEntrega` (`erro`: `Corpo da requisição inválido ou ilegível`), e quando `{id}` não é um número inteiro (`erro`: `Parâmetro de requisição inválido`).

##### Erro de Acesso Negado: `403 Forbidden`

Há duas origens distintas para o `403`:

- **Spring Security (sem corpo `ErroResponse`):** token válido, mas sem a role `ROLE_PROFESSOR` (por exemplo, um aluno).
- **Camada de serviço (`AcessoNegadoException`, com corpo `ErroResponse`):** o professor autenticado **não é o orientador vinculado** à entrega:

```json
{
  "timestamp": "2026-11-03T16:05:00Z",
  "status": 403,
  "erro": "Acesso negado",
  "caminho": "/api/v1/entregas/1/parecer",
  "detalhes": "Somente o orientador vinculado à entrega pode registrar parecer."
}
```

##### Erro de Não Encontrado: `404 Not Found`

```json
{
  "timestamp": "2026-11-03T16:05:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/entregas/99/parecer",
  "detalhes": "Entrega com identificador 99 não foi encontrada."
}
```

##### Erro de Conflito: `409 Conflict`

Retornado quando a entrega já possui status definitivo (`APROVADO` ou `REPROVADO`) ou já está aguardando reenvio (`AJUSTES_NECESSARIOS`):

```json
{
  "timestamp": "2026-11-03T16:05:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/entregas/1/parecer",
  "detalhes": "Esta entrega já possui parecer definitivo (APROVADO) e não pode ser reavaliada."
}
```

##### Erro de Regra de Negócio: `422 Unprocessable Content`

Retornado quando `resultado` é `AJUSTES_NECESSARIOS` ou `REPROVADO` e o `comentario` está ausente ou em branco, ou quando `resultado` é `SUBMETIDO`:

```json
{
  "timestamp": "2026-11-03T16:05:00Z",
  "status": 422,
  "erro": "Regra de negócio violada",
  "caminho": "/api/v1/entregas/1/parecer",
  "detalhes": "O comentário com a justificativa é obrigatório quando o parecer exige ajustes ou reprova a entrega."
}
```

---

## 6. Tabela Oficial de Códigos e Mensagens de Erro

| Endpoint                      | Código | `campo` / Origem                      | Condição                                                                                  | Mensagem (`detalhes`)                                                                                             |
| :---------------------------- | :----: | :------------------------------------ | :---------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------- |
| Todos                         | `401`  | Spring Security                       | Token JWT ausente, expirado ou inválido                                                   | _(Sem corpo `ErroResponse`)_                                                                                      |
| `GET /entregas/orientador`    | `400`  | `MethodArgumentTypeMismatchException` | Query param `status` fora de `StatusEntrega`                                              | `Valor inválido para o parâmetro 'status'. Valores aceitos: SUBMETIDO, AJUSTES_NECESSARIOS, APROVADO, REPROVADO.` |
| `GET /entregas/orientador`    | `403`  | Spring Security                       | Usuário sem `ROLE_PROFESSOR`                                                              | _(Sem corpo `ErroResponse`)_                                                                                      |
| `GET /entregas/orientador`    | `404`  | `ResourceNotFoundException`           | E-mail do token não pertence a nenhum orientador cadastrado                               | `Orientador com e-mail {email} não foi encontrado.`                                                               |
| `GET /entregas/minhas`        | `403`  | Spring Security                       | Usuário sem `ROLE_ALUNO`                                                                  | _(Sem corpo `ErroResponse`)_                                                                                      |
| `GET /entregas/{id}`, `POST`  | `400`  | `MethodArgumentTypeMismatchException` | `{id}` não é um número inteiro                                                            | `Valor inválido para o parâmetro 'id'.`                                                                           |
| `GET /entregas/{id}`          | `403`  | Spring Security                       | Usuário sem `ROLE_ALUNO` e sem `ROLE_PROFESSOR`                                           | _(Sem corpo `ErroResponse`)_                                                                                      |
| `GET /entregas/{id}`          | `403`  | `AcessoNegadoException`               | Usuário autenticado não é o aluno autor nem o orientador vinculado à entrega              | `Acesso permitido apenas ao aluno autor ou ao orientador vinculado a esta entrega.`                               |
| `GET /entregas/{id}`, `POST`  | `404`  | `ResourceNotFoundException`           | Entrega inexistente                                                                       | `Entrega com identificador {id} não foi encontrada.`                                                              |
| `POST /entregas/{id}/parecer` | `400`  | `resultado`                           | Ausente (`null`)                                                                          | `O resultado do parecer é obrigatório.`                                                                           |
| `POST /entregas/{id}/parecer` | `400`  | `comentario`                          | Acima de 2000 caracteres                                                                  | `O comentário deve ter no máximo 2000 caracteres.`                                                                |
| `POST /entregas/{id}/parecer` | `400`  | `HttpMessageNotReadableException`     | JSON mal formatado ou `resultado` fora da enumeração `StatusEntrega`                      | `O corpo da requisição está ausente, mal formatado ou contém um valor incompatível com o tipo esperado.`          |
| `POST /entregas/{id}/parecer` | `403`  | Spring Security                       | Usuário sem `ROLE_PROFESSOR`                                                              | _(Sem corpo `ErroResponse`)_                                                                                      |
| `POST /entregas/{id}/parecer` | `403`  | `AcessoNegadoException`               | Professor autenticado não é o orientador vinculado à entrega                              | `Somente o orientador vinculado à entrega pode registrar parecer.`                                                |
| `POST /entregas/{id}/parecer` | `409`  | `ConflitoDeEstadoException`           | Entrega já avaliada com status `APROVADO` ou `REPROVADO`                                  | `Esta entrega já possui parecer definitivo ({status}) e não pode ser reavaliada.`                                 |
| `POST /entregas/{id}/parecer` | `409`  | `ConflitoDeEstadoException`           | Entrega com status `AJUSTES_NECESSARIOS` (aguardando reenvio do aluno)                    | `Esta entrega aguarda reenvio do aluno antes de receber um novo parecer.`                                         |
| `POST /entregas/{id}/parecer` | `422`  | `RegraDeNegocioException`             | `resultado` em `AJUSTES_NECESSARIOS` ou `REPROVADO` com `comentario` ausente ou em branco | `O comentário com a justificativa é obrigatório quando o parecer exige ajustes ou reprova a entrega.`             |
| `POST /entregas/{id}/parecer` | `422`  | `RegraDeNegocioException`             | `resultado` enviado com valor `SUBMETIDO`                                                 | `O resultado do parecer deve ser APROVADO, AJUSTES_NECESSARIOS ou REPROVADO.`                                     |

### 6.1. Matriz de Roles e Respostas de Erro por Endpoint

| Endpoint                             | Role exigida (Keycloak)                 | `400`                          | `403` | `404`                          | `409`         | `422`         |
| :----------------------------------- | :-------------------------------------- | :----------------------------- | :---: | :----------------------------- | :------------ | :------------ |
| `GET /api/v1/entregas/orientador`    | `ROLE_PROFESSOR`                        | Sim (`status` inválido)        |  Sim  | Sim                            | Não se aplica | Não se aplica |
| `GET /api/v1/entregas/minhas`        | `ROLE_ALUNO`                            | Não se aplica (sem parâmetros) |  Sim  | Não se aplica (ver decisão D6) | Não se aplica | Não se aplica |
| `GET /api/v1/entregas/{id}`          | `ROLE_ALUNO` ou `ROLE_PROFESSOR`        | Sim (`id` inválido)            |  Sim  | Sim                            | Não se aplica | Não se aplica |
| `POST /api/v1/entregas/{id}/parecer` | `ROLE_PROFESSOR` (orientador vinculado) | Sim                            |  Sim  | Sim                            | Sim           | Sim           |

> "Não se aplica" indica que o endpoint é somente leitura (sem conflito de estado nem regra de negócio a violar) ou que a condição não existe para ele. `401 Unauthorized` (token ausente, expirado ou inválido) é gerado pelo Spring Security para todas as rotas, sem corpo `ErroResponse`.

---

## 7. Decisões Assumidas

| #   | Decisão                                                                                                               | Justificativa                                                                                                                                 |
| :-- | :-------------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------- |
| D1  | O acesso ao trabalho pelo orientador ocorre via `urlDocumento` nos endpoints `GET`, sem stream binário.               | Segue estritamente a [ADR-0013](adrs/ADR-0013.md) e os critérios da Issue #210.                                                               |
| D2  | Comentário/justificativa ausente em `AJUSTES_NECESSARIOS` ou `REPROVADO` retorna `422`.                               | Exigência explícita do critério de aceitação da Issue #215 (`422` se ausente ou em branco).                                                   |
| D3  | Tentativa de avaliar entrega já em `APROVADO` ou `REPROVADO` retorna `409 Conflict`.                                  | Exigência explícita da Issue #215; impede sobrescrita acidental de avaliações concluídas.                                                     |
| D4  | A entidade `Parecer` (Flyway `V7`) guarda o histórico de pareceres, e `EntregaResponse` expõe o mais recente.         | Permite que uma entrega reenviada após `AJUSTES_NECESSARIOS` receba novo parecer mantendo a rastreabilidade no banco de dados.                |
| D5  | Sem envio de e-mail ou push ao registrar parecer.                                                                     | Alinhado à Issue #215 e ao escopo do MVP; o aluno acompanha pelo painel via `GET /api/v1/entregas/minhas` ou `GET /api/v1/entregas/{id}`.     |
| D6  | `GET /api/v1/entregas/minhas` retorna `[]` (e não `404`) quando o e-mail do token não corresponde a aluno cadastrado. | O aluno só é cadastrado na primeira solicitação de orientação (US04); para o painel, "sem cadastro" e "sem entregas" têm o mesmo significado. |
| D7  | Status de parecer `AJUSTES_NECESSARIOS` em vez de `RESSALVAS`, citado na [ADR-0013](adrs/ADR-0013.md).                | Valores definidos na Issue #209 e compartilhados com a máquina de estados da US06; prevalecem sobre o exemplo da ADR.                         |
