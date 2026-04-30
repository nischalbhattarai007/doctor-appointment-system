package com.doctorappointment.doctor.grpc;

import com.doctorappointment.*;
import com.doctorappointment.appointment.repository.AppointmentRepoInterface;
import com.doctorappointment.auth.BasicAuthInterceptor;
import com.doctorappointment.auth.util.JwtUtil;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.dto.DoctorScheduleModel;
import com.doctorappointment.doctor.enums.Day;
import com.doctorappointment.doctor.exception.*;
import com.doctorappointment.doctor.helper.DoctorGrpcHelper;
import com.doctorappointment.doctor.repository.DoctorServiceInterface;
import com.doctorappointment.doctor.service.GeocodingService;
import com.doctorappointment.doctor.service.ScheduleService;
import com.doctorappointment.doctor.util.GeoCalculateDistance;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
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
        try {
            DoctorModel doctor = service.addDoctor(
                    DoctorGrpcHelper.fromRegisterRequest(request));
            responseObserver.onNext(
                    DoctorGrpcHelper.toRegisterResponse(doctor, "SUCCESS", "Doctor registered successfully"));
            log.info("Doctor registered successfully");
            responseObserver.onCompleted();
        } catch (EmailAlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ValidationException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    //login
    @Override
    public void doctorLogin(DoctorLoginRequest request, StreamObserver<DoctorLoginResponse> responseObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password = BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            DoctorModel model=service.login(email, password);
            //DoctorModel model = service.getDoctorByEmail(email);
            String token=jwtUtil.generateToken(email,"DOCTOR");
            responseObserver.onNext(
                    DoctorGrpcHelper.toLoginResponse(model, "SUCCESS", "Doctor login successfully",token));
            log.info("Doctor login successfully");
            responseObserver.onCompleted();
        } catch (DoctorEmailNotFoundException e) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    //get by ID doctor
    @Override
    public void getDoctorById(GetDoctorByIdRequest request, StreamObserver<GetDoctorByIdResponse> responseObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            if (email == null ) {
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

    //get by email doctor
    @Override
    public void getDoctorByEmail(GetByDoctorEmailRequest request, StreamObserver<GetByDoctorEmailResponse> responseObserver) {
        try {
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
        } catch (DoctorEmailNotFoundException e) {
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
            if (!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())) {
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Doctor ID doesn't match")
                                .asRuntimeException()
                );
                return;
            }
            DoctorModel doctor = service.updateDoctor(
                    DoctorGrpcHelper.fromUpdateRequest(request));
            responseObserver.onNext(
                    DoctorGrpcHelper.toUpdateResponse(doctor, "SUCCESS", "Doctor updated successfully"));
            log.info("Doctor with ID {} updated successfully", request.getDoctorId());
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
        try {
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
                        Status.UNAUTHENTICATED
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

    @Override
    public void getDoctorsByLocation(LocationRequest request, StreamObserver<DoctorListResponse> responseObserver) {
        try {
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
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    @Override
    public void getNearestDoctor(NearestLocationRequest request, StreamObserver<NearestDoctorListResponse> responseObserver) {
        try {
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
            NearestDoctorListResponse response = NearestDoctorListResponse.newBuilder()
                    .addAllDoctors(doctors.stream().map(d ->
                                    DoctorSummary.newBuilder()
                                            .setDoctorId(d.doctorId().toString())
                                            .setDoctorFirstName(d.firstName())
                                            .setDoctorLastName(d.lastName())
                                            .setSpecialization(d.specialization())
                                            .setStreet(d.street())
                                            .setArea(d.area())
                                            .setCity(d.city())
                                            .setDoctorPhoneNumber(d.phoneNumber())
                                            .setDistanceKm(distances.get(doctors.indexOf(d)))
                                            /*
                                                makes O(N2) time complexity as everytime
                                                it scans the whole table to check
                                             */
                                            .build())
                            .collect(Collectors.toList()))
                    .setDoctorStatus("SUCCESS")
                    .setDoctorMessage("Nearest doctors retrieved successfully")
                    .build();

            responseObserver.onNext(response);
            log.info("Nearest doctors retrieved successfully");
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    @Override
    public void getDoctorAvailability(DoctorAvailabilityRequest request, StreamObserver<DoctorAvailabilityResponse> responseObserver) {
        try {
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
        } catch (DoctorIdNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    @Override
    public void setDoctorSchedule(SetScheduleRequest request, StreamObserver<SetScheduleResponse> responseObserver) {
        try {
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
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid day name. Use day name")
                            .asRuntimeException());
        } catch (DoctorEmailNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (ValidationException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    @Override
    public void getDoctorSchedule(GetScheduleRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
        try {
            String email = BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            if(email == null){
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Missing authentication header")
                                .asRuntimeException());
                return;
            }
            UUID doctorId=UUID.fromString(request.getDoctorId());
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
        }catch (ScheduleNotFoundException e){
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }catch (Exception e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    @Override
    public void getAllDoctorList(EmptyList request, StreamObserver<DoctorList>responseObserver) {
        try{
            List<DoctorModel> doctors=service.getAllDoctors();
            List<DoctorResponse> doctorLists=doctors.stream()
                    .map(doctor->DoctorResponse.newBuilder()
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
            DoctorList response=DoctorList.newBuilder()
                    .addAllDoctors(doctorLists)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

}
