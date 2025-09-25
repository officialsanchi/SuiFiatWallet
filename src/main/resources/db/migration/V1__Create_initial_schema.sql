-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('ADMIN', 'DEVELOPER', 'FINANCE');
CREATE TYPE user_status AS ENUM ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED');
CREATE TYPE environment_type AS ENUM ('SANDBOX', 'LIVE');
CREATE TYPE kyc_status AS ENUM ('NOT_STARTED', 'PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE key_status AS ENUM ('ACTIVE', 'REVOKED');
CREATE TYPE verification_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    environment environment_type DEFAULT 'SANDBOX',
    kyc_status kyc_status DEFAULT 'NOT_STARTED',
    country VARCHAR(100),
    keycloak_group_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_user_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(75) NOT NULL,
    last_name VARCHAR(75) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role user_role DEFAULT 'ADMIN',
    status user_status DEFAULT 'PENDING_VERIFICATION',
    org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- API Keys table
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
    environment environment_type NOT NULL,
    public_key VARCHAR(255) UNIQUE NOT NULL,
    secret_key VARCHAR(255) UNIQUE NOT NULL,
    status key_status DEFAULT 'ACTIVE',
    name VARCHAR(100) DEFAULT 'Default Key',
    last_used TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- KYC Documents table
CREATE TABLE kyc_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    document_url TEXT NOT NULL,
    verification_status verification_status DEFAULT 'PENDING',
    verification_notes TEXT,
    uploaded_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Setup Checklist table
CREATE TABLE setup_checklist (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
    api_keys_generated BOOLEAN DEFAULT FALSE,
    test_wallet_created BOOLEAN DEFAULT FALSE,
    api_docs_explored BOOLEAN DEFAULT FALSE,
    kyc_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_keycloak_id ON users(keycloak_user_id);
CREATE INDEX idx_users_org_id ON users(org_id);
CREATE INDEX idx_api_keys_org_id ON api_keys(org_id);
CREATE INDEX idx_api_keys_public_key ON api_keys(public_key);
CREATE INDEX idx_api_keys_secret_key ON api_keys(secret_key);
CREATE INDEX idx_kyc_documents_org_id ON kyc_documents(org_id);
CREATE INDEX idx_organizations_kyc_status ON organizations(kyc_status);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_kyc_documents_updated_at BEFORE UPDATE ON kyc_documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_setup_checklist_updated_at BEFORE UPDATE ON setup_checklist
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
