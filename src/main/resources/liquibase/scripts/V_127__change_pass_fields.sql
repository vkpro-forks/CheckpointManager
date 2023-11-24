-- liquibase formatted sql

-- changeset ldv236:127
ALTER TABLE passes
    DROP COLUMN note;

ALTER TABLE passes
    RENAME COLUMN name TO comment;
