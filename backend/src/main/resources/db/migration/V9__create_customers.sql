CREATE TABLE IF NOT EXISTS tb_customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(14) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(30),
    image_id VARCHAR(120),
    address_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_customers_tax_id UNIQUE (tax_id),
    CONSTRAINT uq_customers_address_id UNIQUE (address_id),
    CONSTRAINT fk_customers_address FOREIGN KEY (address_id) REFERENCES tb_addresses(id)
);

CREATE INDEX IF NOT EXISTS idx_customers_name ON tb_customers (name);
CREATE INDEX IF NOT EXISTS idx_customers_email ON tb_customers (email);
CREATE INDEX IF NOT EXISTS idx_customers_deleted_at ON tb_customers (deleted_at);
