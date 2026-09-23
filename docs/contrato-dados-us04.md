# Contrato de Dados da API - US04: Solicitação e Aceite de Orientação

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 23/09/2026
- **Responsável Arquitetural:** Damares Gaia (Arquiteta de Software)
- **História de Usuário Relacionada:** US04 – Solicitação e Aceite de Orientação (com a fatia operacional da US03 - botão "Solicitar Orientação" no catálogo)
- **Issue de Critérios de Aceitação:** #11 (cenários 1–2 nativos da issue, com "Aprovado" corrigido para "Aceita"; cenários 3–10 desta seção [11](#11-critérios-de-aceitação-da-us04-formato-bdd) incorporados)
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o **Contrato de Dados formal entre o Frontend (React) e o Backend (Spring Boot)** para os endpoints da US04, implementada na Sprint 2 (entrega `v0.2.0`).

O objetivo é permitir que Frontend, Backend e Qualidade trabalhem **em paralelo**, garantindo:

1. Nomes de campos, tipos e estruturas JSON padronizados.
2. Regras de validação explícitas (Bean Validation no backend e validação de formulário no frontend).
3. Códigos de status HTTP e mensagens de erro previsíveis, definidos neste contrato e refletidos nos cenários BDD da US04 (seção 10).
4. Conformidade com a arquitetura em camadas ([ADR-0003](adrs/ADR-0003.md)) e com o plano da Sprint ([`entregas/sprint-2.md`](entregas/sprint-2.md)).

Este contrato complementa o [contrato da US02](contrato-dados-us02.md), cujos padrões globais são reaproveitados.

---

## 2. Padrões Globais da API

Herdados do contrato da US02, sem alterações:

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Nomenclatura dos Campos (JSON):** `camelCase`
- **Datas:** ISO 8601 em UTC (`YYYY-MM-DDTHH:mm:ssZ`)
- **Formato de Erro:** objeto padrão `ErroResponse` (seção 3.5), o mesmo produzido por `ErrorResponseDTO` / `GlobalExceptionHandler`.
- **Autenticação:** nenhuma nesta sprint (a US01 foi postergada). Todos os endpoints são públicos; ver limitação registrada na [seção 8](#8-decisões-assumidas-e-divergências-com-o-código-atual).

---

## 3. Modelos de Dados Compartilhados

### 3.1. Enumeração `StatusSolicitacao`

| Valor       | Significado                                                                | Definido por                   |
| :---------- | :------------------------------------------------------------------------- | :----------------------------- |
| `PENDENTE`  | Solicitação criada e aguardando resposta do orientador                     | Sistema (criação)              |
| `ACEITA`    | Orientador aceitou; uma vaga foi consumida                                 | Orientador (`PATCH`)           |
| `RECUSADA`  | Orientador recusou, com justificativa                                      | Orientador (`PATCH`)           |
| `CANCELADA` | Solicitação cancelada (reservado no modelo; **sem endpoint** nesta sprint) | - (fora do escopo da Sprint 2) |

### 3.2. `SolicitacaoRequest` (entrada do `POST`)

| Campo          | Tipo   | Obrigatório? | Regras e Restrições                                                       |
| :------------- | :----- | :----------- | :------------------------------------------------------------------------ |
| `orientadorId` | Long   | Sim          | Identificador de um orientador existente. Valor `>= 1`.                   |
| `aluno`        | Objeto | Sim          | Dados do aluno (ver abaixo).                                              |
| `aluno.nome`   | String | Sim          | 3 a 100 caracteres. Não pode conter apenas espaços.                       |
| `aluno.email`  | String | Sim          | Formato de e-mail válido. Máximo 100 caracteres. Chave de busca do aluno. |
| `aluno.curso`  | String | Sim          | 2 a 100 caracteres. Não pode conter apenas espaços.                       |
| `tema`         | String | Sim          | 5 a 150 caracteres. Não pode conter apenas espaços.                       |
| `mensagem`     | String | Sim          | 10 a 1000 caracteres. Proposta/justificativa inicial.                     |

### 3.3. `SolicitacaoResponse` (saída de todos os endpoints de sucesso)

| Campo           | Tipo                | Descrição                                                                                          |
| :-------------- | :------------------ | :------------------------------------------------------------------------------------------------- |
| `id`            | Long                | Identificador da solicitação.                                                                      |
| `status`        | `StatusSolicitacao` | Estado atual.                                                                                      |
| `tema`          | String              | Tema pretendido.                                                                                   |
| `mensagem`      | String              | Mensagem inicial do aluno.                                                                         |
| `justificativa` | String ou `null`    | Motivo da recusa. `null` enquanto o status não for `RECUSADA`.                                     |
| `criadoEm`      | String (ISO 8601)   | Data/hora de criação.                                                                              |
| `aluno`         | Objeto              | `{ id: Long, nome: String, email: String, curso: String }`                                         |
| `orientador`    | Objeto              | `{ id: Long, nome: String, email: String, vagasDisponiveis: Integer }` (vagas **após** a operação) |

### 3.4. `AtualizaStatusRequest` (entrada do `PATCH`)

| Campo           | Tipo   | Obrigatório?                  | Regras e Restrições                                                        |
| :-------------- | :----- | :---------------------------- | :------------------------------------------------------------------------- |
| `status`        | String | Sim                           | Somente `ACEITA` ou `RECUSADA`.                                            |
| `justificativa` | String | Sim, quando `status=RECUSADA` | 10 a 500 caracteres. Se enviada com `ACEITA`, é ignorada e não persistida. |

### 3.5. `ErroResponse` (corpo de todos os erros)

```json
{
  "timestamp": "2026-09-25T14:00:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/solicitacoes",
  "detalhes": []
}
```

| Campo       | Tipo                                   | Descrição                                                                                            |
| :---------- | :------------------------------------- | :--------------------------------------------------------------------------------------------------- |
| `timestamp` | String (ISO 8601)                      | Momento do erro.                                                                                     |
| `status`    | Integer                                | Código HTTP.                                                                                         |
| `erro`      | String                                 | Título fixo por categoria de erro (ver seção 6).                                                     |
| `caminho`   | String                                 | URI da requisição.                                                                                   |
| `detalhes`  | Array de `{campo, mensagem}` ou String | **400:** array com um item por violação. **404, 409 e 422:** String com a mensagem da regra violada. |

---

## 4. Sumário dos Endpoints

| Método  | Rota                                             | Descrição                                                                  | Sucesso       | Quem usa           |
| :------ | :----------------------------------------------- | :------------------------------------------------------------------------- | :------------ | :----------------- |
| `POST`  | `/api/v1/solicitacoes`                           | Aluno envia uma solicitação de orientação                                  | `201 Created` | Aluno              |
| `GET`   | `/api/v1/solicitacoes/orientador/{orientadorId}` | Lista as solicitações recebidas por um orientador                          | `200 OK`      | Orientador         |
| `PATCH` | `/api/v1/solicitacoes/{id}/status`               | Orientador aceita ou recusa uma solicitação                                | `200 OK`      | Orientador         |
| `GET`   | `/api/v1/solicitacoes/{id}`                      | Detalha uma solicitação (também usado pelo aluno para acompanhar o status) | `200 OK`      | Aluno e Orientador |

---

## 5. Especificação Detalhada dos Endpoints

### 5.1. Envio de Solicitação de Orientação

- **Método HTTP:** `POST`
- **Rota:** `/api/v1/solicitacoes`
- **Descrição:** Cria uma solicitação com status `PENDENTE`. O backend busca o `Aluno` pelo e-mail; se não existir, cria o registro (subtipo de `Usuario`, sem credenciais de login) antes de associá-lo à solicitação. Se já existir, o registro é **reaproveitado sem alteração** de `nome` e `curso`.
- **Regras de negócio aplicadas (Strategy):** orientador ativo e com vagas (`vagasDisponiveis > 0`); inexistência de solicitação `PENDENTE` do mesmo aluno para o mesmo orientador.

#### Cabeçalhos da Requisição

```http
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição

```json
{
  "orientadorId": 1,
  "aluno": {
    "nome": "Ana Beatriz Souza",
    "email": "ana.souza@aluno.ifsc.edu.br",
    "curso": "Análise e Desenvolvimento de Sistemas"
  },
  "tema": "Sistema de recomendação de orientadores com IA",
  "mensagem": "Tenho interesse na linha de Engenharia de Software e gostaria de desenvolver meu TCC sobre métricas de qualidade."
}
```

#### Respostas Possíveis

##### Sucesso: `201 Created`

- **Cabeçalho:** `Location: /api/v1/solicitacoes/1`
- **Corpo:**

```json
{
  "id": 1,
  "status": "PENDENTE",
  "tema": "Sistema de recomendação de orientadores com IA",
  "mensagem": "Tenho interesse na linha de Engenharia de Software e gostaria de desenvolver meu TCC sobre métricas de qualidade.",
  "justificativa": null,
  "criadoEm": "2026-09-25T14:00:00Z",
  "aluno": {
    "id": 7,
    "nome": "Ana Beatriz Souza",
    "email": "ana.souza@aluno.ifsc.edu.br",
    "curso": "Análise e Desenvolvimento de Sistemas"
  },
  "orientador": {
    "id": 1,
    "nome": "Dr. Adriano Lima",
    "email": "adriano.lima@ifsc.edu.br",
    "vagasDisponiveis": 3
  }
}
```

> O `POST` da US02 devolve `201` sem corpo (apenas `Location`). Aqui o corpo é **obrigatório**, pois o frontend precisa do `id` e do `status` para exibir o acompanhamento sem uma segunda chamada.

##### Erro de Validação: `400 Bad Request`

```json
{
  "timestamp": "2026-09-25T14:00:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/solicitacoes",
  "detalhes": [
    { "campo": "aluno.email", "mensagem": "Formato de e-mail inválido." },
    { "campo": "tema", "mensagem": "O tema é obrigatório." }
  ]
}
```

##### Não Encontrado: `404 Not Found`

`orientadorId` não corresponde a nenhum orientador.

```json
{
  "timestamp": "2026-09-25T14:00:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/solicitacoes",
  "detalhes": "Orientador com identificador 99 não foi encontrado."
}
```

##### Conflito: `409 Conflict`

Solicitação `PENDENTE` duplicada, ou e-mail do aluno pertencente a um orientador.

```json
{
  "timestamp": "2026-09-25T14:00:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/solicitacoes",
  "detalhes": "Você já possui uma solicitação pendente para este orientador."
}
```

##### Regra de Negócio Violada: `422 Unprocessable Entity`

Orientador sem vagas ou inativo.

```json
{
  "timestamp": "2026-09-25T14:00:00Z",
  "status": 422,
  "erro": "Regra de negócio violada",
  "caminho": "/api/v1/solicitacoes",
  "detalhes": "O orientador não possui vagas disponíveis."
}
```

---

### 5.2. Listagem de Solicitações Recebidas por um Orientador

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/solicitacoes/orientador/{orientadorId}`
- **Descrição:** Retorna as solicitações endereçadas ao orientador, ordenadas por `criadoEm` decrescente (mais recentes primeiro). Sem paginação nesta sprint.
- **Uso no painel do orientador:** o "painel" de notificações da #11 é esta listagem com `?status=PENDENTE`, consultada quando o orientador abre a tela (e ao recarregar/atualizar a lista após aceitar ou recusar). **Não há notificação real nesta sprint** - sem push, WebSocket ou e-mail. O único observer implementado é o `AtualizadorVagasObserver` (decremento de vagas); nenhum observer de notificação deve ser criado apenas para "ilustrar" o padrão sem um consumidor real.

#### Cabeçalhos da Requisição

```http
Accept: application/json
```

#### Parâmetros

| Parâmetro      | Local | Tipo                | Obrigatório? | Descrição                                                                    |
| :------------- | :---- | :------------------ | :----------- | :--------------------------------------------------------------------------- |
| `orientadorId` | Path  | Long                | Sim          | Identificador do orientador.                                                 |
| `status`       | Query | `StatusSolicitacao` | Não          | Filtra por status (ex.: `?status=PENDENTE`). Sem o parâmetro, retorna todos. |

#### Respostas Possíveis

##### Sucesso: `200 OK`

Array de `SolicitacaoResponse`. Se não houver solicitações, retorna `[]` (não é erro).

```json
[
  {
    "id": 2,
    "status": "PENDENTE",
    "tema": "Testes automatizados em APIs REST",
    "mensagem": "Quero aprofundar testes de integração no meu TCC.",
    "justificativa": null,
    "criadoEm": "2026-09-26T10:15:00Z",
    "aluno": {
      "id": 8,
      "nome": "Carlos Eduardo Lima",
      "email": "carlos.lima@aluno.ifsc.edu.br",
      "curso": "Análise e Desenvolvimento de Sistemas"
    },
    "orientador": {
      "id": 1,
      "nome": "Dr. Adriano Lima",
      "email": "adriano.lima@ifsc.edu.br",
      "vagasDisponiveis": 3
    }
  }
]
```

##### Erro de Validação: `400 Bad Request`

Valor de `status` fora da enumeração.

```json
{
  "timestamp": "2026-09-26T10:20:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/solicitacoes/orientador/1",
  "detalhes": [
    {
      "campo": "status",
      "mensagem": "Status inválido. Valores aceitos: PENDENTE, ACEITA, RECUSADA, CANCELADA."
    }
  ]
}
```

##### Não Encontrado: `404 Not Found`

```json
{
  "timestamp": "2026-09-26T10:20:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/solicitacoes/orientador/99",
  "detalhes": "Orientador com identificador 99 não foi encontrado."
}
```

---

### 5.3. Atualização de Status (Aceitar ou Recusar)

- **Método HTTP:** `PATCH`
- **Rota:** `/api/v1/solicitacoes/{id}/status`
- **Descrição:** O orientador responde a uma solicitação `PENDENTE`.
  - `ACEITA`: decrementa em 1 o `vagasDisponiveis` do orientador (via evento `SolicitacaoStatusChangedEvent` → `AtualizadorVagasObserver`, na mesma transação).
  - `RECUSADA`: registra a `justificativa`; as vagas não são alteradas.
- **Transições permitidas:** somente `PENDENTE → ACEITA` e `PENDENTE → RECUSADA` (ver seção 7).

#### Cabeçalhos da Requisição

```http
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição

Aceite:

```json
{ "status": "ACEITA" }
```

Recusa:

```json
{
  "status": "RECUSADA",
  "justificativa": "Já atingi o limite de orientações para este semestre."
}
```

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna `SolicitacaoResponse` atualizado. Em caso de aceite, `orientador.vagasDisponiveis` já reflete o valor decrementado (permite ao frontend atualizar a tela sem nova consulta).

```json
{
  "id": 1,
  "status": "ACEITA",
  "tema": "Sistema de recomendação de orientadores com IA",
  "mensagem": "Tenho interesse na linha de Engenharia de Software e gostaria de desenvolver meu TCC sobre métricas de qualidade.",
  "justificativa": null,
  "criadoEm": "2026-09-25T14:00:00Z",
  "aluno": {
    "id": 7,
    "nome": "Ana Beatriz Souza",
    "email": "ana.souza@aluno.ifsc.edu.br",
    "curso": "Análise e Desenvolvimento de Sistemas"
  },
  "orientador": {
    "id": 1,
    "nome": "Dr. Adriano Lima",
    "email": "adriano.lima@ifsc.edu.br",
    "vagasDisponiveis": 2
  }
}
```

##### Erro de Validação: `400 Bad Request`

Status ausente ou inválido, ou `justificativa` ausente/fora do tamanho ao recusar.

```json
{
  "timestamp": "2026-09-26T11:00:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/solicitacoes/2/status",
  "detalhes": [
    {
      "campo": "justificativa",
      "mensagem": "A justificativa é obrigatória para recusar a solicitação."
    }
  ]
}
```

##### Não Encontrado: `404 Not Found`

```json
{
  "timestamp": "2026-09-26T11:00:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/solicitacoes/99/status",
  "detalhes": "Solicitação com identificador 99 não foi encontrada."
}
```

##### Conflito: `409 Conflict`

A solicitação já foi respondida (status diferente de `PENDENTE`).

```json
{
  "timestamp": "2026-09-26T11:00:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/solicitacoes/1/status",
  "detalhes": "Esta solicitação já foi respondida e não pode ser alterada."
}
```

##### Regra de Negócio Violada: `422 Unprocessable Entity`

Aceite com orientador sem vagas, ou status de destino não permitido neste endpoint (`PENDENTE`, `CANCELADA`).

```json
{
  "timestamp": "2026-09-26T11:00:00Z",
  "status": 422,
  "erro": "Regra de negócio violada",
  "caminho": "/api/v1/solicitacoes/2/status",
  "detalhes": "O orientador não possui vagas disponíveis para aceitar esta solicitação."
}
```

---

### 5.4. Consulta de Detalhes de uma Solicitação

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/solicitacoes/{id}`
- **Descrição:** Recupera uma solicitação pelo identificador. É o endpoint que o aluno usa para **acompanhar o status** (`PENDENTE`, `ACEITA`, `RECUSADA`, `CANCELADA`) a partir do `id` recebido no `POST`.

#### Cabeçalhos da Requisição

```http
Accept: application/json
```

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna um `SolicitacaoResponse` (mesmo formato do exemplo da seção 5.3). Para uma solicitação recusada, `justificativa` vem preenchida:

```json
{
  "id": 3,
  "status": "RECUSADA",
  "tema": "Aplicativo mobile para gestão de TCC",
  "mensagem": "Gostaria de orientação para desenvolver um app mobile.",
  "justificativa": "Já atingi o limite de orientações para este semestre.",
  "criadoEm": "2026-09-25T16:40:00Z",
  "aluno": {
    "id": 9,
    "nome": "Marina Costa",
    "email": "marina.costa@aluno.ifsc.edu.br",
    "curso": "Análise e Desenvolvimento de Sistemas"
  },
  "orientador": {
    "id": 1,
    "nome": "Dr. Adriano Lima",
    "email": "adriano.lima@ifsc.edu.br",
    "vagasDisponiveis": 2
  }
}
```

##### Não Encontrado: `404 Not Found`

```json
{
  "timestamp": "2026-09-26T11:30:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/solicitacoes/99",
  "detalhes": "Solicitação com identificador 99 não foi encontrada."
}
```

---

## 6. Tabela de Códigos de Erro

> **Fonte oficial das mensagens.** A issue #11 não define mensagens de erro; portanto, os textos abaixo são a especificação oficial e devem ser usados **literalmente** no backend (Bean Validation e exceções de negócio), nos testes do backend e nas asserções do frontend. Qualquer alteração de texto deve ser feita neste documento primeiro. Padrão adotado: frase completa terminada em ponto, como nas mensagens da US02.

### 6.1. Erros gerais (título `erro` fixo por categoria)

| Código | `erro`                                 | Formato de `detalhes`        |
| :----- | :------------------------------------- | :--------------------------- |
| `400`  | `Erro de validação nos dados enviados` | Array de `{campo, mensagem}` |
| `404`  | `Recurso não encontrado`               | String                       |
| `409`  | `Conflito de dados`                    | String                       |
| `422`  | `Regra de negócio violada`             | String                       |

### 6.2. `400 Bad Request` - validação de campos

| Endpoint                | `campo`         | Condição                            | `mensagem`                                                                 |
| :---------------------- | :-------------- | :---------------------------------- | :------------------------------------------------------------------------- |
| `POST`                  | `orientadorId`  | Ausente                             | `O orientador é obrigatório.`                                              |
| `POST`                  | `aluno`         | Ausente                             | `Os dados do aluno são obrigatórios.`                                      |
| `POST`                  | `aluno.nome`    | Ausente ou em branco                | `O nome do aluno é obrigatório.`                                           |
| `POST`                  | `aluno.nome`    | Fora de 3–100 caracteres            | `O nome deve ter entre 3 e 100 caracteres.`                                |
| `POST`                  | `aluno.email`   | Ausente ou em branco                | `O e-mail é obrigatório.`                                                  |
| `POST`                  | `aluno.email`   | Formato inválido                    | `Formato de e-mail inválido.`                                              |
| `POST`                  | `aluno.curso`   | Ausente ou em branco                | `O curso é obrigatório.`                                                   |
| `POST`                  | `tema`          | Ausente ou em branco                | `O tema é obrigatório.`                                                    |
| `POST`                  | `tema`          | Fora de 5–150 caracteres            | `O tema deve ter entre 5 e 150 caracteres.`                                |
| `POST`                  | `mensagem`      | Ausente ou em branco                | `A mensagem é obrigatória.`                                                |
| `POST`                  | `mensagem`      | Fora de 10–1000 caracteres          | `A mensagem deve ter entre 10 e 1000 caracteres.`                          |
| `PATCH`                 | `status`        | Ausente                             | `O status é obrigatório.`                                                  |
| `PATCH`                 | `justificativa` | `status=RECUSADA` sem justificativa | `A justificativa é obrigatória para recusar a solicitação.`                |
| `PATCH`                 | `justificativa` | Fora de 10–500 caracteres           | `A justificativa deve ter entre 10 e 500 caracteres.`                      |
| `PATCH` e `GET` (lista) | `status`        | Valor fora da enumeração            | `Status inválido. Valores aceitos: PENDENTE, ACEITA, RECUSADA, CANCELADA.` |

### 6.3. `404 Not Found`

| Endpoint                                    | Condição                | `detalhes`                                               |
| :------------------------------------------ | :---------------------- | :------------------------------------------------------- |
| `POST`, `GET .../orientador/{orientadorId}` | Orientador inexistente  | `Orientador com identificador {id} não foi encontrado.`  |
| `PATCH`, `GET /{id}`                        | Solicitação inexistente | `Solicitação com identificador {id} não foi encontrada.` |

### 6.4. `409 Conflict`

| Endpoint | Condição                                                       | `detalhes`                                                      |
| :------- | :------------------------------------------------------------- | :-------------------------------------------------------------- |
| `POST`   | Aluno já possui solicitação `PENDENTE` para o mesmo orientador | `Você já possui uma solicitação pendente para este orientador.` |
| `PATCH`  | Solicitação com status diferente de `PENDENTE`                 | `Esta solicitação já foi respondida e não pode ser alterada.`   |

> **Removido na v1.1.0:** a regra "e-mail informado já pertence a um usuário
> que não é aluno" foi retirada do escopo desta sprint (não estava coberta
> por nenhum critério de aceitação da #11 e exigiria uma verificação cruzada
> `Aluno`↔`Orientador` sem teste previsto). Ver seção 12 (Melhorias Futuras).

### 6.5. `422 Unprocessable Entity`

| Endpoint | Condição                                      | `detalhes`                                                                    |
| :------- | :-------------------------------------------- | :---------------------------------------------------------------------------- |
| `POST`   | Orientador com `vagasDisponiveis = 0`         | `O orientador não possui vagas disponíveis.`                                  |
| `POST`   | Orientador inativo (`ativo = false`)          | `O orientador não está disponível para receber solicitações.`                 |
| `PATCH`  | `ACEITA` com orientador sem vagas             | `O orientador não possui vagas disponíveis para aceitar esta solicitação.`    |
| `PATCH`  | `status` de destino `PENDENTE` ou `CANCELADA` | `Somente os status ACEITA ou RECUSADA podem ser definidos por este endpoint.` |

**Critério de separação 409 × 422:** `409` indica que o **estado atual dos dados** impede a operação (duplicidade, solicitação já respondida). `422` indica que a requisição é bem formada e o recurso existe, mas uma **regra de negócio** a impede (sem vagas, orientador inativo, transição não permitida neste endpoint).

---

## 7. Máquina de Estados da Solicitação

```text
              ┌──────────► ACEITA     (vagasDisponiveis - 1)
 PENDENTE ────┤
              └──────────► RECUSADA   (exige justificativa)

 CANCELADA: previsto no modelo, sem transição exposta na Sprint 2.
```

`ACEITA`, `RECUSADA` e `CANCELADA` são estados finais nesta sprint. Qualquer `PATCH` sobre uma solicitação fora de `PENDENTE` retorna `409`.

---

## 8. Decisões Assumidas e Divergências com o Código Atual

### 8.1. Decisões adotadas neste contrato

| #   | Decisão                                                                                                                                                                                       | Motivo                                                                         |
| :-- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------- |
| D1  | `POST` devolve `201` **com corpo** (`SolicitacaoResponse`) além do `Location`.                                                                                                                | O aluno não tem login; precisa do `id` para acompanhar o status.               |
| D2  | Aluno existente (mesmo e-mail) é reaproveitado **sem** atualizar `nome`/`curso`.                                                                                                              | Evita sobrescrita de dados por terceiros sem autenticação.                     |
| D3  | Tamanhos mínimos/máximos de `tema` (5–150), `mensagem` (10–1000) e `justificativa` (10–500).                                                                                                  | A #11 não define limites; valores adotados aqui e ajustáveis nas colunas.      |
| D4  | `justificativa` enviada com `ACEITA` é ignorada (sem erro).                                                                                                                                   | Simplifica o frontend; evita 400 por dado irrelevante.                         |
| D5  | Listagem ordenada por `criadoEm` decrescente, sem paginação.                                                                                                                                  | Consistente com a listagem de orientadores da US02.                            |
| D6  | `CANCELADA` existe na enumeração, mas nenhum endpoint a define.                                                                                                                               | O plano da Sprint 2 lista o status, mas só descreve `ACEITA`/`RECUSADA`.       |
| D7  | O acompanhamento do aluno é feito por `GET /{id}` (sem endpoint de listagem por aluno).                                                                                                       | Fora do escopo da sprint; o aluno não possui login (US01 postergada).          |
| D8  | O "painel" do orientador é a listagem `GET .../orientador/{id}?status=PENDENTE`, sem push, WebSocket ou e-mail. Não há observer de notificação - apenas `AtualizadorVagasObserver`, síncrono. | Confirmado pela equipe; atende ao cenário 1 da #11 dentro do escopo da sprint. |
| D12 | A regra de e-mail de aluno coincidente com e-mail de orientador foi removida da v1.0 e não faz parte do escopo da Sprint 2.                                                                   | Sem critério de aceitação correspondente na #11; ver seção 12.                 |
| D9  | O valor do status de aceite é `ACEITA`. A #11 deve ser corrigida ("Aprovado" → "Aceita").                                                                                                     | Confirmado pela equipe; alinha issue, plano da Sprint 2 e código.              |
| D10 | As mensagens de erro são definidas na seção 6 deste contrato; a issue #11 foi editada para incorporar os 12 cenários (seção 10).                                                              | A #11 possuía originalmente apenas os cenários de envio e aceite.              |
| D11 | O `detalhes` do `400` é um array `[{campo, mensagem}]`.                                                                                                                                       | Já é o formato do contrato da US02 e do frontend; corrige o handler.           |

### 8.2. Divergências entre o contrato da US02 e o código atual

Ao preparar este documento, foram encontradas diferenças entre o contrato da US02, o backend e o frontend. Elas afetam a US04 porque o `ErroResponse` é compartilhado:

1. **Formato de `detalhes` no `400`.** O contrato da US02 e o tipo `RespostaErroPadrao` do frontend (`types/Erro.ts`) definem `detalhes` como **array** `[{campo, mensagem}]`; o `GlobalExceptionHandler` atual devolve um **objeto** `{ "campo": "mensagem" }`. O `OrientadorForm` do frontend só mapeia erros quando `Array.isArray(detalhes)`, ou seja, hoje os erros de campo vindos do backend não chegam ao formulário. **Decisão D11: este contrato adota o array**, por ser o formato já documentado e já consumido pelo frontend, e por corrigir o defeito dos erros de campo que hoje não chegam ao formulário. Isso exige ajustar o `GlobalExceptionHandler` e as asserções de `OrientadorControllerTest` (`$.detalhes.nome` etc.) e `GlobalExceptionHandlerTest`.
2. **Duas violações no mesmo campo.** O handler usa `Collectors.toMap`, que lança `IllegalStateException` (resultando em `500`) quando um mesmo campo tem duas violações - cenário provável na US04 (ex.: `tema` em branco viola `@NotBlank` e `@Size`). O formato em array elimina o problema.
3. **Novos handlers necessários.** O handler atual cobre `404`, `409` (apenas `EmailDuplicadoException`) e `400` de Bean Validation. A US04 exige: exceções de regra de negócio mapeadas para `409` e `422` (originadas pelas `ValidadorSolicitacaoStrategy`), e tratamento de `HttpMessageNotReadableException` e `MethodArgumentTypeMismatchException` (valor de `status` fora da enumeração) no formato `ErroResponse`.
4. **`POST` da US02 sem corpo.** Ver decisão D1; o contrato da US02 previa corpo, a implementação devolve apenas `Location`.

### 8.3. Limitações conhecidas

- **Sem autenticação (US01 postergada):** qualquer cliente pode chamar `PATCH` como se fosse o orientador, e os `id` de solicitação são sequenciais e enumeráveis. Aceito para o MVP da Sprint 2 e deve constar em `docs/riscos.md`.
- **Concorrência no aceite:** duas aceitações simultâneas da última vaga devem resultar em uma delas recebendo `422` (ou `409`, por conflito de versão). O backend deve fazer a verificação de vagas e o decremento dentro da mesma transação.

---

## 9. Encaminhamentos após a Aprovação

- [x] Definir a fonte das mensagens de erro (seção 6) e o nome do status de aceite (`ACEITA`).
- [x] Definir o formato de `detalhes` do `400` (array).
- [x] Definir o significado do "painel" do orientador (listagem de pendentes, sem notificação real).
- [x] **Editar a issue #11:** trocar "Aprovado" por "Aceita" no cenário 2 e acrescentar os cenários 3 a 12. _(Issues #10, 11.1–11.9 revisadas; texto final reproduzido na seção 10 deste contrato.)_
- [x] **Criar tarefa de backend** para ajustar o `GlobalExceptionHandler` (array em `detalhes`, novos handlers `409`/`422`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`) e atualizar os testes da US02 afetados. _(Issue 11.9.)_
- [x] Registrar a ausência de autenticação (seção 8.3) em `docs/riscos.md`. _(Issue Doc-4.)_
- [x] Registrar **ADR-0007** (o repositório já possui 6 ADRs - 0001 a 0006 - portanto a nova é a 0007, não a 0008 mencionada em rascunho anterior) e referenciar este contrato na `docs/entregas/sprint-2.md`. _(Issue Doc-1.)_
- [x] Remover a regra de e-mail cruzado (409) do escopo da v1.0. _(Ver seção 12.)_

---

## 10. Critérios de Aceitação da US04 (Formato BDD)

Texto idêntico ao publicado na issue #11 (GitHub), reproduzido aqui para
consulta rápida durante a implementação. Em caso de divergência futura, a
**issue #11 é a fonte de verdade**; este documento deve ser atualizado para
acompanhá-la.

> **Nota técnica:** o aluno é identificado por e-mail no momento do envio
> (criando ou reaproveitando um registro de `Aluno`), e o acompanhamento de
> status é feito via `GET /api/v1/solicitacoes/{id}`.

### Envio da solicitação (Aluno)

**Cenário 1 – Envio bem-sucedido (aluno novo):** Dado que o professor possui
pelo menos 1 vaga disponível; Quando o aluno preenche seus dados (nome,
e-mail, curso), o tema e a mensagem, e clica em "Solicitar Orientação";
Então o sistema cria um novo registro de `Aluno`, registra a solicitação com
status `PENDENTE` (`POST /api/v1/solicitacoes` → `201 Created`, com corpo),
e o professor passa a visualizar a solicitação em seu painel
(`GET /api/v1/solicitacoes/orientador/{id}?status=PENDENTE`).

**Cenário 2 – Envio com aluno já existente:** Dado que já existe um `Aluno`
cadastrado com o e-mail informado; Quando envio uma nova solicitação com
esse mesmo e-mail; Então o sistema reaproveita o `Aluno` existente (sem
duplicar nem sobrescrever `nome`/`curso`) e vincula a nova solicitação a
ele.

**Cenário 3 – Bloqueio por vagas esgotadas:** Dado que o orientador não
possui vagas disponíveis (ou está inativo); Quando o aluno tenta enviar uma
solicitação para ele; Então o botão "Solicitar Orientação" não fica
disponível no Catálogo, e uma chamada direta à API retorna
`422 Unprocessable Entity` com a mensagem "O orientador não possui vagas
disponíveis." (ou "...não está disponível para receber solicitações.").

**Cenário 4 – Bloqueio por solicitação duplicada:** Dado que já existe uma
solicitação `PENDENTE` do mesmo aluno para o mesmo orientador; Quando tento
enviar outra solicitação para ele; Então o sistema retorna `409 Conflict`
com a mensagem "Você já possui uma solicitação pendente para este
orientador."

**Cenário 5 – Dados obrigatórios/inválidos:** Dado que estou preenchendo o
formulário; Quando deixo o tema em branco, ou informo um e-mail em formato
inválido, ou omito qualquer campo obrigatório; Então o sistema bloqueia o
envio e retorna `400 Bad Request` com a lista de campos e mensagens
correspondentes.

### Gestão das solicitações (Professor)

**Cenário 6 – Listagem de pendentes:** Dado que acesso o painel de
solicitações do meu perfil de orientador; Quando a tela carrega; Então vejo
as solicitações com status `PENDENTE`, ordenadas da mais recente para a mais
antiga, exibindo nome do aluno, tema e mensagem.

**Cenário 7 – Aceite:** Dado que visualizo uma solicitação `PENDENTE` e
ainda possuo vagas disponíveis; Quando clico em "Aceitar"; Então o status
muda para `ACEITA` e minhas `vagasDisponiveis` são decrementadas em 1, na
mesma transação (`PATCH /solicitacoes/{id}/status` → `200 OK`).

**Cenário 8 – Bloqueio de aceite sem vagas:** Dado que minhas
`vagasDisponiveis` chegaram a 0 (ex.: aceitei outra solicitação antes);
Quando tento aceitar outra solicitação `PENDENTE`; Então o sistema bloqueia
a ação e retorna `422 Unprocessable Entity` com a mensagem "O orientador não
possui vagas disponíveis para aceitar esta solicitação."; a solicitação
permanece `PENDENTE`.

**Cenário 9 – Recusa com justificativa:** Dado que visualizo uma solicitação
`PENDENTE`; Quando clico em "Recusar" e informo uma justificativa (10 a 500
caracteres); Então o status muda para `RECUSADA`, a justificativa é
registrada, e minhas `vagasDisponiveis` **não** são alteradas.

**Cenário 10 – Recusa sem justificativa:** Dado que estou recusando uma
solicitação; Quando tento confirmar sem preencher a justificativa (ou fora
do tamanho permitido); Então o sistema bloqueia a ação e retorna
`400 Bad Request` com a mensagem "A justificativa é obrigatória para
recusar a solicitação."

**Cenário 11 – Bloqueio de ação em solicitação já finalizada:** Dado que uma
solicitação já está `ACEITA` ou `RECUSADA`; Quando tento aceitar/recusar
novamente; Então o sistema bloqueia a ação e retorna `409 Conflict` com a
mensagem "Esta solicitação já foi respondida e não pode ser alterada."; as
vagas não são alteradas.

### Acompanhamento (Aluno)

**Cenário 12 – Consulta de status:** Dado que possuo o identificador da
solicitação recebido no envio; Quando consulto
`GET /api/v1/solicitacoes/{id}`; Então vejo o status atual (`PENDENTE`,
`ACEITA` ou `RECUSADA`) e, se recusada, a justificativa. Um `id` inexistente
retorna `404 Not Found`.

---

## 11. Melhorias Futuras (fora do escopo da Sprint 2)

Itens identificados durante a elaboração deste contrato, mas **deliberadamente
fora do escopo da v1.0/v1.1** por não terem critério de aceitação
correspondente na #11 e por exigirem trabalho e testes não previstos nesta
sprint. Não devem ser implementados na Sprint 2; registrados aqui para não
serem esquecidos em sprints futuras.

| #   | Item                                                                                        | Motivo do adiamento                                                                                    |
| :-- | :------------------------------------------------------------------------------------------ | :----------------------------------------------------------------------------------------------------- |
| F1  | Bloquear e-mail de aluno coincidente com e-mail de orientador (`409`).                      | Exige verificação cruzada `Aluno`↔`Orientador`; sem critério de aceitação na #11.                      |
| F2  | Endpoint de cancelamento pelo aluno (transição para `CANCELADA`).                           | Status reservado no enum, mas sem endpoint nesta sprint (decisão D6).                                  |
| F3  | Notificação real ao orientador (e-mail, push ou WebSocket) quando uma solicitação é criada. | Fora do escopo; o "painel" é a listagem sob demanda (decisão D8).                                      |
| F4  | Paginação na listagem de solicitações do orientador.                                        | Volume de dados da Sprint 2 não justifica; decisão D5.                                                 |
| F5  | Endpoint de listagem de solicitações por aluno (`GET /solicitacoes?email=`).                | Aluno acompanha via `GET /{id}` (decisão D7); reavaliar quando a US01 (autenticação) for implementada. |
