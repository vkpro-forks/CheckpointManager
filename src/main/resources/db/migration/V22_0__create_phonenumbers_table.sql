CREATE TABLE phone_numbers
(
    id      UUID DEFAULT gen_random_uuid(),
    number  TEXT NOT NULL,
    type    TEXT,
    user_id UUID NOT NULL ,
    note    TEXT,

    CONSTRAINT phone_number_pk PRIMARY KEY (id),
    CONSTRAINT phone_number_fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);


