package com.doctorappointment.appointment.exception;

public class SameDateRescheduleNotAllowedException extends RuntimeException {
    public SameDateRescheduleNotAllowedException(String message) {
        super(message);
    }
}
