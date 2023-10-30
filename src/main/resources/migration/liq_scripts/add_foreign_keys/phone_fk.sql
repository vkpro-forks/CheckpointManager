-- liquibase formatted sql

-- -- changeset fifimova:22
-- ALTER TABLE phones
--     ADD CONSTRAINT phone_fk_user FOREIGN KEY (user_id) REFERENCES users (id);

-- -- changeset fifimova:25
-- ALTER TABLE phones
--     DROP CONSTRAINT phone_fk_user;
--
-- ALTER TABLE phones
--     ADD CONSTRAINT phone_fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
