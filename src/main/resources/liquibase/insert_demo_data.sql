/*
предлагается при необходимости обновления демо-данных удалять строку из таблицы ликвибейса
(не нужно выполнять при работе напрямую в бд через pgAdmin)
DELETE FROM databasechangelog WHERE filename = 'liquibase/insert_demo_data.sql';
*/
-- а также не забывайте добавлять новые таблицы в транкейт
-- (альтернатива - дропать схему, чтобы все скрипты накатывались заново, а не только этот; можно будет сделать и так)

TRUNCATE TABLE crossings, passes, users, user_territory, cars, car_brand, visitors, checkpoints, territories CASCADE;

DO $$
    DECLARE
        -- Объявление переменных
        -- замечено: при добавлении через Идею время ставится по гринвичу, и тогда изменение статусов работает как надо сразу
        -- а через пгАдмин (локально) время ставится текущее как в системе, и тогда придется ждать 3 часа (в мск поясе)
        -- поэтому при работе через пгАдмин (локально) надо ставить коррекцию в этой переменной ('3 hour')
        -- при этом через пгАдмин на СЕРВЕРЕ все правильно работает с '0 hour'
        nowDT timestamp = now() - interval '0 hour';
        ter1_id UUID := uuid_generate_v4();
        ter2_id UUID := uuid_generate_v4();
        ter3_id UUID := uuid_generate_v4();
        chp1_id UUID := uuid_generate_v4();
        chp2_id UUID := uuid_generate_v4();
        chp3_id UUID := uuid_generate_v4();
        chp4_id UUID := uuid_generate_v4();
        user1_id UUID := uuid_generate_v4();
        user2_id UUID := uuid_generate_v4();
        user3_id UUID := uuid_generate_v4();
        user4_id UUID := uuid_generate_v4();
        car1_id UUID := uuid_generate_v4();
        car2_id UUID := uuid_generate_v4();
        visitor1_id UUID := uuid_generate_v4();
        visitor2_id UUID := uuid_generate_v4();
        pass1_id UUID := uuid_generate_v4();
        pass2_id UUID := uuid_generate_v4();
        pass3_id UUID := uuid_generate_v4();
        pass4_id UUID := uuid_generate_v4();
        pass5_id UUID := uuid_generate_v4();
        pass6_id UUID := uuid_generate_v4();
        pass7_id UUID := uuid_generate_v4();
        pass8_id UUID := uuid_generate_v4();

    BEGIN
        INSERT INTO territories (id, name, note, added_at)
        VALUES (ter1_id, 'territory1', 'описание1', nowDT),
               (ter2_id, 'territory2', 'описание2', nowDT),
               (ter3_id, 'territory3', 'описание3', nowDT);

        INSERT INTO checkpoints (id, name, type, note, added_at, territory_id)
        VALUES (chp1_id, 'kpp1', 'UNIVERSAL',   'тер1 - универс',   nowDT, ter1_id),
               (chp2_id, 'kpp2', 'AUTO',        'тер1 - авто',      nowDT, ter1_id),
               (chp3_id, 'kpp3', 'WALK',        'тер1 - пешех',     nowDT, ter1_id),
               (chp4_id, 'kpp4', 'UNIVERSAL',   'тер2 - универс',   nowDT, ter2_id);

        INSERT INTO users (id, full_name, email, password, is_blocked, role, main_number, added_at)
        VALUES (user1_id, 'User', 'user@chp.com',
                '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false, 'USER', '89997776655', '2023-09-25'),
               (user2_id, 'Manager', 'manager@chp.com',
                '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false, 'MANAGER', '89997776654', '2023-09-25'),
               (user3_id, 'Admin', 'admin@chp.com',
                '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false, 'ADMIN', '89897776653', '2023-09-25'),
               (user4_id, 'Security', 'security@chp.com',
                '$2a$12$Zl8XNWuzG9QZmXHHZjtvdufgbzbu2jbBpQqhVeaj6RYQmgB5acIX2', false, 'SECURITY', '89117776653', '2023-09-25');

        insert into user_territory (user_id, territory_id)
        values (user1_id, ter1_id),
               (user1_id, ter2_id),
               (user1_id, ter3_id),
               (user2_id, ter1_id),
               (user3_id, ter1_id),
               (user4_id, ter1_id);

        INSERT INTO car_brand (brand)
        VALUES ('Toyota'), ('Ford'), ('Honda'), ('Nissan')
             , ('Chevrolet'), ('Volkswagen') ,('Hyundai'), ('BMW')
             , ('Mercedes-Benz'), ('Audi'), ('Kia')
             , ('Jeep'), ('Subaru'), ('Mazda');

        INSERT INTO cars (id, license_plate, brand_id)
        VALUES (car1_id, 'А777АА77', (select id from car_brand where brand = 'Toyota')),
               (car2_id, 'В666ВВ66', (select id from car_brand where brand = 'Ford'));

        INSERT INTO visitors (id, full_name, visitor_phone, note)
        VALUES (visitor1_id, 'Петрович', '+79991234567', 'электрик'),
               (visitor2_id, 'Дон Румата Эсторский', '+79991234568', null);

        INSERT INTO passes ( id, user_id, status, type_time, territory_id, added_at
                           , start_time, end_time, comment, car_id, visitor_id, dtype, favorite, expected_direction)

        -- АКТИВНЫЕ ПРОПУСКА НА НЕДЕЛЮ, без пересечений, с ними можно проверять пересечения
        -- автомобильный разовый
        VALUES (pass1_id, user1_id, 'ACTIVE', 'ONETIME', ter1_id, nowDT
               , nowDT, nowDT + interval '7 day', 'ACTIVE FOR WEEK 1', car1_id, null, 'AUTO', true, 'OUT'),

               -- автомобильный постоянный
               (pass2_id, user1_id, 'ACTIVE', 'PERMANENT', ter1_id, nowDT
               , nowDT, nowDT + interval '7 day', 'ACTIVE FOR WEEK 2', car2_id, null, 'AUTO', false, 'OUT'),

               -- пешеходный разовый
               (pass3_id, user1_id, 'ACTIVE', 'ONETIME', ter2_id, nowDT
               , nowDT, nowDT + interval '7 day', 'ACTIVE FOR WEEK 3', null, visitor1_id, 'WALK', true, 'IN'),

               -- пешеходный постоянный
               (pass4_id, user1_id, 'ACTIVE', 'PERMANENT', ter2_id, nowDT
               , nowDT, nowDT + interval '7 day', 'ACTIVE FOR WEEK 4', null, visitor2_id, 'WALK', false, 'IN'),


               -- ПРОПУСКА НА МИНУТУ, они должны поменять статус при проверке
               -- активный автомобильный разовый, нет пересечений - должен УСТАРЕТЬ
               (pass5_id, user2_id, 'ACTIVE', 'ONETIME', ter1_id, nowDT
               , nowDT - interval '1 hour', nowDT, 'should be OUTDATED', car1_id, null, 'AUTO', true, 'IN'),

               -- активный автомобильный разовый, одно пересечение на въезд - должен стать ВАРНИНГ
               (pass6_id, user2_id, 'ACTIVE', 'ONETIME', ter1_id, nowDT
               , nowDT - interval '1 hour', nowDT, 'should be WARNING', car2_id, null, 'AUTO', false, 'OUT'),

               -- активный пешеходный постоянный, пересечения на въезд и на выезд - должен стать ВЫПОЛНЕН
               (pass7_id, user2_id, 'ACTIVE', 'ONETIME', ter1_id, nowDT
               , nowDT - interval '1 hour', nowDT, 'should be COMPLETED', null, visitor1_id, 'WALK', true, 'IN'),

               -- отложенный пешеходный разовый, должен стать АКТИВНЫМ на сутки
               (pass8_id, user2_id, 'DELAYED', 'ONETIME', ter1_id, nowDT, nowDT + interval '1 minute'
               , nowDT + interval '1 day', 'should be ACTIVE', null, visitor2_id, 'WALK', false, 'IN');

        INSERT INTO crossings (pass_id, checkpoint_id, performed_at, local_date_time, direction)
        VALUES (pass1_id, chp1_id, now() - interval '5 second', nowDT, 'IN'),

               (pass2_id, chp2_id, now() + interval '1 second', nowDT + interval '1 second', 'IN'),
               (pass2_id, chp2_id, now() + interval '2 second', nowDT + interval '2 second', 'OUT'),
               (pass2_id, chp2_id, now() + interval '3 second', nowDT + interval '3 second', 'IN'),
               (pass2_id, chp2_id, now() + interval '4 second', nowDT + interval '4 second', 'OUT'),
               (pass2_id, chp2_id, now() + interval '5 second', nowDT + interval '5 second', 'IN'),

               (pass6_id, chp1_id, now() - interval '5 second', nowDT, 'IN'),

               (pass7_id, chp1_id, now() - interval '3 second', nowDT, 'IN'),
               (pass7_id, chp1_id, now() - interval '4 second', nowDT + interval '1 second', 'OUT');
END $$;
