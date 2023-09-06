-- liquibase formatted sql

-- changeset Ldv236:14.3
DELETE
FROM territories
WHERE 1 = 1;

INSERT INTO territories (name, note, added_at)
VALUES ('territory1', 'описание1', '2023-08-26'),
       ('territory2', 'описание2', '2023-08-27'),
       ('territory3', 'описание3', '2023-08-28');

-- changeset Ldv236:26
DELETE FROM territories WHERE 1=1;

INSERT INTO territories (name, note, added_at)
VALUES ('territory1', 'описание1', '2023-08-26'),
       ('territory2', 'описание2', '2023-08-27'),
       ('territory3', 'описание3', '2023-08-28');