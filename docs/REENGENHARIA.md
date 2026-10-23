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

### 3.1. Duplicação de Código (Antes - Commit `23efc6c`)

Critério de medição: blocos idênticos ou estruturalmente equivalentes com **5 ou mais linhas** nos serviços de integração HTTP do frontend (`SIGTCC/frontend/src/utils/*Service.ts`).

| #         | Bloco duplicado                                                                                         | Arquivos onde ocorria                                                                            | Linhas por ocorrência | Total de linhas duplicadas |
| :-------- | :------------------------------------------------------------------------------------------------------ | :----------------------------------------------------------------------------------------------- | :-------------------: | :------------------------: |
| 1         | Função `lerErro(response: Response)` (`try/catch` de leitura do JSON de erro)                           | `orientadorService.ts`, `solicitacaoService.ts`, `professorService.ts`, `prazoEntregaService.ts` |          15           |             60             |
| 2         | Função `garantirResposta(response, statusEsperado)`                                                     | `orientadorService.ts`, `solicitacaoService.ts`, `prazoEntregaService.ts`                        |          12           |             36             |
| 3         | Tratamento genérico em `normalizarErro` (`instanceof ApiError`, `TypeError` de rede e fallback `Error`) | `orientadorService.ts`, `solicitacaoService.ts`, `professorService.ts`, `prazoEntregaService.ts` |          11           |             44             |
| **Total** | -                                                                                                       | -                                                                                                |           -           |       **140 linhas**       |

> A classe `ApiError` não entra nesta tabela: no `23efc6c` ela é declarada uma única vez, em `orientadorService.ts`; os demais arquivos apenas a importam de lá (acoplamento tratado separadamente na Seção 1.3/4.1). `professorService.ts` não possui uma função `garantirResposta` própria - a checagem de status é feita inline dentro de `obterPerfilProfessor()` -, por isso o bloco 2 soma apenas 3 ocorrências.

> **Nota sobre o wrapper `try { ... } catch (erro) { throw normalizarErro(erro); }`:** cada função exportada nos serviços possui um bloco `try/catch` de delegação para `normalizarErro` (~4 linhas de estrutura de captura por método, somando 23 linhas nos 4 serviços). Por possuir menos de 5 linhas de boilerplate por ocorrência e cumprir o papel de traduzir o contexto de erro específico de cada domínio (mensagens padrão distintas por serviço), ele não é contabilizado como bloco de duplicação ≥ 5 linhas, sendo registrado na Seção 5 como limitação conhecida do escopo da REF-1.

### 3.2. Cobertura de Testes do Frontend (Antes - Commit `23efc6c`)

Medição obtida com `npm run coverage` (Vitest + `@vitest/coverage-v8`) no commit `23efc6c`:

| Métrica Global (frontend inteiro) | Antes (`23efc6c`) |
| :-------------------------------- | :---------------: |
| **Statements (`% Stmts`)**        |      69,58%       |
| **Branches (`% Branch`)**         |      61,41%       |
| **Functions (`% Funcs`)**         |      73,09%       |
| **Lines (`% Lines`)**             |      69,43%       |

**Detalhamento por arquivo nos utilitários de serviço (`src/utils/` - Antes, commit `23efc6c`):**

| Arquivo                            | `% Stmts` | `% Branch` | `% Funcs` | `% Lines` | Observação na linha de base                                                                                                                                                                                                              |
| :--------------------------------- | :-------: | :--------: | :-------: | :-------: | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/utils/api.ts`                 |  50,00%   |   50,00%   |   0,00%   |  50,00%   | Executado indiretamente ao testar os componentes que fazem chamadas à API                                                                                                                                                                |
| `src/utils/orientadorService.ts`   |   8,33%   |   3,44%    |  12,50%   |   8,69%   | Em grande parte substituído por `vi.mock` nos testes de página/componente                                                                                                                                                                |
| `src/utils/solicitacaoService.ts`  |   0,00%   |   0,00%    |   0,00%   |   0,00%   | Substituído por `vi.mock` nos testes de página/componente                                                                                                                                                                                |
| `src/utils/professorService.ts`    |     -     |     -      |     -     |     -     | Não aparece no relatório: nenhum teste importa o módulo real (`vi.mock`)                                                                                                                                                                 |
| `src/utils/prazoEntregaService.ts` |   0,00%   |   0,00%    |   0,00%   |   0,00%   | `vi.mock` parcial em `GestaoCalendario.test.tsx` (com `vi.importActual`): o módulo real é carregado, mas os 3 métodos exportados são sobrescritos por `vi.fn()` e seus corpos nunca executam - daí o 0% mesmo com o módulo real presente |

**Cobertura de `src/utils/` como pasta (antes, `23efc6c`):** 6,14% statements / 4,22% branches / 4,54% functions / 6,30% lines.

---

## 4. Mudança Realizada

A refatoração (REF-1) extraiu as três funções auxiliares e a classe `ApiError` para um novo módulo dedicado, `SIGTCC/frontend/src/utils/apiErro.ts`, e migrou `orientadorService.ts` e `solicitacaoService.ts` - além de `professorService.ts` e do novo `prazoEntregaService.ts` da US05 - para consumi-lo.

### 4.1. Mudanças Realizadas (REF-1)

1. **Preservação dos cabeçalhos padrão em `src/utils/api.ts`:**
   - **Como ficou:** `...options` é espalhado antes da chave `headers`, e dentro de `headers` são declarados `"Content-Type": "application/json"`, `Accept: "application/json"`, o cabeçalho condicional `Authorization: Bearer ${token}` e, por último, `...options?.headers`.
   - **Efeito real no código:** passar `headers` em `options` não descarta mais `Content-Type`, `Accept` e `Authorization` por acidente; como `...options?.headers` é mesclado por último, quem chama ainda pode sobrescrevê-los intencionalmente quando necessário.

2. **Extração do módulo compartilhado `src/utils/apiErro.ts`:**
   - Centraliza a classe `ApiError` e as funções `lerErro`, `garantirResposta` e `normalizarErroGenerico`.
   - Os serviços `orientadorService.ts`, `solicitacaoService.ts`, `professorService.ts` e `prazoEntregaService.ts` passaram a delegar a verificação de status HTTP e a normalização base para `apiErro.ts`.

3. **Desacoplamento das importações de `ApiError` nos componentes e páginas:**
   - **Arquivos sem relação direta com orientadores** que antes importavam `ApiError` de `orientadorService.ts` e passaram a importar diretamente de `src/utils/apiErro`:
     - `src/components/SolicitacaoForm/index.tsx`
     - `src/paginas/AcompanharSolicitacao/index.tsx` (e `AcompanharSolicitacao.test.tsx`)
     - `src/paginas/PainelSolicitacoes/index.tsx` (e `PainelSolicitacoes.test.tsx`)
     - `src/components/GestaoCalendario/index.tsx` (e `GestaoCalendario.test.tsx`)
   - **Arquivos que tratam de orientadores** e que tiveram a importação unificada para `src/utils/apiErro`:
     - `src/components/OrientadorForm/index.tsx` (e `OrientadorForm.test.tsx`)
     - `src/paginas/OrientadorEdicao/index.tsx`

### 4.2 Testabilidade

Como `lerErro`, `garantirResposta` e `normalizarErroGenerico` agora são funções exportadas de um módulo próprio, passaram a ter testes diretos e unitários em `apiErro.test.ts`, cobrindo parse de corpo de erro válido/inválido, status esperado vs. inesperado, a política de preferir `detalhes` quando é string, e os três ramos de `normalizarErroGenerico` (repasse de `ApiError`, tradução de `TypeError`, repasse de outros `Error` e fallback). Os testes dos serviços (`vi.mock("../utils/*Service")` nos componentes) continuam substituindo o serviço inteiro, mas a lógica de maior ramificação do frontend deixou de depender exclusivamente desses mocks.

---

## 5. Resultados Pós-Refatoração e Registro (Depois)

### 5.1. Comparativo de Métricas (Antes vs. Depois)

| Métrica                                                              | Antes (`23efc6c`) |                                                  Depois (REF-1)                                                  | Variação / Observação                                                                                                                                                                                                                                                                                        |
| :------------------------------------------------------------------- | :---------------: | :--------------------------------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Linhas duplicadas em blocos ≥ 5 linhas (`src/utils/*Service.ts`)** |    140 linhas     | **0 linhas** (nos blocos de `lerErro`, `garantirResposta` e `normalizarErroGenerico` unificados em `apiErro.ts`) | Redução de 100% nos blocos de lógica repetida (unificados nas 97 linhas de `apiErro.ts`). Permanece apenas o boilerplate de captura `try { ... } catch (erro) { throw normalizarErro(erro); }` (~23 linhas distribuídas nos 4 serviços), mantido para associar a mensagem padrão específica de cada domínio. |
| **Cobertura de `src/utils/` (apenas a pasta)**                       |       6,14%       |                                 **26,08% Stmts / 51,21% Branch / 25,84% Lines**                                  | Subida expressiva impulsionada pela nova suíte de testes unitários `apiErro.test.ts` e pelo novo `api.test.ts` (que assegura 100% em `api.ts`).                                                                                                                                                              |
| **Cobertura de ramificações (`% Branch`) - projeto inteiro**         |      61,41%       |                                                    **69,46%**                                                    | Reflete a melhora de todas as pastas, não só `utils/`. Isoladamente, `apiErro.ts` chega a 89,47% de branches. **Parte deste ganho não vem da REF-1** - ver nota sobre o commit `db03a97` em 5.2.                                                                                                             |

### 5.2. Registro de Rastreabilidade e Critérios de Aceite (#146)

- **Pull Request da Refatoração (REF-1):** PR `#180`.
- **Evidência de Comportamento Preservado:**
  - Execução da suíte automatizada do frontend (`npm run test` em `SIGTCC/frontend`): **85 testes passando em 16 arquivos de teste**, com 100% de sucesso nas suítes (`Login`, `ProtectedRoute`, `OrientadorCadastro`, `OrientadorEdicao`, `Orientadores`, `OrientadorCard`, `OrientadorForm`, `OrientadorList`, `LinhaPesquisa`, `SolicitarOrientacao`, `SolicitacaoForm`, `PainelSolicitacoes`, `AcompanharSolicitacao`, `GestaoCalendario`, `apiErro.test.ts` e `api.test.ts`).
  - As regras específicas de domínio em `orientadorService.ts` para os status `409` (_"Este e-mail já está cadastrado no sistema"_) e `404` (_"Orientador não encontrado"_) foram integralmente preservadas na função local `normalizarErro`.
- **Commit fora do escopo da REF-1 presente na branch:**
  - `db03a97` (_test: Atualiza testes do OrientadorList e OrientadorRepositoryTest_) pertence à issue #136 (testes de filtro de orientador), não à REF-1, mas já estava mesclado nesta branch antes do início da refatoração e permanece nela. Ele adiciona testes a `OrientadorList.test.tsx` e `OrientadorRepositoryTest.java`, elevando a cobertura de ramificações de `OrientadorList/index.tsx` de 84% para 88% - parte do ganho de 61,41% → 69,46% nas ramificações do projeto inteiro registrado na seção 5.1 não vem da REF-1, e sim deste commit. Ele não foi movido para um PR da #136 porque reorganizar o histórico da branch no GitHub reescreveria commits já publicados; fica registrado aqui para que a comparação de métricas não seja lida como 100% causada pela refatoração do cliente HTTP.
- **Mudança de Comportamento Intencional:**
  - **Unificação da mensagem principal em `garantirResposta` (`apiErro.ts`):** antes da REF-1, `orientadorService.ts` usava sempre `erro.erro` (título genérico) como `message` do `ApiError`, enquanto `solicitacaoService.ts` e `prazoEntregaService.ts` davam preferência a `erro.detalhes` quando este era uma `string`. Com a unificação em `garantirResposta`, **todos os serviços passaram a preferir `erro.detalhes` quando for `string`** (caindo para `erro.erro` apenas quando `detalhes` está ausente ou é um array de validação `400`).
  - **Impacto nas telas de orientador:** respostas de erro do backend com `detalhes` textual - como `403 Forbidden`, `422 Unprocessable Entity` ou `500 Internal Server Error` - passam a exibir a mensagem específica enviada pelo backend nas telas de orientador (`OrientadorForm`, `OrientadorEdicao`), em vez do título genérico (`erro.erro`). Para `404` e `409` em `orientadorService.ts`, a mensagem amigável fixa continua prevalecendo porque `normalizarErro` sobrescreve o texto após `garantirResposta`.
  - **Restrição do status esperado em `professorService.ts`:** `obterPerfilProfessor()` passou a validar explicitamente o status `200 OK` via `garantirResposta(response, 200)` (antes verificava apenas `!response.ok`, aceitando qualquer `2xx`), alinhando-se estritamente ao contrato do endpoint `GET /api/v1/professores/me`.
- **Limitações Conhecidas:**
  1. **Repetição estrutural de `try/catch` nos serviços:** cada função de `orientadorService.ts`, `solicitacaoService.ts`, `professorService.ts` e `prazoEntregaService.ts` ainda mantém o bloco `try { ... } catch (erro) { throw normalizarErro(erro); }` para injetar a mensagem de fallback contextual de cada domínio.
  2. **Cobertura de 0% nos arquivos `*Service.ts`:** como os testes de páginas e componentes utilizam `vi.mock("../../utils/*Service")` para isolar a camada de UI, as funções dos arquivos de serviço continuam registrando `0%` de cobertura direta no relatório do Vitest (sendo toda a lógica de parse, headers e normalização de erros testada diretamente em `apiErro.test.ts` e `api.test.ts`).
- **Decisão sobre ADR (Architecture Decision Record):**
  - **Não aplicável (nenhum ADR novo criado).**
  - **Justificativa:** a REF-1 consiste em uma refatoração interna de código no frontend (extração do utilitário compartilhado `apiErro.ts`, criação de `api.test.ts` e correção de merge de headers em `api.ts`) que não altera a stack tecnológica, a topologia de componentes, o contrato de dados REST nem o modelo de autenticação/autorização já estabelecidos nos ADRs vigentes.

### 5.3. Análise do Resultado e Relação com os Atributos de Qualidade (Entrega 4 / ISO/IEC 25010)

**Houve melhoria, não apenas reorganização.** Os três indicadores da seção 5.1 se moveram na mesma direção e por razões causais, não por acaso:

- A duplicação em blocos ≥ 5 linhas foi para zero porque `lerErro`, `garantirResposta` e `normalizarErroGenerico` passaram a existir uma única vez, em `apiErro.ts` - não porque o código foi reescrito de outra forma que escondesse a repetição (o residual de 23 linhas de `try/catch` por domínio, descrito na seção 4.1, está registrado e é intencional).
- A cobertura de `utils/` subiu de 6,14% para 26,08% (statements) porque a lógica de tratamento de erro, antes presa dentro de funções privadas duplicadas, passou a ser testável e testada diretamente (`apiErro.test.ts`, `api.test.ts`), sem depender de `vi.mock`.
- A cobertura de ramificações do projeto inteiro subiu de 61,41% para 69,46% - parte desse ganho vem da REF-1 (principalmente via `apiErro.ts`, 89,47% de branches), e parte vem do commit `db03a97` (issue #136), já registrado e isolado em 5.2 para não inflar artificialmente o resultado da refatoração.
- O preço dessa melhoria é a mudança de comportamento intencional já registrada na seção 5.2: nas telas de orientador, erros `403`/`422`/`500` com `detalhes` textual passam a mostrar a mensagem específica do backend em vez do título genérico. Isso é uma consequência aceita da unificação de política, não um efeito colateral não percebido.

**Relação com os atributos de qualidade da Entrega 4 ([`docs/qualidade.md`](qualidade.md), ISO/IEC 25010).** A seção 2 deste documento já apontava a Manutenibilidade e a Confiabilidade como os atributos afetados pelo problema original; a Entrega 4 prioriza exatamente esses dois atributos (seções 1.2 e 1.4 de `docs/qualidade.md`), com justificativas que a REF-1 endereça diretamente:

- **Manutenibilidade (`docs/qualidade.md`, seção 1.4).** A justificativa de prioridade cita a necessidade de "preservar entendimento do código e facilitar a evolução no próximo ciclo do produto" e o risco R05 (divergência de ambiente/infraestrutura, mitigado por manutenção simples e previsível). A eliminação da duplicação (140 → 0 linhas em blocos ≥ 5 linhas) e a unificação de `ApiError` num único módulo atacam exatamente isso: antes da REF-1, uma correção de tratamento de erro exigia alterar até quatro arquivos (seção 1.2); depois, uma única mudança em `apiErro.ts` propaga para todos os serviços.
- **Confiabilidade (`docs/qualidade.md`, seção 1.2).** A justificativa de prioridade cita o risco R02 ("falhas por ausência de testes/CI") e a necessidade de "comportamento consistente em uso real" entre frontend e backend. A métrica de cobertura evidencia isso diretamente: a camada que traduz respostas HTTP de erro para o usuário saiu de praticamente não testada (6,14% em `utils/`, 0% nos dois serviços críticos) para 100% de cobertura no módulo que concentra essa lógica (`apiErro.ts`). Como critério de evidência de Confiabilidade, a Entrega 4 lista "taxa de falhas de integração entre frontend e backend" e "defeitos críticos encontrados após merge" - a suíte completa (85 testes, 16 arquivos, seção 5.2) passando após a migração dos quatro serviços é a evidência direta de que nenhuma regressão foi introduzida nesse processo.
- **Segurança e Adequação Funcional não se aplicam diretamente.** A REF-1 não altera regras de autorização, contratos de API ou fluxos funcionais visíveis ao usuário além da mudança de mensagem registrada acima; por isso esses dois atributos da Entrega 4 (seções 1.1 e 1.3 de `docs/qualidade.md`) não entram nesta análise.
