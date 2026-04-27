package com.doctorappointment.doctor.repository;

import com.doctorappointment.doctor.dto.DoctorModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DoctorRepoInterface {
    DoctorModel addDoctor(DoctorModel doctor);
    DoctorModel getDoctorById(UUID id);
    DoctorModel getDoctorByEmail(String email);
    void deleteDoctorById(UUID id);
    DoctorModel updateDoctor(DoctorModel doctor,String oldGeohash);
    boolean existsDoctorByEmail(String email);
    List<DoctorModel> getAllDoctors();
    //boolean existsByClinicBuilding(String clinicBuilding);
    boolean existsByClinicAddressAndBuilding(String area,String city,String building);
    List<DoctorModel> findDoctorsByGeohashPrefixes(Set<String> prefixes);
}
