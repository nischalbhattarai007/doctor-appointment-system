package com.doctorappointment.patient_service.service;
import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.exception.EmailAlreadyExistsException;
import com.doctorappointment.patient_service.repository.PatientRepoInterface;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class PatientService {
   private final PatientRepoInterface patientRepo;

    public PatientService(PatientRepoInterface patientRepo) {
        this.patientRepo = patientRepo;
    }
    public void addPatient(PatientModel patient) {
        if(patientRepo.getPatientByEmail(patient.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already exists");
        }
        patient.setPatientId(UUID.randomUUID());
        patientRepo.addPatient(patient);
    }
}
