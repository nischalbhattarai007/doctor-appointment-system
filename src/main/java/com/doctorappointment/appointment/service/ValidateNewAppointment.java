package com.doctorappointment.appointment.service;

import com.doctorappointment.appointment.exception.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class ValidateNewAppointment {
    private static final ZoneId NEPAL_ZONE = ZoneId.of("Asia/Kathmandu");
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
            LocalDate now = LocalDate.now(NEPAL_ZONE);
            LocalDate maxDate = now.plusMonths(3);
            if(localDate.isBefore(now)){
                throw new DateValidationException("Appointment date cannot be in past.");
            }
            if(localDate.isAfter(maxDate)){
                throw new DateValidationException("Appointment date cannot be more then 3 months in advance.");
            }
        } catch (DateTimeParseException e) {
            throw new DateValidationException("Invalid date format. Expected format: YYYY-MM-DD (example: 2026-07-20)");
        }
    }

}
