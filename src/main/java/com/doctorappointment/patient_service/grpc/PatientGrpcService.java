package com.doctorappointment.patient_service.grpc;

import com.doctorappointment.PatientResponse;
import com.doctorappointment.PatientServiceGrpc;
import com.doctorappointment.RegisterPatientRequest;
import com.doctorappointment.patient_service.dto.PatientModel;
import com.doctorappointment.patient_service.helper.PatientGrpcHelper;
import com.doctorappointment.patient_service.service.PatientService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import jakarta.validation.ValidationException;

@Singleton
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase {
    private final PatientService service;

    public PatientGrpcService(PatientService service) {
        this.service = service;
    }
    @Override
    public void registerPatient
            (RegisterPatientRequest request, StreamObserver<PatientResponse>responseStreamObserver) {
        try{
            var patientReq = PatientGrpcHelper.fromRegisterRequest(request);
        PatientModel patientModel = service.addPatient(patientReq);
        responseStreamObserver.onNext(
                PatientGrpcHelper.toResponse
                        (patientModel,"SUCCESS","Patient registered successfully"));
        responseStreamObserver.onCompleted();
    }catch(ValidationException e){
            responseStreamObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }catch (Exception e){
            responseStreamObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

}
