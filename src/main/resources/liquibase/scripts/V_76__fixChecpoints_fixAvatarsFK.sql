-- liquibase formatted sql

-- changeset x3imal:76
UPDATE checkpoints
SET type = 'WALK'
WHERE type = 'PEDESTRIAN';

-- changeset x3imal:76.1
DELETE
FROM checkpoints
WHERE type = 'AUTO'
  AND name = 'kpp4';

-- changeset x3imal:76.2
DROP TABLE IF EXISTS avatars;
CREATE TABLE avatars
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_type VARCHAR(255),
    file_path  VARCHAR(255),
    file_size  BIGINT,
    preview    BYTEA
);


-- changeset x3imal:76.3
ALTER TABLE users
    ADD COLUMN avatar_id UUID,
    ADD CONSTRAINT fk_avatar
        FOREIGN KEY (avatar_id)
            REFERENCES avatars (id)
            ON DELETE SET NULL;




