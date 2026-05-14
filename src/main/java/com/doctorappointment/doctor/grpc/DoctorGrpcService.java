package com.doctorappointment.doctor.grpc;

import com.doctorappointment.*;
import com.doctorappointment.appointment.repository.AppointmentRepoInterface;
import com.doctorappointment.auth.basicauth.BasicAuthInterceptor;
import com.doctorappointment.auth.util.JwtUtil;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorScheduleModel;
import com.doctorappointment.doctor.enums.Day;
import com.doctorappointment.doctor.exception.DoctorIdNotFoundException;
import com.doctorappointment.doctor.helper.DoctorGrpcHelper;
import com.doctorappointment.doctor.repository.DoctorServiceInterface;
import com.doctorappointment.doctor.service.GeocodingService;
import com.doctorappointment.doctor.service.ScheduleService;
import com.doctorappointment.doctor.util.GeoCalculateDistance;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DoctorGrpcService extends DoctorServiceGrpc.DoctorServiceImplBase {
    private final DoctorServiceInterface service;
    private final GeocodingService geocodingService;
    private final AppointmentRepoInterface appointmentRepo;
    private final ScheduleService scheduleService;
    private final JwtUtil jwtUtil;

    public DoctorGrpcService(DoctorServiceInterface service, GeocodingService geocodingService, AppointmentRepoInterface appointmentRepo, ScheduleService scheduleService, JwtUtil jwtUtil) {
        this.service = service;
        this.geocodingService = geocodingService;
        this.appointmentRepo = appointmentRepo;
        this.scheduleService = scheduleService;
        this.jwtUtil = jwtUtil;
    }

    //register doctor
    @Override
    public void registerDoctor(RegisterDoctorRequest request, StreamObserver<RegisterDoctorResponse> responseObserver) {
        DoctorModel doctor = service.addDoctor(
                DoctorGrpcHelper.fromRegisterRequest(request));
        responseObserver.onNext(
                DoctorGrpcHelper.toRegisterResponse(doctor, "SUCCESS", "Doctor registered successfully"));
        log.info("Doctor registered successfully");
        responseObserver.onCompleted();
    }

    //login
    @Override
    public void doctorLogin(DoctorLoginRequest request, StreamObserver<DoctorLoginResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        String role = BasicAuthInterceptor.ROLE_CONTEXT_KEY.get();
        DoctorModel model = service.getDoctorByEmail(email);
        if (model == null || model.isDeleted()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Doctor not found")
                            .asRuntimeException()
            );
            return;
        }
        if (!"DOCTOR".equals(role)) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException());
            return;
        }
        String token = jwtUtil.generateToken(email, "DOCTOR");
        String refreshToken = jwtUtil.refreshToken(email, "DOCTOR");
        responseObserver.onNext(
                DoctorGrpcHelper.toLoginResponse(model, "SUCCESS", "Doctor login successfully", token, refreshToken));
        log.info("Doctor login successfully");
        responseObserver.onCompleted();
    }

    //get by ID doctor
    @Override
    public void getDoctorById(GetDoctorByIdRequest request, StreamObserver<GetDoctorByIdResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Missing authorization header")
                            .asRuntimeException()
            );
            return;
        }
        UUID uuid = UUID.fromString(request.getDoctorId());
        DoctorModel model = service.getDoctorById(uuid);
        responseObserver.onNext(
                DoctorGrpcHelper.toGetByIdResponse(model, "SUCCESS", "Doctor found successfully"));
        log.info("Doctor found with ID {} successfully", uuid);
        responseObserver.onCompleted();
    }

    //get by email doctor
    @Override
    public void getDoctorByEmail(GetByDoctorEmailRequest request, StreamObserver<GetByDoctorEmailResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Missing authorization header")
                            .asRuntimeException()
            );
            return;
        }
        DoctorModel model = service.getDoctorByEmail(request.getDoctorEmail());
        responseObserver.onNext(
                DoctorGrpcHelper.getDoctorByEmailResponse(model, "SUCCESS", "Doctor found successfully"));
        log.info("Doctor found with email {} successfully", request.getDoctorEmail());
        responseObserver.onCompleted();
    }

    //update doctor by ID
    @Override
    public void updateDoctorById(UpdateDoctorRequest request, StreamObserver<UpdateDoctorResponse> responseObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            if (email == null) {
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription(" Missing authentication context")
                                .asRuntimeException());
                return;
            }
            DoctorModel authenticatedDoctor = service.getDoctorByEmail(email);
            if (authenticatedDoctor == null) {
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Authentication doctor not found")
                                .asRuntimeException()
                );
                return;
            }
            DoctorModel doctor = service.updateDoctor(
                    DoctorGrpcHelper.fromUpdateRequest(request)
                            .toBuilder()
                            .doctorId(authenticatedDoctor.doctorId())
                            .build());
            responseObserver.onNext(
                    DoctorGrpcHelper.toUpdateResponse(doctor, "SUCCESS", "Doctor updated successfully"));
            log.info("Doctor with ID {} updated successfully", authenticatedDoctor.doctorId());
            responseObserver.onCompleted();
        } catch (DoctorIdNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    //delete doctor by ID
    @Override
    public void deleteDoctorById(GetDoctorByIdRequest request, StreamObserver<DeleteDoctorResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(" Missing authentication context")
                            .asRuntimeException()
            );
            return;
        }
        DoctorModel authenticatedDoctor = service.getDoctorByEmail(email);
        if (!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(" Unauthorized person access! ")
                            .asRuntimeException()
            );
            return;
        }

        UUID uuid = UUID.fromString(request.getDoctorId());
        service.deleteDoctorById(uuid);
        responseObserver.onNext(
                DoctorGrpcHelper.toDeleteResponse
                        (request.getDoctorId(), "Success", "Doctor deleted successfully"));
        log.info("Doctor with ID {} deleted successfully", uuid);
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorsByLocation(LocationRequest request, StreamObserver<DoctorListResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(" Missing authentication context")
                            .asRuntimeException()
            );
            return;
        }
        double[] coords = geocodingService.getCoordinates(request.getLocationName());
        double latitude = coords[0];
        double longitude = coords[1];

        List<DoctorModel> doctors = service.getDoctorsByLocation(
                latitude, longitude,
                request.getRadiusKm(),
                request.getLimit());
        List<Double> distances = doctors.stream()
                .map(d -> GeoCalculateDistance.calculateDistance(
                        latitude, longitude,
                        d.latitude(), d.longitude()))
                .toList();
        responseObserver.onNext(
                DoctorGrpcHelper.toDoctorListResponse
                        (doctors, distances, "Success", "Doctors found successfully"));
        log.info("Doctors retrieved by location successfully");
        responseObserver.onCompleted();
    }

    @Override
    public void getNearestDoctor(NearestLocationRequest request, StreamObserver<NearestDoctorListResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(" Missing authentication context")
                            .asRuntimeException()
            );
            return;
        }
        double[] coords = geocodingService.getCoordinates(request.getLocationName());
        double latitude = coords[0];
        double longitude = coords[1];
        List<DoctorModel> doctors = service.getNearestDoctors(
                latitude, longitude,
                request.getRadiusKm(),
                request.getLimit()
        );

        List<Double> distances = doctors.stream()
                .map(d -> GeoCalculateDistance.calculateDistance(
                        latitude, longitude,
                        d.latitude(), d.longitude()))
                .toList();
        List<DoctorSummary> doctorSummaries=new ArrayList<>();
        for(int i=0;i<distances.size();i++){
            var d=doctors.get(i);
        DoctorSummary summary=DoctorSummary.newBuilder()
                .setDoctorId(d.doctorId().toString())
                .setDoctorFirstName(d.firstName())
                .setDoctorLastName(d.lastName())
                .setSpecialization(d.specialization())
                .setStreet(d.street())
                .setArea(d.area())
                .setCity(d.city())
                .setDoctorPhoneNumber(d.phoneNumber())
                .setDistanceKm(distances.get(i))
                .build();
        doctorSummaries.add(summary);
        }
        NearestDoctorListResponse response = NearestDoctorListResponse.newBuilder()
                .addAllDoctors(doctorSummaries)
                .setDoctorStatus("Success")
                .setDoctorMessage("Nearest doctors retrieved successfully")
                .build();

        responseObserver.onNext(response);
        log.info("Nearest doctors retrieved successfully");
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorAvailability(DoctorAvailabilityRequest request, StreamObserver<DoctorAvailabilityResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(" Missing authentication context")
                            .asRuntimeException()
            );
            return;
        }
        UUID uuid = UUID.fromString(request.getDoctorId());
        DoctorModel doctor = service.getDoctorAvailability(uuid);
        //book count will come from appointment service
        int bookCount = (int) appointmentRepo.countByDoctorAndDate(uuid, request.getDate());
        int maxCapacity = doctor.dailyLimit();
        boolean isAvailable = bookCount < maxCapacity;
        responseObserver.onNext(
                DoctorGrpcHelper.toAvailabilityResponse(
                        doctor.doctorId().toString(),
                        request.getDate(),
                        isAvailable,
                        bookCount,
                        maxCapacity,
                        "Success",
                        "Doctor availability retrieved successfully"));
        log.info("Doctor availability retrieved successfully");
        responseObserver.onCompleted();
    }

    @Override
    public void setDoctorSchedule(SetScheduleRequest request, StreamObserver<SetScheduleResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        DoctorModel authenticatedDoctor = service.getDoctorByEmail(email);
        if (!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(" You only set your own schedule")
                            .asRuntimeException()
            );
            return;
        }
        UUID doctorId = authenticatedDoctor.doctorId();
        Set<Day> days = request.getWorkingDaysList().stream()
                .map(String::toUpperCase)
                .map(Day::valueOf)
                .collect(Collectors.toSet());
        DoctorScheduleModel schedule = scheduleService.setSchedule(doctorId, days);
        List<String> daysAsString = schedule.working_days().stream()
                .map(Enum::name)
                .toList();
        responseObserver.onNext(SetScheduleResponse.newBuilder()
                .setDoctorId(schedule.doctor_id().toString())
                .addAllWorkingDays(daysAsString)
                .setStatus("Success")
                .setMessage("Schedule saved successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorSchedule(GetScheduleRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
        String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
        if (email == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Missing authentication header")
                            .asRuntimeException());
            return;
        }
        UUID doctorId = UUID.fromString(request.getDoctorId());
        DoctorScheduleModel schedule = scheduleService.getSchedule(doctorId);
        List<String> daysAsString = schedule.working_days().stream()
                .map(Enum::name)
                .toList();
        responseObserver.onNext(GetScheduleResponse.newBuilder()
                .setDoctorId(schedule.doctor_id().toString())
                .addAllWorkingDays(daysAsString)
                .setStatus("Success")
                .setMessage("Schedule Retrieved Successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllDoctorList(EmptyList request, StreamObserver<DoctorList> responseObserver) {
        List<DoctorModel> doctors = service.getAllDoctors();
        List<DoctorResponse> doctorLists = doctors.stream()
                .map(doctor -> DoctorResponse.newBuilder()
                        .setDoctorId(doctor.doctorId().toString())
                        .setDoctorFirstName(doctor.firstName())
                        .setDoctorLastName(doctor.lastName())
                        .setDoctorEmail(doctor.email())
                        .setSpecialization(doctor.specialization())
                        .setStreet(doctor.street())
                        .setArea(doctor.area())
                        .setCity(doctor.city())
                        .setDoctorPhoneNumber(doctor.phoneNumber())
                        .setClinicBuilding(doctor.clinicBuilding())
                        .setClinicName(doctor.clinicName())
                        .build())
                .toList();
        DoctorList response = DoctorList.newBuilder()
                .addAllDoctors(doctorLists)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<RefreshTokenResponse> responseObserver) {
        String incomingRefreshToken = request.getRefreshToken();
        Claims claims = jwtUtil.validateToken(incomingRefreshToken);
        if (!jwtUtil.isRefreshToken(claims)) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Invalid token type refresh token required")
                            .asRuntimeException());
            return;
        }
        String email = jwtUtil.getEmail(claims);
        String role = jwtUtil.getRole(claims);
        String newAccessToken = jwtUtil.generateToken(email, role);
        log.info("Access token refreshed successfully for :{}", email);
        responseObserver.onNext(
                RefreshTokenResponse.newBuilder()
                        .setToken(newAccessToken)
                        .setRefreshToken(incomingRefreshToken)
                        .build());
        responseObserver.onCompleted();
    }
}