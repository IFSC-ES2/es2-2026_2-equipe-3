# Diagrama de Arquitetura: Visão de Contêineres (C2)

Este documento apresenta a **Visão de Contêineres (Nível 2 do modelo C4)** do **Sistema de Gestão Centralizada de TCC (SIGTCC)**, ilustrando as aplicações executáveis, armazenamentos de dados, protocolos de comunicação e como as fronteiras arquiteturais se organizam em tempo de execução no ambiente Docker.

> **Marco:** `v1.0.0-rc.1` (Entrega 9 – Release Candidate), contemplando as histórias US01 a US07.

---

## 1. Diagrama de Contêineres (C2)

```mermaid
flowchart TB
    classDef actor fill:#1e40af,stroke:#1d4ed8,stroke-width:2px,color:#ffffff;
    classDef tunnel fill:#334155,stroke:#1e293b,stroke-width:2px,color:#ffffff;
    classDef front fill:#0284c7,stroke:#0369a1,stroke-width:2px,color:#ffffff;
    classDef back fill:#0369a1,stroke:#075985,stroke-width:2px,color:#ffffff;
    classDef db fill:#0f766e,stroke:#115e59,stroke-width:2px,color:#ffffff;
    classDef iam fill:#6d28d9,stroke:#5b21b6,stroke-width:2px,color:#ffffff;
    classDef cloud fill:#475569,stroke:#334155,stroke-width:2px,color:#ffffff;

    subgraph Atores["Usuários do Sistema"]
        aluno["<b>Aluno</b><br/>Navegador Web"]:::actor
        professor["<b>Professor / Orientador</b><br/>Navegador Web"]:::actor
    end

    subgraph Edge["Exposição Externa (Ambiente de Staging)"]
        funnel["<b>Tailscale Funnel</b><br/>Proxy Reverso TLS Fixo<br/><i>:443 → :80 | :8443 → :8080 | :10000 → :8081</i>"]:::tunnel
    end

    subgraph Docker["Ambiente Docker Compose (Dev Local: HTTP | Staging: Nginx :80)"]
        subgraph FrontCont["Camada de Apresentação"]
            frontend["<b>Frontend SPA</b><br/>React 19.2 + TypeScript + Vite / Nginx<br/><i>Dev :5173 | Prod/Staging :80</i>"]:::front
        end

        subgraph BackCont["Camada de Serviços e Negócio"]
            backend["<b>Backend API (Resource Server)</b><br/>Spring Boot 4.1.1 + Java 21<br/><i>Porta 8080</i>"]:::back
        end

        subgraph DbCont["Persistência Relacional"]
            db[("<b>Banco de Dados MySQL 8.0</b><br/>Migrações Flyway (V1 a V9)<br/><i>Porta 3306</i>")]:::db
        end

        subgraph IamCont["Segurança e Identidade"]
            keycloak["<b>Keycloak 26.8.0 (IAM)</b><br/>Realm sigtcc / JWT RS256<br/><i>Porta 8081 (interna 8080)</i>"]:::iam
        end
    end

    subgraph NuvemExt["Serviços Externos"]
        nuvem["<b>Plataformas em Nuvem</b><br/>Google Drive / OneDrive / GitHub"]:::cloud
    end

    aluno -->|"Acessa SPA [HTTPS via Funnel em Staging / HTTP em Dev]"| funnel
    professor -->|"Acessa SPA [HTTPS via Funnel em Staging / HTTP em Dev]"| funnel
    funnel -->|"Encaminha tráfego local [HTTP]"| frontend
    funnel <-->|"Token ROPC [HTTP local]"| keycloak
    funnel -->|"API REST [HTTP local]"| backend

    frontend <-->|"Login via Direct Access Grant (ROPC) [grant_type=password]"| keycloak
    frontend -->|"Requisições REST [Bearer JWT]"| backend

    backend <-->|"Valida JWT localmente via JWKS [/certs]"| keycloak
    backend <-->|"Persistência transacional [TCP / JDBC :3306]"| db

    aluno -.->|"Hospeda documento e gera link [HTTPS]"| nuvem
    professor -.->|"Acessa link do trabalho submetido [HTTPS]"| nuvem
```

---

## 2. Detalhamento dos Contêineres e Túnel de Staging

| Componente           | Tecnologias Principais                                       | Portas (Dev / Staging)         | Responsabilidade Principal                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| :------------------- | :----------------------------------------------------------- | :----------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Tailscale Funnel** | Tailscale Funnel (Proxy Reverso TLS)                         | `443`, `8443`, `10000` (HTTPS) | Expõe de forma fixa e segura (HTTPS) os serviços de Frontend (`:80`), Backend (`:8080`) e Keycloak (`:8081`) no ambiente de _staging_ (`docker-compose.prod.yml`), substituindo os túneis efêmeros anteriores.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **Frontend SPA**     | React 19.2, TypeScript, Vite (Dev) / Nginx 1.27 (Prod)       | `5173:5173` / `80:80`          | Interface de usuário do tipo Single-Page Application. Gerencia rotas protegidas por papel (`ROLE_ALUNO`, `ROLE_PROFESSOR`) via `ProtectedRoute.tsx`, formulário de login reativo próprio (`paginas/Login/index.tsx`) gerenciado por `AuthContext.tsx` via Direct Access Grant ([ADR-0014](../adrs/ADR-0014.md)) e comunicação com a API REST anexando o token JWT no cabeçalho `Authorization: Bearer <token>` através do cliente HTTP centralizado (`api.ts`), com renovação automática via `refresh_token` (`authService.ts`) quando o token expira ou a API responde `401`, e encerramento da sessão com redirecionamento para `/login` se a renovação falhar.                                                                                                                                                                       |
| **Backend API**      | Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA | `8080:8080`                    | Camada de serviços e regras de negócio. Configurado como **OAuth2 Resource Server** ([ADR-0012](../adrs/ADR-0012.md)), validando assinaturas e claims dos tokens JWT emitidos pelo Keycloak em memória via conjunto de chaves públicas (`JWKS`). Aplica regras desacopladas de elegibilidade com _Strategy_ e atualização de vagas com _Observer_ ([ADR-0007](../adrs/ADR-0007.md)), além de controlar a vigência de turmas ([ADR-0010](../adrs/ADR-0010.md)). Também implementa a submissão de entregas por link externo validado com `@URL(protocol = "https")`, com cálculo da flag `atrasada` e reenvio em `AJUSTES_NECESSARIOS` (US06), e o registro de pareceres do orientador vinculado (US07), conforme a [ADR-0013](../adrs/ADR-0013.md) e os contratos [US06](../contrato-dados-us06.md) e [US07](../contrato-dados-us07.md). |
| **Banco de Dados**   | MySQL 8.0                                                    | `3306:3306` (Dev) / Interna    | Persistência relacional com evolução de esquema versionada via migrações Flyway (`V1` a `V9`). Armazena tabelas `usuarios`, `orientadores`, `perfis_professores` (com coluna `versao` para bloqueio otimista), `linhas_pesquisa`, `alunos`, `solicitacoes_orientacao`, `turmas_tcc`, `prazos_entrega`, `entregas`, `pareceres` e carga inicial de dados via seed.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Keycloak (IAM)**   | Keycloak 26.8.0                                              | `8081:8080`                    | Provedor de Identidade centralizado ([ADR-0011](../adrs/ADR-0011.md), [ADR-0014](../adrs/ADR-0014.md)). Executado com `--import-realm` carregando as definições de `SIGTCC/keycloak/realm-import.json` (com `webOrigins` restrito em staging). Gerencia usuários, credenciais seguras e emite tokens de acesso no formato JWT contendo os papéis `ROLE_ALUNO` e `ROLE_PROFESSOR`.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |

---

## 3. Protocolos e Integrações entre Contêineres

1. **Autenticação e Obtenção de Token (Frontend ↔ Keycloak):**
   - O usuário submete suas credenciais diretamente no formulário de login in-app da SPA (`Login/index.tsx`), gerenciado pelo `AuthContext.tsx`.
   - A aplicação envia uma requisição `POST` com `grant_type=password` ao endpoint `/protocol/openid-connect/token` do Keycloak (Direct Access Grant / ROPC conforme [ADR-0014](../adrs/ADR-0014.md)).
   - Após validação das credenciais com sucesso, o Keycloak retorna o token de acesso (JWT) contendo o claim `realm_access.roles`. O token é gerenciado na sessão da aplicação e propagado automaticamente nas requisições pelo cliente HTTP centralizado (`api.ts`), com permissões de acesso validadas por `ProtectedRoute.tsx`.
2. **Requisições de Domínio (Frontend ↔ Backend):**
   - As chamadas aos endpoints REST (`/api/v1/**`) utilizam HTTP em desenvolvimento local ou HTTPS via Tailscale Funnel em _staging_, transportando payloads JSON e o cabeçalho padrão `Authorization: Bearer <token_jwt>`.
3. **Validação de Token sem Estado (Backend ↔ Keycloak):**
   - Por inicialização adiada ([ADR-0012](../adrs/ADR-0012.md)), o backend **não** contata o Keycloak na subida do contêiner; na primeira requisição autenticada, consulta o endpoint de certificados do Keycloak (`http://keycloak:8080/realms/sigtcc/protocol/openid-connect/certs`) e armazena em cache o conjunto de chaves públicas (JWKS).
   - As requisições subsequentes são validadas **localmente em memória** pelo Spring Security, checando assinatura criptográfica, emissor (`iss`), expiração (`exp`) e audiência (`aud`), sem realizar chamadas de rede adicionais ao Keycloak a cada requisição.
4. **Persistência de Dados (Backend ↔ Banco de Dados):**
   - Comunicação via JDBC através do pool de conexões padrão HikariCP gerenciado pelo Spring Boot, conectando-se ao serviço MySQL interno da rede Docker na porta padrão 3306.
