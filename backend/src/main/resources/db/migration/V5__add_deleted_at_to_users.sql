ALTER TABLE tb_users ADD COLUMN deleted_at TIMESTAMP;
CREATE INDEX idx_users_deleted_at ON tb_users(deleted_at);
