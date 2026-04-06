CREATE TABLE tb_refresh_tokens (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID         NOT NULL,
    token_hash               VARCHAR(255) NOT NULL,
    expires_at               TIMESTAMP    NOT NULL,
    revoked_at               TIMESTAMP,
    replaced_by_token_hash   VARCHAR(255),
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at             TIMESTAMP,
    user_agent               VARCHAR(500),
    ip_address               VARCHAR(45),

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_hash ON tb_refresh_tokens (token_hash);
CREATE INDEX idx_refresh_token_user  ON tb_refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_active ON tb_refresh_tokens (user_id, revoked_at) WHERE revoked_at IS NULL;
