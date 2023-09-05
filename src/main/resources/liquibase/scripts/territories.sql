-- liquibase formatted sql

-- changeset Ldv236:14 context:ignore
CREATE TABLE territories
(
    id       int GENERATED ALWAYS AS IDENTITY NOT NULL,
    name     text                             NOT NULL,
    note     text,
    added_at date,

    CONSTRAINT territory_pk PRIMARY KEY (id)
);

-- changeset Ldv236:14.3 context:ignore
DELETE
FROM territories
WHERE 1 = 1;

INSERT INTO territories (name, note, added_at)
VALUES ('territory1', 'описание1', '2023-08-26'),
       ('territory2', 'описание2', '2023-08-27'),
       ('territory3', 'описание3', '2023-08-28');

