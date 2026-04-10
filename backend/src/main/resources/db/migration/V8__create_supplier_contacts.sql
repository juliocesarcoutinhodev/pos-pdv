CREATE TABLE IF NOT EXISTS tb_contacts (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(120) NOT NULL,
    email   VARCHAR(255),
    phone   VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS tb_supplier_contacts (
    supplier_id  UUID NOT NULL,
    contact_id   UUID NOT NULL,
    PRIMARY KEY (supplier_id, contact_id),
    CONSTRAINT uq_supplier_contacts_contact UNIQUE (contact_id),
    CONSTRAINT fk_supplier_contacts_supplier FOREIGN KEY (supplier_id) REFERENCES tb_suppliers(id) ON DELETE CASCADE,
    CONSTRAINT fk_supplier_contacts_contact FOREIGN KEY (contact_id) REFERENCES tb_contacts(id) ON DELETE CASCADE
);

CREATE INDEX idx_supplier_contacts_supplier ON tb_supplier_contacts(supplier_id);
CREATE INDEX idx_supplier_contacts_contact ON tb_supplier_contacts(contact_id);
