-- =====================================================================
-- CryptoPal — PostgreSQL schema
-- Runs automatically on first container init via docker-entrypoint-initdb.d
-- PostgreSQL is the single source of truth for all persistent financial data.
-- =====================================================================

-- ---------- Users ----------
-- Credentials persisted here (password stored ONLY as a BCrypt hash).
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------- Wallets ----------
-- Fiat/cash balance. One wallet per user. A randomized starting balance
-- is written here at registration time by the backend.
CREATE TABLE IF NOT EXISTS wallets (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    cash_balance NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (cash_balance >= 0),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ---------- Holdings ----------
-- How much of each crypto asset a user currently owns.
CREATE TABLE IF NOT EXISTS holdings (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_symbol VARCHAR(20)    NOT NULL,
    volume       NUMERIC(30,10) NOT NULL DEFAULT 0 CHECK (volume >= 0),
    UNIQUE (user_id, asset_symbol)
);

-- ---------- Transactions ----------
-- Immutable ledger of every completed buy/sell order.
CREATE TABLE IF NOT EXISTS transactions (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(4)     NOT NULL CHECK (type IN ('BUY', 'SELL')),
    asset_symbol VARCHAR(20)    NOT NULL,
    volume       NUMERIC(30,10) NOT NULL CHECK (volume > 0),
    price        NUMERIC(20,2)  NOT NULL CHECK (price >= 0),
    total_value  NUMERIC(20,2)  NOT NULL CHECK (total_value >= 0),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id, created_at DESC);

-- ---------- Price history ----------
-- Periodic price snapshots pushed by the background worker,
-- used later as the trend ledger for AI analysis.
CREATE TABLE IF NOT EXISTS price_history (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_symbol VARCHAR(20)   NOT NULL,
    price        NUMERIC(20,2) NOT NULL CHECK (price >= 0),
    recorded_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_price_history_symbol_time
    ON price_history(asset_symbol, recorded_at DESC);
