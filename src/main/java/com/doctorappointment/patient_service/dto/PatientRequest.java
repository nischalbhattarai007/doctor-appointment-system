package com.doctorappointment.patient_service.dto;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record PatientRequest(UUID patientId,
                             String firstName,
                             String lastName,
                             String email,
                             String password,
                             String phoneNumber,
                             String address)
{}
