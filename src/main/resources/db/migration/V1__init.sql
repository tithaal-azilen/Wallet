CREATE SCHEMA IF NOT EXISTS wallet;

CREATE TABLE IF NOT EXISTS wallet.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet.wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance NUMERIC(38, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES wallet.users(id)
);

CREATE TABLE IF NOT EXISTS wallet.wallet_transaction (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    description VARCHAR(255),
    balance_after NUMERIC(38, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallet.wallet(id)
);
