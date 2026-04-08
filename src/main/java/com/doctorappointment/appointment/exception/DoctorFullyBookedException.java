package com.doctorappointment.appointment.exception;

public class DoctorFullyBookedException extends RuntimeException {
    public DoctorFullyBookedException(String message) {
        super(message);
    }
}
