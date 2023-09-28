-- liquibase formatted sql

-- changeset fifimova:43
INSERT INTO roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN');
