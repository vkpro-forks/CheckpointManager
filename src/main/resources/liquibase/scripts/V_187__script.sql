ALTER TABLE passes
    DROP CONSTRAINT pass_car_fk;
GO

ALTER TABLE passes
    ADD CONSTRAINT pass_car_fk FOREIGN KEY (car_id) REFERENCES cars (id) ON DELETE SET NULL;

GO