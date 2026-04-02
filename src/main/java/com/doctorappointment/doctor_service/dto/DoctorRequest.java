package com.doctorappointment.doctor_service.dto;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record DoctorRequest(
        UUID doctorId,
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber,
        String address,
        String specialization,
        String clinicAddress,
        double latitude,
        double longitude) {

}
