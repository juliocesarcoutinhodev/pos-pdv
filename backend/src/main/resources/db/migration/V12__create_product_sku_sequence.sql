CREATE SEQUENCE IF NOT EXISTS seq_product_sku START WITH 1 INCREMENT BY 1;

DO
$$
DECLARE
    max_sku_code BIGINT;
BEGIN
    SELECT COALESCE(MAX(CAST(SUBSTRING(sku FROM 6) AS BIGINT)), 0)
    INTO max_sku_code
    FROM tb_products
    WHERE sku ~ '^PROD-[0-9]{1,6}$';

    IF max_sku_code > 0 THEN
        PERFORM setval('seq_product_sku', max_sku_code);
    END IF;
END
$$;
