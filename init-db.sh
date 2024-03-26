#!/bin/bash
set -e

# Добавляем плагин в библиотеку и указываем с какой БД работать
echo "shared_preload_libraries = 'pg_cron'" >> /var/lib/postgresql/data/postgresql.conf
echo "cron.database_name = 'chpmanDB'" >> /var/lib/postgresql/data/postgresql.conf

# Перезапускаем Postgres
/usr/lib/postgresql/16/bin/pg_ctl restart

# Создание расширения pg_cron
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS pg_cron;
EOSQL

# Добавление задания в pg_cron в виде: (NAME, CRON, SQL_QUERY)
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT cron.schedule('Delete logs','55 23 * * *', 'DELETE FROM logs WHERE time < now() - interval ''1 day''');
EOSQL