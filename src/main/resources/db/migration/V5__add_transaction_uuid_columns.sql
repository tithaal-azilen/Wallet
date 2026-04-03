-- ============================================================
-- V5: Add denormalized identity columns to wallet_transaction
-- ============================================================
-- These columns allow fast stateless reporting without joins.
-- Initially nullable for backfill.
-- ============================================================

ALTER TABLE wallet_transaction
    ADD COLUMN IF NOT EXISTS user_id   UUID NULL,
    ADD COLUMN IF NOT EXISTS tenant_id UUID NULL;

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_wallet_tx_user_id   ON wallet_transaction (user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_tenant_id ON wallet_transaction (tenant_id);

-- ============================================================
-- Backfill of these columns is handled in Phase 5 scripts.
-- ============================================================
