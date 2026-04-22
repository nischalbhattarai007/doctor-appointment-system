package com.doctorappointment.doctor.constant;

public class DoctorSchema {
    //keyspace
    public static final String KEYSPACE = "doctor_appointment";

    //merge keyspace with database table
    public static final String DOCTORS_TABLE = KEYSPACE + ".doctors";
    //doctor schedule table
    public static final String DOCTORS_TABLE_SCHEDULE = KEYSPACE + ".doctor_schedules";
    // uniqueness doctor table to prevent duplicate address for clinic and location
    public static final String DOCTORS_UNIQUENESS_ADDRESS= KEYSPACE + ".clinic_by_location";

    //column
    public static final String DOCTOR_ID = "doctor_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String ADDRESS = "address";
    public static final String SPECIALIZATION = "specialization";
    public static final String CLINIC_ADDRESS = "clinic_address";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String DAILY_LIMIT = "daily_limit";
    public static final String IS_DELETED = "is_deleted";
    public static final String CLINIC_NAME = "clinic_name";
    public static final String CLINIC_BUILDING = "clinic_building";
    public static final String WORKING_DAYS = "working_days";
}
