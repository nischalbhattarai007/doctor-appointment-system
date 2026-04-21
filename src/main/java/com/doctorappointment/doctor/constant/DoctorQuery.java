package com.doctorappointment.doctor.constant;

import static com.doctorappointment.doctor.constant.DoctorSchema.*;

public class DoctorQuery {

    //insert into doctor query
    public static final String INSERT =
            "INSERT INTO " + DOCTORS_TABLE +
                    " (" +
                    DOCTOR_ID + ", " +
                    FIRST_NAME + ", " +
                    LAST_NAME + ", " +
                    EMAIL + ", " +
                    PASSWORD + ", " +
                    PHONE_NUMBER + ", " +
                    ADDRESS + ", " +
                    SPECIALIZATION + ", " +
                    CLINIC_ADDRESS + ", " +
                    LATITUDE + ", " +
                    LONGITUDE + ", " +
                    DAILY_LIMIT + ", " +
                    IS_DELETED + ", " +
                    CLINIC_NAME + ", " +
                    CLINIC_BUILDING +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //find by doctor id
    public static final String FIND_BY_ID =
            "SELECT * FROM " + DOCTORS_TABLE +
                    " WHERE " + DOCTOR_ID + " =?";

    //find by email
    public static final String FIND_BY_EMAIL =
            "SELECT * FROM " + DOCTORS_TABLE +
                    " WHERE " + EMAIL + " =?";

    //update doctor by id
    public static final String UPDATE =
            "UPDATE " + DOCTORS_TABLE +
                    " SET " +
                    FIRST_NAME + " =?, " +
                    LAST_NAME + " =?, " +
                    PHONE_NUMBER + " =?, " +
                    ADDRESS + " =?, " +
                    SPECIALIZATION + " =?, " +
                    CLINIC_ADDRESS + " =?, " +
                    LATITUDE + " =?, " +
                    LONGITUDE + " =?, " +
                    DAILY_LIMIT + " =?, " +
                    CLINIC_NAME + " =?, " +
                    CLINIC_BUILDING + " =? " +
                    " WHERE " + DOCTOR_ID + " =?";

    //soft delete doctor by id
    public static final String SOFT_DELETE =
            "UPDATE " + DOCTORS_TABLE +
                    " SET " + IS_DELETED + " = true" +
                    " WHERE " + DOCTOR_ID + " =?";

    //exists by email
    public static final String EXISTS_BY_EMAIL =
            "SELECT " + EMAIL + " FROM " + DOCTORS_TABLE +
                    " WHERE " + EMAIL + " =?";

    //get all doctor
    public static final String GET_ALL_DOCTORS =
            "SELECT * FROM " + DOCTORS_TABLE;

    //find by clinic building
    public static final String FIND_BY_CLINIC_BUILDING =
            "SELECT " + DOCTOR_ID + ", " + CLINIC_BUILDING +
                    " FROM " + DOCTORS_TABLE +
                    " WHERE " + CLINIC_BUILDING + " = ?";

    //doctor schedule table insert query
    public static final String INSERT_INTO_DOCTOR_SCHEDULE=
            "INSERT INTO " + DOCTORS_TABLE_SCHEDULE + " (" + DOCTOR_ID + ", " + WORKING_DAYS + ")" +
                    " VALUES (?, ?)";

}
