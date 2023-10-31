-- liquibase formatted sql

-- -- changeset Ldv236:41.1
-- ALTER TABLE passes
--     ADD CONSTRAINT pass_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
--     ADD CONSTRAINT pass_territory_fk FOREIGN KEY (territory_id) REFERENCES territories(id);