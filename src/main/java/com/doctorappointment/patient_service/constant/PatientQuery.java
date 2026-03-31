package com.doctorappointment.patient_service.constant;

import static com.doctorappointment.patient_service.constant.PatientSchema.*;

public class PatientQuery {
    // insert patients details
    public static final String INSERT =
            "INSERT INTO " + PATIENTS_TABLE +
                    " (" +
                    PATIENT_ID + ", " +
                    PATIENT_FIRSTNAME + ", " +
                    PATIENT_LASTNAME + ", " +
                    PATIENT_EMAIL + ", " +
                    PATIENT_PASSWORD + ", " +
                    IS_DELETED + ", " +
                    PATIENT_PHONE + ", " +
                    PATIENT_ADDRESS + ") VALUES (?,?,?,?,?,?,?,?)";

    //get patients by id
    public static final String FIND_BY_ID =
            "SELECT * FROM " + PATIENTS_TABLE +
                    " WHERE " + PATIENT_ID + " =?";

    //get patients by email
    public static final String FIND_BY_EMAIL =
            "SELECT * FROM " + PATIENTS_TABLE +
                    " WHERE " + PATIENT_EMAIL + " =?";

    //update patients details
    public static final String UPDATE =
            "UPDATE " + PATIENTS_TABLE +
                    " SET " +
                    PATIENT_FIRSTNAME + " =?," +
                    PATIENT_LASTNAME + " =?," +
                    PATIENT_PHONE + " =?," +
                    PATIENT_ADDRESS + " =?," +
                    PATIENT_EMAIL + " =?," +
                    PATIENT_PASSWORD + " =?," +
                    " WHERE " + PATIENT_ID + " =?,";

    //soft delete patients details
    public static final String SOFT_DELETE =
            "UPDATE " + PATIENTS_TABLE +
                    " SET " + IS_DELETED + " = true" +
                    " WHERE " + PATIENT_ID + " =?";

    //get all patients
    public static final String GET_ALL_PATIENTS =
            "SELECT * FROM " + PATIENTS_TABLE;

    //exists by email
    public static final String EXISTS_BY_EMAIL =
            "SELECT " + PATIENT_EMAIL + " FROM " + PATIENTS_TABLE + " WHERE " + PATIENT_EMAIL + " =?";
}
