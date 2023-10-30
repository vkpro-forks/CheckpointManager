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

-- changeset Ldv236:14.5
DELETE
FROM territories
WHERE 1 = 1;

INSERT INTO territories (name, note, added_at)
VALUES ('territory1', 'описание1', '2023-08-26'),
       ('territory2', 'описание2', '2023-08-27'),
       ('territory3', 'описание3', '2023-08-28');

-- changeset Ldv236:14.6
DELETE
FROM checkpoints
WHERE 1 = 1;

INSERT INTO checkpoints (name, type, note, added_at, territory_id)
VALUES ('kpp1', 'UNIVERSAL', 'описание1', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp2', 'AUTO', 'описание2', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp3', 'PEDESTRIAN', 'описание3', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp4', 'AUTO', 'описание4', '2023-08-27',
        (select id from territories where name = 'territory2'));
