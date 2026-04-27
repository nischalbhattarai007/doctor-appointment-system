package com.doctorappointment.patient.grpc;

import com.doctorappointment.*;
import com.doctorappointment.auth.BasicAuthInterceptor;
import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.exception.EmailAlreadyExistsException;
import com.doctorappointment.patient.exception.PatientNotFoundException;
import com.doctorappointment.patient.helper.PatientGrpcHelper;
import com.doctorappointment.patient.repository.PatientServiceInterface;
import com.doctorappointment.patient.service.PatientService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Singleton
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase {
    private final PatientServiceInterface service;
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
        } catch (EmailAlreadyExistsException e) {
            responseStreamObserver.onError(
                    Status.ALREADY_EXISTS
                            .withDescription("Email already exists")
                            .asRuntimeException());
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
    public void getPatientById
            (GetByIdRequest request, StreamObserver<GetByIdResponse> responseStreamObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String idString = request.getPatientId();
            UUID id = UUID.fromString(idString);
            PatientModel patientModel = service.getPatientById(id);
            if (patientModel == null) {
                responseStreamObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Patient not found")
                                .asRuntimeException());
                return;
            }
            if (!patientModel.email().equals(email)) {
                responseStreamObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Access denied")
                                .asRuntimeException()
                );
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
            log.info("Patient retrieved from ID successfully");
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void getPatientByEmail
            (GetByEmailRequest request, StreamObserver<GetByEmailResponse> responseStreamObserver) {
        try {
            String email_check = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            if (!email_check.equals(request.getEmail())) {
                responseStreamObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Access denied")
                                .asRuntimeException());
                return;
            }
            String email = request.getEmail();
            PatientModel patientModel = service.getPatientByEmail(email);
            if (patientModel == null) {
                responseStreamObserver.onError(
                        Status.NOT_FOUND
                                .withDescription(" Patient not found")
                                .asRuntimeException());
                return;
            }
            GetByEmailResponse response = GetByEmailResponse.newBuilder()
                    .setPatientId(patientModel.patientId().toString())
                    .setFirstName(patientModel.firstName())
                    .setLastName(patientModel.lastName())
                    .setAddress(patientModel.address())
                    .setPhoneNumber(patientModel.phoneNumber())
                    .setEmail(patientModel.email())
                    .setStatus("Success")
                    .setMessage("Patients retrieved successfully")
                    .build();
            log.info("Patient retrieved from email  successfully");
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void updatePatient
            (UpdatePatientRequest request, StreamObserver<UpdatePatientResponse> responseStreamObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            UUID id = UUID.fromString(request.getPatientId());
            PatientModel existing = service.getPatientById(id);
            if (!existing.email().equals(email)) {
                responseStreamObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Access denied")
                                .asRuntimeException()
                );
                return;
            }
            PatientModel patientModel = service.updatePatient(PatientGrpcHelper.fromUpdateRequest(request));
            responseStreamObserver.onNext(
                    PatientGrpcHelper.toUpdateResponse
                            (patientModel, "Success", "Patient updated successfully"));
            log.info("Patient updated successfully");
            responseStreamObserver.onCompleted();
        } catch (PatientNotFoundException e) {
            responseStreamObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Patient not found")
                            .asRuntimeException());
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void deletePatient
            (GetByIdRequest request, StreamObserver<DeletePatientResponse> responseStreamObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            UUID id = UUID.fromString(request.getPatientId());
            PatientModel existing = service.getPatientById(id);
            if (!existing.email().equals(email)) {
                responseStreamObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Access denied")
                                .asRuntimeException()
                );
                return;
            }
            service.deletePatient(id);
            DeletePatientResponse response = DeletePatientResponse.newBuilder()
                    .setStatus("Success")
                    .setMessage("Patient deleted successfully")
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (PatientNotFoundException e) {
            responseStreamObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Patient not found")
                            .asRuntimeException());
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    //login
    @Override
    public void login
    (LoginRequest request, StreamObserver<LoginResponse> responseStreamObserver) {
        log.info("Login email :{}, password:{}", request.getEmail(), request.getPassword());
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password = BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            log.info("Login attempt for email: {}", email);

            PatientModel patientModel = service.login(email, password);
            LoginResponse response = LoginResponse.newBuilder()
                    .setPatientId(String.valueOf(patientModel.patientId()))
                    .setFirstName(patientModel.firstName())
                    .setLastName(patientModel.lastName())
                    .setAddress(patientModel.address())
                    .setEmail(patientModel.email())
                    .setPhoneNumber(patientModel.phoneNumber())
                    .build();
            responseStreamObserver.onNext(response);
            log.info("Patient login successfully");
            responseStreamObserver.onCompleted();
        } catch (PatientNotFoundException e) {
            responseStreamObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

}
