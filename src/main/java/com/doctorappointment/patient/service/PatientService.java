package com.doctorappointment.patient.service;
import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.dto.PatientRequest;
import com.doctorappointment.patient.exception.EmailAlreadyExistsException;
import com.doctorappointment.patient.exception.InvalidEmailPasswordException;
import com.doctorappointment.patient.exception.InvalidPasswordException;
import com.doctorappointment.patient.exception.PatientNotFoundException;
import com.doctorappointment.patient.repository.PatientRepoInterface;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@Slf4j
@Singleton
public class PatientService {
   private final PatientRepoInterface patientRepo;

    public PatientService(PatientRepoInterface patientRepo) {
        this.patientRepo = patientRepo;
    }
    //add patient
    public PatientModel addPatient(PatientRequest patient) {
        ValidatePatient.validatePatient(
                patient.firstName(),
                patient.lastName(),
                patient.email(),
                patient.phoneNumber(),
                patient.address()
        );
        ValidatePatient.validatePassword(patient.password());
        if(patientRepo.existsPatientByEmail(patient.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        String hashedPassword= BCrypt.hashpw(patient.password(), BCrypt.gensalt());
        PatientModel patientModel=PatientModel.builder()
                .patientId(UUID.randomUUID())
                .firstName(patient.firstName())
                .lastName(patient.lastName())
                .email(patient.email())
                .password(hashedPassword)
                .isDeleted(false)
                .phoneNumber(patient.phoneNumber())
                .address(patient.address())
                .build();
        return patientRepo.addPatient(patientModel);
    }
    //get patient by ID
    public PatientModel getPatientById(UUID id) {
        if(id == null) {
            throw new PatientNotFoundException("Patient not found");
        }
        log.info("Getting patient by id {}", id);
        return patientRepo.getPatientById(id);
    }
    public PatientModel getPatientByEmail(String email) {
        if(email == null) {
            throw new PatientNotFoundException("Patient not found");
        }
        log.info("Getting patient by email {}", email);
        return patientRepo.getPatientByEmail(email);
    }
    //update patient by ID
    public PatientModel updatePatient(PatientRequest patient) {
        ValidatePatient.validatePatient(
                patient.firstName(),
                patient.lastName(),
                patient.email(),
                patient.phoneNumber(),
                patient.address());
        if(patient.patientId() == null) {
            throw new PatientNotFoundException("Patient id is required");
        }
        PatientModel existingPatient=patientRepo.getPatientById(patient.patientId());
        if(existingPatient == null) {
            throw new PatientNotFoundException("Patient not found");
        }
        if(existingPatient.isDeleted()) {
            throw new PatientNotFoundException("Patient not found");
        }
        PatientModel updated = existingPatient.toBuilder()
                .firstName(patient.firstName() != null ? patient.firstName() : existingPatient.firstName())
                .lastName(patient.lastName() != null ? patient.lastName() : existingPatient.lastName())
                .phoneNumber(patient.phoneNumber() != null ? patient.phoneNumber() : existingPatient.phoneNumber())
                .address(patient.address() != null ? patient.address() : existingPatient.address())
                .email(patient.email() != null ? patient.email() : existingPatient.email())
                .build();
        PatientModel result = patientRepo.updatePatient(updated);
        log.info("Patient updated with this ID {}", patient.patientId());
        return result;
    }
    //delete patients by ID
    public void deletePatient(UUID id) {
        if(id == null) {
            throw new PatientNotFoundException("Patient id is required");
        }
        PatientModel existingPatient=patientRepo.getPatientById(id);
        if(existingPatient == null) {
            throw new PatientNotFoundException("Patient not found with this ID : "+ id);
        }
        if(existingPatient.isDeleted()) {
            throw new PatientNotFoundException("Patient is already deleted");
        }
        patientRepo.deletePatient(id);
        log.info("Patient deleted with this ID {}", id);
    }
//    public List<PatientModel> getAllPatients() {
//        log.info("Getting all patients");
//        return patientRepo.getAllPatients();
//    }

    //login
    public PatientModel login(String email, String password) {
        log.info("Login email :{}, password :{}",email,password==null ? "null":password.isEmpty());
        if(email == null || password == null) {
            throw new PatientNotFoundException("Email is required");
        }
        PatientModel patient=patientRepo.getPatientByEmail(email.trim());
        if(patient == null) {
            throw new InvalidEmailPasswordException("Patient not found");
        }
        if(patient.isDeleted()){
            throw new PatientNotFoundException("Patient not found");
        }
        if(!BCrypt.checkpw(password, patient.password())) {
            throw new InvalidEmailPasswordException("Invalid email or password");
        }
        log.info("Logged in with email {} and password {}", email, patient.password());
        return patient;
    }
}
