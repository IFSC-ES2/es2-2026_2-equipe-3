# Ambiente de Staging Estável (Issues #220 e #260)

Staging roda o mesmo stack de produção (`SIGTCC/docker-compose.prod.yml`: MySQL, Keycloak, backend Spring Boot e frontend React) em uma máquina da equipe, exposto por URLs públicas e fixas através do **Tailscale Funnel**. O ambiente permanece no ar para avaliação contínua até a apresentação final e a defesa técnica.

## 1. URLs Fixas do Ambiente

| Serviço             | URL Pública Fixa                               | Porta Local |
| :------------------ | :--------------------------------------------- | :---------- |
| **Frontend**        | `https://sigtcc.cobbler-albacore.ts.net`       | `80`        |
| **Backend (API)**   | `https://sigtcc.cobbler-albacore.ts.net:8443`  | `8080`      |
| **Keycloak (Auth)** | `https://sigtcc.cobbler-albacore.ts.net:10000` | `8081`      |

## 2. Segurança e Web Origins Restrito (Critério #260)

O client `sigtcc-frontend` em `SIGTCC/keycloak/realm-import.json` **não utiliza mais** o wildcard `*`. As origens de CORS (`webOrigins`) e URLs de redirecionamento estão estritamente restritas a:

- `https://sigtcc.cobbler-albacore.ts.net` (ambiente de staging oficial)
- `http://localhost:5173` e `http://localhost` (desenvolvimento local)

## 3. Responsável pela Disponibilidade

- **Responsável:** Eduardo Cardoso (Equipe 3)
- **Disponibilidade acordada:** O ambiente permanece ativo e acessível até a conclusão da Entrega 10 (Apresentação Final) e Entrega 11 (Defesa do Trabalho).

## 4. Procedimento de Reinício

Caso o computador anfitrião seja reiniciado ou o ambiente caia, o procedimento de restabelecimento é:

1. **Garantir que Docker Desktop e Tailscale estão abertos e conectados.**
2. **Conferir o status do Funnel do Tailscale:**
   ```bash
   tailscale funnel status
   ```
   Caso as rotas precisem ser reativadas em segundo plano:
   ```bash
   tailscale funnel --bg --yes 80
   tailscale funnel --bg --yes --https=8443 8080
   tailscale funnel --bg --yes --https=10000 8081
   ```
3. **Subir a aplicação a partir de `SIGTCC/`:**

   ```bash
   cd SIGTCC
   docker compose -f docker-compose.prod.yml --env-file .env up -d --build
   ```

   _(Caso tenha havido qualquer alteração no `realm-import.json`, rode `docker compose -f docker-compose.prod.yml down -v` antes de subir para recriar o volume do Keycloak)._

4. **Validar o ambiente com o Smoke Test:**
   A partir da raiz do repositório (usando Git Bash no Windows ou terminal Linux/macOS):
   ```bash
   KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 bash SIGTCC/scripts/smoke-test.sh
   ```
   O script deve completar todas as verificações com código `0`.

## 5. Credenciais de Teste

Os usuários de teste pré-cadastrados no realm (`sigtcc`) para homologação e demonstração são:

| Papel               | E-mail / Usuário          | Senha            | Finalidade de Teste                                                                 |
| :------------------ | :------------------------ | :--------------- | :---------------------------------------------------------------------------------- |
| **Aluno**           | `aluno@example.com`       | `aluno123`       | Solicitar orientação e submeter entrega por link HTTPS (US06)                       |
| **Orientador**      | `orientador@example.com`  | `orientador123`  | Aceitar solicitações e emitir parecer com avaliação (US07)                          |
| **Responsável TCC** | `responsavel@example.com` | `resp123`        | Cadastrar prazos e gerenciar configurações de turma                                 |
| **Admin Keycloak**  | `admin`                   | `admin_password` | Console administrativo do Keycloak (`https://sigtcc.cobbler-albacore.ts.net:10000`) |

## 6. Histórico de Execução e Validação

- O fluxo completo de autenticação OAuth2/OIDC, controle de concorrência, submissão de entregas e emissão de pareceres foi testado e homologado contra os endpoints HTTPS do Tailscale Funnel através do script `SIGTCC/scripts/smoke-test.sh` (22 verificações bem-sucedidas).
