-- liquibase formatted sql

-- changeset x3imal:17 context:ignore
CREATE TABLE car_brand
(
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(255) NOT NULL
);

CREATE TABLE car_model
(
    id       BIGSERIAL PRIMARY KEY,
    brand_id BIGINT REFERENCES car_brand (id),
    model    VARCHAR(255) NOT NULL
);

-- Добавляем бренд "Toyota" и сохраняем его ID
WITH inserted_toyota AS (
    INSERT INTO car_brand (brand)
        VALUES ('Toyota')
        RETURNING id)

-- Добавляем модели для бренда "Toyota", используя сохраненный ID бренда
INSERT
INTO car_model (brand_id, model)
SELECT id, model
FROM inserted_toyota
         CROSS JOIN (VALUES ('Camry'),
                            ('Corolla'),
                            ('RAV4'),
                            ('Highlander'),
                            ('Tacoma'),
                            ('Prius'),
                            ('Sienna'),
                            ('Yaris'),
                            ('C-HR')) AS models(model);

-- Добавляем бренд "Ford" и сохраняем его ID
WITH inserted_ford AS (
    INSERT INTO car_brand (brand)
        VALUES ('Ford')
        RETURNING id)

-- Добавляем модели для бренда "Ford", используя сохраненный ID бренда
INSERT
INTO car_model (brand_id, model)
SELECT id, model
FROM inserted_ford
         CROSS JOIN (VALUES ('F-150'),
                            ('Focus'),
                            ('Escape'),
                            ('Explorer'),
                            ('Mustang'),
                            ('Edge'),
                            ('Ranger'),
                            ('Fusion')) AS models(model);

-- Добавляем бренд "Honda" и сохраняем его ID
WITH inserted_honda AS (
    INSERT INTO car_brand (brand)
        VALUES ('Honda')
        RETURNING id)

-- Добавляем модели для бренда "Honda", используя сохраненный ID бренда
INSERT
INTO car_model (brand_id, model)
SELECT id, model
FROM inserted_honda
         CROSS JOIN (VALUES ('Civic'),
                            ('Accord'),
                            ('CR-V'),
                            ('Pilot'),
                            ('Fit'),
                            ('HR-V'),
                            ('Odyssey'),
                            ('Insight'),
                            ('Ridgeline')) AS models(model);

-- Добавляем бренд "Nissan" и сохраняем его ID
WITH inserted_nissan AS (
    INSERT INTO car_brand (brand)
        VALUES ('Nissan')
        RETURNING id)

-- Добавляем модели для бренда "Nissan", используя сохраненный ID бренда
INSERT
INTO car_model (brand_id, model)
SELECT id, model
FROM inserted_nissan
         CROSS JOIN (VALUES ('Altima'),
                            ('Maxima'),
                            ('Sentra'),
                            ('Versa'),
                            ('370Z'),
                            ('GT-R'),
                            ('Murano'),
                            ('Rogue'),
                            ('Pathfinder')) AS models(model);

-- changeset x3imal:23 context:ignore
ALTER TABLE car_model
    ADD CONSTRAINT model_unique UNIQUE (model);
