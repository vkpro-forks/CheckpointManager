-- liquibase formatted sql

-- changeset Rndmi:66
CREATE TABLE avatars (
    avatar_holder UUID,
    media_type    VARCHAR(50),
    file_path     VARCHAR(255),
    file_size     BIGSERIAL,
    preview       BYTEA,

    CONSTRAINT avatar_pk PRIMARY KEY (avatar_holder)
);
