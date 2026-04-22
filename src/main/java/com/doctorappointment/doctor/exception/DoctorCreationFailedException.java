package com.doctorappointment.doctor.exception;

public class DoctorCreationFailedException extends RuntimeException {
    public DoctorCreationFailedException(String message) {
        super(message);
    }
}
