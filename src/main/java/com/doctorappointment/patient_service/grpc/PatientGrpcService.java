package com.doctorappointment.patient_service.grpc;

import com.doctorappointment.*;
import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.exception.InvalidPhoneNumberException;
import com.doctorappointment.patient_service.helper.PatientGrpcHelper;
import com.doctorappointment.patient_service.service.PatientService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Singleton
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase {
    private final PatientService service;

    public PatientGrpcService(PatientService service) {
        this.service = service;
    }

    @Override
    public void registerPatient
            (RegisterPatientRequest request, StreamObserver<PatientResponse> responseStreamObserver) {
        try {
            var patientReq = PatientGrpcHelper.fromRegisterRequest(request);
            PatientModel patientModel = service.addPatient(patientReq);
            responseStreamObserver.onNext(
                    PatientGrpcHelper.toResponse
                            (patientModel, "SUCCESS", "Patient registered successfully"));
            log.info("Patient registered successfully");
            responseStreamObserver.onCompleted();
        } catch (ValidationException e) {
            responseStreamObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getPatientById(GetByIdRequest request, StreamObserver<GetByIdResponse> responseStreamObserver) {
        String idString = request.getPatientId();
        UUID id = UUID.fromString(idString);
        try {
            PatientModel patientModel = service.getPatientById(id);
            if (patientModel == null) {
                responseStreamObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Patient not found")
                                .asRuntimeException());
                return;
            }
            GetByIdResponse response = GetByIdResponse.newBuilder()
                    .setPatientId(patientModel.patientId().toString())
                    .setFirstName(patientModel.firstName())
                    .setLastName(patientModel.lastName())
                    .setAddress(patientModel.address())
                    .setPhoneNumber(patientModel.phoneNumber())
                    .setEmail(patientModel.email())
                    .setStatus("Success")
                    .setMessage("Patients retrieved successfully")
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }
}
