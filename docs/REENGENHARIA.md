# Reengenharia: Tratamento de Erros do Cliente HTTP do Frontend

- **Situação:** problema identificado e métrica definida. A refatoração ainda não foi realizada.
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

| `orientadorService.ts` | `solicitacaoService.ts` | Linhas | Conteúdo                                          |
| :--------------------: | :---------------------: | :----: | :------------------------------------------------ |
|         20–46          |          10–36          |   27   | `lerErro` e início de `garantirResposta`          |
|         69–73          |          57–61          |   5    | Estrutura de `catch`                              |
|         78–83          |          80–85          |   6    | Retorno da resposta e estrutura de `catch`        |
|        103–109         |         97–103          |   7    | Estrutura de `catch` e início de `normalizarErro` |
|        124–134         |         104–114         |   11   | Final de `normalizarErro`                         |

### 3.2 Cobertura de testes da pasta `utils`

- **Definição:** cobertura de instruções, ramificações, funções e linhas dos arquivos de `SIGTCC/frontend/src/utils/`, a mesma medida da métrica [M-01](metricas/M-01.md).
- **Como é medida:** `npm run coverage` em `SIGTCC/frontend` (Vitest com `@vitest/coverage-v8`).
- **Valores antes:** relatório de cobertura gerado pela equipe no fechamento da Sprint 2 (01/10/2026).

| Arquivo                 |     Instruções      |    Ramificações     |       Funções       |       Linhas        |
| :---------------------- | :-----------------: | :-----------------: | :-----------------: | :-----------------: |
| `api.ts`                |       3 de 5        |       2 de 2        |       0 de 1        |       3 de 5        |
| `orientadorService.ts`  |       4 de 43       |       1 de 25       |       1 de 8        |       4 de 43       |
| `solicitacaoService.ts` |       0 de 34       |       0 de 20       |       0 de 7        |       0 de 34       |
| **Pasta `utils`**       | **8,53%** (7 de 82) | **6,38%** (3 de 47) | **6,25%** (1 de 16) | **8,53%** (7 de 82) |

### 3.3 Resultado esperado

- Linhas duplicadas e definições repetidas das funções auxiliares próximas de zero.
- Cobertura de `utils` dentro da faixa aceitável da M-01 (70% ou mais), com o tratamento de erros testado diretamente.
- Mensagens exibidas ao usuário inalteradas, comprovado pelos testes existentes de componentes e páginas.

---

## 4. Direção da Refatoração

A refatoração será detalhada e executada em issue e Pull Request próprios. A direção proposta é:

1. **Fixar o comportamento atual com testes.** Antes de alterar o código, escrever testes dos dois serviços simulando `fetch`, cobrindo sucesso, `400` com lista de campos, `404`, `409`, `422` e falha de rede. Esses testes são a evidência de que o comportamento foi preservado.
2. **Extrair um módulo único de cliente HTTP** em `utils/`, contendo `ApiError`, a leitura do corpo de erro e a verificação da resposta. Os serviços passam a conter apenas os endpoints e as mensagens específicas de cada domínio (por exemplo, o texto de e-mail duplicado do cadastro de orientador).
3. **Corrigir a ordem de composição dos cabeçalhos** em `api.ts` (seção 1.5), com teste.
4. **Atualizar as importações** de `ApiError` nos componentes e páginas.

Os valores "antes" deste documento referem-se ao código do final da Sprint 2. Os testes do passo 1 já elevam a cobertura; por isso a comparação final deve usar os valores registrados na seção 3, e não uma nova medição feita no meio da refatoração.

---

## 5. Registro da Refatoração

A preencher quando a refatoração for concluída:

| Item                                              | Registro |
| :------------------------------------------------ | :------- |
| Issue e Pull Request da refatoração               |          |
| Linhas duplicadas entre os serviços (antes: 56)   |          |
| Definições das funções auxiliares (antes: 6)      |          |
| Cobertura de `utils`, instruções (antes: 8,53%)   |          |
| Cobertura de `utils`, ramificações (antes: 6,38%) |          |
| Evidência de comportamento preservado             |          |
| Houve melhoria, piora ou apenas reorganização?    |          |
| Limitações e pontos não resolvidos                |          |
| ADR registrada ou atualizada, se aplicável        |          |
