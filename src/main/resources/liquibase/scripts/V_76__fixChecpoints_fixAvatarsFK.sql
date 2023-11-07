-- liquibase formatted sql

-- changeset x3imal:76
UPDATE checkpoints
SET type = 'WALK'
WHERE type = 'PEDESTRIAN';

-- changeset x3imal:76.1
DELETE FROM checkpoints
WHERE type = 'AUTO' AND id = '16820297-5469-4a40-ab97-d44ebc7dc861';
