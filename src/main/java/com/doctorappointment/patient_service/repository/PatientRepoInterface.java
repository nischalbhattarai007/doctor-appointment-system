package com.doctorappointment.patient_service.repository;

import com.doctorappointment.patient_service.dto.PatientModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepoInterface {
    PatientModel addPatient(PatientModel patient);
    PatientModel getPatientById(UUID id);
    Optional<PatientModel> getPatientByEmail(String email);
    void updatePatient(PatientModel patient);
    void deletePatient(UUID id);
    boolean existsPatientByEmail(String email);
}
