package com.doctorappointment.patient.service;

import com.doctorappointment.patient.exception.InvalidEmailException;
import com.doctorappointment.patient.exception.InvalidPasswordException;
import com.doctorappointment.patient.exception.InvalidPhoneNumberException;
import jakarta.validation.ValidationException;
import java.util.regex.Pattern;

public class ValidatePatient {
    public static void validatePatient(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String address)
    {
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
        phoneNumber = phoneNumber.replaceAll("\\s+", "");
        if (phoneNumber.startsWith("+977")) {
            phoneNumber = phoneNumber.substring(4);
        }
        if (!phoneNumber.matches("\\d{10}")) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }
        if (!phoneNumber.startsWith("97") && !phoneNumber.startsWith("98")) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }
        //address validation
        if (isNullOrEmpty(address)) {
            throw new ValidationException(" address cannot be empty or null");
        }
    }
    public static void validatePassword(String password){
        //password validation
        if (isNullOrEmpty(password)) {
            throw new ValidationException(" password cannot be empty or null");
        }
        if(password.length() < 8) {
            throw new InvalidPasswordException("Password too short");
        }

    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
