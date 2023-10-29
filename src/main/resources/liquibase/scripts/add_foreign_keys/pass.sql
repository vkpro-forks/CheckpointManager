-- liquibase formatted sql

-- changeset Ldv236:41.1
ALTER TABLE passes
    ADD CONSTRAINT pass_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT pass_territory_fk FOREIGN KEY (territory_id) REFERENCES territories(id);


-- changeset x3imal:74.1
ALTER TABLE passes
    ADD CONSTRAINT pass_person_fk FOREIGN KEY (person_id)
        REFERENCES persons (id);

-- changeset Ldv236:70.2
ALTER TABLE passes
    ADD CONSTRAINT pass_car_fk FOREIGN KEY (car_id) REFERENCES cars(id),
    ADD CONSTRAINT pass_person_fk FOREIGN KEY (person_id) REFERENCES persons(id);