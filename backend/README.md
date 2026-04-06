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
│   ├── model/        # Entidades, Value Objects, Enums
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
│   ├── persistence/  # Implementacoes JPA de repositorios (Adapters)
│   └── external/     # Integracoes externas (email, gateways, etc)
│
├── interfaces/
│   ├── rest/         # Controllers, handlers (REST adapters)
│   └── dto/          # DTOs de entrada/saida
│
└── BackendApplication.java
```

**Regra de dependencia:** `domain` nã o depende de ninguem. `application` depende de `domain`. `infrastructure` e `interfaces` dependem de `application` e `domain`. Nada do `domain` ou `application` pode importar pacotes de `infrastructure` ou `interfaces`.

## Tecnologias

Spring Boot 4, Spring Data JPA, Flyway, PostgreSQL, H2 (dev), Actuator + Prometheus, Lombok, Bean Validation, Spring Mail, MapStruct.
