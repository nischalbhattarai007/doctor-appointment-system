package com.doctorappointment.doctor_service.repository;

import com.doctorappointment.doctor_service.dto.DoctorModel;

import java.util.List;
import java.util.UUID;

public interface DoctorRepoInterface {
    DoctorModel addDoctor(DoctorModel doctor);
    DoctorModel getDoctorById(UUID id);
    DoctorModel getDoctorByEmail(String email);
    void deleteDoctorById(UUID id);
    DoctorModel updateDoctor(DoctorModel doctor);
    boolean existsDoctorByEmail(String email);
    List<DoctorModel> getAllDoctors();
}
