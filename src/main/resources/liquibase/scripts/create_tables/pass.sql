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

-- changeset Ldv236:54
ALTER TABLE passes
ADD COLUMN name text;

-- changeset Ldv236:70
ALTER TABLE passes
    ADD COLUMN person_id UUID,
    ADD COLUMN dtype TEXT;

-- changeset Ldv236:70.1
ALTER TABLE passes
    ADD COLUMN car_id UUID;

-- changeset Ldv236:70.3
CREATE TABLE persons
(
    id UUID DEFAULT gen_random_uuid(),
    name TEXT,
    CONSTRAINT person_pk PRIMARY KEY (id)
);

-- changeset Ldv236:70.4
ALTER TABLE passes
    ALTER COLUMN added_at DROP NOT NULL;
