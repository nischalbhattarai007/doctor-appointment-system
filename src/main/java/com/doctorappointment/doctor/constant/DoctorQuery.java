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
                    IS_DELETED +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //find by id
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
                    LONGITUDE + " =?" +
                    DAILY_LIMIT + " =?" +
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

}
