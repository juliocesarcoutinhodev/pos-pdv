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
| `CNPJA_API_URL`                     | `https://api.cnpja.com`              | Base URL do provedor CNPJA       |
| `CNPJA_API_TOKEN`                   | *(sem padrao)*                       | Token da API CNPJA               |
| `LOOKUP_CACHE_ENABLED`              | `true`                               | Habilita cache local para consultas CNPJ/CEP |
| `LOOKUP_CACHE_CNPJ_SUCCESS_TTL_MINUTES` | `1440`                           | TTL (min) de respostas CNPJ com sucesso |
| `LOOKUP_CACHE_CNPJ_NOT_FOUND_TTL_MINUTES` | `60`                           | TTL (min) para CNPJ nao encontrado (404) |
| `LOOKUP_CACHE_CNPJ_MAXIMUM_SIZE`    | `50000`                              | Limite de itens no cache de CNPJ |
| `LOOKUP_CACHE_ZIP_SUCCESS_TTL_MINUTES` | `43200`                           | TTL (min) de respostas CEP com sucesso |
| `LOOKUP_CACHE_ZIP_NOT_FOUND_TTL_MINUTES` | `60`                           | TTL (min) para CEP nao encontrado (404) |
| `LOOKUP_CACHE_ZIP_MAXIMUM_SIZE`     | `50000`                              | Limite de itens no cache de CEP |
| `MINIO_ENDPOINT`                    | `http://localhost:9000`              | Endpoint S3-compatible do MinIO |
| `MINIO_ACCESS_KEY`                  | `minioadmin`                         | Access key do MinIO |
| `MINIO_SECRET_KEY`                  | *(sem padrao)*                       | Secret key do MinIO |
| `MINIO_BUCKET`                      | `images`                             | Bucket para imagens do sistema |
| `MINIO_AUTO_CREATE_BUCKET`          | `true`                               | Cria bucket automaticamente se nao existir |
| `REPORT_COMPANY_LEGAL_NAME`         | vazio                                | Razao social para cabecalho dos relatorios de clientes |
| `REPORT_COMPANY_TRADE_NAME`         | vazio                                | Nome fantasia para cabecalho dos relatorios de clientes |
| `REPORT_COMPANY_TAX_ID`             | vazio                                | CNPJ da empresa no cabecalho dos relatorios de clientes |
| `REPORT_COMPANY_PHONE`              | vazio                                | Telefone da empresa no cabecalho dos relatorios de clientes |
| `REPORT_COMPANY_EMAIL`              | vazio                                | E-mail da empresa no cabecalho dos relatorios de clientes |
| `REPORT_COMPANY_ADDRESS_LINE`       | vazio                                | Endereco em uma linha para cabecalho dos relatorios de clientes |
| `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` | `10MB`                          | Limite maximo por arquivo no upload |
| `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` | `10MB`                       | Limite maximo total da requisicao multipart |

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
| GET    | `/api/v1/cnpj?taxId=37335118000180` | Sim | Consulta dados cadastrais por CNPJ |
| GET    | `/api/v1/zip?code=03195000` | Sim | Consulta endereco por CEP |

**GET `/api/v1/me`** — 200 OK
```json
{
  "id": "uuid",
  "email": "user@test.com",
  "name": "Test User",
  "roles": ["USER", "ADMIN"]
}
```

**GET `/api/v1/cnpj?taxId=37335118000180`** — 200 OK
```json
{
  "updated": "2026-04-09T12:32:05Z",
  "taxId": "37335118000180",
  "name": "CNPJA TECNOLOGIA LTDA",
  "alias": "Cnpja",
  "founded": "2020-06-05",
  "equity": 1000,
  "head": true
}
```
Erros comuns: `400` (CNPJ inválido), `404` (CNPJ não encontrado), `502` (falha no provedor externo).
As consultas usam cache local (configurável) para reduzir consumo de créditos do provedor externo.

**GET `/api/v1/zip?code=03195000`** — 200 OK
```json
{
  "updated": "2026-03-25T03:00:00Z",
  "code": "03195000",
  "municipality": 3550308,
  "street": "Rua do Oratório",
  "number": null,
  "district": "Alto da Mooca",
  "city": "São Paulo",
  "state": "SP"
}
```
Erros comuns: `400` (CEP inválido), `404` (CEP não encontrado), `502` (falha no provedor externo).
As consultas usam cache local (configurável) para reduzir consumo de créditos do provedor externo.

### CRUD de Usuarios (requer role ADMIN)

| Metodo | Path                   | Auth | Descricao                              |
| ------ | ---------------------- | ---- | -------------------------------------- |
| GET    | `/api/v1/users`        | Sim  | Listar usuarios com filtros/paginacao  |
| GET    | `/api/v1/users/{id}`   | Sim  | Detalhes de um usuario                 |
| POST   | `/api/v1/users`        | Sim  | Criar novo usuario com roleIds         |
| PUT    | `/api/v1/users/{id}`   | Sim  | Atualizacao completa (roleIds obrig.)  |
| PATCH  | `/api/v1/users/{id}`   | Sim  | Atualizacao parcial (inclui ativar/desativar) |
| DELETE | `/api/v1/users/{id}`   | Sim  | Desativar usuario (soft delete)        |

**POST `/api/v1/users`** — 201 Created
Use os IDs retornados por `GET /api/v1/roles` no campo `roleIds`.
```json
{
  "email": "joao@email.com",
  "name": "Joao Silva",
  "password": "senha123",
  "roleIds": ["uuid-da-role-user"]
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
  "roleIds": ["uuid-da-role-user", "uuid-da-role-admin"]
}
```

**PATCH `/api/v1/users/{id}`** — 200 OK
Apenas os campos enviados sao aplicados:
```json
{
  "name": "Nome Parcial",
  "roleIds": ["uuid-da-role-admin"]
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
Desativar usuario (soft delete) — define `deletedAt`. O usuario nao aparece mais em queries ativas. Nenhum dado e deletado fisicamente do banco.

**PATCH `/api/v1/users/{id}` com `active`** — 200 OK
Ativar ou desativar usuario:
```json
{
  "active": true
}
```
`"active": true` reativa um usuario desativado, `"active": false` o desativa (equivalente ao DELETE).

### CRUD de Roles (requer role ADMIN)

| Metodo | Path                   | Auth | Descricao                          |
| ------ | ---------------------- | ---- | ---------------------------------- |
| GET    | `/api/v1/roles`        | Sim  | Listar roles com paginacao         |
| GET    | `/api/v1/roles/{id}`   | Sim  | Detalhes de uma role               |
| POST   | `/api/v1/roles`        | Sim  | Criar nova role                    |
| PUT    | `/api/v1/roles/{id}`   | Sim  | Atualizacao completa da role       |
| PATCH  | `/api/v1/roles/{id}`   | Sim  | Atualizacao parcial da role        |
| DELETE | `/api/v1/roles/{id}`   | Sim  | Remover role                       |

**GET `/api/v1/roles?page=0&size=20`** — 200 OK
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "USER",
      "description": "Usuário padrão",
      "createdAt": "2026-04-07T...",
      "updatedAt": "2026-04-07T..."
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

**POST `/api/v1/roles`** — 201 Created
```json
{
  "name": "MANAGER",
  "description": "Gerente da operação"
}
```

Response 201:
```json
{
  "id": "uuid",
  "name": "MANAGER",
  "description": "Gerente da operação",
  "createdAt": "2026-04-07T...",
  "updatedAt": null
}
```

**PUT `/api/v1/roles/{id}`** — 200 OK
```json
{
  "name": "MANAGER",
  "description": "Gerente com acesso total à operação"
}
```

**PATCH `/api/v1/roles/{id}`** — 200 OK
```json
{
  "description": "Gerente atualizado via patch"
}
```

**DELETE `/api/v1/roles/{id}`** — 204 No Content
Remove a role informada quando ela nao for sistemica e nao estiver vinculada a usuarios.

Regras de negocio:
- roles `USER` e `ADMIN` nao podem ser removidas
- roles vinculadas a usuarios nao podem ser removidas
- nesses casos a API retorna `409 Conflict`

### CRUD de Fornecedores

| Metodo | Path                       | Auth | Descricao                                      |
| ------ | -------------------------- | ---- | ---------------------------------------------- |
| GET    | `/api/v1/suppliers`        | Sim  | Listar fornecedores com filtros/paginacao      |
| GET    | `/api/v1/suppliers/{id}`   | Sim  | Detalhes de um fornecedor                      |
| POST   | `/api/v1/suppliers`        | Sim  | Criar novo fornecedor                          |
| PUT    | `/api/v1/suppliers/{id}`   | Sim  | Atualizacao completa                           |
| PATCH  | `/api/v1/suppliers/{id}`   | Sim  | Atualizacao parcial (inclui ativar/desativar)  |
| DELETE | `/api/v1/suppliers/{id}`   | Sim  | Desativar fornecedor (soft delete)             |

Permissões:
- `GET /api/v1/suppliers`, `GET /api/v1/suppliers/{id}` e `POST /api/v1/suppliers` exigem apenas usuário autenticado.
- `PUT`, `PATCH` e `DELETE` em `/api/v1/suppliers/**` exigem role `ADMIN`.

**POST `/api/v1/suppliers`** — 201 Created
```json
{
  "name": "Fornecedor XPTO LTDA",
  "taxId": "37335118000180",
  "email": "contato@fornecedor.com",
  "phone": "11999999999",
  "address": {
    "zipCode": "03195000",
    "street": "Rua do Oratório",
    "number": "100",
    "complement": "Sala 2",
    "district": "Alto da Mooca",
    "city": "São Paulo",
    "state": "SP"
  },
  "contacts": [
    {
      "name": "Maria Silva",
      "email": "maria@fornecedor.com",
      "phone": "11999990000"
    },
    {
      "name": "João Souza",
      "email": "joao@fornecedor.com",
      "phone": "11999990001"
    }
  ]
}
```
`contacts` aceita de `0..N` itens e pode ser enviado vazio.

**GET `/api/v1/suppliers?page=0&size=20`** — 200 OK  
Query params opcionais: `name` (parcial), `taxId` (parcial), `email` (parcial), `active` (true/false), `sortBy` (`name`, `taxId`, `createdAt`) e `sortDirection` (`asc`/`desc`).

**DELETE `/api/v1/suppliers/{id}`** — 204 No Content  
Desativar fornecedor (soft delete) — define `deletedAt`. Nenhum dado e deletado fisicamente.

### CRUD de Clientes

| Metodo | Path                       | Auth | Descricao                                      |
| ------ | -------------------------- | ---- | ---------------------------------------------- |
| GET    | `/api/v1/customers`        | Sim  | Listar clientes com filtros/paginacao          |
| GET    | `/api/v1/customers/reports/summary`  | Sim  | Gerar PDF resumido de clientes (Jasper)        |
| GET    | `/api/v1/customers/reports/detailed` | Sim  | Gerar PDF detalhado de clientes (Jasper)       |
| GET    | `/api/v1/customers/{id}`   | Sim  | Detalhes de um cliente                         |
| POST   | `/api/v1/customers`        | Sim  | Criar novo cliente                             |
| PUT    | `/api/v1/customers/{id}`   | Sim  | Atualizacao completa                           |
| PATCH  | `/api/v1/customers/{id}`   | Sim  | Atualizacao parcial (inclui ativar/desativar) |
| DELETE | `/api/v1/customers/{id}`   | Sim  | Desativar cliente (soft delete)                |

Permissões:
- `GET /api/v1/customers`, `GET /api/v1/customers/{id}` e `POST /api/v1/customers` exigem apenas usuário autenticado.
- `GET /api/v1/customers/reports/summary` e `GET /api/v1/customers/reports/detailed` exigem usuário autenticado.
- `PUT`, `PATCH` e `DELETE` em `/api/v1/customers/**` exigem role `ADMIN`.

**POST `/api/v1/customers`** — 201 Created
```json
{
  "name": "João da Silva",
  "taxId": "12345678901",
  "email": "joao@email.com",
  "phone": "11999999999",
  "birthDate": "1992-08-15",
  "gender": "MASCULINO",
  "ieOrRg": "RG-1234567",
  "imageId": "3d5bfa71-412a-4f73-bdd8-3fb4dfe58f99",
  "address": {
    "zipCode": "03195000",
    "street": "Rua do Oratório",
    "number": "100",
    "complement": "Casa",
    "district": "Alto da Mooca",
    "city": "São Paulo",
    "state": "SP"
  }
}
```

**GET `/api/v1/customers?page=0&size=20`** — 200 OK  
Query params opcionais: `name` (parcial), `taxId` (parcial), `email` (parcial), `active` (true/false), `sortBy` (`name`, `taxId`, `createdAt`) e `sortDirection` (`asc`/`desc`).

**GET `/api/v1/customers/reports/summary`** — 200 OK  
Retorna PDF (`application/pdf`) com relatório resumido usando template `reports/customers/customers-summary.jrxml`.

**GET `/api/v1/customers/reports/detailed`** — 200 OK  
Retorna PDF (`application/pdf`) com relatório detalhado usando template `reports/customers/customers-detailed.jrxml`.

Filtros opcionais para ambos os relatórios: `name` (parcial), `active` (`true`/`false`), `birthMonth` (`1..12`), `birthDateFrom` (`yyyy-MM-dd`) e `birthDateTo` (`yyyy-MM-dd`).

No relatório detalhado, o cabeçalho da empresa é preenchido pelas variáveis `REPORT_COMPANY_*` (trade name/razão social, CNPJ, contato e endereço).

### CRUD de Produtos

| Metodo | Path                      | Auth | Descricao                                      |
| ------ | ------------------------- | ---- | ---------------------------------------------- |
| GET    | `/api/v1/products`        | Sim  | Listar produtos com filtros/paginacao          |
| GET    | `/api/v1/products/next-sku` | Sim | Gerar sugestao de SKU para novo cadastro       |
| GET    | `/api/v1/products/{id}`   | Sim  | Detalhes de um produto                         |
| POST   | `/api/v1/products`        | Sim  | Criar novo produto                             |
| PUT    | `/api/v1/products/{id}`   | Sim  | Atualizacao completa                           |
| PATCH  | `/api/v1/products/{id}`   | Sim  | Atualizacao parcial (inclui ativar/desativar) |
| DELETE | `/api/v1/products/{id}`   | Sim  | Desativar produto (soft delete)                |

Permissões:
- `GET /api/v1/products`, `GET /api/v1/products/next-sku`, `GET /api/v1/products/{id}` e `POST /api/v1/products` exigem apenas usuário autenticado.
- `PUT`, `PATCH` e `DELETE` em `/api/v1/products/**` exigem role `ADMIN`.

**GET `/api/v1/products/next-sku`** — 200 OK  
Retorna uma sugestão de SKU em formato numérico aleatório de 6 dígitos (ex.: `144236`) para facilitar o cadastro.

No cadastro/atualização de produto, o campo `supplierId` é opcional e permite vincular o produto a um fornecedor existente.
Nos campos comerciais, você pode:
- informar `costPrice` + `marginPercentage` para o backend calcular `salePrice`;
- informar `costPrice` + `salePrice` para o backend retornar `marginPercentage` calculada.

**POST `/api/v1/products`** — 201 Created
```json
{
  "sku": "144236",
  "barcode": "7891234567890",
  "name": "Arroz Tipo 1 5kg",
  "description": "Pacote com 5kg",
  "brand": "Marca XPTO",
  "category": "Mercearia",
  "supplierId": "6ecaf8e3-4fec-45af-a6f9-e2fc342f3f72",
  "unit": "UN",
  "costPrice": 18.9,
  "marginPercentage": 31.75,
  "salePrice": 24.9,
  "promotionalPrice": 22.9,
  "stockQuantity": 50,
  "minimumStock": 10,
  "ncm": "10063021",
  "cest": "1704700",
  "cfop": "5102",
  "taxOrigin": "0",
  "taxSituation": "60",
  "icmsRate": 18,
  "pisSituation": "01",
  "pisRate": 1.65,
  "cofinsSituation": "01",
  "cofinsRate": 7.6,
  "imageId": "3d5bfa71-412a-4f73-bdd8-3fb4dfe58f99"
}
```

**GET `/api/v1/products?page=0&size=20`** — 200 OK  
Query params opcionais: `name` (parcial), `sku` (parcial), `barcode` (parcial), `category` (parcial), `active` (true/false), `sortBy` (`name`, `sku`, `barcode`, `category`, `salePrice`, `stockQuantity`, `createdAt`) e `sortDirection` (`asc`/`desc`).

### Impressão de Etiquetas de Gôndola

| Metodo | Path                              | Auth | Descricao                                              |
| ------ | --------------------------------- | ---- | ------------------------------------------------------ |
| GET    | `/api/v1/labels/suggestions`      | Sim  | Sugestoes de produtos para montar lista de etiquetas   |
| POST   | `/api/v1/labels/jobs`             | Sim  | Criar lote de impressao com snapshot dos itens         |
| GET    | `/api/v1/labels/jobs`             | Sim  | Listar historico de lotes para reimpressao             |
| GET    | `/api/v1/labels/jobs/{id}`        | Sim  | Detalhar lote com itens e quantidades                  |
| GET    | `/api/v1/labels/jobs/{id}/report` | Sim  | Gerar PDF do lote no template Jasper (`100x30`)        |

Permissões:
- Todos os endpoints de `/api/v1/labels/**` exigem usuário autenticado.

**GET `/api/v1/labels/suggestions?date=2026-04-13&page=0&size=20`** — 200 OK  
Query params opcionais: `date` (`yyyy-MM-dd`, padrão dia atual), `name` (parcial), `sku` (parcial), `category` (parcial), `sortBy` (`name`, `sku`, `category`, `createdAt`) e `sortDirection` (`asc`/`desc`).

**POST `/api/v1/labels/jobs`** — 201 Created
```json
{
  "referenceDate": "2026-04-13",
  "items": [
    {
      "productId": "6b9f6f9f-2c6f-4b23-a8e2-71b06dc1b2dd",
      "quantity": 5
    },
    {
      "productId": "8e6a2d55-4f2f-4e57-8f9d-4129e4e198f4",
      "quantity": 2
    }
  ]
}
```

**GET `/api/v1/labels/jobs?page=0&size=20`** — 200 OK  
Query params opcionais: `referenceDate` (`yyyy-MM-dd`), `sortBy` (`createdAt`, `referenceDate`) e `sortDirection` (`asc`/`desc`).

**GET `/api/v1/labels/jobs/{id}/report`** — 200 OK  
Retorna o PDF (`application/pdf`) do lote com base no template `reports/labels/gondola-label.jrxml`.

### Dashboard (requer JWT)

| Metodo | Path                        | Auth | Descricao                                        |
| ------ | --------------------------- | ---- | ------------------------------------------------ |
| GET    | `/api/v1/dashboard/overview`| Sim  | Totais do dia, top produtos e resumo de pagamentos |

**GET `/api/v1/dashboard/overview`** — 200 OK
```json
{
  "todaySalesAmount": 1580.00,
  "todaySalesCount": 42,
  "todayAverageTicket": 37.62,
  "activeUsers": 5,
  "activeCustomers": 310,
  "activeSuppliers": 12,
  "activeProducts": 480,
  "lowStockProducts": 7,
  "openCashRegisters": 2,
  "recentSales": [...],
  "topProducts": [...],
  "paymentSummary": [...]
}
```

### PDV - Caixa e Vendas (requer JWT)

| Metodo | Path                                | Auth | Descricao                                     |
| ------ | ----------------------------------- | ---- | --------------------------------------------- |
| GET    | `/api/v1/pdv/cash/current`          | Sim  | Retorna o caixa aberto do usuário logado      |
| POST   | `/api/v1/pdv/cash/open`             | Sim  | Abre caixa para o usuário logado              |
| POST   | `/api/v1/pdv/cash/close`            | Sim  | Fecha o caixa atual com o valor contado       |
| POST   | `/api/v1/pdv/cash/movements`        | Sim  | Lança suprimento ou sangria no caixa atual    |
| GET    | `/api/v1/pdv/products/lookup`       | Sim  | Busca produto por SKU ou código de barras     |
| POST   | `/api/v1/pdv/sales`                 | Sim  | Registra uma venda no caixa aberto            |
| GET    | `/api/v1/pdv/sales/recent`          | Sim  | Lista últimas N vendas do caixa atual         |
| GET    | `/api/v1/pdv/sales/history`         | Sim  | Histórico paginado de vendas (admin vê todos) |

**POST `/api/v1/pdv/cash/open`** — 201 Created
```json
{ "openingAmount": 100.00 }
```

**POST `/api/v1/pdv/cash/close`** — 200 OK
```json
{ "closingAmount": 185.40 }
```
Response inclui `openingAmount`, `suppliesAmount`, `withdrawalsAmount`, `salesAmount`, `expectedCashAmount`, `closingAmount` e `differenceAmount`.

**POST `/api/v1/pdv/cash/movements`** — 201 Created
```json
{
  "type": "SUPPLY",
  "amount": 50.00,
  "note": "Suprimento de troco"
}
```
`type`: `SUPPLY` (suprimento) ou `WITHDRAWAL` (sangria).

**GET `/api/v1/pdv/products/lookup?code=144236`** — 200 OK  
Busca por SKU ou código de barras. Retorna `id`, `sku`, `barcode`, `name`, `unit`, `unitPrice` e `stockQuantity`.

**POST `/api/v1/pdv/sales`** — 201 Created
```json
{
  "paymentMethod": "CASH",
  "discountAmount": 0.00,
  "paidAmount": 50.00,
  "notes": "Troco para R$ 50",
  "items": [
    { "productId": "uuid-do-produto", "quantity": 2 }
  ]
}
```
`paymentMethod`: `CASH`, `PIX`, `DEBIT_CARD`, `CREDIT_CARD`, `OTHER`.

**GET `/api/v1/pdv/sales/history?page=0&size=20`** — 200 OK  
Query params opcionais: `dateFrom`, `dateTo` (`yyyy-MM-dd`), `userId` (apenas ADMIN), `paymentMethod`, `sortBy` (`createdAt`, `totalAmount`, `paymentMethod`), `sortDirection` (`asc`/`desc`).

### PDV - Relatorios (requer role ADMIN)

| Metodo | Path                                  | Auth        | Descricao                                      |
| ------ | ------------------------------------- | ----------- | ---------------------------------------------- |
| GET    | `/api/v1/pdv/reports/daily-sales`     | Sim (ADMIN) | Totais, horarios e top produtos do dia         |
| GET    | `/api/v1/pdv/reports/closing`         | Sim (ADMIN) | Resumo de fechamento por sessao de caixa       |

**GET `/api/v1/pdv/reports/daily-sales?date=2026-04-30`** — 200 OK  
`date` opcional (padrão = hoje). Retorna `salesCount`, `totalAmount`, `averageTicket`, quebra por `paymentSummary`, `hourlySummary` (hora a hora) e `topProducts` (top 10 por quantidade).

**GET `/api/v1/pdv/reports/closing?date=2026-04-30`** — 200 OK  
`date` opcional (padrão = hoje). Retorna totalizadores globais e por sessao: `openingAmount`, `suppliesAmount`, `withdrawalsAmount`, `salesAmount`, `cashBalance`, `closingAmount` e diferença.

### PDV - Monitoramento (requer role ADMIN)

| Metodo | Path                                                       | Auth        | Descricao                                              |
| ------ | ---------------------------------------------------------- | ----------- | ------------------------------------------------------ |
| GET    | `/api/v1/pdv/monitor/open-cash-registers`                  | Sim (ADMIN) | Lista caixas abertos e fechados do período             |
| GET    | `/api/v1/pdv/monitor/open-cash-registers/{sessionId}/summary` | Sim (ADMIN) | Resumo completo de uma sessao de caixa              |

**GET `/api/v1/pdv/monitor/open-cash-registers?dateFrom=2026-04-30&dateTo=2026-04-30`** — 200 OK  
Query params opcionais (padrão = hoje). Retorna caixas abertos e fechados no período com saldo esperado, valor de fechamento e diferença.

**GET `/api/v1/pdv/monitor/open-cash-registers/{sessionId}/summary`** — 200 OK  
Retorna detalhes completos: movimentações (suprimentos, sangrias), resumo por forma de pagamento e últimas vendas da sessão.

### Imagens (MinIO)

| Metodo | Path                            | Auth | Descricao                                          |
| ------ | ------------------------------- | ---- | -------------------------------------------------- |
| POST   | `/api/v1/images/upload`         | Sim  | Upload de imagem (`multipart/form-data`, campo `file`) |
| GET    | `/api/v1/images/{imageId}`      | Sim  | Download/visualizacao da imagem                    |
| GET    | `/api/v1/images/{imageId}/metadata` | Sim | Metadados da imagem armazenada                     |

**POST `/api/v1/images/upload`** — 201 Created
```json
{
  "imageId": "3d5bfa71-412a-4f73-bdd8-3fb4dfe58f99",
  "fileName": "cliente.png",
  "contentType": "image/png",
  "size": 24532,
  "uploadedAt": "2026-04-10T18:40:00Z",
  "downloadUrl": "http://localhost:8080/api/v1/images/3d5bfa71-412a-4f73-bdd8-3fb4dfe58f99",
  "metadataUrl": "http://localhost:8080/api/v1/images/3d5bfa71-412a-4f73-bdd8-3fb4dfe58f99/metadata"
}
```

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
As roles padrao `USER` e `ADMIN` sao garantidas pelo bootstrap da aplicacao durante a subida do contexto.

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
│       ├── role/     # Casos de uso de roles (CRUD administrativo)
│       ├── customer/ # Casos de uso de clientes (CRUD + imagem)
│       ├── product/  # Casos de uso de produtos (CRUD + SKU/margem)
│       ├── label/    # Casos de uso de impressão de etiquetas de gôndola
│       ├── image/    # Casos de uso de upload/download de imagens
│       ├── supplier/ # Casos de uso de fornecedores (CRUD administrativo)
│       └── user/     # Casos de uso de usuário (CRUD administrativo)
│
├── infrastructure/
│   ├── config/       # Configuracoes Spring (Security, CORS, etc)
│   ├── external/     # Adapters de integração externa (CNPJA, MinIO)
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

| Campo      | Tipo          | Notas                |
| ---------- | ------------- | -------------------- |
| id         | UUID          | PK, auto-gen         |
| name       | VARCHAR(20)   | UNIQUE, NOT NULL     |
| description| VARCHAR(255)  | Nullable             |
| created_at | TIMESTAMP     | Auto                 |
| updated_at | TIMESTAMP     | Auto                 |

Roles padrao garantidas no bootstrap:

| Nome    | Descricao                     |
| ------- | ----------------------------- |
| `USER`  | Usuário padrão                |
| `ADMIN` | Administrador do sistema      |

Para atribuir um perfil a um usuario, consulte primeiro `GET /api/v1/roles` e use o `id` retornado.

### User-Role (`tb_user_roles`)

| Campo   | Tipo | Notas                           |
| ------- | ---- | ------------------------------- |
| user_id | UUID | FK → tb_users(id), CASCADE DELETE|
| role_id | UUID | FK → tb_roles(id), CASCADE DELETE|

### Address (`tb_addresses`)

| Campo      | Tipo          | Notas                     |
| ---------- | ------------- | ------------------------- |
| id         | UUID          | PK, auto-gen              |
| zip_code   | VARCHAR(8)    | NOT NULL                  |
| street     | VARCHAR(255)  | NOT NULL                  |
| number     | VARCHAR(20)   | Nullable                  |
| complement | VARCHAR(120)  | Nullable                  |
| district   | VARCHAR(120)  | NOT NULL                  |
| city       | VARCHAR(120)  | NOT NULL                  |
| state      | VARCHAR(2)    | NOT NULL                  |

### Supplier (`tb_suppliers`)

| Campo      | Tipo          | Notas                                |
| ---------- | ------------- | ------------------------------------ |
| id         | UUID          | PK, auto-gen                         |
| name       | VARCHAR(150)  | NOT NULL                             |
| tax_id     | VARCHAR(14)   | UNIQUE, NOT NULL                     |
| email      | VARCHAR(255)  | Nullable                             |
| phone      | VARCHAR(30)   | Nullable                             |
| address_id | UUID          | FK → tb_addresses(id), UNIQUE        |
| created_at | TIMESTAMP     | Auto                                 |
| updated_at | TIMESTAMP     | Auto                                 |
| deleted_at | TIMESTAMP     | Nullable (soft delete)               |

### Customer (`tb_customers`)

| Campo      | Tipo          | Notas                                |
| ---------- | ------------- | ------------------------------------ |
| id         | UUID          | PK, auto-gen                         |
| name       | VARCHAR(150)  | NOT NULL                             |
| tax_id     | VARCHAR(14)   | UNIQUE, NOT NULL                     |
| email      | VARCHAR(255)  | Nullable                             |
| phone      | VARCHAR(30)   | Nullable                             |
| birth_date | DATE          | Nullable                             |
| gender     | VARCHAR(40)   | Nullable                             |
| ie_or_rg   | VARCHAR(30)   | Nullable                             |
| image_id   | VARCHAR(120)  | Nullable (referencia lógica no MinIO)|
| address_id | UUID          | FK → tb_addresses(id), UNIQUE        |
| created_at | TIMESTAMP     | Auto                                 |
| updated_at | TIMESTAMP     | Auto                                 |
| deleted_at | TIMESTAMP     | Nullable (soft delete)               |

### Contact (`tb_contacts`)

| Campo   | Tipo          | Notas                |
| ------- | ------------- | -------------------- |
| id      | UUID          | PK, auto-gen         |
| name    | VARCHAR(120)  | NOT NULL             |
| email   | VARCHAR(255)  | Nullable             |
| phone   | VARCHAR(30)   | Nullable             |

### Supplier-Contact (`tb_supplier_contacts`)

| Campo      | Tipo | Notas                                             |
| ---------- | ---- | ------------------------------------------------- |
| supplier_id| UUID | FK → tb_suppliers(id), CASCADE DELETE             |
| contact_id | UUID | FK → tb_contacts(id), CASCADE DELETE, UNIQUE      |

### Product (`tb_products`)

| Campo             | Tipo           | Notas                                              |
| ----------------- | -------------- | -------------------------------------------------- |
| id                | UUID           | PK, auto-gen                                       |
| sku               | VARCHAR(60)    | UNIQUE, NOT NULL                                   |
| barcode           | VARCHAR(20)    | UNIQUE, Nullable                                   |
| name              | VARCHAR(150)   | NOT NULL                                           |
| description       | VARCHAR(1000)  | Nullable                                           |
| brand             | VARCHAR(120)   | Nullable                                           |
| category          | VARCHAR(100)   | Nullable                                           |
| unit              | VARCHAR(10)    | NOT NULL (ex.: UN, KG, CX)                        |
| cost_price        | NUMERIC(15,2)  | Nullable                                           |
| sale_price        | NUMERIC(15,2)  | NOT NULL                                           |
| promotional_price | NUMERIC(15,2)  | Nullable                                           |
| stock_quantity    | NUMERIC(15,3)  | NOT NULL, default 0                                |
| minimum_stock     | NUMERIC(15,3)  | NOT NULL, default 0                                |
| supplier_id       | UUID           | FK → tb_suppliers(id), Nullable                   |
| ncm/cest/cfop     | VARCHAR        | Campos fiscais opcionais                           |
| image_id          | VARCHAR(120)   | Nullable (referencia lógica no MinIO)             |
| created_at        | TIMESTAMP      | Auto                                               |
| updated_at        | TIMESTAMP      | Auto                                               |
| deleted_at        | TIMESTAMP      | Nullable (soft delete)                             |

### LabelPrintJob (`tb_label_print_jobs`)

| Campo          | Tipo     | Notas                          |
| -------------- | -------- | ------------------------------ |
| id             | UUID     | PK                             |
| reference_date | DATE     | NOT NULL                       |
| created_at     | TIMESTAMPTZ | Auto                        |

### LabelPrintJobItem (`tb_label_print_job_items`)

| Campo             | Tipo           | Notas                              |
| ----------------- | -------------- | ---------------------------------- |
| id                | UUID           | PK                                 |
| job_id            | UUID           | FK → tb_label_print_jobs(id), CASCADE DELETE |
| product_id        | UUID           | FK → tb_products(id)               |
| sku               | VARCHAR(60)    | Snapshot no momento da impressão  |
| barcode           | VARCHAR(20)    | Nullable, snapshot                 |
| name              | VARCHAR(150)   | Snapshot                           |
| unit              | VARCHAR(10)    | Snapshot                           |
| sale_price        | NUMERIC(15,2)  | Snapshot                           |
| promotional_price | NUMERIC(15,2)  | Nullable, snapshot                 |
| quantity          | INTEGER        | Quantidade de etiquetas            |

### CashRegisterSession (`tb_cash_register_sessions`)

| Campo          | Tipo          | Notas                              |
| -------------- | ------------- | ---------------------------------- |
| id             | UUID          | PK, auto-gen                       |
| user_id        | UUID          | FK → tb_users(id)                  |
| opening_amount | NUMERIC(15,2) | NOT NULL                           |
| opened_at      | TIMESTAMP     | Auto                               |
| closed_at      | TIMESTAMP     | Nullable                           |
| closing_amount | NUMERIC(15,2) | Nullable (preenchido no fechamento)|
| status         | VARCHAR(20)   | `OPEN` ou `CLOSED`                 |

### CashMovement (`tb_cash_register_movements`)

| Campo                    | Tipo          | Notas                            |
| ------------------------ | ------------- | -------------------------------- |
| id                       | UUID          | PK, auto-gen                     |
| cash_register_session_id | UUID          | FK → tb_cash_register_sessions(id) |
| user_id                  | UUID          | FK → tb_users(id)                |
| type                     | VARCHAR(20)   | `SUPPLY` ou `WITHDRAWAL`         |
| amount                   | NUMERIC(15,2) | NOT NULL                         |
| note                     | VARCHAR(255)  | Nullable                         |
| created_at               | TIMESTAMP     | Auto                             |

### PdvSale (`tb_pdv_sales`)

| Campo                    | Tipo          | Notas                              |
| ------------------------ | ------------- | ---------------------------------- |
| id                       | UUID          | PK, auto-gen                       |
| cash_register_session_id | UUID          | FK → tb_cash_register_sessions(id) |
| user_id                  | UUID          | FK → tb_users(id)                  |
| payment_method           | VARCHAR(20)   | `CASH`, `PIX`, `DEBIT_CARD`, `CREDIT_CARD`, `OTHER` |
| subtotal_amount          | NUMERIC(15,2) | NOT NULL                           |
| discount_amount          | NUMERIC(15,2) | NOT NULL                           |
| total_amount             | NUMERIC(15,2) | NOT NULL                           |
| paid_amount              | NUMERIC(15,2) | NOT NULL                           |
| change_amount            | NUMERIC(15,2) | NOT NULL                           |
| notes                    | VARCHAR(255)  | Nullable                           |
| created_at               | TIMESTAMP     | Auto                               |

### PdvSaleItem (`tb_pdv_sale_items`)

| Campo      | Tipo          | Notas                          |
| ---------- | ------------- | ------------------------------ |
| id         | UUID          | PK, auto-gen                   |
| sale_id    | UUID          | FK → tb_pdv_sales(id)          |
| product_id | UUID          | FK → tb_products(id), Nullable |
| sku        | VARCHAR(60)   | Snapshot                       |
| barcode    | VARCHAR(20)   | Nullable, snapshot             |
| name       | VARCHAR(150)  | Snapshot                       |
| unit       | VARCHAR(10)   | Nullable, snapshot             |
| unit_price | NUMERIC(15,2) | NOT NULL, snapshot             |
| quantity   | NUMERIC(15,3) | NOT NULL                       |
| line_total | NUMERIC(15,2) | NOT NULL                       |

### Usuário Admin Padrão

Na inicializacao, se nao existir, e criado automaticamente um usuario admin:

| Campo    | Valor            |
| -------- | ---------------- |
| email    | `admin@email.com`|
| nome     | `Admin`          |
| senha    | `admin123`       |
| roles    | `ADMIN`, `USER`  |

O bootstrap apenas cria esse usuario quando ele nao existe; ele nao reseta a senha de um admin ja existente.
Altere a senha em producao imediatamente apos o primeiro login.

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

Spring Boot 4, Spring Security, Spring Data JPA, Flyway, PostgreSQL, H2 (test), Actuator + Prometheus, Lombok, MapStruct, Bean Validation, JJWT (0.12.6), Spring Mail, MinIO Java SDK.
