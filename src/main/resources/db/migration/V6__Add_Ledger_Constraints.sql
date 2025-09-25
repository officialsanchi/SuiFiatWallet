-- Add constraints for organization ledgers
ALTER TABLE organization_ledgers 
ADD CONSTRAINT chk_org_ledger_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'));

ALTER TABLE organization_ledgers 
ADD CONSTRAINT uk_org_ledger_organization UNIQUE (organization_id);

-- Add constraints for fiat balances
ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_total_balance_positive CHECK (total_balance >= 0);

ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_available_balance_positive CHECK (available_balance >= 0);

ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_reserved_balance_positive CHECK (reserved_balance >= 0);

ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_pending_credit_positive CHECK (pending_credit >= 0);

ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_pending_debit_positive CHECK (pending_debit >= 0);

ALTER TABLE fiat_balances 
ADD CONSTRAINT chk_fiat_balance_consistency 
CHECK (total_balance = available_balance + reserved_balance);

-- Add constraints for crypto balances
ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_total_balance_positive CHECK (total_balance >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_available_balance_positive CHECK (available_balance >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_reserved_balance_positive CHECK (reserved_balance >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_staked_balance_positive CHECK (staked_balance >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_minimum_balance_positive CHECK (minimum_balance >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_pending_credit_positive CHECK (pending_credit >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_pending_debit_positive CHECK (pending_debit >= 0);

ALTER TABLE crypto_balances 
ADD CONSTRAINT chk_crypto_balance_consistency 
CHECK (total_balance = available_balance + reserved_balance + staked_balance);

-- Add constraints for ledger entries
ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_amount_positive CHECK (amount > 0);

ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'));

ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_transaction_type 
CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'INTERNAL_TRANSFER', 'EXTERNAL_TRANSFER'));

-- Add constraint to ensure either fiat or crypto balance fields are populated, not both
ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_balance_type 
CHECK (
    (fiat_balance_before IS NOT NULL AND fiat_balance_after IS NOT NULL AND 
     crypto_balance_before IS NULL AND crypto_balance_after IS NULL) 
    OR 
    (crypto_balance_before IS NOT NULL AND crypto_balance_after IS NOT NULL AND 
     fiat_balance_before IS NULL AND fiat_balance_after IS NULL)
);

-- Add constraint for blockchain entries
ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_blockchain_fields 
CHECK (
    (blockchain_network IS NOT NULL AND crypto_balance_before IS NOT NULL) 
    OR 
    (blockchain_network IS NULL AND fiat_balance_before IS NOT NULL)
);

-- Add constraint for payment gateway entries
ALTER TABLE ledger_entries 
ADD CONSTRAINT chk_ledger_gateway_fields 
CHECK (
    (payment_gateway IS NOT NULL AND fiat_balance_before IS NOT NULL) 
    OR 
    (payment_gateway IS NULL)
);

-- Add unique constraint for entry_id
ALTER TABLE ledger_entries 
ADD CONSTRAINT uk_ledger_entry_id UNIQUE (entry_id);

-- Add composite index for efficient querying
CREATE INDEX idx_ledger_entry_wallet_date ON ledger_entries(wallet_id, entry_date DESC);
CREATE INDEX idx_ledger_entry_org_currency ON ledger_entries(organization_ledger_id, currency);
CREATE INDEX idx_ledger_entry_type_date ON ledger_entries(entry_type, entry_date DESC);

-- Add partial indexes for blockchain and gateway entries
CREATE INDEX idx_ledger_entry_blockchain_hash ON ledger_entries(blockchain_hash) 
WHERE blockchain_hash IS NOT NULL;

CREATE INDEX idx_ledger_entry_gateway_ref ON ledger_entries(gateway_reference) 
WHERE gateway_reference IS NOT NULL;
