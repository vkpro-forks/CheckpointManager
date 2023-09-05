CREATE TABLE territories
(
    id 					int GENERATED ALWAYS AS IDENTITY NOT NULL,
    name				text NOT NULL,
    note				text,
    added_at            date,

    CONSTRAINT territory_pk PRIMARY KEY (id)
);