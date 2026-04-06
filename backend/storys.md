Objetivo
Criar a base de domínio e persistência para autenticação com refresh token.

Descrição
Modelar User e a entidade de sessão/refresh token com suporte a rotação e revogação, com migrations Flyway.

Critérios de Aceite

Tabelas criadas via Flyway:

users (email único)

tb_refresh_tokens

Campos mínimos:

tb_users: id, email, name, password_hash (nullable), provider (LOCAL/GOOGLE), created_at, updated_at

tb_refresh_tokens: id, user_id, token_hash, expires_at, revoked_at, replaced_by_token_id (ou hash), created_at, last_used_at, user_agent/ip (opcional)

Refresh token nunca salvo em texto puro (armazenar hash)

Tarefas

Criar modelos no domínio + JPA na infra

Criar migrations Flyway

Validar schema no postgres

DoD

Migrações aplicam no startup sem erro

Restrições/índices essenciais (email unique, FK user_id)

