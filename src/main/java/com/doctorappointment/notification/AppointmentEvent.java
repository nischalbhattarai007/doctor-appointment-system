package com.doctorappointment.notification;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
@Serdeable
@Builder
public record AppointmentEvent(
    String appointmentId,
    String patientId,
    String doctorId,
    String date,
    String status,
    String reason,
    String recipientType,
    String message)
{}

