-- liquibase formatted sql

-- changeset Ldv236:26
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
