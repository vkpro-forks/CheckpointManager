--changeset x3imal: 51.1
ALTER TABLE crossing
    ADD CONSTRAINT fk_crossing_passes
        FOREIGN KEY (passes_id) REFERENCES passes(id)
            ON DELETE CASCADE;

--changeset x3imal: 51.2
ALTER TABLE crossing
    ADD CONSTRAINT fk_crossing_checkpoints
        FOREIGN KEY (checkpoint_id) REFERENCES checkpoints(id)
            ON DELETE CASCADE;
