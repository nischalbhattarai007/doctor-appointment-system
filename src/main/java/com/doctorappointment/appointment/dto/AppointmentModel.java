package com.doctorappointment.appointment.dto;

import com.doctorappointment.appointment.constant.AppointmentStatus;
import com.doctorappointment.appointment.exception.*;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Introspected
@Serdeable
@Builder(toBuilder = true)
public record AppointmentModel (
        UUID appointmentId,
        UUID doctorId,
        UUID patientId,
        String date,
        String status,
        String notes,
        String reason,
        String cancelledBy,
        Instant createdAt)
{
    public AppointmentModel{
        if(patientId == null){
            throw new PatientIdNotFoundException("Patient Id is null");
        }
        if(doctorId == null){
            throw new DoctorIdNotFoundException("Doctor Id is null");
        }
        if(isNullOrEmpty(date)){
            throw new DateNotFoundException("Date is null or empty");
        }
        try{
            LocalDate localDate = LocalDate.parse(date);
            if(localDate.isBefore(LocalDate.now())){
                throw new DateValidationException(" Appointment date cannot be in the past");
            }
        }catch(DateTimeParseException e){
            throw new DateValidationException("Invalid date format -use YYYY-MM-DD ");
        }
        if(isNullOrEmpty(status)){
            throw new EmptyStatusValidationException("Status is null or empty");
        }
        if(!status.equals(AppointmentStatus.PENDING) &&
                !status.equals(AppointmentStatus.CONFIRMED) &&
                !status.equals(AppointmentStatus.REJECTED) &&
                !status.equals(AppointmentStatus.CANCELLED)){
            throw new InvalidStatusException("Invalid status" + status);
        }
    }
    public static boolean isNullOrEmpty(String value){
        return value == null || value.isEmpty();
    }
}
