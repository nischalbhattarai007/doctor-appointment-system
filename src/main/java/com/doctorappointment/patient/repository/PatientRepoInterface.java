package com.doctorappointment.patient.repository;

import com.doctorappointment.patient.dto.PatientModel;

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
