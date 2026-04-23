package com.doctorappointment.doctor.service;

import com.doctorappointment.patient.exception.InvalidEmailException;
import com.doctorappointment.patient.exception.InvalidPhoneNumberException;
import jakarta.validation.ValidationException;

import java.util.regex.Pattern;

public class ValidateDoctor {
    public static void validateDoctor(String firstName,
                                      String lastName,
                                      String email,
                                      String phoneNumber,
                                      String specialization,
                                      String address,
                                      String clinicAddress,
                                      double latitude,
                                      double longitude){
        if (isNullOrEmpty(firstName)) {
            throw new ValidationException("First name cannot be empty or null");
        }
        if (isNullOrEmpty(lastName)) {
            throw new ValidationException("Last name cannot be empty or null");
        }
        if (isNullOrEmpty(email)) {
            throw new ValidationException("Email cannot be empty or null");
        }
        String emailRegex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailException("Invalid email address");
        }
        if (isNullOrEmpty(phoneNumber)) {
            throw new ValidationException("Phone number cannot be empty or null");
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
        if (isNullOrEmpty(address)) {
            throw new ValidationException("Address cannot be empty or null");
        }
        if (isNullOrEmpty(specialization)) {
            throw new ValidationException("Specialization cannot be empty or null");
        }
        if (isNullOrEmpty(clinicAddress)) {
            throw new ValidationException("Clinic address cannot be empty or null");
        }
        if (latitude < -90 || latitude > 90) {
            throw new ValidationException("Invalid latitude value");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ValidationException("Invalid longitude value");
        }
    }
    public static void validatePassword(String password){
        if (isNullOrEmpty(password)) {
            throw new ValidationException("Password cannot be empty or null");
        }
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
    }
    public static boolean isNullOrEmpty(String value) {
        return  value.isEmpty();
    }
}
