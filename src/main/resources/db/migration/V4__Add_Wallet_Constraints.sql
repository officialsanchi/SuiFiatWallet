-- Add additional constraints and validations

-- Ensure balance consistency
ALTER TABLE wallets ADD CONSTRAINT chk_balance_consistency 
    CHECK (balance = available_balance + reserved_balance);

-- Ensure positive balances
ALTER TABLE wallets ADD CONSTRAINT chk_positive_balance 
    CHECK (balance >= 0);

ALTER TABLE wallets ADD CONSTRAINT chk_positive_available_balance 
    CHECK (available_balance >= 0);

ALTER TABLE wallets ADD CONSTRAINT chk_positive_reserved_balance 
    CHECK (reserved_balance >= 0);

-- Ensure positive transaction amounts
ALTER TABLE transactions ADD CONSTRAINT chk_positive_amount 
    CHECK (amount > 0);

ALTER TABLE transactions ADD CONSTRAINT chk_positive_fee 
    CHECK (fee >= 0);

-- Ensure blockchain fields are set for crypto wallets
-- Note: This would be enforced at application level for flexibility

-- Ensure bank fields are set for fiat wallets
-- Note: This would be enforced at application level for flexibility

-- Unique constraint for organization + user_ref + currency + wallet_type
ALTER TABLE wallets ADD CONSTRAINT uk_wallet_org_user_currency_type 
    UNIQUE (organization_id, user_ref, currency, wallet_type);

-- Ensure external reference uniqueness when provided
CREATE UNIQUE INDEX idx_transactions_external_reference_unique 
    ON transactions(external_reference) 
    WHERE external_reference IS NOT NULL;

-- Ensure blockchain hash uniqueness when provided
CREATE UNIQUE INDEX idx_transactions_blockchain_hash_unique 
    ON transactions(blockchain_hash) 
    WHERE blockchain_hash IS NOT NULL;

-- Ensure gateway reference uniqueness when provided
CREATE UNIQUE INDEX idx_transactions_gateway_reference_unique 
    ON transactions(gateway_reference) 
    WHERE gateway_reference IS NOT NULL;
