package com.doctorappointment.appointment.dto;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record AppointmentRequest(
        UUID appointmentId,
        UUID   patientId,
        UUID   doctorId,
        String appointment_date,
        String notes,
        String reason,
        String cancelledBy)
{

}
