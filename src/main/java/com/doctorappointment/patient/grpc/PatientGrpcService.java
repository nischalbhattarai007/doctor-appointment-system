package com.doctorappointment.patient.grpc;

import com.doctorappointment.*;
import com.doctorappointment.auth.basicauth.BasicAuthInterceptor;
import com.doctorappointment.auth.util.JwtUtil;
import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.exception.EmailAlreadyExistsException;
import com.doctorappointment.patient.exception.PatientNotFoundException;
import com.doctorappointment.patient.helper.PatientGrpcHelper;
import com.doctorappointment.patient.repository.PatientServiceInterface;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.micronaut.grpc.annotation.GrpcService;
import jakarta.inject.Singleton;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase {
    private final PatientServiceInterface service;
    private final JwtUtil jwtUtil;

    public PatientGrpcService(PatientServiceInterface service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerPatient
            (RegisterPatientRequest request, StreamObserver<PatientResponse> responseStreamObserver) {
            var patientReq = PatientGrpcHelper.fromRegisterRequest(request);
            PatientModel patientModel = service.addPatient(patientReq);
            responseStreamObserver.onNext(
                    PatientGrpcHelper.toResponse
                            (patientModel, "SUCCESS", "Patient registered successfully"));
            log.info("Patient registered successfully");
            responseStreamObserver.onCompleted();
    }

    @Override
    public void getPatientById
            (GetByIdRequest request, StreamObserver<GetByIdResponse> responseStreamObserver) {
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
    }

    @Override
    public void getPatientByEmail
            (GetByEmailRequest request, StreamObserver<GetByEmailResponse> responseStreamObserver) {
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
    }

    @Override
    public void updatePatient
            (UpdatePatientRequest request, StreamObserver<UpdatePatientResponse> responseStreamObserver) {
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
    }

    @Override
    public void deletePatient
            (GetByIdRequest request, StreamObserver<DeletePatientResponse> responseStreamObserver) {
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
            service.deletePatient(id);
            DeletePatientResponse response = DeletePatientResponse.newBuilder()
                    .setStatus("Success")
                    .setMessage("Patient deleted successfully")
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
    }

    //login
    @Override
    public void login
    (LoginRequest request, StreamObserver<LoginResponse> responseStreamObserver) {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String role = BasicAuthInterceptor.ROLE_CONTEXT_KEY.get();
            PatientModel patientModel = service.getPatientByEmail(email);
            if (patientModel == null || patientModel.isDeleted()) {
                responseStreamObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Patient not found")
                                .asRuntimeException()
                );
                return;
            }
            // role check only patient account can use this
            if (!"PATIENT".equals(role)) {
                responseStreamObserver.onError(
                        Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException());
                return;
            }
            String token = jwtUtil.generateToken(email, role);
            String refreshToken = jwtUtil.refreshToken(email, role);
            LoginResponse response = LoginResponse.newBuilder()
                    .setPatientId(String.valueOf(patientModel.patientId()))
                    .setFirstName(patientModel.firstName())
                    .setLastName(patientModel.lastName())
                    .setAddress(patientModel.address())
                    .setEmail(patientModel.email())
                    .setPhoneNumber(patientModel.phoneNumber())
                    .setToken(token)
                    .setRefreshToken(refreshToken)
                    .build();
            responseStreamObserver.onNext(response);
            log.info("Patient login successfully");
            responseStreamObserver.onCompleted();
    }

    @Override
    public void getAllPatient(Empty request, StreamObserver<PatientListResponse> responseStreamObserver) {
            List<PatientModel> patients = service.getAllPatients();
            for (PatientModel patient : patients) {
                PatientListResponse response = PatientListResponse.newBuilder()
                        .setPatientId(patient.patientId().toString())
                        .setFirstName(patient.firstName())
                        .setLastName(patient.lastName())
                        .setEmail(patient.email())
                        .setPhoneNumber(patient.phoneNumber())
                        .setAddress(patient.address())
                        .build();
                responseStreamObserver.onNext(response);
            }
            responseStreamObserver.onCompleted();
    }

    @Override
    public void refreshTokens(RefreshTokenRequests requests, StreamObserver<RefreshTokenResponses> responseStreamObserver) {
        String incomingRefreshToken = requests.getRefreshToken();
        Claims claims = jwtUtil.validateToken(incomingRefreshToken);
        if (!jwtUtil.isRefreshToken(claims)) {
            responseStreamObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Invalid token type refresh token required")
                            .asRuntimeException());
            return;
        }
        String email = jwtUtil.getEmail(claims);
        String role = jwtUtil.getRole(claims);
        String newAccessToken = jwtUtil.generateToken(email, role);
        log.info("Access token refreshed successfully for :{}", email);
        responseStreamObserver.onNext(
                RefreshTokenResponses.newBuilder()
                        .setToken(newAccessToken)
                        .setRefreshToken(incomingRefreshToken)
                        .build());
        responseStreamObserver.onCompleted();
    }
}
