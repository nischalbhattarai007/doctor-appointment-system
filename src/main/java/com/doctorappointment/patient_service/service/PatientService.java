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
    public PatientModel getPatientById(UUID id) {
        if(id == null) {
            throw new PatientNotFoundException("Patient not found");
        }
        log.info("Getting patient by id {}", id);
        return patientRepo.getPatientById(id);
    }
}
