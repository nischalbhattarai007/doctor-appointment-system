package com.doctorappointment.auth;

import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.repository.PatientRepoInterface;
import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;
@Singleton
public class BasicAuthValidator {
    private final PatientRepoInterface patientRepo;
    private final DoctorRepoInterface doctorRepo;
    public BasicAuthValidator(PatientRepoInterface patientRepo, DoctorRepoInterface doctorRepo) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }
    public boolean validate(String email, String password) {
        PatientModel patient=patientRepo.getPatientByEmail(email);
        //check patient table
       if(patient!=null && !patient.isDeleted()){
        return BCrypt.checkpw(password,patient.password());}
       //check doctor table
        DoctorModel doctor=doctorRepo.getDoctorByEmail(email);
        if(doctor!=null && !doctor.isDeleted()){
            return BCrypt.checkpw(password,doctor.password());
        }
        return false;
    }
}
