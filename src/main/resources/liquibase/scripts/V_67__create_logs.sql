-- liquibase formatted sql

-- changeset Ldv236:67
CREATE TABLE logs
(
    time    timestamp,
    logger  VARCHAR(254),
    level   VARCHAR(254),
    message VARCHAR(254)
);