package com.doctorappointment.auth;

import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.repository.PatientRepoInterface;
import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;
@Singleton
public class BasicAuthValidator {
    private final PatientRepoInterface patientRepo;
    public BasicAuthValidator(PatientRepoInterface patientRepo) {
        this.patientRepo = patientRepo;
    }
    public boolean validate(String email, String password) {
        PatientModel patient=patientRepo.getPatientByEmail(email);
        if(patient==null){
            return false;
        }
        if(patient.isDeleted()){
            return false;
        }
        return BCrypt.checkpw(password,patient.password());
    }
}
