package com.doctorappointment.doctor.helper;

import com.doctorappointment.*;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorRequest;
import jakarta.inject.Singleton;

import java.util.List;
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
                .clinicName(doctorRequest.getClinicName())
                .clinicBuilding(doctorRequest.getClinicBuilding())
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
                .setClinicName(doctor.clinicName())
                .setClinicBuilding(doctor.clinicBuilding())
                .setDoctorStatus(status)
                .setDoctorMessage(message)

                .build();
    }

    //update doctor by ID
    public static DoctorModel fromUpdateRequest
    (UpdateDoctorRequest request) {
        return DoctorModel.builder()
                .doctorId(UUID.fromString(request.getDoctorId()))
                .firstName(request.getDoctorFirstName())
                .lastName(request.getDoctorLastName())
                .phoneNumber(request.getDoctorPhoneNumber())
                .address(request.getDoctorAddress())
                .email(request.getDoctorEmail())
                .specialization(request.getSpecialization())
                .clinicAddress(request.getClinicAddress())
                .clinicName(request.getClinicName())
                .clinicBuilding(request.getClinicBuilding())
                .dailyLimit(request.getDailyLimit())
                .build();
    }

    //update doctor response
    public static UpdateDoctorResponse toUpdateResponse(DoctorModel model, String status, String message) {
        return UpdateDoctorResponse.newBuilder()
                .setDoctorId(model.doctorId().toString())
                .setDoctorFirstName(model.firstName())
                .setDoctorLastName(model.lastName())
                .setDoctorPhoneNumber(model.phoneNumber())
                .setDoctorAddress(model.address())
                .setDoctorEmail(model.email())
                .setSpecialization(model.specialization())
                .setClinicAddress(model.clinicAddress())
                .setLatitude(model.latitude())
                .setLongitude(model.longitude())
                .setClinicName(model.clinicName())
                .setClinicBuilding(model.clinicBuilding())
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

    //get by ID
    public static GetDoctorByIdResponse toGetByIdResponse(DoctorModel doctor,
                                                          String status,
                                                          String message) {
        return GetDoctorByIdResponse.newBuilder()
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
                .setClinicName(doctor.clinicName())
                .setClinicBuilding(doctor.clinicBuilding())
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }

    //get by email
    public static GetByDoctorEmailResponse getDoctorByEmailResponse(DoctorModel doctor,
                                                                    String status,
                                                                    String message) {
        return GetByDoctorEmailResponse.newBuilder()
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
                .setClinicName(doctor.clinicName())
                .setClinicBuilding(doctor.clinicBuilding())
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }

    //delete doctor
    public static DeleteDoctorResponse toDeleteResponse(String doctorId,
                                                        String status,
                                                        String message) {
        return DeleteDoctorResponse.newBuilder()
                .setDoctorId(doctorId)
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }

    //doctor list response
    public static DoctorListResponse toDoctorListResponse(List<DoctorModel> doctors,
                                                          List<Double> distances,
                                                          String status,
                                                          String message) {
        DoctorListResponse.Builder builder = DoctorListResponse.newBuilder()
                .setDoctorStatus(status)
                .setDoctorMessage(message);
        for (int i = 0; i < doctors.size(); i++) {
            DoctorModel d = doctors.get(i);
            builder.addDoctors(DoctorSummary.newBuilder()
                    .setDoctorId(d.doctorId().toString())
                    .setDoctorFirstName(d.firstName())
                    .setDoctorLastName(d.lastName())
                    .setSpecialization(d.specialization())
                    .setClinicAddress(d.clinicAddress())
                    .setDoctorPhoneNumber(d.phoneNumber())
                    .setDistanceKm(distances.get(i))
                    .build());
        }
        return builder.build();
    }
//response doctor availability
    public static DoctorAvailabilityResponse toAvailabilityResponse(String doctorId,
                                                                    String date,
                                                                    boolean isAvailable,
                                                                    int bookedCount,
                                                                    int maxCapacity,
                                                                    String status,
                                                                    String message) {
        return DoctorAvailabilityResponse.newBuilder()
                .setDoctorId(doctorId)
                .setDate(date)
                .setIsAvailable(isAvailable)
                .setBookedCount(bookedCount)
                .setMaxCapacity(maxCapacity)
                .setDoctorStatus(status)
                .setDoctorMessage(message)
                .build();
    }

}
