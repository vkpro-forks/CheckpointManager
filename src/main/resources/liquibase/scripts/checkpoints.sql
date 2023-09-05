-- liquibase formatted sql

-- changeset Ldv236:7 context:ignore
CREATE TABLE checkpoints
(
    id   int GENERATED ALWAYS AS IDENTITY NOT NULL,
    name text                             NOT NULL,
    type text,
    note text,

    CONSTRAINT checkpoints_pk PRIMARY KEY (id)
);

-- changeset Ldv236:9 context:ignore
ALTER TABLE checkpoints
    ADD COLUMN added_at timestamp;

-- changeset Ldv236:14.1 context:ignore
ALTER TABLE checkpoints
    ALTER COLUMN added_at TYPE date;

-- changeset Ldv236:14.2 context:ignore
ALTER TABLE checkpoints
    ADD COLUMN territory_id INTEGER;

ALTER TABLE checkpoints
    ADD CONSTRAINT checkpoint_territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id);

-- changeset Ldv236:14.3 context:ignore
DELETE
FROM checkpoints
WHERE 1 = 1;

ALTER TABLE checkpoints
    ALTER COLUMN territory_id SET NOT NULL;

INSERT INTO checkpoints (name, type, note, added_at, territory_id)
VALUES ('kpp1', 'UNIVERSAL', 'описание1', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp2', 'AUTO', 'описание2', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp3', 'PEDESTRIAN', 'описание3', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp4', 'AUTO', 'описание4', '2023-08-27',
        (select id from territories where name = 'territory2'));

