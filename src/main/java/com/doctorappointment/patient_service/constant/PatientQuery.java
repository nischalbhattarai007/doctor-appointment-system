package com.doctorappointment.patient_service.constant;
import static com.doctorappointment.patient_service.constant.PatientSchema.*;
public class PatientQuery {
    //
    public static final String INSERT=
            "INSERT INTO " + PATIENTS_TABLE +
                    " ("+
                    PATIENT_ID + ", " +
                    PATIENT_FIRSTNAME + ", " +
                    PATIENT_LASTNAME + ", " +
                    PATIENT_EMAIL + ", " +
                    PATIENT_PASSWORD+ ", "+
                    PATIENT_ADDRESS+ ", "+
                    PATIENT_PHONE+", "+
                    IS_DELETED + ") VALUES (:" +
                    PATIENT_ID + ", :" +
                    PATIENT_FIRSTNAME + ", :" +
                    PATIENT_LASTNAME + ", :" +
                    PATIENT_EMAIL + ", :" +
                    PATIENT_PASSWORD+ ", :"+
                    PATIENT_ADDRESS+ ", :"+
                    PATIENT_PHONE+", :"+
                    IS_DELETED +
                    ")";

    public static final String FIND_BY_ID =
            "SELECT * FROM " + PATIENTS_TABLE +
                    " WHERE " + PATIENT_ID + " =:" + PATIENT_ID;

    public static final String FIND_BY_EMAIL =
            "SELECT * FROM " + PATIENTS_TABLE +
                    " WHERE " + PATIENT_EMAIL + " =:" + PATIENT_EMAIL;

    public static final String UPDATE =
            "UPDATE " + PATIENTS_TABLE +
                    " SET "  +
                    PATIENT_FIRSTNAME   + " =:" + PATIENT_FIRSTNAME   + ", " +
                    PATIENT_LASTNAME    + " =:" + PATIENT_LASTNAME    + ", " +
                    PATIENT_PHONE + " =:" + PATIENT_PHONE + ", " +
                    PATIENT_ADDRESS      + " =:" + PATIENT_ADDRESS      +
                    " WHERE " + PATIENT_ID + " =:" + PATIENT_ID;

    public static final String SOFT_DELETE =
            "UPDATE " + PATIENTS_TABLE +
                    " SET "  + IS_DELETED + " = true" +
                    " WHERE " + PATIENT_ID + " =:" + PATIENT_ID;
}
