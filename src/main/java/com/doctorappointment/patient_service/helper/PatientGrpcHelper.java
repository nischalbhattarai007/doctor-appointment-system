package com.doctorappointment.patient_service.helper;

import com.doctorappointment.PatientResponse;
import com.doctorappointment.RegisterPatientRequest;
import com.doctorappointment.UpdatePatientRequest;
import com.doctorappointment.UpdatePatientResponse;
import com.doctorappointment.patient_service.dto.PatientModel;

import java.util.UUID;

public class PatientGrpcHelper {
    public static PatientModel fromRegisterRequest(RegisterPatientRequest request) {
//        PatientModel patientModel = new PatientModel();
//        patientModel.setFirstName(request.getFirstName());
//        patientModel.setLastName(request.getLastName());
//        patientModel.setEmail(request.getEmail());
//        patientModel.setPassword(request.getPassword());
//        patientModel.setAddress(request.getAddress());
//        patientModel.setPhoneNumber(request.getPhoneNumber());
//        return patientModel;

        return PatientModel.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .isDeleted(false)
                .build();
    }
    public static PatientResponse toResponse(PatientModel patient,
                                             String status,
                                             String message) {
        return PatientResponse.newBuilder()
                .setPatientId(patient.patientId().toString())
                .setFirstName(patient.firstName())
                .setLastName(patient.lastName())
                .setEmail(patient.email())
                .setPhoneNumber(patient.phoneNumber())
                .setAddress(patient.address())
                .setStatus(status)
                .setMessage(message)
                .build();
    }
    public static PatientModel fromUpdateRequest(UpdatePatientRequest request) {
        return PatientModel.builder()
                .patientId(UUID.fromString(request.getPatientId()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();
    }
    public static UpdatePatientResponse toUpdateResponse(PatientModel patient,
                                                                         String status,
                                                                         String message) {
        return UpdatePatientResponse.newBuilder()
                .setPatientId(patient.patientId().toString())
                .setFirstName(patient.firstName())
                .setLastName(patient.lastName())
                .setEmail(patient.email())
                .setPhoneNumber(patient.phoneNumber())
                .setAddress(patient.address())
                .setStatus(status)
                .setMessage(message)
                .build();
    }
}
