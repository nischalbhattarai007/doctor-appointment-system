package com.doctorappointment.appointment.exception;

public class AppointmentAlreadyActiveException extends RuntimeException {
    public AppointmentAlreadyActiveException(String message) {
        super(message);
    }
}
