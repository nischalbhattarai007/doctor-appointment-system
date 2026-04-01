package com.doctorappointment.patient_service.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.doctorappointment.config.ScyllaDbConfig;
import com.doctorappointment.patient_service.constant.PatientQuery;
import com.doctorappointment.patient_service.constant.PatientSchema;
import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.exception.PatientNotFoundException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Singleton
@Slf4j
class PatientRepository implements PatientRepoInterface {
    private final ScyllaDbConfig config;
    private final CqlSession session;
    private final PreparedStatement insertPatient;
    private final PreparedStatement getPatientById;
    private final PreparedStatement getPatientByEmail;
    private final PreparedStatement updatePatient;
    private final PreparedStatement deletePatient;
//    private final PreparedStatement getAllPatients;


    PatientRepository(ScyllaDbConfig config) {
        this.config = config;
        this.session= config.getSession();
        this.insertPatient=session.prepare(PatientQuery.INSERT);
        this.getPatientById=session.prepare(PatientQuery.FIND_BY_ID);
        this.getPatientByEmail=session.prepare(PatientQuery.FIND_BY_EMAIL);
        this.updatePatient=session.prepare(PatientQuery.UPDATE);
        this.deletePatient=session.prepare(PatientQuery.SOFT_DELETE);
//        this.getAllPatients=session.prepare(PatientQuery.GET_ALL_PATIENTS);

    }

    @Override
    public PatientModel addPatient(PatientModel patient) {
       BoundStatement bs=insertPatient.bind(
               patient.patientId(),
               patient.firstName(),
               patient.lastName(),
               patient.email(),
               patient.password(),
               false,
               patient.phoneNumber(),
               patient.address());
       session.execute(bs);
       return patient;
    }
    @Override
    public PatientModel getPatientById(UUID id) {
        BoundStatement bs=getPatientById.bind(id);
        Row row = session.execute(bs).one();
        if(row==null){
            return null;
        }
        return PatientModel.builder()
                .patientId(row.getUuid(PatientSchema.PATIENT_ID))
                .firstName(row.getString(PatientSchema.PATIENT_FIRSTNAME))
                .lastName(row.getString(PatientSchema.PATIENT_LASTNAME))
                .email(row.getString(PatientSchema.PATIENT_EMAIL))
                .password(row.getString(PatientSchema.PATIENT_PASSWORD))
                .address(row.getString(PatientSchema.PATIENT_ADDRESS))
                .phoneNumber(row.getString(PatientSchema.PATIENT_PHONE))
                .isDeleted(row.getBoolean(PatientSchema.IS_DELETED))
                .build();
    }
    @Override
    public PatientModel getPatientByEmail(String email) {
       BoundStatement bs=getPatientByEmail.bind(email);
        Row row = session.execute(bs).one();
        if(row==null){
            return null;
        }
        return PatientModel.builder()
                .patientId(row.getUuid(PatientSchema.PATIENT_ID))
                .firstName(row.getString(PatientSchema.PATIENT_FIRSTNAME))
                .lastName(row.getString(PatientSchema.PATIENT_LASTNAME))
                .email(row.getString(PatientSchema.PATIENT_EMAIL))
                .password(row.getString(PatientSchema.PATIENT_PASSWORD))
                .address(row.getString(PatientSchema.PATIENT_ADDRESS))
                .phoneNumber(row.getString(PatientSchema.PATIENT_PHONE))
                .isDeleted(row.getBoolean(PatientSchema.IS_DELETED))
                .build();
    }

    @Override
    public PatientModel updatePatient(PatientModel patient) {
        BoundStatement bs=updatePatient.bind(
                patient.firstName(),
                patient.lastName(),
                patient.phoneNumber(),
                patient.address(),
                patient.email(),
                patient.password(),
                patient.patientId());
        session.execute(bs);
        return patient;
    }

    @Override
    public void deletePatient(UUID id) {
        if(id==null){
            throw new PatientNotFoundException("Patient id is null");
        }
        BoundStatement bs=deletePatient.bind(id);
        session.execute(bs);
    }

    @Override
    public boolean existsPatientByEmail(String email) {
        var ps=session.prepare(PatientQuery.EXISTS_BY_EMAIL);
        BoundStatement bs=ps.bind(email);
        ResultSet rs=session.execute(bs);
        return rs.one()!=null;
        }
//        @Override
//    public List<PatientModel> getAllPatients() {
//        ResultSet rs=session.execute(PatientQuery.GET_ALL_PATIENTS);
//        List<PatientModel> patients=new ArrayList<>();
//        for(Row row:rs){
//            patients.add(PatientModel.builder()
//                    .patientId(row.getUuid(PatientSchema.PATIENT_ID))
//                    .firstName(row.getString(PatientSchema.PATIENT_FIRSTNAME))
//                    .lastName(row.getString(PatientSchema.PATIENT_LASTNAME))
//                    .email(row.getString(PatientSchema.PATIENT_EMAIL))
//                    .password(row.getString(PatientSchema.PATIENT_PASSWORD))
//                    .phoneNumber(row.getString(PatientSchema.PATIENT_PHONE))
//                    .address(row.getString(PatientSchema.PATIENT_ADDRESS))
//                    .isDeleted(row.getBoolean(PatientSchema.IS_DELETED))
//                    .build());}
//        return patients;
//        }
    }
