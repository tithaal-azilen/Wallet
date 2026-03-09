-- Add the recipient_wallet_id column if it doesn't already exist (in case Hibernate auto-create ran first)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'wallet' 
                   AND table_name = 'wallet_transaction' 
                   AND column_name = 'recipient_wallet_id') THEN
        ALTER TABLE wallet.wallet_transaction ADD COLUMN recipient_wallet_id BIGINT;
        
        -- Add foreign key constraint
        ALTER TABLE wallet.wallet_transaction
        ADD CONSTRAINT fk_recipient_wallet
        FOREIGN KEY (recipient_wallet_id)
        REFERENCES wallet.wallet (id);
    END IF;
END $$;

-- Backfill the recipient_wallet_id by parsing the description
-- The description format for transfers is: "Transfer to wallet id: X"
UPDATE wallet.wallet_transaction
SET recipient_wallet_id = CAST(SUBSTRING(description FROM 'Transfer to wallet id: ([0-9]+)') AS BIGINT)
WHERE description LIKE 'Transfer to wallet id: %'
  AND type = 'DEBIT'
  AND recipient_wallet_id IS NULL;
