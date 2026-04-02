package com.doctorappointment.doctor_service.helper;

import com.doctorappointment.*;
import com.doctorappointment.doctor_service.dto.DoctorModel;
import com.doctorappointment.doctor_service.dto.DoctorRequest;
import jakarta.inject.Singleton;

import java.util.UUID;
@Singleton
public class DoctorGrpcHelper {
    //register for doctor
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

    //register response to doctor
    public static RegisterDoctorResponse toRegisterResponse
            (DoctorModel doctor, String status, String message) {
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

    //update doctor by ID
   public static DoctorRequest fromUpdateRequest
           (UpdateDoctorRequest request) {
        return DoctorRequest.builder()
                .doctorId(UUID.fromString(request.getDoctorId()))
                .firstName(request.getDoctorFirstName())
                .lastName(request.getDoctorLastName())
                .phoneNumber(request.getDoctorPhoneNumber())
                .address(request.getDoctorAddress())
                .specialization(request.getSpecialization())
                .clinicAddress(request.getClinicAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
   }
   //update doctor response
    public static UpdateDoctorResponse toUpdateResponse(DoctorModel model,String status,String message) {
        return UpdateDoctorResponse.newBuilder()
                .setDoctorId(model.doctorId().toString())
                .setDoctorFirstName(model.firstName())
                .setDoctorLastName(model.lastName())
                .setDoctorEmail(model.email())
                .setDoctorPhoneNumber(model.phoneNumber())
                .setDoctorAddress(model.address())
                .setSpecialization(model.specialization())
                .setClinicAddress(model.clinicAddress())
                .setLatitude(model.latitude())
                .setLongitude(model.longitude())
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }

    //login
    public static DoctorLoginResponse toLoginResponse(DoctorModel doctor,
                                                      String status,
                                                      String message) {
        return DoctorLoginResponse.newBuilder()
                .setDoctorId(doctor.doctorId().toString())
                .setDoctorFirstName(doctor.firstName())
                .setDoctorLastName(doctor.lastName())
                .setDoctorEmail(doctor.email())
                .setDoctorPhoneNumber(doctor.phoneNumber())
                .setDoctorAddress(doctor.address())
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }


}
