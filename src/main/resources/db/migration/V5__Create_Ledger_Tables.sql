-- Create organization ledgers table
CREATE TABLE organization_ledgers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ledger_id VARCHAR(50) NOT NULL UNIQUE,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    ledger_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_multi_currency BOOLEAN NOT NULL DEFAULT true,
    default_currency VARCHAR(10) DEFAULT 'NGN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create fiat balances table
CREATE TABLE fiat_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL UNIQUE REFERENCES wallets(id) ON DELETE CASCADE,
    currency VARCHAR(10) NOT NULL,
    total_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    reserved_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    pending_credit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    pending_debit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    last_transaction_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create crypto balances table
CREATE TABLE crypto_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL UNIQUE REFERENCES wallets(id) ON DELETE CASCADE,
    currency VARCHAR(10) NOT NULL,
    blockchain_network VARCHAR(50) NOT NULL,
    total_balance DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    available_balance DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    reserved_balance DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    pending_credit DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    pending_debit DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    staked_balance DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    minimum_balance DECIMAL(19,8) NOT NULL DEFAULT 0.00000000,
    last_block_height BIGINT,
    last_transaction_hash VARCHAR(100),
    last_transaction_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create ledger entries table
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id VARCHAR(50) NOT NULL UNIQUE,
    organization_ledger_id UUID NOT NULL REFERENCES organization_ledgers(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    description VARCHAR(500) NOT NULL,
    reference VARCHAR(100),
    external_reference VARCHAR(100),
    
    -- Separate balance tracking for fiat and crypto
    fiat_balance_before DECIMAL(19,2),
    fiat_balance_after DECIMAL(19,2),
    crypto_balance_before DECIMAL(19,8),
    crypto_balance_after DECIMAL(19,8),
    
    -- Blockchain specific fields
    blockchain_network VARCHAR(50),
    blockchain_hash VARCHAR(100),
    block_number BIGINT,
    gas_fee DECIMAL(19,8),
    
    -- Payment gateway specific fields
    payment_gateway VARCHAR(50),
    gateway_reference VARCHAR(100),
    gateway_fee DECIMAL(19,2),
    
    entry_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for organization ledgers
CREATE INDEX idx_org_ledger_organization ON organization_ledgers(organization_id);
CREATE INDEX idx_org_ledger_status ON organization_ledgers(status);
CREATE INDEX idx_org_ledger_currency ON organization_ledgers(default_currency);

-- Create indexes for fiat balances
CREATE INDEX idx_fiat_balance_wallet ON fiat_balances(wallet_id);
CREATE INDEX idx_fiat_balance_currency ON fiat_balances(currency);

-- Create indexes for crypto balances
CREATE INDEX idx_crypto_balance_wallet ON crypto_balances(wallet_id);
CREATE INDEX idx_crypto_balance_currency ON crypto_balances(currency);
CREATE INDEX idx_crypto_balance_network ON crypto_balances(blockchain_network);
CREATE INDEX idx_crypto_balance_hash ON crypto_balances(last_transaction_hash);

-- Create indexes for ledger entries
CREATE INDEX idx_ledger_entry_organization ON ledger_entries(organization_ledger_id);
CREATE INDEX idx_ledger_entry_wallet ON ledger_entries(wallet_id);
CREATE INDEX idx_ledger_entry_transaction ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entry_date ON ledger_entries(entry_date);
CREATE INDEX idx_ledger_entry_currency ON ledger_entries(currency);
CREATE INDEX idx_ledger_entry_type ON ledger_entries(entry_type);
CREATE INDEX idx_ledger_entry_tx_type ON ledger_entries(transaction_type);
CREATE INDEX idx_ledger_entry_blockchain ON ledger_entries(blockchain_network);
CREATE INDEX idx_ledger_entry_gateway ON ledger_entries(payment_gateway);
CREATE INDEX idx_ledger_entry_reference ON ledger_entries(reference);
CREATE INDEX idx_ledger_entry_external_ref ON ledger_entries(external_reference);

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_organization_ledgers_updated_at 
    BEFORE UPDATE ON organization_ledgers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_fiat_balances_updated_at 
    BEFORE UPDATE ON fiat_balances 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_crypto_balances_updated_at 
    BEFORE UPDATE ON crypto_balances 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
