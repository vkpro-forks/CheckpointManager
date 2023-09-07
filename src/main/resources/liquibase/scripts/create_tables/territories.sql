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

-- changeset Ldv236:26
ALTER TABLE territories
    ALTER COLUMN id DROP IDENTITY;
ALTER TABLE territories
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());
ALTER TABLE territories
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

