-- liquibase formatted sql

-- -- changeset Ldv236:14.2
-- ALTER TABLE checkpoints
--     ADD CONSTRAINT checkpoint_territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id);

-- -- changeset Ldv236:26
-- ALTER TABLE checkpoints
--     DROP CONSTRAINT checkpoint_territory_fk;
--
-- ALTER TABLE checkpoints ADD CONSTRAINT checkpoint_territory_fk
--     FOREIGN KEY (territory_id) REFERENCES territories (id);