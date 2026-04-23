package com.doctorappointment.doctor.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.doctorappointment.doctor.constant.DoctorQuery;
import com.doctorappointment.doctor.constant.DoctorSchema;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorRequest;
import com.doctorappointment.doctor.exception.DoctorCreationFailedException;
import jakarta.inject.Singleton;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
class DoctorRepository implements DoctorRepoInterface {
    private final CqlSession session;
    private final PreparedStatement insertDoctor;
    private final PreparedStatement findDoctorById;
    private final PreparedStatement findDoctorByEmail;
    private final PreparedStatement getAllDoctors;
    private final PreparedStatement updateDoctor;
    private final PreparedStatement softDeleteDoctor;
    private final PreparedStatement existsDoctorByEmail;
    //    private final PreparedStatement findByClinicBuilding;
    private final PreparedStatement insertClinicAddress;
    private final PreparedStatement findByClinicAddressAndBuilding;
    private final PreparedStatement deleteFromUniquenessAddress;

    DoctorRepository(CqlSession session) {
        this.session = session;
        this.insertDoctor = session.prepare(DoctorQuery.INSERT);
        this.findDoctorById = session.prepare(DoctorQuery.FIND_BY_ID);
        this.findDoctorByEmail = session.prepare(DoctorQuery.FIND_BY_EMAIL);
        this.updateDoctor = session.prepare(DoctorQuery.UPDATE);
        this.softDeleteDoctor = session.prepare(DoctorQuery.SOFT_DELETE);
        this.existsDoctorByEmail = session.prepare(DoctorQuery.EXISTS_BY_EMAIL);
        this.getAllDoctors = session.prepare(DoctorQuery.GET_ALL_DOCTORS);
        //this.findByClinicBuilding = session.prepare(DoctorQuery.FIND_BY_CLINIC_BUILDING);
        this.insertClinicAddress = session.prepare(DoctorQuery.INSERT_UNIQUENESS_DOCTOR_ADDRESS);
        this.findByClinicAddressAndBuilding = session.prepare(DoctorQuery.FIND_BY_CLINIC_ADDRESS_BUILDING);
        this.deleteFromUniquenessAddress = session.prepare(DoctorQuery.DELETE_BY_CLINIC_ADDRESS_BUILDING);
    }

    @Override
    public DoctorModel addDoctor(DoctorModel doctor) {
        BoundStatement bs = insertDoctor.bind(
                doctor.doctorId(),
                doctor.firstName(),
                doctor.lastName(),
                doctor.email(),
                doctor.password(),
                doctor.phoneNumber(),
                doctor.address(),
                doctor.specialization(),
                doctor.clinicAddress(),
                doctor.latitude(),
                doctor.longitude(),
                doctor.dailyLimit(),
                false,
                doctor.clinicName(),
                doctor.clinicBuilding()
        );
        BoundStatement bs2 = insertClinicAddress.bind(
                doctor.doctorId(),
                doctor.clinicAddress(),
                doctor.clinicBuilding());
        ResultSet rs;
        try{
        rs = session.execute(bs2);
        }catch (Exception e){
            throw new DoctorCreationFailedException(e.getMessage());
        }
        Row row = rs.one();
        if (row == null || !row.getBoolean("[applied]")) {
            throw new DoctorCreationFailedException("Doctor already exists");
        }
        try{
        session.execute(bs);
        }catch(Exception e){
            session.execute(deleteFromUniquenessAddress.bind(doctor.clinicAddress(),doctor.clinicBuilding()));
            throw e;
        }
        return  doctor;
    }

    @Override
    public DoctorModel getDoctorById(UUID id) {
        BoundStatement bs = findDoctorById.bind(id);
        Row row = session.execute(bs).one();
        if (row == null) {
            return null;
        }
        return mapRow(row);

    }

    @Override
    public DoctorModel getDoctorByEmail(String email) {
        BoundStatement bs = findDoctorByEmail.bind(email);
        Row row = session.execute(bs).one();
        if (row == null) {
            return null;
        }
        return mapRow(row);
    }

    @Override
    public void deleteDoctorById(UUID id) {
        BoundStatement bs = softDeleteDoctor.bind(id);
        session.execute(bs);
    }

    @Override
    public DoctorModel updateDoctor(DoctorModel doctor) {
        BoundStatement bs = updateDoctor.bind(
                doctor.firstName(),
                doctor.lastName(),
                doctor.phoneNumber(),
                doctor.address(),
                doctor.specialization(),
                doctor.clinicAddress(),
                doctor.latitude(),
                doctor.longitude(),
                doctor.dailyLimit(),
                doctor.doctorId(),
                doctor.clinicName(),
                doctor.clinicBuilding()
        );
        session.execute(bs);
        return doctor;
    }

    @Override
    public boolean existsDoctorByEmail(String email) {
        BoundStatement bs = existsDoctorByEmail.bind(email);
        return session.execute(bs).one() != null;
    }

    @Override
    public List<DoctorModel> getAllDoctors() {
        ResultSet rs = session.execute(getAllDoctors.bind());
        List<DoctorModel> doctors = new ArrayList<>();
        for (Row row : rs) {
            doctors.add(mapRow(row));
        }
        return doctors;
    }

//    @Override
//    public boolean existsByClinicBuilding(String clinicBuilding) {
//        BoundStatement bs=findByClinicBuilding.bind(clinicBuilding.trim().toLowerCase());
//        return session.execute(bs).one() != null;
//    }

    @Override
    public boolean existsByClinicAddressAndBuilding(String clinicAddress, String building) {
        BoundStatement bs = findByClinicAddressAndBuilding.bind(clinicAddress.trim().toLowerCase(), building.trim().toLowerCase());
        return session.execute(bs).one() != null;
    }

    private DoctorModel mapRow(Row row) {
        return DoctorModel.builder()
                .doctorId(row.getUuid(DoctorSchema.DOCTOR_ID))
                .firstName(row.getString(DoctorSchema.FIRST_NAME))
                .lastName(row.getString(DoctorSchema.LAST_NAME))
                .email(row.getString(DoctorSchema.EMAIL))
                .password(row.getString(DoctorSchema.PASSWORD))
                .phoneNumber(row.getString(DoctorSchema.PHONE_NUMBER))
                .address(row.getString(DoctorSchema.ADDRESS))
                .specialization(row.getString(DoctorSchema.SPECIALIZATION))
                .clinicAddress(row.getString(DoctorSchema.CLINIC_ADDRESS))
                .latitude(row.getDouble(DoctorSchema.LATITUDE))
                .longitude(row.getDouble(DoctorSchema.LONGITUDE))
                .dailyLimit(row.getInt(DoctorSchema.DAILY_LIMIT))
                .isDeleted(row.getBoolean(DoctorSchema.IS_DELETED))
                .clinicBuilding(row.getString(DoctorSchema.CLINIC_BUILDING))
                .clinicName(row.getString(DoctorSchema.CLINIC_NAME))
                .build();
    }
}
