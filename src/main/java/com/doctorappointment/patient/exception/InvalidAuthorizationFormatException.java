package com.doctorappointment.patient.exception;

public class InvalidAuthorizationFormatException extends RuntimeException {
    public InvalidAuthorizationFormatException(String message) {
        super(message);
    }
}
