-- т.к. теперь есть view, поменять тип столбца не просто
DO
$$
-- сначала сохраняем view в переменную
    DECLARE
        v_pass_view_def   text;
        DECLARE exec_text text;
    BEGIN
        v_pass_view_def := pg_get_viewdef('pass_in_out_view'::regclass);
        DROP VIEW pass_in_out_view;

-- удалили view, далее создается enum, меняется тип столбца на enum, переносятся предыдущие значения
        CREATE TYPE pass_status_enum AS ENUM (
            'DELAYED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'OUTDATED', 'WARNING'
            );

        ALTER TABLE passes
            ALTER COLUMN status TYPE pass_status_enum USING status::pass_status_enum;

        exec_text := format('create view pass_in_out_view as %s',
                            v_pass_view_def);
        EXECUTE exec_text;
-- далее выполняется скрипт из переменной (то что view)
    END
$$;

GO