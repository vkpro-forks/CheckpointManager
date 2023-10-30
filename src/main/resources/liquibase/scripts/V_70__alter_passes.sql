-- liquibase formatted sql

-- changeset Ldv236:70
ALTER TABLE passes
    ADD COLUMN car_id    UUID,
    ADD COLUMN person_id UUID,
    ADD COLUMN dtype     TEXT;

-- changeset Ldv236:70.1
CREATE TABLE persons
(
    id   UUID DEFAULT gen_random_uuid(),
    name TEXT,
    CONSTRAINT person_pk PRIMARY KEY (id)
);

-- changeset Ldv236:70.2
ALTER TABLE passes
    ADD CONSTRAINT pass_car_fk FOREIGN KEY (car_id) REFERENCES cars (id),
    ADD CONSTRAINT pass_person_fk FOREIGN KEY (person_id) REFERENCES persons (id);

-- changeset Ldv236:70.3
ALTER TABLE passes
    ALTER COLUMN added_at DROP NOT NULL;