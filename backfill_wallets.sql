-- ============================================================
-- Phase 5: Backfill wallet table with new UUIDs
-- ============================================================

UPDATE wallet w
SET user_id = u.auth_user_id,
    tenant_id = o.auth_tenant_id
FROM users u
LEFT JOIN organizations o ON o.id = u.organization_id
WHERE w.user_id_old = u.id;

-- After verification, run:
-- ALTER TABLE wallet ALTER COLUMN user_id SET NOT NULL;
