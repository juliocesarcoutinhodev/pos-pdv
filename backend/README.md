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
  "refreshToken": "eyJ...",
  "expiresIn": 604800
}
```

**POST `/refresh`** — 200 OK
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 604800
}
```

O refresh token é rotacionado a cada uso — o anterior é invalidado e um novo é retornado.

**POST `/logout`** — 204 No Content
```json
{
  "refreshToken": "eyJ..."
}
```

Enviar `revokeAll: true` no body para revogar todos os refresh tokens do usuário. Após logout, qualquer tentativa de usar o refresh token revogado retorna `401`.

### Protegidos (requerem JWT)

Qualquer endpoint fora das rotas publicas retorna `401` se o token nao for fornecido ou for invalido.

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
│   └── usecase/      # Casos de uso, Commands e Results
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
