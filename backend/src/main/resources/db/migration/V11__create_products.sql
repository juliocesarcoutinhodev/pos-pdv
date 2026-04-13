CREATE TABLE IF NOT EXISTS tb_products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku                 VARCHAR(60) NOT NULL UNIQUE,
    barcode             VARCHAR(20) UNIQUE,
    name                VARCHAR(150) NOT NULL,
    description         VARCHAR(1000),
    brand               VARCHAR(120),
    category            VARCHAR(100),
    unit                VARCHAR(10) NOT NULL,
    cost_price          NUMERIC(15, 2),
    sale_price          NUMERIC(15, 2) NOT NULL,
    promotional_price   NUMERIC(15, 2),
    stock_quantity      NUMERIC(15, 3) NOT NULL DEFAULT 0,
    minimum_stock       NUMERIC(15, 3) NOT NULL DEFAULT 0,
    ncm                 VARCHAR(8),
    cest                VARCHAR(7),
    cfop                VARCHAR(4),
    tax_origin          VARCHAR(2),
    tax_situation       VARCHAR(10),
    icms_rate           NUMERIC(5, 2),
    pis_situation       VARCHAR(4),
    pis_rate            NUMERIC(5, 2),
    cofins_situation    VARCHAR(4),
    cofins_rate         NUMERIC(5, 2),
    image_id            VARCHAR(120),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_name ON tb_products (name);
CREATE INDEX IF NOT EXISTS idx_products_sku ON tb_products (sku);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON tb_products (barcode);
CREATE INDEX IF NOT EXISTS idx_products_category ON tb_products (category);
CREATE INDEX IF NOT EXISTS idx_products_deleted_at ON tb_products (deleted_at);
