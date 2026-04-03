package com.doctorappointment.doctor.exception;

public class DoctorEmailNotFoundException extends RuntimeException {
    public DoctorEmailNotFoundException(String message) {
        super(message);
    }
}
