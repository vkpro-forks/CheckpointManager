-- liquibase formatted sql

-- changeset x3imal:51
CREATE TABLE crossings
(
    id              UUID DEFAULT gen_random_uuid(),
    pass_id         UUID        NOT NULL,
    checkpoint_id   UUID        NOT NULL,
    local_date_time TIMESTAMPTZ NOT NULL,
    direction       VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
);

