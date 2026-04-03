package com.doctorappointment.appointment.exception;

public class DateNotFoundException extends RuntimeException {
    public DateNotFoundException(String message) {
        super(message);
    }
}
