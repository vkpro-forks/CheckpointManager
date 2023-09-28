-- liquibase formatted sql

-- changeset fifimova:43
INSERT INTO users (full_name, date_of_birth, email, password, is_blocked, main_number, added_at)
VALUES ('User', '2000-10-10', 'user@chp.com', 'IktnJ4jP6.6jcmWIHXe4Lc6F4G1TuG1Q8ZgFascfIuZPd3AOO', false,
        '89997776655', '2023-09-25'),
       ('Another User', '1999-10-10', 'user2@chp.com', 'IktnJ4jP6.6jcmWIHXe4Lc6F4G1TuG1Q8ZgFascfIuZPd3AOO', false,
        '89997776654', '2023-09-25'),
       ('Admin', '1998-10-10', 'admin@chp.com', 'IktnJ4jP6.6jcmWIHXe4Lc6F4G1TuG1Q8ZgFascfIuZPd3AOO', false,
        '89997776653', '2023-09-25');

