# Backend - POS PDV

API RESTful do sistema POS PDV, construida com **Spring Boot 4.0.5** e **Java 25**.

## Pré-requisitos

- Java 25+
- Maven 3.9+ (ou `./mvnw`)
- PostgreSQL 16+

## Executando localmente

1. Copie o template de variaveis de ambiente

```bash
cp .env.example .env
```

2. Ajuste os valores no `.env` conforme seu ambiente local

3. Rode a aplicacao

```bash
./mvnw spring-boot:run
```

Ou com o JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/backend-*.jar
```

A API sobe na porta `8080` (configuravel via `SERVER_PORT`). No profile `local` o contexto root e `/`, em `prod` e `/pospdv`.

## Perfis

| Perfil   | Ativacao                              | Uso                   |
| -------- | ------------------------------------- | --------------------- |
| `local`  | padrão (`SPRING_PROFILES_ACTIVE=local`) | Desenvolvimento local |
| `test`   | automatico nos testes                 | Testes com H2         |
| `prod`   | `SPRING_PROFILES_ACTIVE=prod`         | Producao              |

## Variaveis de ambiente

Todas configuraveis via `.env` (padrao Spring Boot 4.x).

### Obrigatorias

| Variavel                     | Descricao                     | Exemplo                                   |
| ---------------------------- | ----------------------------- | ----------------------------------------- |
| `SPRING_DATASOURCE_URL`      | JDBC URL do banco de dados    | `jdbc:postgresql://localhost:5432/pospdv` |
| `SPRING_DATASOURCE_USERNAME` | Usuario do banco              | `postgres`                                |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco                | `secret`                                  |

### Opcionais

| Variavel                            | Padrao                               | Descricao                        |
| ----------------------------------- | ------------------------------------ | -------------------------------- |
| `SERVER_PORT`                       | `8080`                               | Porta HTTP do servidor           |
| `SPRING_PROFILES_ACTIVE`            | `local`                              | Perfil Spring ativo              |
| `SPRING_FLYWAY_URL`                 | herda de `SPRING_DATASOURCE_URL`     | URL para migracoes Flyway        |
| `SPRING_FLYWAY_USER`                | herda de `SPRING_DATASOURCE_USERNAME`| Usuario para Flyway              |
| `SPRING_FLYWAY_PASSWORD`            | herda de `SPRING_DATASOURCE_PASSWORD`| Senha para Flyway                |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE` | `health,info`                        | Endpoints Actuator expostos      |
| `APP_CORS_ALLOWED_ORIGINS`          | `http://localhost:3000,http://localhost:5173` | Origens permitidas no CORS |
| `JWT_SECRET`                        | *(sem padrao)*                       | Chave JWT em Base64 (min. 256 bits) |
| `JWT_ISSUER`                        | `pospdv`                             | Issuer do JWT                    |
| `JWT_ACCESS_TOKEN_EXPIRATION`       | `3600` (local) / `900` (prod)        | TTL do access token em segundos  |
| `JWT_REFRESH_TOKEN_EXPIRATION`      | `604800` (7 dias)                    | TTL do refresh token em segundos  |
| `SECURITY_REFRESH_COOKIE_SECURE`    | `false` (local) / `true` (prod)      | Flag Secure no cookie            |
| `SECURITY_REFRESH_COOKIE_SAME_SITE` | `Lax` (local) / `None` (prod)        | Atributo SameSite do cookie      |
| `SECURITY_REFRESH_COOKIE_DOMAIN`    | vazio (local) / `.seudominio.com` (prod) | Domínio do cookie (para subdomínios) |

## Autenticacao com Cookies

O refresh token é entregue e recebido **exclusivamente via cookie HttpOnly**, impedindo acesso via JavaScript e protegendo contra ataques XSS.

### Comportamento por perfil

| Perfil | `Secure` | `SameSite` | `Domain`           |
| ------ | -------- | ---------- | ------------------ |
| local  | false    | Lax        | (vazio)            |
| prod   | true     | None       | `${REFRESH_COOKIE_DOMAIN}` |

### Fluxo

1. **Login** → access token no body, refresh token no cookie HttpOnly
2. **Refresh** → lê cookie automaticamente, retorna novo access token + atualiza cookie
3. **Logout** → revoga todos os tokens do usuário, limpa cookie (Max-Age=0)

### Frontend

O frontend deve incluir `credentials: "include"` em todas as requisições fetch/axios para enviar cookies automaticamente.

```js
// Exemplo com fetch
fetch("http://api.exemplo.com/api/v1/auth/login", {
  method: "POST",
  credentials: "include",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ email, password })
});
```

## Endpoints

### Actuator

- `GET /actuator/health` (local) ou `/pospdv/actuator/health` (prod) — liveness/readiness

### Autenticacao

| Metodo | Path                      | Auth | Descricao                            |
| ------ | ------------------------- | ---- | ------------------------------------ |
| POST   | `/api/v1/auth/register`   | Nao  | Cadastrar novo usuario               |
| POST   | `/api/v1/auth/login`      | Nao  | Autenticar e receber tokens          |
| POST   | `/api/v1/auth/refresh`    | Nao  | Renovar access token com refresh token|
| POST   | `/api/v1/auth/logout`     | Nao  | Revogar refresh token (logout)       |

#### Responses

**POST `/login`** — 200 OK
```json
{
  "user": { "id": "...", "email": "user@test.com", "name": "Test User", "provider": "LOCAL" },
  "accessToken": "eyJ...",
  "expiresIn": 604800
}
```
Refresh token entregue via cookie HttpOnly (não visível no body).

**POST `/refresh`** — 200 OK
```json
{
  "accessToken": "eyJ...",
  "expiresIn": 604800
}
```
Lê refresh token do cookie automaticamente, retorna novo access token e atualiza o cookie (rotação).

**POST `/logout`** — 204 No Content
Revoga todos os refresh tokens do usuário e limpa o cookie.

### Usuário (requer JWT)

| Metodo | Path         | Auth | Descricao                 |
| ------ | ------------ | ---- | ------------------------- |
| GET    | `/api/v1/me` | Sim  | Informacoes do usuario logado |

**GET `/api/v1/me`** — 200 OK
```json
{
  "id": "uuid",
  "email": "user@test.com",
  "name": "Test User",
  "roles": ["USER", "ADMIN"]
}
```

### CRUD de Usuarios (requer role ADMIN)

| Metodo | Path                   | Auth | Descricao                              |
| ------ | ---------------------- | ---- | -------------------------------------- |
| GET    | `/api/v1/users`        | Sim  | Listar usuarios com filtros/paginacao  |
| GET    | `/api/v1/users/{id}`   | Sim  | Detalhes de um usuario                 |
| POST   | `/api/v1/users`        | Sim  | Criar novo usuario com roleIds         |
| PUT    | `/api/v1/users/{id}`   | Sim  | Atualizacao completa (roleIds obrig.)  |
| PATCH  | `/api/v1/users/{id}`   | Sim  | Atualizacao parcial                    |
| DELETE | `/api/v1/users/{id}`   | Sim  | Soft delete (desativar usuario)        |

**POST `/api/v1/users`** — 201 Created
```json
{
  "email": "joao@email.com",
  "name": "Joao Silva",
  "password": "senha123",
  "roleIds": ["00000000-0000-0000-0000-000000000001"]
}
```
Response 201:
```json
{
  "id": "uuid",
  "email": "joao@email.com",
  "name": "Joao Silva",
  "provider": "LOCAL",
  "roles": ["USER"],
  "createdAt": "2026-04-06T...",
  "updatedAt": null,
  "active": true
}
```

**PUT `/api/v1/users/{id}`** — 200 OK
Todos os campos obrigatorios exceto `email` e `password`:
```json
{
  "email": "novo@email.com",
  "name": "Nome Atualizado",
  "password": "novaSenha123",
  "roleIds": ["00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002"]
}
```

**PATCH `/api/v1/users/{id}`** — 200 OK
Apenas os campos enviados sao aplicados:
```json
{
  "name": "Nome Parcial",
  "roleIds": ["00000000-0000-0000-0000-000000000002"]
}
```

**GET `/api/v1/users?page=0&size=20`** — 200 OK
Query params opcionais: `name` (parcial), `email` (parcial), `active` (true/false).
```json
{
  "content": [{...}],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

**DELETE `/api/v1/users/{id}`** — 204 No Content
Soft delete — define `deletedAt` no usuario. O usuario nao aparece mais em queries ativas.

### Administrativo (requer role ADMIN)

| Metodo | Path                     | Auth | Descricao                 |
| ------ | ------------------------ | ---- | ------------------------- |
| GET    | `/api/v1/admin/ping`     | Sim  | Endpoint de teste ADMIN   |
| GET    | `/api/v1/admin-only`     | Sim  | Endpoint protegido ADMIN  |

**GET `/api/v1/admin/ping`** — 200 OK
```
Admin area
```

Sem role ADMIN → `403 Forbidden` com body padrao:
```json
{
  "error": "Proibido",
  "message": "Acesso negado. Você não tem permissão para acessar este recurso.",
  "timestamp": "..."
}
```

### Protegidos (requerem JWT)

Qualquer endpoint fora das rotas publicas retorna `401` se o token nao for fornecido ou for invalido.

### Controle de Acesso (RBAC)

Apos a autenticacao, o framework de autorizacao do Spring Security utiliza roles como `GrantedAuthority` para verificar acesso:

| Regra                 | Comportamento                                   |
| --------------------- | ----------------------------------------------- |
| `anyRequest().authenticated()` | Qualquer endpoint protegido exige JWT  |
| `/api/v1/admin/**`   | Exige role `ADMIN` — retorna 403 caso contrario |
| `@PreAuthorize`       | Usado em endpoints administrativos              |
| Sem token             | Retorna 401                                     |
| Token valido sem role | Retorna 403 em endpoints restritos              |

## Logging

Logs estruturados no padrao: `timestamp [thread] level [pospdv,profile] logger - message`

| Perfil  | `br.com.topone` | `org.springframework.web` | `org.hibernate.SQL` |
| ------- | --------------- | ------------------------- | ------------------- |
| local   | DEBUG           | DEBUG                     | DEBUG (+ bind TRACE)|
| prod    | INFO            | WARN                      | WARN                |

Rotas publicas: `/api/v1/health`, `/api/v1/auth/**`, `/actuator/health` (GET).

## Testes

```bash
./mvnw test
```

Profile `test` usa H2 in-memory com Flyway desabilitado.

## Estrutura de pacotes

O projeto segue Clean Architecture com hexagonal boundaries.

```
br.com.topone.backend
├── domain/
│   ├── model/        # POJOs puros — ZERO dependencias externas
│   ├── repository/   # Interfaces de repositorio (Ports)
│   └── exception/    # Excecoes de dominio
│
├── application/
│   └── usecase/
│       ├── login/    # Casos de uso de autenticação (Login, Logout, RefreshToken)
│       └── user/     # Casos de uso de usuário (CRUD administrativo)
│
├── infrastructure/
│   ├── config/       # Configuracoes Spring (Security, CORS, etc)
│   ├── persistence/
│   │   ├── entity/   # JPA entities — isoladas do dominio
│   │   ├── jpa/      # Spring Data JPA repositories
│   │   ├── mapper/   # MapStruct: domain ↔ entity
│   │   └── adapter/  # Implementam os Ports delegando para JPA
│   └── security/     # JWT filter, token service
│
├── interfaces/
│   ├── rest/         # Controllers (REST adapters)
│   └── dto/          # DTOs de entrada/saida, response records
│
└── BackendApplication.java
```

**Regra de dependencia:** `domain` nao depende de ninguem. `application` depende de `domain`. `infrastructure` e `interfaces` dependem de `application` e `domain`. As entidades do `domain/model` sao POJOs puros — sem `@Entity`, `@Column`, ou qualquer anotacao JPA/Spring. Conversao entre domain e entity via MapStruct em `infrastructure/persistence/mapper/`.

## Dominio

### User (`tb_users`)

| Campo            | Tipo          | Notas                         |
| ---------------- | ------------- | ----------------------------- |
| id               | UUID          | PK, auto-gen                  |
| email            | VARCHAR(255)  | UNIQUE, NOT NULL              |
| name             | VARCHAR(100)  | NOT NULL                      |
| password_hash    | VARCHAR(255)  | Nullable (oauth users)        |
| provider         | VARCHAR(10)   | `LOCAL` ou `GOOGLE`           |
| created_at       | TIMESTAMP     | Auto                          |
| updated_at       | TIMESTAMP     | Auto                          |
| deleted_at       | TIMESTAMP     | Nullable (soft delete)        |

### Role (`tb_roles`)

| Campo | Tipo         | Notas          |
| ----- | ------------ | -------------- |
| id    | UUID         | PK, auto-gen   |
| name  | VARCHAR(20)  | UNIQUE, NOT NULL |

Roles disponiveis: `USER`, `ADMIN`.
Para atribuir um perfil a um usuario, use o UUID da role:

| UUID                               | Nome  |
| ---------------------------------- | ----- |
| `00000000-0000-0000-0000-000000000001` | USER  |
| `00000000-0000-0000-0000-000000000002` | ADMIN |

### User-Role (`tb_user_roles`)

| Campo   | Tipo | Notas                           |
| ------- | ---- | ------------------------------- |
| user_id | UUID | FK → tb_users(id), CASCADE DELETE|
| role_id | UUID | FK → tb_roles(id), CASCADE DELETE|

### Usuário Admin Padrão

Na inicialização, se não existir, é criado automaticamente um usuário admin:

| Campo    | Valor            |
| -------- | ---------------- |
| email    | `admin@email.com`|
| nome     | `Admin`          |
| senha    | `admin123`       |
| roles    | `ADMIN`, `USER`  |

Altere a senha em produção imediatamente após o primeiro login.

### Refresh Token (`tb_refresh_tokens`)

| Campo                    | Tipo          | Notas                               |
| ------------------------ | ------------- | ----------------------------------- |
| id                       | UUID          | PK, auto-gen                        |
| user_id                  | UUID          | FK → tb_users(id), CASCADE DELETE   |
| token_hash               | VARCHAR(255)  | NOT NULL (nunca token puro)         |
| expires_at               | TIMESTAMP     | NOT NULL                            |
| revoked_at               | TIMESTAMP     | Nullable                            |
| replaced_by_token_hash   | VARCHAR(255)  | Nullable (rotação de token)         |
| created_at               | TIMESTAMP     | Auto                                |
| last_used_at             | TIMESTAMP     | Nullable                            |
| user_agent               | VARCHAR(500)  | Nullable                            |
| ip_address               | VARCHAR(45)   | Nullable                            |

## Tecnologias

Spring Boot 4, Spring Security, Spring Data JPA, Flyway, PostgreSQL, H2 (test), Actuator + Prometheus, Lombok, MapStruct, Bean Validation, JJWT (0.12.6), Spring Mail.
