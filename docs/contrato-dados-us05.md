# Contrato de Dados da API - US05: Calendário de Prazos

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 15/10/2026
- **História de Usuário Relacionada:** US05 - Calendário de Prazos (Sprint 3)
- **Issue Relacionada:** #12 - Configuração do Calendário de Entregas
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o contrato de dados entre o Frontend (React) e o Backend (Spring Boot) para o **calendário de prazos de entrega** do semestre: Projeto de TCC, TCC 1 e TCC 2.

Conforme o [relatório de refatoração de escopo](entregas/sprint-3.md) e a [ADR-0010](adrs/ADR-0010.md), não existe papel de login de Coordenador. Quem cadastra e edita os prazos é o **professor responsável pela `Turma_TCC` vigente**, verificado no banco de dados, a mesma regra que define o campo `responsavelTurma` do [contrato da US01](contrato-dados-us01.md). Alunos e professores consultam os prazos.

---

## 2. Padrões Globais da API

Herdados dos contratos da [US01](contrato-dados-us01.md), [US02](contrato-dados-us02.md) e [US04](contrato-dados-us04.md), sem alterações:

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Nomenclatura dos Campos (JSON):** `camelCase`
- **Datas:** `dataLimite` no formato ISO-8601 de data, sem hora (`AAAA-MM-DD`, ex.: `"2026-11-25"`).
- **Formato de Erro:** objeto padrão `ErroResponse` ([contrato da US04, seção 3.5](contrato-dados-us04.md)). No `400`, `detalhes` é um array `[{campo, mensagem}]`; nos demais códigos, uma String.
- **Autenticação:** todas as rotas exigem `Authorization: Bearer <token>` emitido pelo Keycloak ([ADR-0012](adrs/ADR-0012.md)). O professor é identificado pela claim `email` do token, como em `GET /api/v1/professores/me`.

---

## 3. Modelos de Dados

### 3.1. `TipoEntrega` (enumeração)

| Valor         | `descricao`      |
| :------------ | :--------------- |
| `PROJETO_TCC` | `Projeto de TCC` |
| `TCC1`        | `TCC 1`          |
| `TCC2`        | `TCC 2`          |

### 3.2. `PrazoEntregaRequest` (entrada, criação)

| Campo        | Tipo          | Obrigatório | Regras                                                     |
| :----------- | :------------ | :---------- |:-----------------------------------------------------------|
| `tipo`       | `TipoEntrega` | Sim         | Um dos valores do `TipoEntrega`.                           |
| `dataLimite` | Data          | Sim         | Não pode estar no passado; deve respeitar a sequência. |

### 3.3. `PrazoEntregaUpdate` (entrada, edição)

| Campo        | Tipo | Obrigatório | Regras                              |
| :----------- | :--- | :---------- | :---------------------------------- |
| `dataLimite` | Data | Sim         | Não pode estar no passado; deve respeitar a sequência.|

O `tipo` não é editável: para trocar o tipo, edita-se o prazo do tipo correto.

### 3.4. `PrazoEntregaResponse` (saída)

| Campo           | Tipo          | Descrição                                                                                                     |
| :-------------- | :------------ | :------------------------------------------------------------------------------------------------------------ |
| `id`            | Long          | Identificador do prazo.                                                                                       |
| `semestre`      | String        | Semestre da `Turma_TCC` à qual o prazo pertence (ex.: `"2026/2"`).                                            |
| `tipo`          | `TipoEntrega` | Tipo da entrega.                                                                                              |
| `descricao`     | String        | Nome do tipo para exibição (ex.: `"TCC 1"`).                                                                  |
| `dataLimite`    | Data          | Data limite da entrega.                                                                                       |
| `diasRestantes` | Long          | Dias entre hoje (fuso `America/Sao_Paulo`) e `dataLimite`. `0` no próprio dia; negativo se o prazo já passou. |

---

## 4. Especificação dos Endpoints

### 4.1. Consultar Prazos da Turma Vigente

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/prazos`
- **Permissão:** `ROLE_ALUNO` ou `ROLE_PROFESSOR`.
- **Descrição:** Lista os prazos da `Turma_TCC` com `vigente = true`, ordenados pelo fluxo (Projeto de TCC, TCC 1, TCC 2). Tipos ainda sem prazo simplesmente não aparecem. Sem turma vigente, devolve lista vazia.

##### Sucesso: `200 OK`

```json
[
  {
    "id": 1,
    "semestre": "2026/2",
    "tipo": "PROJETO_TCC",
    "descricao": "Projeto de TCC",
    "dataLimite": "2026-11-25",
    "diasRestantes": 41
  },
  {
    "id": 2,
    "semestre": "2026/2",
    "tipo": "TCC1",
    "descricao": "TCC 1",
    "dataLimite": "2027-03-15",
    "diasRestantes": 151
  }
]
```

### 4.2. Cadastrar Prazo

- **Método HTTP:** `POST`
- **Rota:** `/api/v1/prazos`
- **Permissão:** `ROLE_PROFESSOR` **e** ser o professor responsável pela `Turma_TCC` vigente.
- **Descrição:** Cadastra o prazo de um tipo de entrega na turma atual. A turma não é enviada pelo cliente: é sempre a atual.

#### Corpo da Requisição

```json
{
  "tipo": "PROJETO_TCC",
  "dataLimite": "2026-11-25"
}
```

##### Sucesso: `201 Created`

Cabeçalho `Location: /api/v1/prazos/{id}` e corpo `PrazoEntregaResponse`:

```json
{
  "id": 1,
  "semestre": "2026/2",
  "tipo": "PROJETO_TCC",
  "descricao": "Projeto de TCC",
  "dataLimite": "2026-11-25",
  "diasRestantes": 41
}
```

##### Erro: `403 Forbidden` (professor que não é o responsável)

```json
{
  "timestamp": "2026-10-15T13:00:00Z",
  "status": 403,
  "erro": "Acesso negado",
  "caminho": "/api/v1/prazos",
  "detalhes": "Somente o professor responsável pela Turma_TCC vigente pode gerenciar os prazos de entrega."
}
```

##### Erro: `409 Conflict` (tipo já cadastrado)

```json
{
  "timestamp": "2026-10-15T13:00:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/prazos",
  "detalhes": "Já existe um prazo cadastrado para TCC 1 na turma 2026/2. Edite o prazo existente."
}
```

### 4.3. Editar Prazo

- **Método HTTP:** `PATCH`
- **Rota:** `/api/v1/prazos/{id}`
- **Permissão:** `ROLE_PROFESSOR` **e** ser o professor responsável pela `Turma_TCC` vigente.
- **Descrição:** Substitui a data limite de um prazo da turma vigente. As regras de data (seção 5) são reaplicadas.

#### Corpo da Requisição

```json
{
  "dataLimite": "2027-04-01"
}
```

##### Sucesso: `200 OK`

Corpo `PrazoEntregaResponse` com a nova data e o `diasRestantes` recalculado.

##### Erro: `404 Not Found`

```json
{
  "timestamp": "2026-10-15T13:00:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/prazos/99",
  "detalhes": "Prazo de entrega com identificador 99 não foi encontrado."
}
```

##### Erro: `422 Unprocessable Content` (prazo de turma que não é a atual)

`detalhes`: `"Somente prazos da Turma_TCC atual podem ser alterados."`

---

## 5. Regras de Validação de Data

| Regra                                                                                         | Código | `campo` / mensagem                                                                                     |
| :-------------------------------------------------------------------------------------------- | :----: | :----------------------------------------------------------------------------------------------------- |
| `tipo` ou `dataLimite` ausentes                                                               | `400`  | `tipo`: `O tipo de entrega é obrigatório.` / `dataLimite`: `A data limite é obrigatória.`              |
| `tipo` fora da enumeração ou data mal formatada                                               | `400`  | `Corpo da requisição inválido ou ilegível`                                        |
| `dataLimite` anterior a hoje (fuso `America/Sao_Paulo`). Hoje é aceito.                       | `400`  | `dataLimite`: `A data limite não pode estar no passado.`                                               |
| `dataLimite` igual ou anterior ao prazo de uma entrega **anterior** do fluxo                  | `422`  | `A data limite de TCC 1 (20/11/2026) deve ser posterior à data limite de Projeto de TCC (25/11/2026).` |
| `dataLimite` igual ou posterior ao prazo de uma entrega **seguinte** do fluxo (já cadastrada) | `422`  | `A data limite de Projeto de TCC (01/04/2027) deve ser anterior à data limite de TCC 1 (15/03/2027).`  |

Exemplo de `400`:

```json
{
  "timestamp": "2026-10-15T13:00:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/prazos",
  "detalhes": [
    {
      "campo": "dataLimite",
      "mensagem": "A data limite não pode estar no passado."
    }
  ]
}
```

---

## 6. Sumário dos Códigos de Status

| Código | Quando ocorre                                                                                                                              |
| :----- | :----------------------------------------------------------------------------------------------------------------------------------------- |
| `200`  | Consulta de prazos ou edição bem-sucedida.                                                                                                 |
| `201`  | Prazo cadastrado.                                                                                                                          |
| `400`  | Campos ausentes, corpo ilegível ou data no passado.                                                                                        |
| `401`  | Token ausente ou inválido (gerado pelo Spring Security, sem corpo `ErroResponse`).                                                         |
| `403`  | Aluno tentando cadastrar/editar (Spring Security, sem corpo) **ou** professor que não é o responsável pela turma vigente (`ErroResponse`). |
| `404`  | Prazo inexistente na edição.                                                                                                               |
| `409`  | Tipo de entrega que já possui prazo na turma vigente.                                                                                      |
| `422`  | Data fora da sequência do fluxo ou prazo de turma que não é a vigente.                                                                     |

---

## 7. Uso Esperado no Frontend

1. O painel de aluno e o de professor chamam `GET /api/v1/prazos` e exibem cada prazo com a contagem regressiva a partir de `diasRestantes` (cenários 1, 3 e 6).
2. A aba "Gestão da Turma/Calendário" só aparece quando `GET /api/v1/professores/me` devolve `responsavelTurma: true` (US01, cenários 3 e 4). O `403` do backend é a garantia final do cenário 2 da US05, mesmo que a tela seja acessada diretamente.
3. Para um tipo ainda sem prazo, usar `POST`; para um tipo que já aparece na consulta, usar `PATCH /{id}` (cenário 4). Um `409` no `POST` indica que a lista está desatualizada.
4. Após salvar, recarregar a lista para refletir o novo `diasRestantes`.

---

## 8. Decisões Assumidas

| Decisão                                                                                              | Justificativa                                                                                                                                                    |
|:-----------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| A turma é sempre a atual; o cliente não informa `turmaId`.                                           | Só o responsável pela turma atual pode gerenciar prazos; aceitar `turmaId` abriria a edição de turmas antigas sem regra definida.                                |
| Um prazo por tipo e turma (restrição única `turma_id` + `tipo`). Cadastro repetido retorna `409`.    | Cenário 4: editar substitui o prazo anterior, nunca cria um segundo.                                                                                             |
| A sequência é validada nos dois sentidos e de forma estrita (datas iguais são rejeitadas).           | O cenário 5 descreve só "anterior à entrega anterior"; validar também a entrega seguinte impede que a edição do Projeto de TCC deixe o calendário inconsistente. |
| Data no passado é `400` (erro de campo); quebra de sequência é `422` (regra de negócio).             | Segue o critério 409 × 422 do contrato da US04: a data passada é inválida por si só; a sequência depende de outros dados.                                        |
| Sem turma vigente, ninguém é responsável: `POST`/`PATCH` retornam `403` e `GET` retorna lista vazia. | Mesma regra de `responsavelTurma` da US01.                                                                                                                       |
| Professor não responsável recebe `403` antes de qualquer consulta ao prazo (não recebe `404`).       | Não revela a existência de prazos a quem não pode gerenciá-los.                                                                                                  |
| `diasRestantes` é calculado pelo backend com o relógio no fuso `America/Sao_Paulo`.                  | Evita divergência entre o fuso do navegador e o do servidor (o contêiner roda em UTC).                                                                           |
