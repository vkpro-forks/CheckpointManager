-- liquibase formatted sql

-- changeset fifimova:43
CREATE TABLE roles
(
    id   SERIAL NOT NULL,
    name TEXT NOT NULL,

    CONSTRAINT role_pk PRIMARY KEY (id)
);
