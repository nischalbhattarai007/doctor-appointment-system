package com.doctorappointment.patient_service.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
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
    public PatientModel addPatient(PatientModel patient) {
       BoundStatement bs=insertPatient.bind(
               patient.patientId(),
               patient.firstName(),
               patient.lastName(),
               patient.email(),
               patient.password(),
               patient.phoneNumber(),
               patient.isDeleted());
       session.execute(bs);
       return patient;
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

    @Override
    public boolean existsPatientByEmail(String email) {
        var ps=session.prepare(PatientQuery.EXISTS_BY_EMAIL);
        BoundStatement bs=ps.bind(email);
        ResultSet rs=session.execute(bs);
        return rs.one()!=null;
        }
    }
