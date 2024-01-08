-- liquibase formatted sql

-- changeset x3imal:111
ALTER TABLE territories
    ADD COLUMN avatar_id UUID,
    ADD CONSTRAINT fk_avatar
        FOREIGN KEY (avatar_id)
            REFERENCES avatars (id)
            ON DELETE SET NULL;