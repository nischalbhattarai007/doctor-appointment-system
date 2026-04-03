package com.doctorappointment.appointment.exception;

public class DoctorIdNotFoundException extends RuntimeException {
    public DoctorIdNotFoundException(String message) {
        super(message);
    }
}
