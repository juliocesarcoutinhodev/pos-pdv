INSERT INTO tb_roles (id, name)
VALUES (gen_random_uuid(), 'USER')
ON CONFLICT DO NOTHING;

INSERT INTO tb_roles (id, name)
VALUES (gen_random_uuid(), 'ADMIN')
ON CONFLICT DO NOTHING;
