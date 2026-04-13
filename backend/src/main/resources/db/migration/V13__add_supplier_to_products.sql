DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'tb_products'
          AND column_name = 'supplier_id'
    ) THEN
        ALTER TABLE tb_products
            ADD COLUMN supplier_id UUID;
    END IF;
END
$$;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_products_supplier'
    ) THEN
        ALTER TABLE tb_products
            ADD CONSTRAINT fk_products_supplier
                FOREIGN KEY (supplier_id) REFERENCES tb_suppliers(id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_products_supplier_id ON tb_products (supplier_id);
