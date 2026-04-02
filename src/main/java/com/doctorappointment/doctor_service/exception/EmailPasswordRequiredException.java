package com.doctorappointment.doctor_service.exception;

public class EmailPasswordRequiredException extends RuntimeException {
    public EmailPasswordRequiredException(String message) {
        super(message);
    }
}
