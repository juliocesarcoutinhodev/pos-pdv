CREATE TABLE IF NOT EXISTS tb_roles (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tb_user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON tb_user_roles(user_id);
CREATE INDEX idx_user_roles_role ON tb_user_roles(role_id);
