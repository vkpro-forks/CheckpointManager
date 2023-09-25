-- liquibase formatted sql

-- changeset fifimova:43
ALTER TABLE users_roles
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_roles
    ADD CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES roles (id);
