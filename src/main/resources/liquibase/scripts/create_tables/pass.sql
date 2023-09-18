-- liquibase formatted sql

-- changeset Ldv236:41
CREATE TABLE passes
(
    id              UUID DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    status          text NOT NULL,
    type_time       text NOT NULL,
    territory_id    UUID NOT NULL,
    note            text,
    added_at        timestamp NOT NULL,
    start_time      timestamp NOT NULL,
    end_time        timestamp NOT NULL,

    CONSTRAINT pass_pk PRIMARY KEY (id),
    CONSTRAINT check_time CHECK (end_time > start_time)
);