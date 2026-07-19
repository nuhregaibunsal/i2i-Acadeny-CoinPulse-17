CREATE TABLE IF NOT EXISTS cash_transactions (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(10)   NOT NULL CHECK (type IN ('DEPOSIT', 'SEND', 'RECEIVE')),
    counterparty VARCHAR(50),
    amount       NUMERIC(20,2) NOT NULL CHECK (amount > 0),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cash_transactions_user
    ON cash_transactions(user_id, created_at DESC);
