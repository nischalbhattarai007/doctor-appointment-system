package com.doctorappointment.doctor.dto;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record DoctorRequest(
        UUID doctorId,
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber,
        String address,
        String specialization,
       // String clinicAddress,
        double latitude,
        double longitude,
        String clinicName,
        String clinicBuilding,
        String geoHash,
        String street,
        String area,
        String city) {

}
