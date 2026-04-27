package com.doctorappointment.patient.repository;

import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.dto.PatientRequest;

import java.util.UUID;

public interface PatientServiceInterface {
    PatientModel addPatient(PatientRequest patient);

    PatientModel getPatientById(UUID id);

    PatientModel getPatientByEmail(String email);

    PatientModel updatePatient(PatientRequest patient);

    void deletePatient(UUID id);

    PatientModel login(String email, String password);
}
