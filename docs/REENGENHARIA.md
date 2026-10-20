# Reengenharia: Tratamento de Erros do Cliente HTTP do Frontend

- **Situação:** refatoração realizada (#146). Ver registro na seção 5.
- **Código analisado:** estado ao final da Sprint 2 (commit `b5c5a3f`).

---

## 1. Problema Identificado

A camada de acesso à API do frontend (`SIGTCC/frontend/src/utils/`) trata erros HTTP com três funções auxiliares que foram **copiadas** de `orientadorService.ts` (Sprint 1) para `solicitacaoService.ts` (Sprint 2). As cópias já divergiram entre si, a classe de erro compartilhada mora no módulo errado e nada disso é exercitado pelos testes.

O problema combina duplicação, baixa coesão, acoplamento indevido e baixa testabilidade.

### 1.1 Duplicação

As funções `lerErro`, `garantirResposta` e `normalizarErro` estão definidas nos dois arquivos:

| Função             | `orientadorService.ts` | `solicitacaoService.ts` | Situação das cópias |
| :----------------- | :--------------------: | :---------------------: | :------------------ |
| `lerErro`          |      linhas 21–35      |      linhas 11–25       | Idênticas           |
| `garantirResposta` |      linhas 37–50      |      linhas 27–44       | Divergentes         |
| `normalizarErro`   |     linhas 108–136     |     linhas 102–116      | Divergentes         |

Trecho idêntico nos dois arquivos (`lerErro`):

```typescript
async function lerErro(response: Response): Promise<RespostaErroPadrao> {
  try {
    const erro = (await response.json()) as Partial<RespostaErroPadrao>;
    return {
      status: response.status,
      erro: erro.erro || "Erro ao processar a requisição",
      detalhes: erro.detalhes,
    };
  } catch {
    return {
      status: response.status,
      erro: response.statusText || "Erro ao processar a requisição",
    };
  }
}
```

Além das três funções, as oito funções exportadas dos dois serviços repetem a mesma estrutura de `try { ... } catch (erro) { throw normalizarErro(erro); }`.

### 1.2 Divergência entre as cópias

As duas versões de `garantirResposta` escolhem mensagens diferentes para o mesmo formato de erro da API (`ErrorResponseDTO`).

`orientadorService.ts` (linhas 46–47) usa sempre o título genérico do erro:

```typescript
const erro = await lerErro(response);
throw new ApiError(response.status, erro.erro, erro.detalhes);
```

`solicitacaoService.ts` (linhas 36–41) prefere a mensagem específica enviada pelo backend:

```typescript
const erro = await lerErro(response);
const mensagemPrincipal =
  typeof erro.detalhes === "string"
    ? erro.detalhes
    : erro.erro || "Erro ao processar a requisição";
throw new ApiError(response.status, mensagemPrincipal, erro.detalhes);
```

O mesmo ocorre em `normalizarErro`. A versão de `orientadorService.ts` (linhas 109–125) substitui a mensagem de **qualquer** resposta `409` por "Este e-mail já está cadastrado no sistema" e de qualquer `404` por "Orientador não encontrado", descartando o que o backend informou. A versão de `solicitacaoService.ts` não faz nenhuma substituição. Na prática, existem duas políticas de tratamento de erro para uma única API, e qual delas vale depende de qual arquivo foi copiado.

### 1.3 Baixa coesão e acoplamento indevido

A classe `ApiError`, que representa um erro de qualquer chamada à API, está definida dentro de `orientadorService.ts` (linhas 5–19). Por isso, código que não tem relação com orientadores depende desse módulo:

```typescript
// SIGTCC/frontend/src/utils/solicitacaoService.ts, linha 2
import { ApiError } from "./orientadorService";
```

A mesma importação aparece em `components/SolicitacaoForm/index.tsx`, `paginas/AcompanharSolicitacao/index.tsx` e `paginas/PainelSolicitacoes/index.tsx`.

### 1.4 Baixa testabilidade

- As três funções auxiliares são privadas aos módulos. Só podem ser testadas indiretamente, por meio de cada função exportada, e em dobro, por causa das cópias.
- Os sete arquivos de teste que usam os serviços substituem o módulo inteiro por `vi.mock(...)`. Não existe teste dos serviços em si.
- Como resultado, a lógica com mais ramificações do frontend não é executada por nenhum teste. No relatório de cobertura do fechamento da Sprint 2, o único trecho executado de `orientadorService.ts` é o construtor de `ApiError`, e `solicitacaoService.ts` tem 0% de cobertura.

### 1.5 Defeito latente relacionado

Em `utils/api.ts` (linhas 6–13), `...options` é espalhado depois de `headers`:

```typescript
const response = await fetch(`${API_BASE_URL}${endpoint}`, {
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...options?.headers,
  },
  ...options,
});
```

Se alguma chamada passar `headers` em `options`, o objeto `headers` montado acima é substituído por inteiro e os cabeçalhos `Content-Type` e `Accept` se perdem. Hoje nenhuma chamada passa `headers`, então o defeito não se manifesta. Ele passa a importar na US01, quando as requisições precisarão do cabeçalho `Authorization`. Como `api.ts` não tem teste, nada acusaria o problema.

---

## 2. Por que o Problema é Relevante

**Manutenção.** Qualquer mudança no tratamento de erros precisa ser feita em dois lugares, e a seção 1.2 mostra que isso já falhou uma vez: a melhoria feita na Sprint 2 (usar a mensagem do backend) não voltou para o serviço da Sprint 1. O usuário recebe mensagens específicas nas telas de solicitação e genéricas nas telas de orientador.

**Testabilidade.** Funções privadas e duplicadas, somadas a testes que substituem os módulos por _mocks_, deixaram a pasta `utils` com 8,53% de cobertura. Essa pasta é a principal causa de a cobertura do frontend ter caído para a zona de alerta da métrica [M-01](metricas/M-01.md) (67,74%).

**Extensibilidade.** A Sprint 3 amplia exatamente esta camada:

- **US01:** todas as respostas `401` e `403` precisarão de tratamento (redirecionar ao login, informar falta de permissão). No desenho atual, esse tratamento teria de ser escrito nas duas cópias de `garantirResposta` e `normalizarErro`.
- **US05:** o calendário de prazos exige um novo serviço. Seguindo o padrão atual, ele seria a terceira cópia das mesmas funções.

Refatorar antes de implementar a US01 e a US05 evita que o problema dobre de tamanho durante a própria sprint.

**Atributos de qualidade.** O problema afeta diretamente a **manutenibilidade** e, pela ausência de testes na camada que traduz erros para o usuário, a **confiabilidade**, dois dos atributos priorizados em [`docs/qualidade.md`](qualidade.md).

---

## 3. Métrica para Comparação Antes/Depois

Foram escolhidas duas métricas.

### 3.1 Duplicação entre os serviços de `utils`

- **Definição:** número de linhas pertencentes a blocos idênticos de cinco ou mais linhas entre `orientadorService.ts` e `solicitacaoService.ts`, ignorando indentação.
- **Como foi medida:** comparação linha a linha dos dois arquivos.

| Medida                                                                             | Valor antes                |
| :--------------------------------------------------------------------------------- | :------------------------- |
| Linhas duplicadas (em cada arquivo)                                                | 56, em 5 blocos            |
| Proporção de `solicitacaoService.ts` (116 linhas)                                  | 48%                        |
| Proporção de `orientadorService.ts` (136 linhas)                                   | 41%                        |
| Definições das funções auxiliares de erro                                          | 6 (3 funções × 2 arquivos) |
| Arquivos que importam `ApiError` de `orientadorService` sem tratar de orientadores | 4                          |

Blocos idênticos encontrados:

| Bloco | `orientadorService.ts` | `solicitacaoService.ts` | Linhas | Conteúdo                                                                                                                            |
| :---: | :--------------------: | :---------------------: | :----: | :---------------------------------------------------------------------------------------------------------------------------------- |
|   1   |         21–35          |          11–25          |   15   | Função `lerErro` completa (parse do corpo de erro).                                                                                 |
|   2   |         37–46          |          27–36          |   10   | Assinatura e condição de `garantirResposta`, até a leitura de `lerErro(response)`.                                                  |
|   3   |         48–50          |          43–45          |   3    | Fechamento de `garantirResposta` (`return response; }`).                                                                            |
|   4   |        127–131         |         107–111         |   5    | Tratamento de `TypeError` em `normalizarErro` (mensagem de falha de conexão).                                                       |
|   5   |           -            |            -            |   23   | Estrutura `try { ... } catch (erro) { throw normalizarErro(erro); }` repetida em cada uma das 4 funções exportadas de cada arquivo. |

### 3.2 Cobertura de testes da pasta `utils`

- **Definição:** percentual de linhas executadas pela suíte do Vitest nos arquivos de `SIGTCC/frontend/src/utils/`.
- **Como foi medida:** relatório de cobertura do Vitest (`npm run test -- --coverage`), reportado também em [M-01](metricas/M-01.md).

| Medida                                           | Valor antes (fechamento Sprint 2) |
| :----------------------------------------------- | :-------------------------------- |
| Cobertura de `utils/` (linhas)                   | 8,53%                             |
| Cobertura total do frontend (linhas)             | 67,74% (Zona de Alerta)           |
| Funções auxiliares de erro testáveis diretamente | 0 (todas privadas ao módulo)      |

---

## 4. Mudança Realizada

A refatoração (REF-1) extraiu as três funções auxiliares e a classe `ApiError` para um novo módulo dedicado, `SIGTCC/frontend/src/utils/apiErro.ts`, e migrou `orientadorService.ts` e `solicitacaoService.ts` - além de `professorService.ts` e do novo `prazoEntregaService.ts` da US05 - para consumi-lo.

### 4.1 Design aplicado

- **`apiErro.ts`** passou a ser a única fonte de verdade para tratamento de erro HTTP, exportando `ApiError`, `lerErro`, `garantirResposta` e `normalizarErroGenerico`.
- A política de mensagens foi unificada adotando o comportamento de `solicitacaoService.ts` (preferir `erro.detalhes` quando é uma string) em `garantirResposta`, eliminando a divergência descrita na seção 1.2.
- `normalizarErroGenerico(erro, mensagemPadrao)` cobre apenas o tratamento genérico (repassar `ApiError`, traduzir falha de rede, cair para mensagem padrão). Cada serviço mantém sua própria função `normalizarErro`, fina, que chama `normalizarErroGenerico` e aplica regras específicas por cima quando necessário - por exemplo, a substituição de mensagens de `409`/`404` que só existia em `orientadorService.ts` continua lá, isolada, em vez de contaminar o módulo compartilhado.
- O defeito latente em `api.ts` (seção 1.5) foi corrigido: `...options` agora é espalhado antes de `headers`, garantindo que `Content-Type` e `Accept` nunca sejam sobrescritos por cabeçalhos passados em `options`.
- Todos os pontos que importavam `ApiError` de `orientadorService.ts` sem tratar de orientadores (`SolicitacaoForm`, `AcompanharSolicitacao`, `PainelSolicitacoes`, `GestaoCalendario`, `OrientadorForm`, `OrientadorEdicao`) passaram a importar diretamente de `apiErro.ts`, removendo o acoplamento indevido descrito na seção 1.3.

### 4.2 Testabilidade

Como `lerErro`, `garantirResposta` e `normalizarErroGenerico` agora são funções exportadas de um módulo próprio, passaram a ter testes diretos e unitários em `apiErro.test.ts`, cobrindo parse de corpo de erro válido/inválido, status esperado vs. inesperado, a política de preferir `detalhes` quando é string, e os três ramos de `normalizarErroGenerico` (repasse de `ApiError`, tradução de `TypeError`, repasse de outros `Error` e fallback). Os testes dos serviços (`vi.mock("../utils/*Service")` nos componentes) continuam substituindo o serviço inteiro, mas a lógica de maior ramificação do frontend deixou de depender exclusivamente desses mocks.

---

## 5. Registro da Execução

- **Branch:** `refactor/146-refatoracao-orientada-a-design`.
- **Commits principais:**
  - `1ca665b` - feat: extrai cliente HTTP compartilhado (ApiError, lerErro, garantirResposta)
  - `630fb80` - refactor: migra os services para o cliente HTTP compartilhado
  - `71c0fc7` - fix: corrige ordem de composição dos cabeçalhos em apiFetch
  - `90c7dc3` - refactor: atualiza importações de ApiError para o novo módulo apiErro

### 5.1 Métrica 1 - Duplicação entre os serviços de `utils`

| Medida                                                                             | Antes                      | Depois                                                                 |
| :--------------------------------------------------------------------------------- | :------------------------- | :--------------------------------------------------------------------- |
| Linhas duplicadas entre `orientadorService.ts` e `solicitacaoService.ts`           | 56, em 5 blocos            | 0 - as funções compartilhadas existem em um único lugar (`apiErro.ts`) |
| Definições das funções auxiliares de erro                                          | 6 (3 funções × 2 arquivos) | 3 (uma só vez, em `apiErro.ts`)                                        |
| Arquivos que importam `ApiError` de `orientadorService` sem tratar de orientadores | 4                          | 0                                                                      |

> Nota: a seção 4.1 lista seis arquivos migrados porque inclui também `GestaoCalendario`, `OrientadorForm` e `OrientadorEdicao`, que passaram a importar `ApiError` de `apiErro.ts` depois da Sprint 2. A métrica acima considera apenas os quatro arquivos medidos no estado inicial.

### 5.2 Métrica 2 - Cobertura de testes da pasta `utils`

| Medida                               | Antes                   | Depois                   |
| :----------------------------------- | :---------------------- | :----------------------- |
| Cobertura de `apiErro.ts` (linhas)   | - (módulo não existia)  | 100%                     |
| Cobertura de `utils/` (linhas)       | 8,53%                   | 22,47%¹                  |
| Cobertura total do frontend (linhas) | 67,74% (Zona de Alerta) | 73,92% (Faixa Aceitável) |

¹ `orientadorService.ts`, `solicitacaoService.ts` e `prazoEntregaService.ts` continuam em 0% porque os testes de componente os substituem via `vi.mock`, por design (o objetivo da REF-1 era tornar testável a lógica de tratamento de erro, não os serviços em si). Essa lógica agora está isolada em `apiErro.ts`, com 100% de cobertura e testada sem mocks. A tela `OrientadorCadastro` (40,9% das linhas) segue como item pendente, já registrado em [M-01](metricas/M-01.md), e não faz parte do escopo desta refatoração.

### 5.3 Conclusão

Os dois sintomas descritos na seção 1 - duplicação com divergência e baixa testabilidade - foram eliminados: existe uma única implementação de `lerErro`/`garantirResposta`/`normalizarErroGenerico`, com uma única política de mensagens, coberta por testes diretos. A cobertura total do frontend saiu da Zona de Alerta e voltou à Faixa Aceitável da métrica [M-01](metricas/M-01.md). O defeito latente em `api.ts` foi corrigido antes de a US01 (autenticação) passar a depender do cabeçalho `Authorization`, confirmando a motivação registrada na seção 2. Hoje o `apiFetch` já inclui o cabeçalho `Authorization` quando há token, depois de `Content-Type` e `Accept` e antes de `options.headers`, sem que nenhum deles seja perdido.