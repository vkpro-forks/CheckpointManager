ALTER TABLE passes
    DROP CONSTRAINT pass_territory_fk;
GO

ALTER TABLE passes
    ADD CONSTRAINT pass_territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id) ON DELETE CASCADE;

GO