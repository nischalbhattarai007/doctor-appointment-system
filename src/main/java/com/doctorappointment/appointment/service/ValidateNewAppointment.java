package com.doctorappointment.appointment.service;

import com.doctorappointment.appointment.exception.*;

import java.time.LocalDate;
import java.util.UUID;

public class ValidateNewAppointment {
    public static void validateNewAppointment(UUID patientId, UUID doctorId,
                                              String date, String status) {
        if (patientId == null) {
            throw new PatientIdNotFoundException("Patient Id is required");
        }
        if (doctorId == null) {
            throw new DoctorIdNotFoundException("Doctor Id is required");
        }
        if (date == null || date.isEmpty()) {
            throw new DateNotFoundException("appointment_date is required");
        }
        if (status == null || status.isEmpty()) {
            throw new EmptyStatusValidationException("Status is required");
        }
    }
    public static void validateDate(String date) {
        if (date == null || date.isEmpty()) {
            throw new DateNotFoundException("appointment_date is required");
        }
        try {
            LocalDate localDate = LocalDate.parse(date);
            if (localDate.isBefore(LocalDate.now())) {
                throw new DateValidationException("Appointment date cannot be in the past");
            }
        } catch (DateTimeParseException e) {
            throw new DateValidationException("Invalid date format — use YYYY-MM-DD");
        }
    }
}
