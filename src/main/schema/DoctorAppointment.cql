-- Keyspace
CREATE KEYSPACE IF NOT EXISTS doctor_appointment
            WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE doctor_appointment;

-- =========================
-- TABLES
-- =========================

CREATE TABLE IF NOT EXISTS appointment_uniqueness
(
    patient_id     uuid,
    doctor_id      uuid,
    appt_date      text,
    appointment_id uuid,
    PRIMARY KEY ((patient_id, doctor_id), appt_date)
) WITH CLUSTERING ORDER BY (appt_date ASC);

CREATE TABLE IF NOT EXISTS appointments
(
    appointment_id   uuid,
    appointment_date text,
    cancelled_by     text,
    created_at       timestamp,
    doctor_id        uuid,
    notes            text,
    patient_id       uuid,
    reason           text,
    status           text,
    PRIMARY KEY (appointment_id)
);

CREATE TABLE IF NOT EXISTS appointments_by_doctor
(
    doctor_id        uuid,
    appointment_date text,
    appointment_id   uuid,
    status           text,
    PRIMARY KEY ((doctor_id, appointment_date), appointment_id)
) WITH CLUSTERING ORDER BY (appointment_id ASC);

CREATE TABLE IF NOT EXISTS appointments_by_patient
(
    patient_id       uuid,
    appointment_id   uuid,
    appointment_date text,
    status           text,
    PRIMARY KEY (patient_id, appointment_id)
) WITH CLUSTERING ORDER BY (appointment_id ASC);

CREATE TABLE IF NOT EXISTS appointments_by_patient_doctor_date
(
    patient_id       uuid,
    doctor_id        uuid,
    appointment_date text,
    appointment_id   uuid,
    PRIMARY KEY ((patient_id, doctor_id, appointment_date), appointment_id)
) WITH CLUSTERING ORDER BY (appointment_id ASC);

CREATE TABLE IF NOT EXISTS clinic_by_location
(
    clinic_address  text,
    clinic_building text,
    doctor_id       uuid,
    PRIMARY KEY ((clinic_address, clinic_building))
);

CREATE TABLE IF NOT EXISTS doctor_schedules
(
    doctor_id    uuid,
    working_days set<text>,
    PRIMARY KEY (doctor_id)
);

CREATE TABLE IF NOT EXISTS doctors
(
    doctor_id       uuid,
    address         text,
    clinic_address  text,
    clinic_building text,
    clinic_name     text,
    daily_limit     int,
    email           text,
    first_name      text,
    is_deleted      boolean,
    last_name       text,
    latitude        double,
    longitude       double,
    password        text,
    phone_number    text,
    specialization  text,
    PRIMARY KEY (doctor_id)
);

CREATE TABLE IF NOT EXISTS patients
(
    patient_id   uuid,
    address      text,
    email        text,
    first_name   text,
    is_deleted   boolean,
    last_name    text,
    password     text,
    phone_number text,
    PRIMARY KEY (patient_id)
);

-- =========================
-- INDEXES
-- =========================

CREATE INDEX IF NOT EXISTS doctor_by_email
    ON doctors (email);

CREATE INDEX IF NOT EXISTS patient_by_email
    ON patients (email);