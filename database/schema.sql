-- Run in pgAdmin Query Tool after connecting to careconnect_db.
-- This creates the table if absent; it does not migrate an existing table.

DO $$
BEGIN
    IF current_database() <> 'careconnect_db' THEN
        RAISE EXCEPTION
            'Connected to database %, but CareConnect requires careconnect_db.',
            current_database();
    END IF;
END;
$$;

CREATE TABLE IF NOT EXISTS public.patients (
    patient_id BIGINT GENERATED ALWAYS AS IDENTITY,
    full_name VARCHAR(150) NOT NULL,
    age SMALLINT NOT NULL,
    gender VARCHAR(30) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(254) NOT NULL,
    address TEXT,
    blood_group VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT patients_pkey PRIMARY KEY (patient_id),
    CONSTRAINT patients_full_name_check
        CHECK (btrim(full_name) <> ''),
    CONSTRAINT patients_age_check
        CHECK (age BETWEEN 1 AND 120),
    CONSTRAINT patients_gender_check
        CHECK (gender IN ('Female', 'Male', 'Other')),
    CONSTRAINT patients_phone_check
        CHECK (phone ~ '^[0-9]{10}$'),
    CONSTRAINT patients_email_check
        CHECK (email ~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'),
    CONSTRAINT patients_blood_group_check
        CHECK (blood_group IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'))
);

CREATE INDEX IF NOT EXISTS patients_created_at_idx
    ON public.patients (created_at DESC, patient_id DESC);
