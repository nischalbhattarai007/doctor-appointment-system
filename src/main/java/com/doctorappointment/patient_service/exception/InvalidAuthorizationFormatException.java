package com.doctorappointment.patient_service.exception;

public class InvalidAuthorizationFormatException extends RuntimeException {
    public InvalidAuthorizationFormatException(String message) {
        super(message);
    }
}
