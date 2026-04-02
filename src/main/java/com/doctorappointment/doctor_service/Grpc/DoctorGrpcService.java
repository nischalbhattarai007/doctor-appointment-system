package com.doctorappointment.doctor_service.Grpc;

import com.doctorappointment.*;
import com.doctorappointment.auth.BasicAuthInterceptor;
import com.doctorappointment.doctor_service.dto.DoctorModel;
import com.doctorappointment.doctor_service.exception.DoctorEmailNotFoundException;
import com.doctorappointment.doctor_service.exception.EmailAlreadyExistsException;
import com.doctorappointment.doctor_service.exception.ValidationException;
import com.doctorappointment.doctor_service.helper.DoctorGrpcHelper;
import com.doctorappointment.doctor_service.service.DoctorService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
@Singleton
@Slf4j
public class DoctorGrpcService extends DoctorServiceGrpc.DoctorServiceImplBase {
    private final DoctorService service;
    public DoctorGrpcService(DoctorService service) {
        this.service = service;
    }
    //register doctor
    @Override
    public void registerDoctor(RegisterDoctorRequest request, StreamObserver<RegisterDoctorResponse> responseObserver) {
        try{
            DoctorModel doctor=service.addDoctor(
                    DoctorGrpcHelper.fromRegisterRequest(request));
            responseObserver.onNext(
                    DoctorGrpcHelper.toRegisterResponse(doctor,"SUCCESS","Doctor registered successfully"));
            log.info("Doctor registered successfully");
            responseObserver.onCompleted();
        }catch (EmailAlreadyExistsException e){
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
        catch(ValidationException e){
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
        catch(Exception e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }

    //login
    @Override
    public void doctorLogin(DoctorLoginRequest request, StreamObserver<DoctorLoginResponse>  responseObserver) {
        try{
         String email= BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
         DoctorModel model=service.getDoctorByEmail(email);
         responseObserver.onNext(
                 DoctorGrpcHelper.toLoginResponse(model,"SUCCESS","Doctor login successfully"));
         log.info("Doctor login successfully");
         responseObserver.onCompleted();
        }
        catch (DoctorEmailNotFoundException e){
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription(e.getMessage())
                    .asRuntimeException());
        }
        catch(Exception e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asException());
        }
    }
}
