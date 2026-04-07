package com.doctorappointment.appointment.grpc;
import com.doctorappointment.AppointmentServiceCreateRequest;
import com.doctorappointment.AppointmentServiceGrpc;
import com.doctorappointment.AppointmentServiceResponse;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.helper.AppointmentGrpcHelper;
import com.doctorappointment.appointment.service.AppointmentService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
@Singleton
public class AppointmentGrpcService extends AppointmentServiceGrpc.AppointmentServiceImplBase{
    private final AppointmentService service;
    public AppointmentGrpcService(AppointmentService service) {
        this.service = service;
    }
    public void requestAppointment(AppointmentServiceCreateRequest request, StreamObserver<AppointmentServiceResponse>responseObserver){
        try{
            AppointmentModel model=service.requestAppointment(AppointmentGrpcHelper.fromAppointmentRequest(request));
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentResponse
                            (model, "SUCCESS", "Appointment requested successfully"));
            responseObserver.onCompleted();
        }catch(Exception e){
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }
}
