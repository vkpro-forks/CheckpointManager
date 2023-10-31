-- liquibase formatted sql

-- changeset Ldv236:26
DELETE
FROM checkpoints
WHERE 1 = 1;
DELETE
FROM territories
WHERE 1 = 1;

ALTER TABLE checkpoints
    ALTER COLUMN id DROP IDENTITY;
ALTER TABLE checkpoints
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());
ALTER TABLE checkpoints
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE checkpoints
    DROP CONSTRAINT checkpoint_territory_fk;

ALTER TABLE territories
    ALTER COLUMN id DROP IDENTITY;
ALTER TABLE territories
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());
ALTER TABLE territories
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE checkpoints
    ALTER COLUMN territory_id TYPE uuid USING (uuid_generate_v4());
ALTER TABLE checkpoints
    ADD CONSTRAINT checkpoint_territory_fk
        FOREIGN KEY (territory_id) REFERENCES territories (id);

INSERT INTO territories (name, note, added_at)
VALUES ('territory1', 'описание1', '2023-08-26'),
       ('territory2', 'описание2', '2023-08-27'),
       ('territory3', 'описание3', '2023-08-28');

INSERT INTO checkpoints (name, type, note, added_at, territory_id)
VALUES ('kpp1', 'UNIVERSAL', 'описание1', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp2', 'AUTO', 'описание2', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp3', 'PEDESTRIAN', 'описание3', '2023-08-26',
        (select id from territories where name = 'territory1')),
       ('kpp4', 'AUTO', 'описание4', '2023-08-27',
        (select id from territories where name = 'territory2'));