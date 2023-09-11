-- liquibase formatted sql

-- changeset ldv236:15
CREATE TABLE user_territory
(
    user_id         UUID,
    territory_id    UUID
);