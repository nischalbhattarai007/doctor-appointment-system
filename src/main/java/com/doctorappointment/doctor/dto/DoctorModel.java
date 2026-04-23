package com.doctorappointment.doctor.dto;

import com.doctorappointment.patient.exception.InvalidEmailException;
import com.doctorappointment.patient.exception.InvalidPhoneNumberException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.ValidationException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Introspected
@Serdeable
@Builder(toBuilder = true)
public record DoctorModel(
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
        double longitude,
        int dailyLimit,
        boolean isDeleted,
        String clinicName,
        String clinicBuilding)
{}


