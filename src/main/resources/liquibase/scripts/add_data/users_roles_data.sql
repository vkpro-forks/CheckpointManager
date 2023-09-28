-- liquibase formatted sql

--changeset fifimova:43
INSERT INTO users_roles (user_id, role_id)
VALUES ((select id from users where full_name = 'User'), 1),
       ((select id from users where full_name = 'Another User'), 1),
       ((select id from users where full_name = 'Admin'), 2);

