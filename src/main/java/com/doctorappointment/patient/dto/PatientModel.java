package com.doctorappointment.patient.dto;

import com.doctorappointment.patient.exception.InvalidEmailException;
import com.doctorappointment.patient.exception.InvalidPasswordException;
import com.doctorappointment.patient.exception.InvalidPhoneNumberException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.ValidationException;
import lombok.*;

import java.util.UUID;
import java.util.regex.Pattern;

@Introspected
@Serdeable
@Builder(toBuilder = true)
public record PatientModel(UUID patientId,
                           String firstName,
                           String lastName,
                           String email,
                           String password,
                           boolean isDeleted,
                           String phoneNumber,
                           String address) {}