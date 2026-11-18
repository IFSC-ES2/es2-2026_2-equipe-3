# Diagrama de Arquitetura: Visão de Componentes do Backend (C3)

Este documento apresenta a **Visão de Componentes (Nível 3 do modelo C4)** do contêiner **Backend API (Spring Boot 4.1.1 / Java 21)** do **SIGTCC**, detalhando os pacotes reais implementados no módulo `br.edu.ifsc.gestao_tcc` e suas interações.

> **Marco:** `v1.0.0-rc.1` (Entrega 9 – Release Candidate).

---

## 1. Diagrama de Componentes (C3)

```mermaid
flowchart TB
    classDef client fill:#1e40af,stroke:#1d4ed8,stroke-width:2px,color:#ffffff;
    classDef sec fill:#6d28d9,stroke:#5b21b6,stroke-width:2px,color:#ffffff;
    classDef web fill:#0284c7,stroke:#0369a1,stroke-width:2px,color:#ffffff;
    classDef biz fill:#0369a1,stroke:#075985,stroke-width:2px,color:#ffffff;
    classDef pat fill:#0e7490,stroke:#155e75,stroke-width:2px,color:#ffffff;
    classDef data fill:#0f766e,stroke:#115e59,stroke-width:2px,color:#ffffff;
    classDef ext fill:#475569,stroke:#334155,stroke-width:2px,color:#ffffff;

    spa["<b>Frontend SPA (React)</b><br/>Requisições REST + Bearer JWT"]:::client
    kc["<b>Keycloak (IAM)</b><br/>Endpoint JWKS (/certs)"]:::ext
    mysql[("<b>MySQL 8.0</b><br/>Tabelas gerenciadas via Flyway (V1–V9)")]:::ext

    subgraph Backend["Contêiner Backend API (br.edu.ifsc.gestao_tcc)"]
        config["<b>config</b><br/>SecurityConfig, KeycloakRealmRoleConverter, ClockConfig"]:::sec
        controllers["<b>controller</b><br/>OrientadorController, SolicitacaoController,<br/>PerfilProfessorController, PrazoEntregaController, EntregaController"]:::web
        ex["<b>exception</b><br/>GlobalExceptionHandler e exceções de domínio"]:::web
        services["<b>service</b><br/>OrientadorService, SolicitacaoService,<br/>PerfilProfessorService, PrazoEntregaService, EntregaService"]:::biz
        validation["<b>validation (Strategy)</b><br/>ValidacaoSolicitacaoOrientacao e<br/>3 implementações (@Component)"]:::pat
        eventobs["<b>event & observer (Observer)</b><br/>SolicitacaoStatusChangedEvent e<br/>AtualizadorVagasObserver"]:::pat
        repos["<b>repository & model</b><br/>JpaRepositories e Entidades JPA"]:::data
    end

    spa -->|"JSON / Bearer JWT"| config
    config -->|"Validação local JWT / JWKS"| kc
    config -->|"Requisição autorizada"| controllers
    controllers -.->|"Exceções mapeadas para ErrorResponseDTO"| ex
    ex -.->|"Usa Clock de ClockConfig"| config
    controllers -->|"DTOs"| services
    services -->|"Valida elegibilidade (US04)"| validation
    services -->|"Publica evento síncrono"| eventobs
    services -->|"Operações transacionais"| repos
    eventobs -->|"Atualiza vagas em PerfilOrientador (@Version)"| repos
    repos -->|"JDBC / Hibernate (validate)"| mysql
```

---

## 2. Mapeamento de Pacotes e Classes Reais

| Pacote                              | Classes Principais                                                                                                                                                                      | Responsabilidade Arquitetural                                                                                              |
| :---------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------- |
| `br.edu.ifsc.gestao_tcc.config`     | `SecurityConfig`, `KeycloakRealmRoleConverter`, `ClockConfig`                                                                                                                           | Resource Server OAuth2/JWT, regras de autorização por papel (`ROLE_ALUNO`, `ROLE_PROFESSOR`), CORS e `Clock` institucional |
| `br.edu.ifsc.gestao_tcc.controller` | `OrientadorController`, `SolicitacaoController`, `PerfilProfessorController`, `PrazoEntregaController`, `EntregaController`                                                             | Exposição dos endpoints REST `/api/v1/**`, validação de entrada (`@Valid`) e códigos HTTP                                  |
| `br.edu.ifsc.gestao_tcc.dto`        | `OrientadorRequestDTO`, `SolicitacaoRequestDTO`, `EntregaRequestDTO`, `ParecerRequestDTO`, `ErrorResponseDTO`, `ErroCampoDTO`, entre outros                                             | Contratos de entrada e saída da API REST, desacoplados das entidades JPA                                                   |
| `br.edu.ifsc.gestao_tcc.exception`  | `GlobalExceptionHandler`, `RegraDeNegocioException`, `AcessoNegadoException`, `ResourceNotFoundException`, `EmailDuplicadoException`, etc.                                              | Tratamento centralizado de exceções de validação, negócio, segurança, concorrência (`409`) e erros genéricos (`500`)       |
| `br.edu.ifsc.gestao_tcc.service`    | `OrientadorService`, `SolicitacaoService`, `PerfilProfessorService`, `PrazoEntregaService`, `EntregaService`                                                                            | Regras de negócio, controle de propriedade/vínculo e delimitação transacional (`@Transactional`)                           |
| `br.edu.ifsc.gestao_tcc.validation` | `ValidacaoSolicitacaoOrientacao`, `ValidacaoVagasDisponiveis`, `ValidacaoOrientadorAtivo`, `ValidacaoSolicitacaoDuplicada`                                                              | Padrão **Strategy** para validação polimórfica de elegibilidade de solicitações                                            |
| `br.edu.ifsc.gestao_tcc.event`      | `SolicitacaoStatusChangedEvent`                                                                                                                                                         | Record de evento de domínio emitido na mudança de status de uma solicitação                                                |
| `br.edu.ifsc.gestao_tcc.observer`   | `AtualizadorVagasObserver`                                                                                                                                                              | Padrão **Observer** (`@EventListener` síncrono) para decremento transacional de vagas                                      |
| `br.edu.ifsc.gestao_tcc.repository` | `UsuarioRepository`, `OrientadorRepository`, `AlunoRepository`, `SolicitacaoRepository`, `TurmaTCCRepository`, `PrazoEntregaRepository`, `EntregaRepository`, `ParecerRepository`, etc. | Persistência via Spring Data JPA e travas (`PESSIMISTIC_WRITE` em entregas/pareceres)                                      |
| `br.edu.ifsc.gestao_tcc.model`      | `Usuario`, `Orientador`, `PerfilOrientador` (`@Version`), `LinhaPesquisa`, `Aluno`, `SolicitacaoOrientacao`, `TurmaTCC`, `PrazoEntrega`, `Entrega`, `Parecer`                           | Modelo de domínio orientado a objetos mapeado para tabelas relacionais MySQL                                               |
