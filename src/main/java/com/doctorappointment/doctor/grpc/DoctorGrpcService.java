package com.doctorappointment.doctor.grpc;

import com.doctorappointment.*;
import com.doctorappointment.auth.BasicAuthInterceptor;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.exception.DoctorEmailNotFoundException;
import com.doctorappointment.doctor.exception.DoctorIdNotFoundException;
import com.doctorappointment.doctor.exception.EmailAlreadyExistsException;
import com.doctorappointment.doctor.exception.ValidationException;
import com.doctorappointment.doctor.helper.DoctorGrpcHelper;
import com.doctorappointment.doctor.service.DoctorService;
import com.doctorappointment.doctor.service.GeocodingService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DoctorGrpcService extends DoctorServiceGrpc.DoctorServiceImplBase {
    private final DoctorService service;
    private final GeocodingService  geocodingService;

    public DoctorGrpcService(DoctorService service, GeocodingService geocodingService) {
        this.service = service;
        this.geocodingService = geocodingService;
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
            DoctorModel model = service.getDoctorByEmail(email);
            responseObserver.onNext(
                    DoctorGrpcHelper.toLoginResponse(model, "SUCCESS", "Doctor login successfully"));
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
                            .asException());
        }
    }

    //get by ID doctor
    @Override
    public void getDoctorById(GetDoctorByIdRequest request, StreamObserver<GetDoctorByIdResponse> responseObserver) {
        try {
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
            log.info("location_name from request: '{}'", request.getLocationName());
            double[] coords = geocodingService.getCoordinates(request.getLocationName());
            double latitude  = coords[0];
            double longitude = coords[1];

            List<DoctorModel> doctors = service.getDoctorsByLocation(
                    latitude,longitude,
                    request.getRadiusKm(),
                    request.getLimit());
            List<Double> distances = doctors.stream()
                    .map(d -> service.calculateDistance(
                            latitude,longitude,
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
            double[] coords=geocodingService.getCoordinates(request.getLocationName());
            double latitude=coords[0];
            double longitude=coords[1];
            List<DoctorModel> doctors = service.getNearestDoctors(
                    latitude,longitude,
                    request.getRadiusKm(),
                    request.getLimit()
            );

            List<Double> distances = doctors.stream()
                    .map(d -> service.calculateDistance(
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
                                            .setClinicAddress(d.clinicAddress())
                                            .setDoctorPhoneNumber(d.phoneNumber())
                                            .setDistanceKm(distances.get(doctors.indexOf(d)))
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
    public void getDoctorAvailability(DoctorAvailabilityRequest request,StreamObserver<DoctorAvailabilityResponse> responseObserver) {
        try {
            UUID uuid = UUID.fromString(request.getDoctorId());
            DoctorModel doctor = service.getDoctorAvailability(uuid);
            //book count will come from appointment service
            int bookCount=0;
            int maxCapacity=10;
            boolean isAvailable=bookCount < maxCapacity;
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
        }catch (DoctorIdNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
        catch (Exception e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

}
