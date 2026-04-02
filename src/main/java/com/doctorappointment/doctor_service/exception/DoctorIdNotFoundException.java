package com.doctorappointment.doctor_service.exception;

public class DoctorIdNotFoundException extends RuntimeException {
    public DoctorIdNotFoundException(String message) {
        super(message);
    }
}
