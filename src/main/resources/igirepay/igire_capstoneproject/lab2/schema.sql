-- IGIREPAY Capstone - Lab 2 (JDBC + PostgreSQL)
-- NOTE: This schema uses UUID primary keys to match Lab2 DAO code.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS processed_requests;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE accounts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,

    pin VARCHAR(10) NOT NULL,
    is_locked BOOLEAN DEFAULT FALSE,
    pin_attempts INT DEFAULT 0,

    daily_withdrawn_amount DECIMAL(15,2) DEFAULT 0.00,
    last_withdrawal_date DATE DEFAULT CURRENT_DATE,

    CONSTRAINT fk_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE CASCADE
);

CREATE TABLE transactions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id uuid NOT NULL,

    reference_id uuid UNIQUE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,

    amount DECIMAL(15,2) NOT NULL,
    fee DECIMAL(15,2) NOT NULL,

    source_account_number VARCHAR(50) NOT NULL,
    target_account_number VARCHAR(50) NOT NULL,

    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE
);

CREATE TABLE processed_requests (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_id uuid UNIQUE NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

