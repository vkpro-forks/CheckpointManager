-- liquibase formatted sql

-- changeset x3imal:74
CREATE TABLE persons
(
    id           UUID DEFAULT gen_random_uuid(),
    full_name    VARCHAR(255) NOT NULL,
    person_phone VARCHAR(20),
    note         TEXT,

    CONSTRAINT person_pk PRIMARY KEY (id)
);

