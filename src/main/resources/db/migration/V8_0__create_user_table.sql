CREATE TABLE "user"
(
    id            BIGSERIAL NOT NULL,
    full_name     VARCHAR(255),
    date_of_birth DATE,
    email         VARCHAR(255),
    password      VARCHAR(255),
    is_blocked    BOOLEAN
);

ALTER TABLE "user"
    ADD CONSTRAINT user_id PRIMARY KEY (id);

