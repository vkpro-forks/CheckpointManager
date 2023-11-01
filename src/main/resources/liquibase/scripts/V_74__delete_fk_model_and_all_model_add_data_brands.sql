-- liquibase formatted sql

-- changeset x3imal:74
ALTER TABLE passes DROP CONSTRAINT pass_person_fk;
DROP TABLE IF EXISTS persons;


-- changeset x3imal:74.1
CREATE TABLE persons
(
    id           UUID DEFAULT gen_random_uuid(),
    full_name    VARCHAR(255) NOT NULL,
    person_phone VARCHAR(20),
    note         TEXT,

    CONSTRAINT person_pk PRIMARY KEY (id)
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

-- changeset x3imal:74.7
-- Добавляем новые бренды
INSERT INTO car_brand (brand) VALUES ('Toyota');
INSERT INTO car_brand (brand) VALUES ('Ford');
INSERT INTO car_brand (brand) VALUES ('Honda');
INSERT INTO car_brand (brand) VALUES ('Nissan');
INSERT INTO car_brand (brand) VALUES ('Chevrolet');
INSERT INTO car_brand (brand) VALUES ('Volkswagen');
INSERT INTO car_brand (brand) VALUES ('Hyundai');
INSERT INTO car_brand (brand) VALUES ('BMW');
INSERT INTO car_brand (brand) VALUES ('Mercedes-Benz');
INSERT INTO car_brand (brand) VALUES ('Audi');
INSERT INTO car_brand (brand) VALUES ('Kia');
INSERT INTO car_brand (brand) VALUES ('Jeep');
INSERT INTO car_brand (brand) VALUES ('Subaru');
INSERT INTO car_brand (brand) VALUES ('Mazda');
