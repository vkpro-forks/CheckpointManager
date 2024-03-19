CREATE TYPE currency_enum AS ENUM (
    'RUB', 'USD', 'EUR', 'CNY'
    );

GO

CREATE TABLE donations
(
    id           UUID    DEFAULT gen_random_uuid() PRIMARY KEY,
    amount       NUMERIC                  NOT NULL,
    currency     currency_enum            NOT NULL,
    comment      VARCHAR(128)             NOT NULL,
    confirmed    BOOLEAN DEFAULT FALSE,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description  VARCHAR                  NOT NULL,
    payment_id   UUID,
    user_id      UUID REFERENCES users (id)
)
    GO