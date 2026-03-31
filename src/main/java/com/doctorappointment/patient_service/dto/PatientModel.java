package com.doctorappointment.patient_service.dto;

import com.doctorappointment.patient_service.exception.InvalidEmailException;
import com.doctorappointment.patient_service.exception.InvalidPhoneNumberException;
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
                           String address) {

    public PatientModel {
        //name validation
        if (isNullOrEmpty(firstName)) {
            throw new ValidationException(" first name cannot be empty or null");
        }
        if (isNullOrEmpty(lastName)) {
            throw new ValidationException(" last name cannot be empty or null");
        }

        //email validation
        if (isNullOrEmpty(email)) {
            throw new ValidationException(" email cannot be empty or null");
        }
        String emailRegex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailException("Invalid email address");
        }

        //phone validation
        if (isNullOrEmpty(phoneNumber)) {
            throw new ValidationException(" phone number cannot be empty or null");
        }
        String normalizedPhone = phoneNumber;
        if (phoneNumber.startsWith("+977")) {
            normalizedPhone = phoneNumber.substring(4);
        } else if (phoneNumber.startsWith("977")) {
            normalizedPhone = phoneNumber.substring(3);
        }
        if (!normalizedPhone.matches("\\d{10}")) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }
        if (!normalizedPhone.startsWith("97") && !normalizedPhone.startsWith("98")) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }
        phoneNumber = "+977 " + normalizedPhone;

        //password validation
        if (isNullOrEmpty(password)) {
            throw new ValidationException(" password cannot be empty or null");
        }
        if (password.length() < 8) {
            throw new ValidationException(" password must be at least 8 characters long");
        }

        //address validation
        if (isNullOrEmpty(address)) {
            throw new ValidationException(" address cannot be empty or null");
        }
    }

    public boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
