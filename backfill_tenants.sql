ALTER TABLE organizations ADD COLUMN IF NOT EXISTS auth_tenant_id UUID;
UPDATE organizations SET auth_tenant_id = '2c974446-706e-4c40-80c4-f8ef1e37cb4a' WHERE id = 3;
UPDATE organizations SET auth_tenant_id = 'acb13086-64aa-4f25-ad89-173c84836478' WHERE id = 2;
UPDATE organizations SET auth_tenant_id = '38764c9c-f7c1-4a5b-81cc-138662895bee' WHERE id = 4;
