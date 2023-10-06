-- liquibase formatted sql

-- changeset fifimova:8
CREATE TABLE users
(
    id            BIGSERIAL NOT NULL,
    full_name     VARCHAR(255),
    date_of_birth DATE,
    email         VARCHAR(255),
    password      VARCHAR(255),
    is_blocked    BOOLEAN
);

ALTER TABLE users
    ADD CONSTRAINT user_pk PRIMARY KEY (id);

-- changeset fifimova:11.1
ALTER TABLE users
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT email_unique UNIQUE (email);

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN id DROP DEFAULT;

ALTER TABLE users
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());

ALTER TABLE users
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- changeset fifimova:19
ALTER TABLE users
    ADD COLUMN role TEXT;

-- changeset fifimova:21
ALTER TABLE users
    ALTER COLUMN is_blocked SET DEFAULT false;

-- changeset fifimova:25
ALTER TABLE users
    ADD COLUMN main_number TEXT;

-- changeset fifimova:25.1
ALTER TABLE users
    ADD COLUMN added_at timestamp;

--changeset fifimova:43.1
ALTER TABLE users
    ADD CONSTRAINT number_unique UNIQUE (main_number);


