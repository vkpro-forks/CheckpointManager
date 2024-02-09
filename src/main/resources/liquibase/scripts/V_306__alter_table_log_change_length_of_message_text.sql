ALTER TABLE public.logs
    ALTER COLUMN message TYPE TEXT
        USING message::text;

GO