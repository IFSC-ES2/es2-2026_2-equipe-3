# Contrato de Dados da API — US02: Cadastro de Perfil de Orientador e Vagas

- **Versão do Documento:** 1.0.0
- **Data de Elaboração:** 11/09/2026
- **Responsável Arquitetural:** Damares Gaia (Arquiteta de Software)
- **História de Usuário Relacionada:** US02 – Cadastro de Perfil de Orientador e Vagas (Vertical Slice da Sprint 1)
- **Status:** Aprovado para Implementação

---

## 1. Visão Geral e Objetivo

Este documento define o **Contrato de Dados formal entre o Frontend (React) e o Backend (Spring Boot)** para a implementação do _vertical slice_ do MVP na Sprint 1.

O objetivo deste contrato é permitir que as equipes de Frontend, Backend e Qualidade desenvolvam suas respectivas partes em paralelo, garantindo:

1. Padronização dos nomes de campos, tipos e estruturas de JSON.
2. Regras de validação estritas (Bean Validation / validação em formulários).
3. Previsibilidade de códigos de status HTTP e formato das mensagens de erro.
4. Conformidade com a arquitetura em camadas definida na [ADR-0003](adrs/ADR-0003.md).

---

## 2. Padrões Globais da API

- **Formato de Comunicação:** `application/json; charset=UTF-8`
- **Prefixo de Rota:** `/api/v1`
- **Padrão de Nomenclatura dos Campos (JSON):** `camelCase` (ex: `linhasDePesquisa`, `vagasDisponiveis`)
- **Fuso Horário e Datas:** Formato ISO 8601 em UTC (`YYYY-MM-DDTHH:mm:ssZ`)

---

## 3. Sumário dos Endpoints

| Método   | Rota                        | Descrição                                                                        | Status HTTP de Sucesso |
| :------- | :-------------------------- | :------------------------------------------------------------------------------- | :--------------------- |
| `POST`   | `/api/v1/orientadores`      | Cadastra um novo perfil de professor orientador e suas vagas                     | `201 Created`          |
| `GET`    | `/api/v1/orientadores`      | Lista todos os orientadores cadastrados e suas vagas disponíveis                 | `200 OK`               |
| `GET`    | `/api/v1/orientadores/{id}` | Recupera os dados detalhados de um orientador específico por ID                  | `200 OK`               |
| `PATCH`  | `/api/v1/orientadores/{id}` | Atualiza dados cadastrais, linhas de pesquisa e número de vagas de um orientador | `200 OK`               |
| `DELETE` | `/api/v1/orientadores/{id}` | Remove ou desativa o cadastro de um orientador no sistema                        | `204 No Content`       |

> **Nota de Escopo do Vertical Slice (Sprint 1):** O contrato de dados especifica a API REST completa do recurso `orientadores`. Para o _vertical slice_ funcional demonstrável da Sprint 1, a implementação prioriza o fluxo de cadastro (`POST`) e consulta/vitrine (`GET`), seguindo com a atualização (`PATCH`) e exclusão (`DELETE`) na sequência do backlog.

---

## 4. Especificação Detalhada dos Endpoints

### 4.1. Cadastro de Perfil de Orientador

- **Método HTTP:** `POST`
- **Rota:** `/api/v1/orientadores`
- **Descrição:** Registra os dados cadastrais, linhas de pesquisa e vagas ofertadas por um professor.

#### Cabeçalhos da Requisição

```http
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição (Request Body)

```json
{
  "nome": "Dr. Adriano Lima",
  "email": "adriano.lima@ifsc.edu.br",
  "departamento": "DAE - Câmpus São José",
  "linhasDePesquisa": [
    "Engenharia de Software",
    "Qualidade de Software",
    "Sistemas Distribuídos"
  ],
  "vagasDisponiveis": 3,
  "biografia": "Professor com foco em processos de software, métricas e metodologias ágeis."
}
```

#### Dicionário de Dados e Regras de Validação (Entrada)

| Campo              | Tipo             | Obrigatório? | Regras e Restrições de Negócio                                                       |
| :----------------- | :--------------- | :----------- | :----------------------------------------------------------------------------------- |
| `nome`             | String           | Sim          | Mínimo 3 e máximo 100 caracteres. Não pode conter apenas espaços.                    |
| `email`            | String           | Sim          | Deve possuir formato de e-mail válido (RFC 5322). Identificador único do orientador. |
| `departamento`     | String           | Não          | Máximo 100 caracteres. Representa o departamento/câmpus de lotação.                  |
| `linhasDePesquisa` | Array de Strings | Sim          | Deve conter ao menos 1 item. Cada linha deve ter entre 2 e 80 caracteres.            |
| `vagasDisponiveis` | Integer          | Sim          | Número inteiro maior ou igual a 0 ($\ge 0$). Limite máximo sugerido de 20 vagas.     |
| `biografia`        | String           | Não          | Texto descritivo opcional com tamanho máximo de 500 caracteres.                      |

#### Respostas Possíveis

##### Sucesso: `201 Created`

- **Cabeçalho:** `Location: /api/v1/orientadores/1`
- **Corpo:**

```json
{
  "id": 1,
  "nome": "Dr. Adriano Lima",
  "email": "adriano.lima@ifsc.edu.br",
  "departamento": "DAE - Câmpus São José",
  "linhasDePesquisa": [
    "Engenharia de Software",
    "Qualidade de Software",
    "Sistemas Distribuídos"
  ],
  "vagasDisponiveis": 3,
  "biografia": "Professor com foco em processos de software, métricas e metodologias ágeis.",
  "ativo": true,
  "criadoEm": "2026-09-11T09:30:00Z"
}
```

##### Erro de Validação: `400 Bad Request`

Retornado quando um ou mais campos violam as regras estipuladas.

```json
{
  "timestamp": "2026-09-11T09:30:00Z",
  "status": 400,
  "erro": "Erro de validação nos dados enviados",
  "caminho": "/api/v1/orientadores",
  "detalhes": [
    {
      "campo": "vagasDisponiveis",
      "mensagem": "O número de vagas não pode ser negativo"
    },
    {
      "campo": "email",
      "mensagem": "Formato de e-mail inválido"
    }
  ]
}
```

##### Conflito: `409 Conflict`

Retornado caso o e-mail informado já esteja registrado para outro orientador.

```json
{
  "timestamp": "2026-09-11T09:30:00Z",
  "status": 409,
  "erro": "Conflito de dados",
  "caminho": "/api/v1/orientadores",
  "detalhes": "O e-mail informado já está cadastrado no sistema."
}
```

---

### 4.2. Listagem de Orientadores (Catálogo / Vitrine)

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/orientadores`
- **Descrição:** Retorna a relação de professores orientadores cadastrados, utilizada pelos alunos para consulta das vagas e linhas de pesquisa.

#### Cabeçalhos da Requisição

```http
Accept: application/json
```

#### Parâmetros de Consulta (Query Params - Opcionais)

- `area`: Filtro textual opcional por linha de pesquisa ou área de interesse (ex: `/api/v1/orientadores?area=Software`).

#### Respostas Possíveis

##### Sucesso: `200 OK`

```json
[
  {
    "id": 1,
    "nome": "Dr. Adriano Lima",
    "email": "adriano.lima@ifsc.edu.br",
    "departamento": "DAE - Câmpus São José",
    "linhasDePesquisa": [
      "Engenharia de Software",
      "Qualidade de Software",
      "Sistemas Distribuídos"
    ],
    "vagasDisponiveis": 3,
    "biografia": "Professor com foco em processos de software, métricas e metodologias ágeis.",
    "ativo": true
  },
  {
    "id": 2,
    "nome": "Profa. Maria Oliveira",
    "email": "maria.oliveira@ifsc.edu.br",
    "departamento": "DAE - Câmpus São José",
    "linhasDePesquisa": [
      "Inteligência Artificial",
      "Processamento de Linguagem Natural"
    ],
    "vagasDisponiveis": 1,
    "biografia": "Pesquisas em IA aplicada à educação.",
    "ativo": true
  }
]
```

---

### 4.3. Consulta de Detalhes de um Orientador

- **Método HTTP:** `GET`
- **Rota:** `/api/v1/orientadores/{id}`
- **Descrição:** Recupera os dados completos de um orientador específico através de seu identificador numérico.

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna o objeto do orientador correspondente ao ID informado.

##### Não Encontrado: `404 Not Found`

```json
{
  "timestamp": "2026-09-11T09:30:00Z",
  "status": 404,
  "erro": "Recurso não encontrado",
  "caminho": "/api/v1/orientadores/99",
  "detalhes": "Orientador com identificador 99 não foi encontrado."
}
```

---

### 4.4. Atualização de Dados, Linhas de Pesquisa e Vagas de Orientador

- **Método HTTP:** `PATCH`
- **Rota:** `/api/v1/orientadores/{id}`
- **Descrição:** Atualiza parcialmente os dados cadastrais, linhas de pesquisa, biografia ou quantidade de vagas de um orientador existente.

#### Cabeçalhos da Requisição

```http
Content-Type: application/json
Accept: application/json
```

#### Corpo da Requisição (Request Body - Todos os campos são opcionais)

```json
{
  "linhasDePesquisa": [
    "Engenharia de Software",
    "Qualidade de Software",
    "Inteligência Artificial Aplicada"
  ],
  "vagasDisponiveis": 5,
  "biografia": "Atualização das linhas de pesquisa com foco em IA e testes de software."
}
```

#### Respostas Possíveis

##### Sucesso: `200 OK`

Retorna o objeto do orientador com os dados atualizados.

##### Erro de Validação: `400 Bad Request`

Retornado caso os campos enviados sejam inválidos (ex: vagas negativas).

##### Não Encontrado: `404 Not Found`

Retornado caso o ID do orientador não exista.

---

### 4.5. Remoção / Desativação de Perfil de Orientador

- **Método HTTP:** `DELETE`
- **Rota:** `/api/v1/orientadores/{id}`
- **Descrição:** Remove o cadastro de um orientador do sistema ou altera seu status para inativo (`ativo: false`), impedindo novos vínculos.

#### Respostas Possíveis

##### Sucesso: `204 No Content`

O cadastro foi removido ou desativado com sucesso (sem corpo de resposta).

##### Não Encontrado: `404 Not Found`

Retornado caso o orientador com o ID informado não seja encontrado.

---

## 5. Diretrizes Arquiteturais para Implementação

Para manter a conformidade com as decisões registradas nas ADRs do projeto, os desenvolvedores devem seguir os seguintes padrões estruturais:

### 5.1. Para o Backend (Spring Boot - Issue #37)

Conforme a [ADR-0003 (Arquitetura em Camadas)](adrs/ADR-0003.md):

- Os pacotes devem ser organizados em:
  - `br.edu.ifsc.gestao_tcc.controller`: anotação `@RestController`, mapeamento de rotas e retorno de `ResponseEntity`.
  - `br.edu.ifsc.gestao_tcc.dto`: classes DTO (Data Transfer Objects) para desacoplar a API das entidades do banco.
  - `br.edu.ifsc.gestao_tcc.service`: lógica de negócio e regras de validação.
  - `br.edu.ifsc.gestao_tcc.repository`: interfaces Spring Data JPA (`OrientadorRepository`).
  - `br.edu.ifsc.gestao_tcc.model` ou `entity`: entidades JPA anotadas (`Orientador.java`).

#### Sugestão de Assinatura das Classes DTO (Referência):

```java
// br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO
public record OrientadorRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @Size(max = 100, message = "O departamento deve ter no máximo 100 caracteres")
    String departamento,

    @NotEmpty(message = "Informe ao menos uma linha de pesquisa")
    List<@NotBlank String> linhasDePesquisa,

    @NotNull(message = "A quantidade de vagas é obrigatória")
    @Min(value = 0, message = "O número de vagas não pode ser negativo")
    Integer vagasDisponiveis,

    @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
    String biografia
) {}
```

---

### 5.2. Para o Frontend (React com TypeScript - Issue #38)

Conforme a [ADR-0006 (React + TypeScript)](adrs/ADR-0006.md):

- Os tipos devem ser definidos em `src/frontend/src/types/orientador.ts`.

#### Sugestão de Tipagem TypeScript (Referência):

```typescript
export interface OrientadorInput {
  nome: string;
  email: string;
  departamento?: string;
  linhasDePesquisa: string[];
  vagasDisponiveis: number;
  biografia?: string;
}

export interface Orientador extends OrientadorInput {
  id: number;
  ativo: boolean;
  criadoEm?: string;
}

export interface ErroValidacaoCampo {
  campo: string;
  mensagem: string;
}

export interface RespostaErroPadrao {
  timestamp: string;
  status: number;
  erro: string;
  caminho: string;
  detalhes?: ErroValidacaoCampo[] | string;
}
```

---

### 5.3. Para o QA (Testes Automatizados - Issue #39)

O Engenheiro de Qualidade deve implementar testes de unidade automatizados focados nas regras deste contrato:

1. **Casos de Sucesso:**
   - Cadastro com todos os campos válidos retorna status HTTP 201 e dados preenchidos com ID.
   - Listagem retorna status HTTP 200 com a coleção de orientadores.
   - Atualização parcial de perfil/vagas (`PATCH`) com dados válidos retorna status HTTP 200 com os campos atualizados.
   - Remoção/desativação (`DELETE`) de orientador existente retorna status HTTP 204.
2. **Casos de Falha / Exceção:**
   - Envio de vagas negativas (`-1`) gera erro de validação (HTTP 400).
   - Envio de e-mail inválido ou vazio gera erro de validação (HTTP 400).
   - Envio de lista vazia de linhas de pesquisa gera erro de validação (HTTP 400).
   - Consulta ou atualização de orientador com ID inexistente gera HTTP 404.

---

## 6. Critérios de Aceitação da US02 (Formato BDD)

### Cenário 1: Cadastro realizado com sucesso

- **Dado** que o professor informa nome "Dr. Adriano Lima", e-mail "adriano.lima@ifsc.edu.br", 3 vagas e a linha "Engenharia de Software";
- **Quando** submeter os dados para cadastro;
- **Então** o sistema deve salvar o perfil com status ativo;
- **E** retornar o código de status HTTP 201 Created com o identificador único gerado.

### Cenário 2: Tentativa de cadastro com dados inválidos

- **Dado** que o usuário deixa o campo nome em branco ou informa vagas negativas;
- **Quando** submeter a requisição;
- **Então** o sistema não deve persistir os dados;
- **E** deve retornar o código HTTP 400 Bad Request com a lista detalhada dos campos com erro.

### Cenário 3: Exibição no catálogo

- **Dado** que existem orientadores cadastrados no banco de dados;
- **Quando** o aluno acessar a página de catálogo de orientadores;
- **Então** o sistema deve listar todos os orientadores ativos com seus nomes, áreas de pesquisa e total de vagas disponíveis.

### Cenário 4: Atualização de perfil e vagas de orientador

- **Dado** que um orientador cadastrado deseja atualizar suas vagas disponíveis de 3 para 5 e incluir uma nova linha de pesquisa;
- **Quando** submeter a requisição de atualização parcial via `PATCH /api/v1/orientadores/{id}`;
- **Então** o sistema deve atualizar os dados do orientador no banco de dados;
- **E** retornar o código de status HTTP 200 OK com o objeto atualizado.

### Cenário 5: Remoção ou desativação de orientador

- **Dado** que um orientador não oferecerá mais vagas de orientação no semestre;
- **Quando** submeter a requisição de remoção via `DELETE /api/v1/orientadores/{id}`;
- **Então** o sistema deve desativar ou remover o perfil;
- **E** retornar o código de status HTTP 204 No Content.
