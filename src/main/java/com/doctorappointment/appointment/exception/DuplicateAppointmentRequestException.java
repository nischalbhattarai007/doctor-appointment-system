package com.doctorappointment.appointment.exception;

public class DuplicateAppointmentRequestException extends RuntimeException {
    public DuplicateAppointmentRequestException(String message) {
        super(message);
    }
}
