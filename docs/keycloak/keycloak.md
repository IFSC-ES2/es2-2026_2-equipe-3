# Keycloak: Realm, Clients e Roles do SIGTCC

Este documento descreve a configuração de autenticação do SIGTCC no Keycloak e como reproduzi-la no ambiente de desenvolvimento. A configuração é versionada em [`SIGTCC/keycloak/sigtcc-realm.json`](../SIGTCC/keycloak/sigtcc-realm.json) e importada automaticamente quando o contêiner sobe.

> **Somente desenvolvimento.** O arquivo do realm contém usuários e senhas de teste e URLs de `localhost`. Ele não deve ser usado em produção.

---

## 1. O que está configurado

### 1.1 Realm

| Propriedade        | Valor                                  |
| :----------------- | :------------------------------------- |
| Nome               | `sigtcc`                               |
| Login              | E-mail e senha (o e-mail é o username) |
| Autocadastro       | Desabilitado                           |
| Idioma             | Português (`pt-BR`)                    |
| Emissor dos tokens | `http://localhost:8081/realms/sigtcc`  |

### 1.2 Clients

| Client            | Tipo                                      | Uso                                                                                                                                               |
| :---------------- | :---------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------ |
| `sigtcc-frontend` | Público, Authorization Code + PKCE (S256) | Usado pelo React para redirecionar o usuário ao login. Aceita redirecionamentos e origens de `http://localhost:5173`.                             |
| `sigtcc-backend`  | Confidencial, sem fluxos de login         | Representa a API do Spring Boot como audiência (`aud`) dos tokens. O backend só valida tokens, por isso nenhum segredo é versionado ou utilizado. |

O client `sigtcc-frontend` possui um mapeador de audiência que inclui `sigtcc-backend` no campo `aud` do token de acesso.

### 1.3 Roles

Roles de realm, publicadas no token de acesso em `realm_access.roles`:

| Role             | Quem recebe                          |
| :--------------- | :----------------------------------- |
| `ROLE_ALUNO`     | Alunos de TCC.                       |
| `ROLE_PROFESSOR` | Qualquer docente, orientador ou não. |

Não existe role de coordenador. Conforme a seção 1.1 do [relatório de refatoração de escopo](refatoracao-escopo-sprint-3.md), a permissão de gerir a turma e o calendário é verificada no banco de dados, checando se o professor autenticado é o responsável pela `Turma_TCC` do semestre vigente.

### 1.4 Usuários de teste

| E-mail (login)                      | Senha          | Role             | Papel no sistema                                |
| :---------------------------------- | :------------- | :--------------- | :---------------------------------------------- |
| `aluno@example.com`                 | `aluno123`     | `ROLE_ALUNO`     | Aluno.                                          |
| `professor.orientador@example.com`  | `professor123` | `ROLE_PROFESSOR` | Professor apenas orientador.                    |
| `professor.responsavel@example.com` | `professor123` | `ROLE_PROFESSOR` | Professor responsável pela `Turma_TCC` vigente. |

Os dois professores são idênticos no Keycloak. O que distingue o responsável é o vínculo com a `Turma_TCC` no banco de dados: os dados de desenvolvimento da entidade `Turma_TCC` devem apontar para o usuário de e-mail `professor.responsavel@example.com`.

---

## 2. Passo a passo

### 2.1 Subir o ambiente

Na pasta `SIGTCC/`:

```bash
docker compose up
```

O serviço `keycloak` inicia com `--import-realm` e importa todos os arquivos `.json` da pasta `SIGTCC/keycloak/`. Um realm só é importado se ainda não existir; se já existir, o arquivo é ignorado.

### 2.2 Conferir a importação

1. Acesse o console administrativo em `http://localhost:8081` com o usuário administrador definido no `.env` (`KEYCLOAK_ADMIN_USERNAME` e `KEYCLOAK_ADMIN_PASSWORD`).
2. Selecione o realm **sigtcc** no seletor de realms.
3. Em **Clients**, confira `sigtcc-frontend` e `sigtcc-backend`.
4. Em **Realm roles**, confira `ROLE_ALUNO` e `ROLE_PROFESSOR`.
5. Em **Users**, confira os três usuários de teste.

### 2.3 Conferir as roles no token

1. Em **Clients**, abra `sigtcc-frontend`.
2. Na aba **Client scopes**, abra **Evaluate**.
3. No campo **Users**, selecione um usuário de teste.
4. Abra **Generated access token**.

O token deve conter a role do usuário em `realm_access.roles` e `sigtcc-backend` em `aud`.

### 2.4 Alterar a configuração

O arquivo `sigtcc-realm.json` é a fonte da verdade. Para alterar o realm:

1. Edite o arquivo `SIGTCC/keycloak/sigtcc-realm.json`.
2. No console administrativo, com o realm **sigtcc** selecionado, abra **Realm settings** e use **Action > Delete** para remover o realm atual.
3. Reinicie o serviço para reimportar:

   ```bash
   docker compose restart keycloak
   ```

Alterações feitas apenas pelo console ficam no volume local de quem as fez e não chegam aos outros integrantes. Para compartilhar uma alteração, ela precisa estar no arquivo.

> Evite `docker compose down -v` para reimportar o realm: o comando também apaga o volume do MySQL.

---

## 3. Referência para a integração

Valores que o backend e o frontend usarão ao integrar com o Keycloak:

| Item                                              | Valor                                                              |
| :------------------------------------------------ | :----------------------------------------------------------------- |
| URL do Keycloak (navegador)                       | `http://localhost:8081`                                            |
| Realm                                             | `sigtcc`                                                           |
| Client do frontend                                | `sigtcc-frontend` (PKCE com `S256` obrigatório)                    |
| Emissor (`iss`)                                   | `http://localhost:8081/realms/sigtcc`                              |
| Audiência (`aud`)                                 | `sigtcc-backend`                                                   |
| Chaves públicas, a partir do contêiner do backend | `http://keycloak:8080/realms/sigtcc/protocol/openid-connect/certs` |
| Local das roles no token                          | `realm_access.roles`                                               |

De dentro do contêiner, o backend não alcança `localhost:8081`. Por isso a validação do emissor usa o endereço do navegador e a busca das chaves usa o nome do serviço (`keycloak:8080`).
