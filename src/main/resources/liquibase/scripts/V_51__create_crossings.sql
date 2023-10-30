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

-- changeset x3imal:51.1
ALTER TABLE crossings
    ADD CONSTRAINT fk_crossing_passes
        FOREIGN KEY (pass_id) REFERENCES passes (id)
            ON DELETE CASCADE;

-- changeset x3imal:51.2
ALTER TABLE crossings
    ADD CONSTRAINT fk_crossing_checkpoints
        FOREIGN KEY (checkpoint_id) REFERENCES checkpoints (id)
            ON DELETE CASCADE;