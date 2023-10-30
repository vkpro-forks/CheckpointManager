-- liquibase formatted sql

-- changeset Ldv236:41
CREATE TABLE passes
(
    id           UUID DEFAULT gen_random_uuid(),
    user_id      UUID      NOT NULL,
    status       text      NOT NULL,
    type_time    text      NOT NULL,
    territory_id UUID      NOT NULL,
    note         text,
    added_at     timestamp NOT NULL,
    start_time   timestamp NOT NULL,
    end_time     timestamp NOT NULL,

    CONSTRAINT pass_pk PRIMARY KEY (id),
    CONSTRAINT check_time CHECK (end_time > start_time)
);

-- changeset Ldv236:41.1
ALTER TABLE passes
    ADD CONSTRAINT pass_user_fk FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT pass_territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id);