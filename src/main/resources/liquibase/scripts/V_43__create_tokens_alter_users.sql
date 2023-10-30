-- liquibase formatted sql

-- changeset fifimova:43
CREATE TABLE tokens
(
    id         UUID DEFAULT gen_random_uuid(),
    token      TEXT UNIQUE NOT NULL,
    token_type TEXT        NOT NULL,
    revoked    BOOLEAN,
    expired    BOOLEAN,
    user_id    UUID        NOT NULL,

    CONSTRAINT token_pk PRIMARY KEY (id)

);

-- changeset fifimova:43.1
ALTER TABLE users
    ADD CONSTRAINT number_unique UNIQUE (main_number);

-- changeset fifimova:43.2
ALTER TABLE tokens
    ADD CONSTRAINT token_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE;

-- changeset fifimova:43.3
INSERT INTO users (full_name, date_of_birth, email, password, is_blocked, role, main_number, added_at)
VALUES ('User', '2000-10-10', 'user@chp.com', '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false,
        'USER',
        '89997776655', '2023-09-25'),
       ('Manager', '1999-10-10', 'manager@chp.com', '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2',
        false, 'MANAGER',
        '89997776654', '2023-09-25'),
       ('Admin', '1998-10-10', 'admin@chp.com', '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false,
        'ADMIN',
        '89897776653', '2023-09-25'),
       ('Security', '1998-10-10', 'security@chp.com', '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2',
        false, 'SECURITY',
        '89117776653', '2023-09-25');