-- liquibase formatted sql

-- changeset Ldv236:14
CREATE TABLE territories
(
    id       int GENERATED ALWAYS AS IDENTITY NOT NULL,
    name     text                             NOT NULL,
    note     text,
    added_at date,

    CONSTRAINT territory_pk PRIMARY KEY (id)
);

-- changeset Ldv236:14.1
ALTER TABLE checkpoints
    ALTER COLUMN added_at TYPE date;

-- changeset Ldv236:14.2
ALTER TABLE checkpoints
    ADD COLUMN territory_id INTEGER;

-- changeset Ldv236:14.3
ALTER TABLE checkpoints
    ALTER COLUMN territory_id SET NOT NULL;

-- changeset Ldv236:14.4
ALTER TABLE checkpoints
    ADD CONSTRAINT checkpoint_territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id);

