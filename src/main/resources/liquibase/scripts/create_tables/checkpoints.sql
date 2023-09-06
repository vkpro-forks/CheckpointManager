-- liquibase formatted sql

-- changeset Ldv236:7
CREATE TABLE checkpoints
(
    id   int GENERATED ALWAYS AS IDENTITY NOT NULL,
    name text                             NOT NULL,
    type text,
    note text,

    CONSTRAINT checkpoints_pk PRIMARY KEY (id)
);

-- changeset Ldv236:9
ALTER TABLE checkpoints
    ADD COLUMN added_at timestamp;

-- changeset Ldv236:14.1
ALTER TABLE checkpoints
    ALTER COLUMN added_at TYPE date;

-- changeset Ldv236:14.2
ALTER TABLE checkpoints
    ADD COLUMN territory_id INTEGER;

-- changeset Ldv236:14.3 c
ALTER TABLE checkpoints
    ALTER COLUMN territory_id SET NOT NULL;

-- changeset Ldv236:26
ALTER TABLE checkpoints
    ALTER COLUMN id DROP IDENTITY;
ALTER TABLE checkpoints
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());
ALTER TABLE checkpoints
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE checkpoints
    ALTER COLUMN territory_id TYPE uuid USING (uuid_generate_v4());