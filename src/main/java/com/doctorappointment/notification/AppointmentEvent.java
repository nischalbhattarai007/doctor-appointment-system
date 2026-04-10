package com.doctorappointment.notification;
import lombok.Builder;
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

