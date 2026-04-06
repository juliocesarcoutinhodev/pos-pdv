# Backend - POS PDV

API RESTful do sistema POS PDV, construida com **Spring Boot 4.0.5** e **Java 25**.

## Pré-requisitos

- Java 25+
- Maven 3.9+ (ou `./mvnw`)
- PostgreSQL 16+
- Docker (opcional)

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

A API sobe na porta `8080` (configuravel via `SERVER_PORT`). No profile `local` o contexto root e `/`.

## Perfis

| Perfil  | Ativacao                     | Uso                          |
| ------- | ---------------------------- | ---------------------------- |
| `local` | padrao (`SPRING_PROFILES_ACTIVE=local`) | Desenvolvimento local |
| `prod`  | `SPRING_PROFILES_ACTIVE=prod`            | Producao                 |

## Variaveis de ambiente

Todas configuraveis via `.env` (padrao Spring Boot 4.x).

### Obrigatorias

| Variavel                          | Descricao                                  | Exemplo                                         |
| --------------------------------- | ------------------------------------------ | ----------------------------------------------- |
| `SPRING_DATASOURCE_URL`           | JDBC URL do banco de dados                 | `jdbc:postgresql://localhost:5432/pospdv`       |
| `SPRING_DATASOURCE_USERNAME`      | Usuario do banco                           | `postgres`                                      |
| `SPRING_DATASOURCE_PASSWORD`      | Senha do banco                             | `secret`                                        |
| `SPRING_FLYWAY_URL`               | URL para migracoes Flyway                  | herda de `SPRING_DATASOURCE_URL`                |
| `SPRING_FLYWAY_USER`              | Usuario para Flyway                        | herda de `SPRING_DATASOURCE_USERNAME`           |
| `SPRING_FLYWAY_PASSWORD`          | Senha para Flyway                          | herda de `SPRING_DATASOURCE_PASSWORD`           |

### Opcionais

| Variavel                               | Padrao        | Descricao                          |
| -------------------------------------- | ------------- | ---------------------------------- |
| `SERVER_PORT`                          | `8080`        | Porta HTTP do servidor             |
| `SPRING_PROFILES_ACTIVE`               | `local`       | Perfil Spring ativo                |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE`    | `health,info` | Endpoints Actuator expostos        |

## Endpoints

### Actuator

- `/actuator/health` (local) ou `/pospdv/actuator/health` (prod) — liveness/readiness probes

### API

Context-path em producao: `/pospdv`

## Logging

Logs estruturados no padrao: `timestamp [thread] level [pospdv,profile] logger - message`

| Perfil  | `br.com.topone` | `org.springframework.web` | `org.hibernate.SQL` |
| ------- | --------------- | ------------------------- | ------------------- |
| local   | DEBUG           | DEBUG                     | DEBUG (+ bind TRACE)|
| prod    | INFO            | WARN                      | WARN                |

## Testes

```bash
./mvnw test
```

## Estrutura de pacotes

O projeto segue Clean Architecture com hexagonal boundaries.

```
br.com.topone.backend
├── domain/
│   ├── model/        # POJOs puros (ENTIDADES DE DOMÍNIO) — ZERO dependências externas
│   ├── repository/   # Interfaces de repositorio (Ports)
│   ├── service/      # Servicos de dominio
│   └── exception/    # Excecoes de dominio (BusinessException, etc)
│
├── application/
│   ├── usecase/      # Casos de uso (interfaces ou implementacoes)
│   └── service/      # Servicos de aplicacao
│
├── infrastructure/
│   ├── config/       # Configuracoes Spring (beans, security, etc)
│   ├── persistence/
│   │   ├── entity/   # JPA entities (@Entity) — isoladas do dominio
│   │   ├── jpa/      # Spring Data JPA repositories
│   │   ├── mapper/   # Conversao domain ↔ entity
│   │   └── adapter/  # Implementam os Ports delegando para JPA
│   └── external/     # Integracoes externas (email, gateways, etc)
│
├── interfaces/
│   ├── rest/         # Controllers, handlers (REST adapters)
│   └── dto/          # DTOs de entrada/saida
│
└── BackendApplication.java
```

**Regra de dependencia:** `domain` nao depende de ninguem. `application` depende de `domain`. `infrastructure` e `interfaces` dependem de `application` e `domain`. As entidades do `domain/model` são **POJOs puros** — sem `@Entity`, `@Column`, ou qualquer anotação JPA/Spring. A conversão entre domain models e JPA entities acontece via mappers em `infrastructure/persistence/mapper/`.

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

Spring Boot 4, Spring Data JPA, Flyway, PostgreSQL, H2 (dev), Actuator + Prometheus, Lombok, Bean Validation, Spring Mail, MapStruct.
