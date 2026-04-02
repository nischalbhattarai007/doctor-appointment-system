package com.doctorappointment.doctor_service.Grpc;

import com.doctorappointment.DoctorServiceGrpc;
import com.doctorappointment.RegisterDoctorRequest;
import com.doctorappointment.RegisterDoctorResponse;
import com.doctorappointment.doctor_service.dto.DoctorModel;
import com.doctorappointment.doctor_service.exception.EmailAlreadyExistsException;
import com.doctorappointment.doctor_service.exception.ValidationException;
import com.doctorappointment.doctor_service.helper.DoctorGrpcHelper;
import com.doctorappointment.doctor_service.service.DoctorService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

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
}
