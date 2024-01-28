-- liquibase formatted sql

-- changeset ldv236:291
DROP VIEW pass_in_out_view;

CREATE VIEW pass_in_out_view AS
WITH cross_pairs AS
         (WITH InRanked AS (
             SELECT pass_id, local_date_time as time,
                    ROW_NUMBER() OVER (PARTITION BY pass_id ORDER BY local_date_time) as rn
             FROM crossings
             WHERE direction = 'IN'),
               OutRanked AS (
                   SELECT pass_id, local_date_time as time,
                          ROW_NUMBER() OVER (PARTITION BY pass_id ORDER BY local_date_time) as rn
                   FROM crossings
                   WHERE direction = 'OUT')

          SELECT COALESCE(i.pass_id, o.pass_id) as pass_id,
                 i.time as in_time, o.time as out_time
          FROM InRanked i
                   FULL JOIN OutRanked o ON i.pass_id = o.pass_id
              AND i.rn = o.rn)

SELECT p.dtype, p.comment as pass_comment, t.name as terr_name,
       p.time_type as pass_time_type, p.status as pass_status,
       c.license_plate as car, b.brand as car_brand, v.full_name as visitor,
       cpr.in_time, cpr.out_time, p.id as pass_id,
       p.territory_id, p.user_id
FROM passes p
         JOIN territories t on p.territory_id = t.id
         LEFT JOIN cars c on p.car_id = c.id
         LEFT JOIN visitors v on p.visitor_id = v.id
         LEFT JOIN car_brand b on c.brand_id = b.id
         JOIN cross_pairs cpr on cpr.pass_id = p.id;