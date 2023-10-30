-- liquibase formatted sql

-- changeset ldv236:15
CREATE TABLE user_territory
(
    user_id      UUID,
    territory_id UUID
);

-- changeset ldv236:15.1
ALTER TABLE user_territory
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_territory
    ADD CONSTRAINT territory_fk FOREIGN KEY (territory_id) REFERENCES territories (id);

ALTER TABLE user_territory
    ADD CONSTRAINT user_territory_pk PRIMARY KEY (user_id, territory_id);