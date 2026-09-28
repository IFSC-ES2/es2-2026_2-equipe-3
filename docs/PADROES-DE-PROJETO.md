# Documento de Arquitetura: Aplicação de Padrões de Projeto OO na US04

> **SIGTCC - Sistema de Gestão Centralizada de TCC**  
> **Artefato:** `docs/PADROES-DE-PROJETO.md`  
> **Autora:** Damares Gaia (Scrum Master & Arquiteta de Software)  
> **Status:** Aprovado  
> **Rastreabilidade:** Issue #11 (US04), Issue #91 (Doc-1 / ADRs), Issue #92 (Doc-2), Issue #83 (Backend Strategy), Issue #84 (Backend Observer)

---

## 1. Introdução e Motivação Arquitetural

No ciclo de desenvolvimento da **Sprint 2 (marco da US04 - Solicitação e Aceite de Orientação)**, a equipe de engenharia identificou desafios arquiteturais críticos na modelagem da camada de negócio do backend:

1. **Validações de Elegibilidade Variáveis e Expansíveis:** A criação de uma solicitação de orientação exige múltiplas checagens de negócio (capacidade de vagas do orientador, orientador ativo, ausência de solicitação pendente duplicada para o mesmo discente, consistência de tema e curso). Concentrar essas checagens em cadeias imperativas de `if/else` dentro de um único serviço viola o princípio **Aberto/Fechado (OCP - _Open/Closed Principle_)**, gerando uma classe inflada (_God Class_) de manutenção arriscada.
2. **Desacoplamento de Reações na Mudança de Estado:** Quando uma solicitação transita para o status `ACEITA` ou `RECUSADA`, ações colaterais distintas precisam ser disparadas (ex.: decremento atômico de vaga do orientador e registro de log de auditoria). Acoplar essas chamadas diretamente no método de transição do serviço viola o princípio da **Responsabilidade Única (SRP - _Single Responsibility Principle_)**.

Para resolver esses desafios com rigor técnico, foram formalizadas as decisões na [**`ADR-0007`**](file:///c:/Users/damares.gaia/ifsc/es2-2026_2-equipe-3/docs/adrs/ADR-0007.md), adotando dois padrões de projeto clássicos do _Gang of Four (GoF)_: **Strategy** e **Observer**.

Este documento especifica a estrutura de classes, os contratos de interfaces, os diagramas de interação e as diretrizes de implementação para os desenvolvedores backend.

---

## 2. Padrão Strategy (Validações de Elegibilidade da US04)

### 2.1 Problema Resolvido

No fluxo `POST /api/v1/solicitacoes`, cada regra de elegibilidade possui critérios de checagem e exceções semânticas distintas:

- Orientador sem vagas -> erro HTTP 422 (`VagasIndisponiveisException`);
- Orientador inativo -> erro HTTP 422 (`OrientadorInativoException`);
- Solicitação pendente já existente para o mesmo discente e orientador -> erro HTTP 409 (`SolicitacaoDuplicadaException`);
- Tema ou dados obrigatórios ausentes -> erro HTTP 400 (`MethodArgumentNotValidException` ou `RegraDeNegocioException`).

O padrão **Strategy** encapsula cada regra de validação em uma classe especialista independente, permitindo adicionar, alterar ou remover regras futuras sem modificar uma única linha do `SolicitacaoService`.

### 2.2 Diagrama de Classes (Strategy)

```mermaid
classDiagram
    direction TB

    class SolicitacaoService {
        -List~ValidadorSolicitacaoStrategy~ validadores
        -SolicitacaoRepository solicitacaoRepository
        -OrientadorRepository orientadorRepository
        -AlunoRepository alunoRepository
        +SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO dto)
    }

    class SolicitacaoContexto {
        <<record>>
        -Aluno aluno
        -Orientador orientador
        -String tema
        -String mensagem
        +aluno() Aluno
        +orientador() Orientador
        +tema() String
        +mensagem() String
    }

    class ValidadorSolicitacaoStrategy {
        <<interface>>
        +validar(SolicitacaoContexto contexto) void
    }

    class ValidacaoVagasDisponiveisStrategy {
        +validar(SolicitacaoContexto contexto) void
    }

    class ValidacaoOrientadorAtivoStrategy {
        +validar(SolicitacaoContexto contexto) void
    }

    class ValidacaoSolicitacaoDuplicadaStrategy {
        -SolicitacaoRepository solicitacaoRepository
        +validar(SolicitacaoContexto contexto) void
    }

    class ValidacaoTemaObrigatorioStrategy {
        +validar(SolicitacaoContexto contexto) void
    }

    SolicitacaoService --> ValidadorSolicitacaoStrategy : injeta List via Spring
    SolicitacaoService ..> SolicitacaoContexto : cria e passa
    ValidadorSolicitacaoStrategy <|.. ValidacaoVagasDisponiveisStrategy : implementa
    ValidadorSolicitacaoStrategy <|.. ValidacaoOrientadorAtivoStrategy : implementa
    ValidadorSolicitacaoStrategy <|.. ValidacaoSolicitacaoDuplicadaStrategy : implementa
    ValidadorSolicitacaoStrategy <|.. ValidacaoTemaObrigatorioStrategy : implementa
    ValidadorSolicitacaoStrategy ..> SolicitacaoContexto : consome
```

### 2.3 Estrutura de Pacotes e Nomenclatura

Os componentes devem ser criados no pacote dedicado:

```
br.edu.ifsc.gestao_tcc.strategy/
├── ValidadorSolicitacaoStrategy.java        (Interface base)
├── SolicitacaoContexto.java                 (DTO/Record de contexto de validação)
├── ValidacaoVagasDisponiveisStrategy.java   (Regra de cota de vagas > 0)
├── ValidacaoOrientadorAtivoStrategy.java    (Regra de orientador ativo no sistema)
├── ValidacaoSolicitacaoDuplicadaStrategy.java (Regra de duplicidade aluno-orientador)
└── ValidacaoTemaObrigatorioStrategy.java    (Regra de consistência do tema)
```

### 2.4 Contratos de Interface e Implementação

#### Interface Central (`ValidadorSolicitacaoStrategy.java`)

```java
package br.edu.ifsc.gestao_tcc.strategy;

public interface ValidadorSolicitacaoStrategy {
    /**
     * Executa a regra de validação específica sobre o contexto da solicitação.
     * @param contexto dados consolidados de Aluno, Orientador e Proposta.
     * @throws RegraDeNegocioException se a regra for violada (HTTP 422).
     * @throws ConflitoDeEstadoException se houver conflito de duplicidade (HTTP 409).
     */
    void validar(SolicitacaoContexto contexto);
}
```

#### Record de Contexto (`SolicitacaoContexto.java`)

```java
package br.edu.ifsc.gestao_tcc.strategy;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;

public record SolicitacaoContexto(
    Aluno aluno,
    Orientador orientador,
    String tema,
    String mensagem
) {}
```

#### Estratégia de Vagas Disponíveis (`ValidacaoVagasDisponiveisStrategy.java`)

```java
package br.edu.ifsc.gestao_tcc.strategy;

import br.edu.ifsc.gestao_tcc.exception.VagasIndisponiveisException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ValidacaoVagasDisponiveisStrategy implements ValidadorSolicitacaoStrategy {

    @Override
    public void validar(SolicitacaoContexto contexto) {
        var orientador = contexto.orientador();
        if (orientador.getPerfil() == null || orientador.getPerfil().getVagasDisponiveis() <= 0) {
            throw new VagasIndisponiveisException("O orientador selecionado não possui vagas disponíveis.");
        }
    }
}
```

#### Estratégia de Orientador Ativo (`ValidacaoOrientadorAtivoStrategy.java`)

```java
package br.edu.ifsc.gestao_tcc.strategy;

import br.edu.ifsc.gestao_tcc.exception.OrientadorInativoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ValidacaoOrientadorAtivoStrategy implements ValidadorSolicitacaoStrategy {

    @Override
    public void validar(SolicitacaoContexto contexto) {
        if (!contexto.orientador().isAtivo()) {
            throw new OrientadorInativoException("O orientador selecionado está inativo.");
        }
    }
}
```

#### Estratégia de Duplicidade (`ValidacaoSolicitacaoDuplicadaStrategy.java`)

```java
package br.edu.ifsc.gestao_tcc.strategy;

import br.edu.ifsc.gestao_tcc.exception.SolicitacaoDuplicadaException;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ValidacaoSolicitacaoDuplicadaStrategy implements ValidadorSolicitacaoStrategy {

    private final SolicitacaoRepository solicitacaoRepository;

    public ValidacaoSolicitacaoDuplicadaStrategy(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Override
    public void validar(SolicitacaoContexto contexto) {
        boolean existePendente = solicitacaoRepository.existsByAlunoIdAndOrientadorIdAndStatus(
            contexto.aluno().getId(),
            contexto.orientador().getId(),
            StatusSolicitacao.PENDENTE
        );

        if (existePendente) {
            throw new SolicitacaoDuplicadaException("Já existe uma solicitação pendente para este orientador.");
        }
    }
}
```

#### Injeção e Execução no `SolicitacaoService`

```java
@Service
public class SolicitacaoService {

    private final List<ValidadorSolicitacaoStrategy> validadores;
    private final SolicitacaoRepository solicitacaoRepository;

    public SolicitacaoService(List<ValidadorSolicitacaoStrategy> validadores,
                              SolicitacaoRepository solicitacaoRepository) {
        this.validadores = validadores;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO dto) {
        // 1. Busca ou instancia Aluno e Orientador
        Aluno aluno = obterOuCriarAluno(dto.aluno());
        Orientador orientador = buscarOrientador(dto.orientadorId());

        // 2. Monta o contexto e itera sobre todas as estratégias injetadas
        SolicitacaoContexto contexto = new SolicitacaoContexto(aluno, orientador, dto.tema(), dto.mensagem());
        validadores.forEach(validador -> validador.validar(contexto));

        // 3. Persistência da solicitação no banco
        SolicitacaoOrientacao solicitacao = new SolicitacaoOrientacao(aluno, orientador, dto.tema(), dto.mensagem());
        solicitacaoRepository.save(solicitacao);

        return SolicitacaoMapper.toResponseDTO(solicitacao);
    }
}
```

### 2.5 Testabilidade Unitária Isolada (Sem Contexto Spring)

O padrão Strategy viabiliza testes unitários rápidos e 100% isolados utilizando apenas JUnit 5 e Mockito:

```java
@ExtendWith(MockitoExtension.class)
class ValidacaoVagasDisponiveisStrategyTest {

    private final ValidacaoVagasDisponiveisStrategy strategy = new ValidacaoVagasDisponiveisStrategy();

    @Test
    void deveLancarExcecaoQuandoVagasForemZero() {
        Orientador orientador = new Orientador();
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(0);
        orientador.setPerfil(perfil);

        SolicitacaoContexto contexto = new SolicitacaoContexto(new Aluno(), orientador, "Tema Teste", "Msg");

        assertThrows(VagasIndisponiveisException.class, () -> strategy.validar(contexto));
    }

    @Test
    void devePassarComSucessoQuandoPossuirVagas() {
        Orientador orientador = new Orientador();
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(3);
        orientador.setPerfil(perfil);

        SolicitacaoContexto contexto = new SolicitacaoContexto(new Aluno(), orientador, "Tema Teste", "Msg");

        assertDoesNotThrow(() -> strategy.validar(contexto));
    }
}
```

---

## 3. Padrão Observer (Transição de Status e Efeitos Colaterais)

### 3.1 Problema Resolvido

No endpoint `PATCH /api/v1/solicitacoes/{id}/status`, o orientador atualiza o estado da solicitação para `ACEITA` ou `RECUSADA`.
Ao transitar para `ACEITA`:

- A cota de vagas do orientador deve ser decrementada em 1;
- O registro de auditoria da decisão deve ser persistido;
- Futuramente, notificações podem ser acopladas.

Se essas operações forem chamadas diretamente no método de negócio (`perfilService.decrementarVaga(id)`), cria-se alto acoplamento e risco de inconsistência caso a persistência da solicitação falhe após o decremento.

O padrão **Observer** resolve essa questão publicando o evento de domínio `SolicitacaoStatusChangedEvent`. O Spring Boot distribui o evento para os observadores (ouvintes/listeners) cadastrados.

### 3.2 Diagrama de Classes e Sequência (Observer)

```mermaid
classDiagram
    direction TB

    class SolicitacaoService {
        -ApplicationEventPublisher eventPublisher
        -SolicitacaoRepository solicitacaoRepository
        +atualizarStatus(Long id, StatusUpdateDTO dto) SolicitacaoResponseDTO
    }

    class SolicitacaoStatusChangedEvent {
        <<record>>
        -SolicitacaoOrientacao solicitacao
        -Orientador orientador
        -StatusSolicitacao statusAnterior
        -StatusSolicitacao statusNovo
        -String justificativa
        +solicitacao() SolicitacaoOrientacao
        +orientador() Orientador
        +statusNovo() StatusSolicitacao
    }

    class AtualizadorVagasObserver {
        -OrientadorRepository orientadorRepository
        +aoMudarStatus(SolicitacaoStatusChangedEvent evento) void
    }

    class LogAuditoriaObserver {
        -Logger log
        +aoMudarStatus(SolicitacaoStatusChangedEvent evento) void
    }

    SolicitacaoService ..> SolicitacaoStatusChangedEvent : publica via ApplicationEventPublisher
    AtualizadorVagasObserver ..> SolicitacaoStatusChangedEvent : assina (@EventListener)
    LogAuditoriaObserver ..> SolicitacaoStatusChangedEvent : assina (@EventListener)
```

```mermaid
sequenceDiagram
    autonumber
    actor Orientador as Docente / Orientador
    participant Controller as SolicitacaoController
    participant Service as SolicitacaoService (Publisher)
    participant Publisher as ApplicationEventPublisher
    participant ObsVagas as AtualizadorVagasObserver
    participant ObsLog as LogAuditoriaObserver
    participant DB as Banco de Dados (MySQL)

    Orientador ->> Controller: PATCH /api/v1/solicitacoes/1/status { status: "ACEITA" }
    Controller ->> Service: atualizarStatus(1, dto)

    rect rgb(240, 253, 244)
        note over Service, DB: Início da Transação (@Transactional)
        Service ->> DB: UPDATE solicitacoes SET status = 'ACEITA'
        Service ->> Publisher: publishEvent(SolicitacaoStatusChangedEvent)

        Publisher ->> ObsVagas: aoMudarStatus(evento)
        ObsVagas ->> DB: UPDATE perfis_professores SET vagas_disponiveis = vagas - 1

        Publisher ->> ObsLog: aoMudarStatus(evento)
        ObsLog ->> DB: Persiste log de auditoria / emite log INFO
        note over Service, DB: Commit Atômico da Transação
    end

    Service -->> Controller: SolicitacaoResponseDTO
    Controller -->> Orientador: HTTP 200 OK
```

### 3.3 Estrutura de Pacotes

Os componentes devem ser organizados em:

```
br.edu.ifsc.gestao_tcc.observer/
├── SolicitacaoStatusChangedEvent.java   (Record do Evento de Domínio)
├── AtualizadorVagasObserver.java        (Listener síncrono transacional de vagas)
└── LogAuditoriaObserver.java            (Listener de auditoria e rastreabilidade)
```

### 3.4 Contratos e Implementação

#### Evento de Domínio (`SolicitacaoStatusChangedEvent.java`)

```java
package br.edu.ifsc.gestao_tcc.observer;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;

public record SolicitacaoStatusChangedEvent(
    SolicitacaoOrientacao solicitacao,
    Orientador orientador,
    StatusSolicitacao statusAnterior,
    StatusSolicitacao statusNovo,
    String justificativa
) {}
```

#### Observador de Vagas com Transacionalidade Estrita (`AtualizadorVagasObserver.java`)

```java
package br.edu.ifsc.gestao_tcc.observer;

import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AtualizadorVagasObserver {

    private final OrientadorRepository orientadorRepository;

    public AtualizadorVagasObserver(OrientadorRepository orientadorRepository) {
        this.orientadorRepository = orientadorRepository;
    }

    /**
     * Executa de forma síncrona dentro da mesma transação do PATCH de status.
     * Caso o decremento falhe, a transação inteira sofre rollback conjunto.
     */
    @EventListener
    public void aoMudarStatus(SolicitacaoStatusChangedEvent evento) {
        if (evento.statusNovo() == StatusSolicitacao.ACEITA) {
            var orientador = evento.orientador();
            var perfil = orientador.getPerfil();

            if (perfil != null && perfil.getVagasDisponiveis() > 0) {
                perfil.setVagasDisponiveis(perfil.getVagasDisponiveis() - 1);
                orientadorRepository.save(orientador);
            }
        }
    }
}
```

#### Observador de Auditoria (`LogAuditoriaObserver.java`)

```java
package br.edu.ifsc.gestao_tcc.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LogAuditoriaObserver {

    private static final Logger log = LoggerFactory.getLogger(LogAuditoriaObserver.class);

    @EventListener
    public void aoMudarStatus(SolicitacaoStatusChangedEvent evento) {
        log.info("[AUDITORIA_TCC] Solicitacao ID={} transicionou de {} para {}. Orientador ID={}, Justificativa={}",
            evento.solicitacao().getId(),
            evento.statusAnterior(),
            evento.statusNovo(),
            evento.orientador().getId(),
            evento.justificativa() != null ? evento.justificativa() : "N/A"
        );
    }
}
```

#### Publicação no `SolicitacaoService`

```java
@Transactional
public SolicitacaoResponseDTO atualizarStatus(Long id, StatusUpdateDTO dto) {
    SolicitacaoOrientacao solicitacao = solicitacaoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

    if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
        throw new SolicitacaoJaRespondidaException("A solicitação já foi respondida anteriormente.");
    }

    StatusSolicitacao statusAnterior = solicitacao.getStatus();
    StatusSolicitacao statusNovo = StatusSolicitacao.valueOf(dto.status());

    solicitacao.setStatus(statusNovo);
    if (dto.justificativa() != null) {
        solicitacao.setJustificativaRecusa(dto.justificativa());
    }
    solicitacaoRepository.save(solicitacao);

    // Dispara evento síncrono para os observers
    eventPublisher.publishEvent(new SolicitacaoStatusChangedEvent(
        solicitacao,
        solicitacao.getOrientador(),
        statusAnterior,
        statusNovo,
        dto.justificativa()
    ));

    return SolicitacaoMapper.toResponseDTO(solicitacao);
}
```

---

## 4. Matriz de Trade-offs e Boas Práticas

| Padrão       | Vantagens Arquiteturais                                                                                                                                      | Desvantagens / Trade-offs                                                                            | Mitigação Adotada                                                                                                                                                            |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Strategy** | - Respeito integral ao OCP e SRP.<br>- Adição de regras via novas classes `@Component`.<br>- Testes unitários com JUnit 5 puros.                             | - Maior quantidade de classes e interfaces no pacote.                                                | Organização em pacote coeso (`strategy`) com ordenação clara via `@Order`.                                                                                                   |
| **Observer** | - Desacoplamento entre o fluxo principal da solicitação e subsistemas secundários.<br>- Facilidade para plugar novos ouvintes no futuro (e-mails, webhooks). | - Risco de perda de atomicidade se os ouvintes rodarem de forma assíncrona desacoplada da transação. | Uso de **`@EventListener` síncrono transacional**, garantindo que a atualização da solicitação e o decremento de vaga ocorram na mesma transação atômica (`@Transactional`). |

---

## 5. Rastreabilidade com Backlog e Contratos

Este design arquitetural atende e orienta diretamente as seguintes entregas da Sprint 2:

- **Issue #11 (US04 - Solicitação e Aceite de Orientação):** História de usuário macro;
- **Issue #91 (Doc-1 - ADR-0007 e ADR-0008):** Formalização das decisões arquiteturais;
- **Issue #92 (Doc-2 - docs/PADROES-DE-PROJETO.md):** Esta especificação técnica;
- **Issue #83 (`[Backend] Validações (Strategy) + POST /solicitacoes`):** Implementação das classes do pacote `strategy`;
- **Issue #84 (`[Backend] Aceitar/Recusar (Observer) + listagem de pendentes`):** Implementação das classes do pacote `observer`.

---

## 6. Referências

- Gamma, E., Helm, R., Johnson, R., Vlissides, J. (1994). _Design Patterns: Elements of Reusable Object-Oriented Software_. Addison-Wesley.
- Refactoring Guru: [Strategy Pattern](https://refactoring.guru/design-patterns/strategy) e [Observer Pattern](https://refactoring.guru/design-patterns/observer).
- Spring Framework Documentation: [Application Events and Listeners](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events).
- Decisão Arquitetural: [`docs/adrs/ADR-0007.md`](./adrs/ADR-0007.md).
- Contrato de Dados da API: [`docs/contrato-dados-us04.md`](./contrato-dados-us04.md).
