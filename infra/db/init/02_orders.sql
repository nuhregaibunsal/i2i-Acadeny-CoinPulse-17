CREATE TABLE IF NOT EXISTS pending_orders (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(4)     NOT NULL CHECK (type IN ('BUY', 'SELL')),
    asset_symbol VARCHAR(20)    NOT NULL,
    direction    VARCHAR(5)     NOT NULL CHECK (direction IN ('ABOVE', 'BELOW')),
    target_price NUMERIC(20,2)  NOT NULL CHECK (target_price >= 0),
    volume       NUMERIC(30,10) NOT NULL CHECK (volume > 0),
    status       VARCHAR(10)    NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_pending_orders_status ON pending_orders(status);
CREATE INDEX IF NOT EXISTS idx_pending_orders_user ON pending_orders(user_id, created_at DESC);
