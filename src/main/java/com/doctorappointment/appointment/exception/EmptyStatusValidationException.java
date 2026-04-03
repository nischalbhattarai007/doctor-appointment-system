package com.doctorappointment.appointment.exception;

public class EmptyStatusValidationException extends RuntimeException {
    public EmptyStatusValidationException(String message) {
        super(message);
    }
}
