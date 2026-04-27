package com.doctorappointment.doctor.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.doctorappointment.doctor.constant.DoctorQuery;
import com.doctorappointment.doctor.constant.DoctorSchema;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.exception.DoctorCreationFailedException;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final PreparedStatement insertGeohash;
    private final PreparedStatement deleteGeohash;
    private final PreparedStatement findByGeohash;

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
        this.insertGeohash=session.prepare(DoctorQuery.INSERT_GEOHASH);
        this.deleteGeohash=session.prepare(DoctorQuery.DELETE_GEOHASH);
        this.findByGeohash=session.prepare(DoctorQuery.FIND_BY_GEOHASH_PREFIXES);
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
                doctor.latitude(),
                doctor.longitude(),
                doctor.dailyLimit(),
                false,
                doctor.clinicName(),
                doctor.clinicBuilding(),
                doctor.geoHash(),
                doctor.area(),
                doctor.city(),
                doctor.street()
        );
        BoundStatement bs2 = insertClinicAddress.bind(
                doctor.doctorId(),
                doctor.area(),
                doctor.city(),
                doctor.clinicBuilding());
        BoundStatement bs3=insertGeohash.bind(
                doctor.geoHash(),
                doctor.doctorId()
        );
        ResultSet rs;
        try {
            rs = session.execute(bs2);
        } catch (Exception e) {
            throw new DoctorCreationFailedException(e.getMessage());
        }
        Row row = rs.one();
        if (!row.getBoolean("[applied]")) {
            throw new DoctorCreationFailedException("Doctor already exists");
        }
        try {
            session.execute(bs);
            session.execute(bs3);
        } catch (Exception e) {
            session.execute(deleteFromUniquenessAddress.bind(
                    doctor.area(),
                    doctor.city(),
                    doctor.clinicBuilding()));
            throw e;
        }
        return doctor;
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
    public DoctorModel updateDoctor(DoctorModel doctor,String oldGeohash) {
        BoundStatement bs = updateDoctor.bind(
                doctor.firstName(),
                doctor.lastName(),
                doctor.phoneNumber(),
                doctor.address(),
                doctor.email(),
                doctor.specialization(),
                doctor.latitude(),
                doctor.longitude(),
                doctor.dailyLimit(),
                doctor.clinicName(),
                doctor.clinicBuilding(),
                doctor.geoHash(),
                doctor.area(),
                doctor.city(),
                doctor.street(),
                doctor.doctorId()
        );
        session.execute(bs);
        //sync geohash lookup table only if it changed
        if(!doctor.geoHash().equals(oldGeohash)) {
            session.execute(deleteGeohash.bind(oldGeohash,doctor.doctorId()));
            session.execute(insertGeohash.bind(doctor.geoHash(),doctor.doctorId()));
        }
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
    public boolean existsByClinicAddressAndBuilding(String area,String city, String building) {
        BoundStatement bs = findByClinicAddressAndBuilding.bind(
                city.trim().toLowerCase(),
                area.trim().toLowerCase(),
                building.trim().toLowerCase());
        return session.execute(bs).one() != null;
    }

    @Override
    public List<DoctorModel> findDoctorsByGeohashPrefixes(Set<String> prefixes) {
        BoundStatement bs=findByGeohash.bind(new ArrayList<>(prefixes));
        ResultSet rs = session.execute(bs);
        List<DoctorModel> doctors = new ArrayList<>();
        for (Row row : rs) {
            UUID doctorId=row.getUuid(DoctorSchema.DOCTOR_ID);
            DoctorModel doctor=getDoctorById(doctorId);
            if (doctor != null) {
                doctors.add(doctor);
            }
        }
        return doctors;
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
                .latitude(row.getDouble(DoctorSchema.LATITUDE))
                .longitude(row.getDouble(DoctorSchema.LONGITUDE))
                .dailyLimit(row.getInt(DoctorSchema.DAILY_LIMIT))
                .isDeleted(row.getBoolean(DoctorSchema.IS_DELETED))
                .clinicBuilding(row.getString(DoctorSchema.CLINIC_BUILDING))
                .clinicName(row.getString(DoctorSchema.CLINIC_NAME))
                .geoHash(row.getString(DoctorSchema.GEOHASH))
                .area(row.getString(DoctorSchema.AREA))
                .city(row.getString(DoctorSchema.CITY))
                .street(row.getString(DoctorSchema.STREET))
                .build();
    }
}
