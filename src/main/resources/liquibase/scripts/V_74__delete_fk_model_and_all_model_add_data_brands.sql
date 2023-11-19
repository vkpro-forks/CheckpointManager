-- liquibase formatted sql

-- changeset x3imal:74
ALTER TABLE passes DROP CONSTRAINT pass_visitor_fk;
DROP TABLE IF EXISTS visitors;


-- changeset x3imal:74.1
CREATE TABLE visitors
(
    id           UUID DEFAULT gen_random_uuid(),
    full_name    VARCHAR(255) NOT NULL,
    visitor_phone VARCHAR(20),
    note         TEXT,

    CONSTRAINT visitor_pk PRIMARY KEY (id)
);

-- changeset x3imal:74.2
ALTER TABLE cars DROP CONSTRAINT IF EXISTS cars_model_id_fkey;
ALTER TABLE cars DROP CONSTRAINT IF EXISTS cars_trailer_id_fkey;
ALTER TABLE car_model DROP CONSTRAINT IF EXISTS car_model_brand_id_fkey;
ALTER TABLE cars DROP CONSTRAINT IF EXISTS cars_brand_id_fkey;
ALTER TABLE cars DROP COLUMN IF EXISTS model_id;
ALTER TABLE cars DROP COLUMN IF EXISTS type;
ALTER TABLE cars DROP COLUMN IF EXISTS year;
ALTER TABLE cars DROP COLUMN IF EXISTS color;

-- changeset x3imal:74.3
DROP TABLE IF EXISTS model;
DROP TABLE IF EXISTS trailer;
DROP TABLE IF EXISTS car_model;

-- changeset x3imal:74.4
-- Удаление таблицы car_brand
DROP TABLE IF EXISTS car_brand;


-- changeset x3imal:74.5
-- Создаем таблицу брендов
CREATE TABLE car_brand (
                           id BIGSERIAL PRIMARY KEY,
                           brand VARCHAR(50) NOT NULL
);

-- changeset x3imal:74.6
ALTER TABLE cars ADD CONSTRAINT cars_brand_id_fkey FOREIGN KEY (brand_id) REFERENCES car_brand(id);

