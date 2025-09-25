-- Create wallets table
CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id VARCHAR(50) NOT NULL UNIQUE,
    organization_id UUID NOT NULL,
    user_ref VARCHAR(100),
    wallet_type VARCHAR(10) NOT NULL CHECK (wallet_type IN ('FIAT', 'CRYPTO')),
    currency VARCHAR(10) NOT NULL,
    balance DECIMAL(19,8) NOT NULL DEFAULT 0,
    available_balance DECIMAL(19,8) NOT NULL DEFAULT 0,
    reserved_balance DECIMAL(19,8) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED')),
    blockchain_address VARCHAR(255),
    blockchain_network VARCHAR(50),
    bank_account_number VARCHAR(20),
    bank_code VARCHAR(10),
    bank_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    external_reference VARCHAR(100),
    organization_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'INTERNAL_TRANSFER', 'EXTERNAL_TRANSFER')),
    source_wallet_id UUID,
    destination_wallet_id UUID,
    external_address VARCHAR(255),
    bank_account_number VARCHAR(20),
    bank_code VARCHAR(10),
    bank_name VARCHAR(100),
    amount DECIMAL(19,8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    fee DECIMAL(19,8) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    blockchain_hash VARCHAR(255),
    blockchain_network VARCHAR(50),
    payment_gateway VARCHAR(50),
    gateway_reference VARCHAR(100),
    description VARCHAR(500),
    failure_reason VARCHAR(500),
    webhook_sent BOOLEAN NOT NULL DEFAULT FALSE,
    webhook_attempts INTEGER NOT NULL DEFAULT 0,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (source_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,
    FOREIGN KEY (destination_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_wallets_organization_id ON wallets(organization_id);
CREATE INDEX idx_wallets_user_ref ON wallets(user_ref);
CREATE INDEX idx_wallets_wallet_id ON wallets(wallet_id);
CREATE INDEX idx_wallets_status ON wallets(status);
CREATE INDEX idx_wallets_currency ON wallets(currency);
CREATE INDEX idx_wallets_org_user_currency ON wallets(organization_id, user_ref, currency);

CREATE INDEX idx_transactions_organization_id ON transactions(organization_id);
CREATE INDEX idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_external_reference ON transactions(external_reference);
CREATE INDEX idx_transactions_source_wallet_id ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_destination_wallet_id ON transactions(destination_wallet_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_webhook_pending ON transactions(status, webhook_sent, webhook_attempts) WHERE webhook_sent = FALSE AND webhook_attempts < 5;

-- Add triggers to update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
