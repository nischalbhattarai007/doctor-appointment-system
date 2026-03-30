package com.doctorappointment.patient_service.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.doctorappointment.config.ScyllaDbConfig;
import com.doctorappointment.patient_service.constant.PatientQuery;
import com.doctorappointment.patient_service.constant.PatientSchema;
import com.doctorappointment.patient_service.dto.PatientModel;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Singleton
class PatientRepository implements PatientRepoInterface {
    private final ScyllaDbConfig config;
    private final CqlSession session;
    private final PreparedStatement insertPatient;
    private final PreparedStatement getPatientById;
    private final PreparedStatement updatePatient;


    PatientRepository(ScyllaDbConfig config) {
        this.config = config;
        this.session= config.getSession();
        this.insertPatient=session.prepare(PatientQuery.INSERT);
        this.getPatientById=session.prepare(PatientQuery.FIND_BY_ID);
        this.updatePatient=session.prepare(PatientQuery.UPDATE);
    }

    @Override
    public void addPatient(PatientModel patient) {
        session.execute(insertPatient.bind()
                .setUuid(PatientSchema.PATIENT_ID, patient.getPatientId())
                .setString(PatientSchema.PATIENT_FIRSTNAME, patient.getFirstName())
                .setString(PatientSchema.PATIENT_LASTNAME, patient.getLastName())
                .setString(PatientSchema.PATIENT_EMAIL, patient.getEmail())
                .setString(PatientSchema.PATIENT_PASSWORD, patient.getPassword())
                .setString(PatientSchema.PATIENT_ADDRESS, patient.getAddress())
                .setString(PatientSchema.PATIENT_PHONE, patient.getPhoneNumber())
                .setBoolean(PatientSchema.IS_DELETED, patient.isDeleted())
        );
    }

    @Override
    public Optional<PatientModel> getPatientById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<PatientModel> getPatientByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public void updatePatient(PatientModel patient) {

    }

    @Override
    public void deletePatient(UUID id) {

    }


}
