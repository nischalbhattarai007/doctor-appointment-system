package com.doctorappointment.appointment.constant;

public class AppointmentSchema {
    // keyspace
    public static final String KEYSPACE = "doctor_appointment";

    //tables
    public static final String APPOINTMENTS_TABLE = KEYSPACE + ".appointments";
    public static final String APPOINTMENTS_BY_DOCTOR_TABLE = KEYSPACE + ".appointments_by_doctor";
    public static final String APPOINTMENTS_BY_PATIENT_TABLE = KEYSPACE + ".appointments_by_patient";

    //column
    public static final String APPOINTMENT_ID = "appointment_id";
    public static final String PATIENT_ID = "patient_id";
    public static final String DOCTOR_ID = "doctor_id";
    public static final String APPOINTMENT_DATE = "appointment_date";
    public static final String STATUS = "status";
    public static final String NOTES = "notes";
    public static final String REASON = "reason";
    public static final String CANCELLED_BY = "cancelled_by";
    public static final String CREATED_AT = "created_at";
}
