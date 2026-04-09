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
        String appointment_date,
        String status,
        String notes,
        String reason,
        String cancelledBy,
        Instant createdAt) {
}