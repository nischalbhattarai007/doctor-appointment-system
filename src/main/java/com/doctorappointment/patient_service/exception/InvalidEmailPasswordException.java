package com.doctorappointment.patient_service.exception;

public class InvalidEmailPasswordException extends RuntimeException {
    public InvalidEmailPasswordException(String message) {
        super(message);
    }
}
