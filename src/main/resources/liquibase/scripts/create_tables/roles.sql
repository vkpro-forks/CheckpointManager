-- liquibase formatted sql

-- changeset fifimova:43
CREATE TABLE roles
(
    id   UUID DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,

    CONSTRAINT role_pk PRIMARY KEY (id)
);
