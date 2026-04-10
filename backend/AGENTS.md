# AGENTS Guide (POS PDV Backend)

## Project snapshot
- Stack in code/docs: Java 25 + Spring Boot 4.0.5 (`pom.xml`), Maven wrapper (`./mvnw`), PostgreSQL + Flyway in runtime, H2 in tests (`src/test/resources/application-test.yaml`).
- Package topology follows Hexagonal/Clean boundaries: `domain`, `application`, `infrastructure`, `interfaces` (see `README.md` and `src/main/java/br/com/topone/backend`).

## Architecture and dependency flow
- Keep controllers thin in `interfaces/rest`; they validate/map and delegate to use cases (`AuthController`, `UserManagementController`).
- Business orchestration lives in `application/usecase/**` (e.g., `LoginUseCase`, `RefreshTokenUseCase`) and depends on domain ports, not JPA.
- Persistence adapters in `infrastructure/persistence/adapter/**` implement domain repositories and bridge to Spring Data JPA + MapStruct mappers.
- Domain models are framework-agnostic POJOs (`domain/model/User.java`, `domain/model/RefreshToken.java`), with behavior like soft-delete/reactivation/token state.

## Security and request lifecycle
- API is versioned under `/api/v1/**`; `prod` adds context path `/pospdv` (`application-prod.yaml`).
- JWT auth is stateless via `JwtAuthenticationFilter`; authorization uses role authorities (`ROLE_ADMIN`, `ROLE_USER`) and `@PreAuthorize`.
- Public routes are explicit in `SecurityConfig.PUBLIC_ENDPOINTS`; most routes require bearer token.
- Refresh token is cookie-only (`RefreshTokenCookieService`), HttpOnly, profile-specific `secure/sameSite/domain` via `security.refresh-cookie.*`.
- Token rotation is implemented in `RefreshTokenUseCase`: old token is marked replaced/revoked, new token hash is persisted.

## Persistence and data rules
- Soft delete is first-class: `deletedAt` controls active users (`UserJpaRepository` queries + `DeactivateUserUseCase`).
- Never store raw refresh tokens; only hashes are persisted (`TokenHashService`, `RefreshTokenRepositoryAdapter`).
- Roles are DB-seeded (`src/main/resources/db/migration/V4__seed_roles.sql`) and resolved from UUIDs in adapter logic.
- Startup data bootstrap ensures admin user exists/gets repaired (`DataInitializer`).

## API/error conventions in this codebase
- Request DTOs are records with Bean Validation annotations (`RegisterRequest`, `LoginRequest`, etc.).
- Error payloads currently use records `ErrorResponse` / `ValidationErrorResponse` with fields `{status,error,message,timestamp}` (+ `details` for validation).
- Security layer returns direct JSON on 401/403 in `SecurityConfig` and `JwtAuthenticationFilter`; keep messages compatible with existing tests.
- Correlation ID is mandatory in response headers and MDC (`CorrelationIdFilter`, header `X-Correlation-Id`).

## Dev workflows that matter
- Local run (expects `.env`): `./mvnw spring-boot:run`.
- Test suite: `./mvnw test` (profile `test`, H2, Flyway disabled).
- Build jar: `./mvnw clean package -DskipTests`.
- Health endpoint differs by profile: `/actuator/health` (local) vs `/pospdv/actuator/health` (prod context path).

## Testing patterns to follow
- Use Mockito for use-case unit tests (`src/test/java/.../application/usecase/*Test.java`).
- Use `@SpringBootTest` + `MockMvc` for auth/RBAC behavior (`RbacAuthorizationTest`).
- Existing tests assert Portuguese error messages and status codes; avoid changing these contracts unintentionally.

## Key integration points
- DB: PostgreSQL + Flyway migrations in `src/main/resources/db/migration`.
- Auth/session: JJWT (`JwtTokenService`) + refresh cookie transport.
- Observability/protection: Actuator + Prometheus dependency, correlation-id filter, and Bucket4j+Caffeine rate limit (`RateLimitingFilter`).

