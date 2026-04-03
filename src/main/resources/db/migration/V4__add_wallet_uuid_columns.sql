-- ============================================================
-- V4: Add UUID identity columns to wallet table
-- ============================================================
-- These columns link each wallet to the Auth Service's user/tenant UUIDs.
-- The columns are nullable initially to allow the backfill phase (Phase 5)
-- to populate existing rows from the CSV mapping files before adding NOT NULL.
-- ============================================================

-- 1. Rename legacy Long user_id
ALTER TABLE wallet RENAME COLUMN user_id TO user_id_old;
ALTER TABLE wallet ALTER COLUMN user_id_old DROP NOT NULL;

-- 2. Add new UUID columns
ALTER TABLE wallet
    ADD COLUMN user_id   UUID NULL,
    ADD COLUMN tenant_id UUID NULL;


-- Index for fast per-user wallet lookups
CREATE INDEX IF NOT EXISTS idx_wallet_user_id   ON wallet (user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tenant_id ON wallet (tenant_id);

-- ============================================================
-- NOTE: After running the data backfill (see backfill scripts),
-- enforce NOT NULL on user_id:
--
--   ALTER TABLE wallet ALTER COLUMN user_id SET NOT NULL;
--
-- tenant_id may remain nullable for non-org users.
-- ============================================================
