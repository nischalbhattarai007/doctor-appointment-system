package com.doctorappointment.patient_service.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Introspected
@Serdeable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatientModel {
    private UUID patientId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isDeleted;
    private String phoneNumber;
    private String address;
}
