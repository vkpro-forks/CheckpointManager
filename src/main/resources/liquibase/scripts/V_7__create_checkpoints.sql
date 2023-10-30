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