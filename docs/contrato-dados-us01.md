# Contrato de Dados da API - US01: Autenticação e Perfis de Usuário

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 12/10/2026
- **História de Usuário Relacionada:** US01 - Autenticação e Perfis de Usuário (Sprint 3)
- **Issue Relacionada:** #129 - Expor no backend se o professor logado é responsável pela `Turma_TCC` (US01-3)
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o contrato de dados entre o Frontend (React) e o Backend (Spring Boot) para o endpoint que informa **se o professor autenticado é o responsável pela `Turma_TCC` vigente**. Com essa informação, o frontend decide quais abas exibir no dashboard do professor.

Conforme o [relatório de refatoração de escopo](entregas/sprint-3.md) e a [configuração do Keycloak](keycloak/keycloak.md), a gestão da turma e do calendário **não é um papel de login**. O Keycloak entrega apenas `ROLE_ALUNO` e `ROLE_PROFESSOR`; a responsabilidade pela turma é verificada no banco de dados.

Este contrato complementa os contratos da [US02](contrato-dados-us02.md) e da [US04](contrato-dados-us04.md), cujos padrões globais são reaproveitados.

---

## 2. Padrões Globais da API

Herdados do contrato da US02, sem alterações:

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Nomenclatura dos Campos (JSON):** `camelCase`
- **Formato de Erro:** objeto padrão `ErroResponse` ([contrato da US04, seção 3.5](contrato-dados-us04.md)), produzido por `ErrorResponseDTO` / `GlobalExceptionHandler`.

Alteração em relação aos contratos anteriores:

- **Autenticação:** todas as rotas exigem um token de acesso JWT emitido pelo Keycloak (`Authorization: Bearer <token>`). Rotas sob `/api/v1/professores/**` exigem, além disso, a role `ROLE_PROFESSOR`.

---

## 3. Modelos de Dados

### 3.1. `PerfilProfessorResponse` (saída)

| Campo              | Tipo             | Descrição                                                                                               |
| :----------------- | :--------------- | :------------------------------------------------------------------------------------------------------ |
| `id`               | Long             | Identificador do professor (mesmo `id` usado em `/api/v1/orientadores/{id}`).                           |
| `nome`             | String           | Nome do professor.                                                                                      |
| `email`            | String           | E-mail do professor.                                                                                    |
| `departamento`     | String ou `null` | Departamento do professor.                                                                              |
| `responsavelTurma` | Boolean          | `true` se o professor é o responsável pela `Turma_TCC` vigente; `false` caso contrário. Nunca é `null`. |

---

## 4. Especificação do Endpoint

### 4.1. Perfil do Professor Autenticado

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/professores/me`
- **Descrição:** Retorna os dados do professor identificado pelo token e o indicador `responsavelTurma`.
- **Identificação do professor:** o backend lê a claim `email` do token e busca o professor pelo e-mail, sem diferenciar maiúsculas de minúsculas.
- **Regra de negócio:** `responsavelTurma` é `true` somente se existir uma `Turma_TCC` com `vigente = true` cujo `professor_responsavel_id` seja o do professor autenticado. Ser responsável por uma turma que não é a vigente resulta em `false`.

#### Cabeçalhos da Requisição

```http
Authorization: Bearer <token de acesso do Keycloak>
Accept: application/json
```

#### Respostas Possíveis

##### Sucesso: `200 OK` (professor responsável pela turma vigente)

```json
{
  "id": 3,
  "nome": "Professor Responsável pela Turma",
  "email": "professor.responsavel@example.com",
  "departamento": "DAELN",
  "responsavelTurma": true
}
```

##### Sucesso: `200 OK` (professor que apenas orienta)

```json
{
  "id": 4,
  "nome": "Professor Orientador",
  "email": "professor.orientador@example.com",
  "departamento": "DAELN",
  "responsavelTurma": false
}
```

##### Erro: `401 Unauthorized`

Requisição sem token ou com token inválido. A resposta é gerada pelo Spring Security, traz o cabeçalho `WWW-Authenticate` e **não** possui corpo `ErroResponse`.

##### Erro: `403 Forbidden`

Token válido sem a role `ROLE_PROFESSOR` (por exemplo, um aluno). A resposta é gerada pelo Spring Security e **não** possui corpo `ErroResponse`.

##### Erro: `404 Not Found`

O token não possui a claim `email`, ou nenhum professor cadastrado tem o e-mail do token.

```json
{
  "timestamp": "2026-10-12T22:00:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/professores/me",
  "detalhes": "Professor com e-mail desconhecido@example.com não foi encontrado."
}
```

---

## 5. Sumário dos Códigos de Status

| Código | Quando ocorre                                                          |
| :----- | :--------------------------------------------------------------------- |
| `200`  | Professor identificado; `responsavelTurma` informa a responsabilidade. |
| `401`  | Token ausente ou inválido.                                             |
| `403`  | Token válido, mas sem `ROLE_PROFESSOR`.                                |
| `404`  | Professor do token não encontrado no cadastro de orientadores.         |

---

## 6. Uso Esperado no Frontend

1. Após o login, chamar `GET /api/v1/professores/me` com o token do usuário com `ROLE_PROFESSOR`.
2. Se `responsavelTurma` for `true`, exibir também as abas de gestão da turma e do calendário; se for `false`, exibir somente as abas de orientação.
3. Tratar `404` como "professor autenticado sem cadastro no sistema" e **não** como `responsavelTurma: false`.

---

## 7. Decisões Assumidas

| Decisão                                                         | Justificativa                                                                                                                                   |
| :-------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| Endpoint novo (`/api/v1/professores/me`) em vez de campo extra. | Não existe endpoint de perfil do usuário autenticado; alterar `OrientadorResponse` afetaria o catálogo (US02/US03), que é público para o aluno. |
| O professor é identificado pelo e-mail do token.                | O realm usa o e-mail como username e o cadastro de orientadores tem e-mail único; não há vínculo por `sub` do Keycloak no modelo atual.         |
| Consulta reaproveitada da US01-2.                               | `TurmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue`, conforme o critério de aceitação da issue.                                   |
| Rotas sob `/api/v1/professores/**` exigem `ROLE_PROFESSOR`.     | Um aluno não tem perfil de professor; retornar `403` evita expor um `404` ambíguo.                                                              |
