package com.doctorappointment.doctor_service.exception;

public class DoctorEmailNotFoundException extends RuntimeException {
    public DoctorEmailNotFoundException(String message) {
        super(message);
    }
}
