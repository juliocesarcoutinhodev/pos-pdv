CREATE TABLE IF NOT EXISTS tb_addresses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zip_code    VARCHAR(8)   NOT NULL,
    street      VARCHAR(255) NOT NULL,
    number      VARCHAR(20),
    complement  VARCHAR(120),
    district    VARCHAR(120) NOT NULL,
    city        VARCHAR(120) NOT NULL,
    state       VARCHAR(2)   NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_suppliers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    tax_id      VARCHAR(14)  NOT NULL UNIQUE,
    email       VARCHAR(255),
    phone       VARCHAR(30),
    address_id  UUID         NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP,
    CONSTRAINT fk_suppliers_address FOREIGN KEY (address_id) REFERENCES tb_addresses(id)
);

CREATE INDEX idx_suppliers_deleted_at ON tb_suppliers(deleted_at);
CREATE INDEX idx_suppliers_name ON tb_suppliers(name);
CREATE INDEX idx_suppliers_tax_id ON tb_suppliers(tax_id);
