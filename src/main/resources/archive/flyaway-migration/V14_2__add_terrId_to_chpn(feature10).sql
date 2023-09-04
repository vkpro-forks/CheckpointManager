ALTER TABLE checkpoints
    ADD COLUMN territory_id INTEGER;

ALTER TABLE checkpoints
    ADD CONSTRAINT checkpoint_territory_fk FOREIGN KEY (territory_id) REFERENCES territories(id);