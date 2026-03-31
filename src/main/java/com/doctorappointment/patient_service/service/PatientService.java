package com.doctorappointment.patient_service.service;
import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.exception.EmailAlreadyExistsException;
import com.doctorappointment.patient_service.exception.PatientNotFoundException;
import com.doctorappointment.patient_service.repository.PatientRepoInterface;
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
    public PatientModel addPatient(PatientModel patient) {
        if(patientRepo.existsPatientByEmail(patient.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        String hashedPassword= BCrypt.hashpw(patient.password(), BCrypt.gensalt());
        return patientRepo.addPatient(patient.toBuilder()
                .patientId(UUID.randomUUID())
                .password(hashedPassword)
                .build());
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
    public PatientModel updatePatient(PatientModel patient) {
        if(patient.patientId() == null) {
            throw new PatientNotFoundException("Patient id is required");
        }
        PatientModel existingPatient=patientRepo.getPatientById(patient.patientId());
        if(existingPatient == null) {
            throw new PatientNotFoundException("Patient not found");
        }
        if(existingPatient.isDeleted()) {
            throw new PatientNotFoundException("Patient is already deleted");
        }
        PatientModel updated=patient.toBuilder()
                .email(existingPatient.email())
                .password(existingPatient.password())
                .build();
        patientRepo.updatePatient(updated);
        log.info("Patient updated with this ID {}", patient.patientId());
        return updated;
    }
    //delete patients by Id
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
}
