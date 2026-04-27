package com.doctorappointment.doctor.repository;

import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorRequest;

import java.util.List;
import java.util.UUID;

public interface DoctorServiceInterface {

    DoctorModel addDoctor(DoctorRequest doctor);

    DoctorModel getDoctorById(UUID id);

    DoctorModel getDoctorByEmail(String email);

    DoctorModel updateDoctor(DoctorModel doctor);

    void deleteDoctorById(UUID id);

    DoctorModel login(String email, String password);

    DoctorModel getDoctorAvailability(UUID id);

    List<DoctorModel> getDoctorsByLocation(double latitude, double longitude, double radiusKm, int limit);

    List<DoctorModel> getNearestDoctors(double lat1, double lon1, double radiusKm, int limit);

    List<DoctorModel> getAllDoctors();
}
