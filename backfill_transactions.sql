-- ============================================================
-- Phase 5: Backfill wallet_transaction table with denormalized UUIDs
-- ============================================================

UPDATE wallet_transaction t
SET user_id = w.user_id,
    tenant_id = w.tenant_id
FROM wallet w
WHERE t.wallet_id = w.id;

-- After verification, run:
-- ALTER TABLE wallet_transaction ALTER COLUMN user_id SET NOT NULL;
-- ALTER TABLE wallet_transaction ALTER COLUMN tenant_id SET NOT NULL;
