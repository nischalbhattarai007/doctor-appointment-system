package com.doctorappointment.doctor.exception;

public class EmailPasswordRequiredException extends RuntimeException {
    public EmailPasswordRequiredException(String message) {
        super(message);
    }
}
