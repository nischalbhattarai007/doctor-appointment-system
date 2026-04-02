package com.doctorappointment.doctor_service.helper;

import com.doctorappointment.RegisterDoctorRequest;
import com.doctorappointment.RegisterDoctorResponse;
import com.doctorappointment.doctor_service.dto.DoctorRequest;

public class DoctorGrpcHelper {
    public static DoctorRequest fromRegisterRequest
            (RegisterDoctorRequest doctorRequest) {
        return DoctorRequest.builder()
                .firstName(doctorRequest.getDoctorFirstName())
                .lastName(doctorRequest.getDoctorLastName())
                .email(doctorRequest.getDoctorEmail())
                .password(doctorRequest.getPassword())
                .phoneNumber(doctorRequest.getDoctorPhoneNumber())
                .address(doctorRequest.getDoctorAddress())
                .specialization(doctorRequest.getSpecialization())
                .clinicAddress(doctorRequest.getClinicAddress())
                .latitude(doctorRequest.getLatitude())
                .longitude(doctorRequest.getLongitude())
                .build();
    }

    public static RegisterDoctorResponse toRegisterResponse
            (DoctorRequest doctor, String status, String message) {
        return RegisterDoctorResponse.newBuilder()
                .setDoctorId(doctor.doctorId().toString())
                .setDoctorFirstName(doctor.firstName())
                .setDoctorLastName(doctor.lastName())
                .setDoctorEmail(doctor.email())
                .setDoctorPhoneNumber(doctor.phoneNumber())
                .setDoctorAddress(doctor.address())
                .setSpecialization(doctor.specialization())
                .setClinicAddress(doctor.clinicAddress())
                .setLatitude(doctor.latitude())
                .setLongitude(doctor.longitude())
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }
}
