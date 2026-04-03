package com.doctorappointment.appointment.exception;

public class PatientIdNotFoundException extends RuntimeException {
    public PatientIdNotFoundException(String message) {
        super(message);
    }
}
