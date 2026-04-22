package com.doctorappointment.doctor.exception;

public class DuplicateClinicException extends RuntimeException {
    public DuplicateClinicException(String message) {
        super(message);
    }
}
