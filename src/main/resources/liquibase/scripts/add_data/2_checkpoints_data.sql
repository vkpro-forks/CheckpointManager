-- liquibase formatted sql

-- changeset Ldv236:14.3
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

-- changeset Ldv236:26
DELETE FROM checkpoints WHERE 1=1;

INSERT INTO checkpoints (name, type, note, added_at, territory_id)
VALUES ('kpp1', 'UNIVERSAL', 'описание1', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp2', 'AUTO', 'описание2', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp3', 'PEDESTRIAN', 'описание3', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp4', 'AUTO', 'описание4', '2023-08-27',
        (select id from territories where name = 'territory2'));

