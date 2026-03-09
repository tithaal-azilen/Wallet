-- V1__init.sql
-- Baseline database schema for Wallet Application mapped from JPA entities.

-- 1. Create organizations table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    org_code VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 2. Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    phone_number VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    organization_id BIGINT,
    role VARCHAR(255) DEFAULT 'ROLE_USER' NOT NULL,
    status VARCHAR(255) DEFAULT 'ACTIVE' NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- 3. Create wallet table
CREATE TABLE wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance NUMERIC(38, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    next_deduction_date DATE,
    last_deduction_attempt DATE,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create index for wallet
CREATE INDEX idx_wallet_next_deduction_date ON wallet(next_deduction_date);

-- 4. Create wallet_transaction table
CREATE TABLE wallet_transaction (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    description VARCHAR(255),
    balance_after NUMERIC(38, 2) NOT NULL,
    reference_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uk_wallet_transaction_reference UNIQUE (wallet_id, reference_id),
    CONSTRAINT fk_wallet_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallet(id)
);

-- 5. Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);
