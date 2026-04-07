package com.doctorappointment.doctor.exception;

public class DoctorFullyBookedException extends RuntimeException {
    public DoctorFullyBookedException(String message) {
        super(message);
    }
}
