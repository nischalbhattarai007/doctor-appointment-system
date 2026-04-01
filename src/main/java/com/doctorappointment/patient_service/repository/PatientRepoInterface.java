package com.doctorappointment.patient_service.repository;

import com.doctorappointment.patient_service.dto.PatientModel;

import java.util.List;
import java.util.UUID;

public interface PatientRepoInterface {
    PatientModel addPatient(PatientModel patient);
    PatientModel getPatientById(UUID id);
    PatientModel getPatientByEmail(String email);
    PatientModel updatePatient(PatientModel patient);
    void deletePatient(UUID id);
    boolean existsPatientByEmail(String email);
//    List<PatientModel> getAllPatients();


}
