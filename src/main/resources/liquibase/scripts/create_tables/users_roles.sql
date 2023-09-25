-- liquibase formatted sql

-- changeset fifimova:43
CREATE TABLE users_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT user_role PRIMARY KEY (user_id, role_id)
);