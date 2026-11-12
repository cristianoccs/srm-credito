CREATE TABLE receivables (
    id UUID PRIMARY KEY,
    assignor VARCHAR(160) NOT NULL,
    face_value NUMERIC(19, 6) NOT NULL CHECK (face_value > 0),
    term_months INTEGER NOT NULL CHECK (term_months > 0),
    maturity_date DATE,
    type VARCHAR(32) NOT NULL,
    original_currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_receivables_type CHECK (type IN ('DUPLICATA_MERCANTIL', 'CHEQUE_PRE_DATADO')),
    CONSTRAINT ck_receivables_currency CHECK (original_currency = 'BRL'),
    CONSTRAINT ck_receivables_status CHECK (status IN ('OPEN', 'SETTLED'))
);

CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    quote_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(19, 10) NOT NULL CHECK (rate > 0),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_exchange_pair CHECK (base_currency = 'BRL' AND quote_currency = 'USD')
);

CREATE INDEX idx_exchange_rates_pair_validity
    ON exchange_rates (base_currency, quote_currency, valid_from DESC);

CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    receivable_id UUID NOT NULL UNIQUE,
    idempotency_key VARCHAR(160) NOT NULL UNIQUE,
    face_value_brl NUMERIC(19, 6) NOT NULL,
    present_value_brl NUMERIC(19, 6) NOT NULL,
    discount_brl NUMERIC(19, 6) NOT NULL,
    final_amount NUMERIC(19, 6) NOT NULL,
    payment_currency VARCHAR(3) NOT NULL,
    exchange_rate_id UUID,
    exchange_rate_value NUMERIC(19, 10),
    settled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_settlements_receivable FOREIGN KEY (receivable_id) REFERENCES receivables (id),
    CONSTRAINT fk_settlements_exchange_rate FOREIGN KEY (exchange_rate_id) REFERENCES exchange_rates (id),
    CONSTRAINT ck_settlements_currency CHECK (payment_currency IN ('BRL', 'USD')),
    CONSTRAINT ck_settlements_amounts CHECK (face_value_brl > 0 AND present_value_brl > 0 AND final_amount > 0)
);

CREATE INDEX idx_settlements_settled_at ON settlements (settled_at DESC);
CREATE INDEX idx_settlements_payment_currency ON settlements (payment_currency);
