package com.doctorappointment.patient_service.constant;

public class PatientSchema {
    //keyspace
    public static final String KEYSPACE="doctor_appointment";

    //tables
    public static final String PATIENTS_TABLE=KEYSPACE+".patients";

    //columns
    public static final String PATIENT_ID = "patient_id";
    public static final String PATIENT_FIRSTNAME = "first_name";
    public static final String PATIENT_LASTNAME = "last_name";
    public static final String PATIENT_EMAIL = "email";
    public static final String PATIENT_PASSWORD = "password";
    public static final String PATIENT_ADDRESS = "address";
    public static final String PATIENT_PHONE = "phone_number";
    public static final boolean IS_DELETED = false;

}
