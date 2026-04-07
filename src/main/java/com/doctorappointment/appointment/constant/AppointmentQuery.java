package com.doctorappointment.appointment.constant;

import static com.doctorappointment.appointment.constant.AppointmentSchema.*;

public class AppointmentQuery {
    //Save to main appointments table
    public static final String INSERT =
            "INSERT INTO " + APPOINTMENTS_TABLE +
                    " (" +
                    APPOINTMENT_ID + ", " +
                    PATIENT_ID + ", " +
                    DOCTOR_ID + ", " +
                    DATE + ", " +
                    STATUS + ", " +
                    NOTES + ", " +
                    REASON + ", " +
                    CANCELLED_BY + ", " +
                    CREATED_AT +
                    ") VALUES (?,?,?,?,?,?,?,?,?)";

    // appointment find by ID
    public static final String FIND_BY_ID =
            "SELECT * FROM " + APPOINTMENTS_TABLE +
                    " WHERE " + APPOINTMENT_ID + " =?";

    //update status
    public static final String UPDATE_STATUS =
            "UPDATE " + APPOINTMENTS_TABLE +
                    " SET " +
                    STATUS + " =?, " +
                    REASON + " =?, " +
                    CANCELLED_BY + " =?" +
                    " WHERE " + APPOINTMENT_ID + " =?";

    //Save to doctor lookup table
    public static final String INSERT_BY_DOCTOR =
            "INSERT INTO " + APPOINTMENTS_BY_DOCTOR_TABLE +
                    " (" +
                    DOCTOR_ID + ", " +
                    DATE + ", " +
                    APPOINTMENT_ID + ", " +
                    STATUS +
                    ") VALUES (?,?,?,?)";

    //appointment find by doctor
    public static final String FIND_BY_DOCTOR =
            "SELECT * FROM " + APPOINTMENTS_BY_DOCTOR_TABLE +
                    " WHERE " + DOCTOR_ID + " =?" +
                    " AND " + DATE + " =?";

    //update status by doctor
    public static final String UPDATE_STATUS_BY_DOCTOR =
            "UPDATE " + APPOINTMENTS_BY_DOCTOR_TABLE +
                    " SET " + STATUS + " =?" +
                    " WHERE " + DOCTOR_ID + " =?" +
                    " AND " + DATE + " =?" +
                    " AND " + APPOINTMENT_ID + " =?";

    //Save to patient lookup table
    public static final String INSERT_BY_PATIENT =
            "INSERT INTO " + APPOINTMENTS_BY_PATIENT_TABLE +
                    " (" +
                    PATIENT_ID + ", " +
                    APPOINTMENT_ID + ", " +
                    DATE + ", " +
                    STATUS +
                    ") VALUES (?,?,?,?)";

    //appointment find by patient by ID
    public static final String FIND_BY_PATIENT =
            "SELECT * FROM " + APPOINTMENTS_BY_PATIENT_TABLE +
                    " WHERE " + PATIENT_ID + " =?";

    //appointment status update by patient
    public static final String UPDATE_STATUS_BY_PATIENT =
            "UPDATE " + APPOINTMENTS_BY_PATIENT_TABLE +
                    " SET " + STATUS + " =?" +
                    " WHERE " + PATIENT_ID + " =?" +
                    " AND " + APPOINTMENT_ID + " =?";

    //count by doctor date
    public static final String COUNT_BY_DOCTOR_DATE =
            "SELECT COUNT(*) FROM " + APPOINTMENTS_BY_DOCTOR_TABLE +
                    " WHERE " + DOCTOR_ID + " =?" +
                    " AND "   + DATE      + " =?";
}
