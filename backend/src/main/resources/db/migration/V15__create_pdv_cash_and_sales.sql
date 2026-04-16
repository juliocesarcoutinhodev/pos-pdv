CREATE TABLE IF NOT EXISTS tb_cash_register_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    opening_amount NUMERIC(15,2) NOT NULL,
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    closing_amount NUMERIC(15,2),
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cash_register_sessions_user FOREIGN KEY (user_id) REFERENCES tb_users(id)
);

CREATE INDEX IF NOT EXISTS idx_cash_register_sessions_user_status
    ON tb_cash_register_sessions(user_id, status);

CREATE TABLE IF NOT EXISTS tb_cash_register_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cash_register_session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cash_register_movements_session
        FOREIGN KEY (cash_register_session_id) REFERENCES tb_cash_register_sessions(id),
    CONSTRAINT fk_cash_register_movements_user
        FOREIGN KEY (user_id) REFERENCES tb_users(id)
);

CREATE INDEX IF NOT EXISTS idx_cash_register_movements_session_created
    ON tb_cash_register_movements(cash_register_session_id, created_at DESC);

CREATE TABLE IF NOT EXISTS tb_pdv_sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cash_register_session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    subtotal_amount NUMERIC(15,2) NOT NULL,
    discount_amount NUMERIC(15,2) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    paid_amount NUMERIC(15,2) NOT NULL,
    change_amount NUMERIC(15,2) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pdv_sales_session
        FOREIGN KEY (cash_register_session_id) REFERENCES tb_cash_register_sessions(id),
    CONSTRAINT fk_pdv_sales_user
        FOREIGN KEY (user_id) REFERENCES tb_users(id)
);

CREATE INDEX IF NOT EXISTS idx_pdv_sales_session_created
    ON tb_pdv_sales(cash_register_session_id, created_at DESC);

CREATE TABLE IF NOT EXISTS tb_pdv_sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL,
    product_id UUID,
    sku VARCHAR(60),
    barcode VARCHAR(20),
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(10),
    unit_price NUMERIC(15,2) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL,
    line_total NUMERIC(15,2) NOT NULL,
    CONSTRAINT fk_pdv_sale_items_sale
        FOREIGN KEY (sale_id) REFERENCES tb_pdv_sales(id),
    CONSTRAINT fk_pdv_sale_items_product
        FOREIGN KEY (product_id) REFERENCES tb_products(id)
);

CREATE INDEX IF NOT EXISTS idx_pdv_sale_items_sale
    ON tb_pdv_sale_items(sale_id);
