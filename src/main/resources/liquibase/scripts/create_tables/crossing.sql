--changeset x3imal: 51
CREATE TABLE crossing
(
    id              UUID DEFAULT gen_random_uuid() NOT NULL,
    passes_id       UUID                           NOT NULL,
    checkpoint_id   UUID                           NOT NULL,
    local_date_time TIMESTAMPTZ                    NOT NULL,
    direction       VARCHAR(30)                    NOT NULL,
    PRIMARY KEY (id)
);


