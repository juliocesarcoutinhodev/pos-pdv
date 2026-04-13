CREATE TABLE IF NOT EXISTS tb_label_print_jobs (
    id UUID PRIMARY KEY,
    reference_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tb_label_print_job_items (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(60) NOT NULL,
    barcode VARCHAR(20),
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    sale_price NUMERIC(15,2) NOT NULL,
    promotional_price NUMERIC(15,2),
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_label_print_items_job FOREIGN KEY (job_id) REFERENCES tb_label_print_jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_label_print_items_product FOREIGN KEY (product_id) REFERENCES tb_products(id)
);

CREATE INDEX IF NOT EXISTS idx_label_print_jobs_reference_date ON tb_label_print_jobs(reference_date);
CREATE INDEX IF NOT EXISTS idx_label_print_items_job_id ON tb_label_print_job_items(job_id);
CREATE INDEX IF NOT EXISTS idx_label_print_items_product_id ON tb_label_print_job_items(product_id);
