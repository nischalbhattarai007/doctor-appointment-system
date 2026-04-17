package com.doctorappointment.appointment.grpc;

import com.doctorappointment.*;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;
import com.doctorappointment.appointment.exception.AppointmentAlreadyActiveException;
import com.doctorappointment.appointment.exception.UnauthorizedAccessException;
import com.doctorappointment.appointment.helper.AppointmentGrpcHelper;
import com.doctorappointment.appointment.service.AppointmentService;
import com.doctorappointment.auth.BasicAuthInterceptor;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.exception.AppointmentNotFoundException;
import com.doctorappointment.doctor.exception.DoctorFullyBookedException;
import com.doctorappointment.doctor.service.DoctorService;
import com.doctorappointment.patient.dto.PatientModel;
import com.doctorappointment.patient.service.PatientService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
public class AppointmentGrpcService extends AppointmentServiceGrpc.AppointmentServiceImplBase {
    private final AppointmentService service;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AppointmentGrpcService(AppointmentService service, DoctorService doctorService, PatientService patientService) {
        this.service = service;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @Override
    public void requestAppointment(AppointmentServiceCreateRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
            String email= BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password = BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            if(email==null || password==null){
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Missing authorization header")
                                 .asRuntimeException()
                );
            }
            PatientModel patient=patientService.getPatientByEmail(email);
//            AppointmentRequest appointmentRequest=AppointmentRequest.builder()
//                    .patientId(patient.patientId())
//                    .doctorId(UUID.fromString(request.getDoctorId()))
//                    .appointment_date(request.getDate())
//                    .notes(request.getNotes())
//                    .build();
            AppointmentModel model = service.requestAppointment(AppointmentGrpcHelper.fromAppointmentRequest(request));
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentResponse
                            (model, "SUCCESS", "Appointment requested successfully"));
            log.info("Appointment requested successfully");
            responseObserver.onCompleted();
        } catch (DoctorFullyBookedException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void confirmAppointment
            (AppointmentActionRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            DoctorModel authenticatedDoctor=doctorService.getDoctorByEmail(email);
            //check valid doctor or not
            if(!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())){
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("You only confirm your own appointment")
                                .asRuntimeException()
                );
                return;
            }
            UUID appointmentId = UUID.fromString(request.getAppointmentServiceId());
            UUID doctorId =authenticatedDoctor.doctorId();
            AppointmentModel model = service.confirmAppointment(appointmentId, doctorId);
            responseObserver.onNext
                    (AppointmentGrpcHelper.toAppointmentServiceResponse(model, "SUCCESS","Appointment confirmed successfully")
            );
            log.info("Appointment confirmed successfully");
            responseObserver.onCompleted();
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (AppointmentAlreadyActiveException e) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    //cancel appointment
    @Override
    public void cancelAppointment
    (AppointmentServiceCancelRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
          //  UUID appointmentId = UUID.fromString(request.getAppointmentServiceId());
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            PatientModel authenticatedPatient=patientService.getPatientByEmail(email);
            var id=authenticatedPatient.patientId();
            log.info("id check : {} ", id);
            //check valid patient or not
            if(!authenticatedPatient.patientId().toString().equals(request.getPatientId())){
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Patient with id not matched")
                                .asRuntimeException()
                );
                return;
            }
            UUID patientId = authenticatedPatient.patientId();
            AppointmentRequest cancelRequest = AppointmentGrpcHelper.fromCancelRequest(request);
            AppointmentModel appointment = service.cancelAppointment(
                    cancelRequest.appointmentId(),
                    patientId,
                    request.getReason());
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentServiceResponse
                            (appointment, "SUCCESS", "Appointment cancelled successfully")
            );
            log.info("Appointment cancelled successfully");
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    //reschedule appointment
    @Override
    public void rescheduleAppointment
    (AppointmentRescheduleRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            DoctorModel authenticatedDoctor=doctorService.getDoctorByEmail(email);
            if(!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())){
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Doctor with id not matched")
                                .asRuntimeException()
                );
                return;
            }
            UUID appointmentId = UUID.fromString(request.getAppointmentServiceId());
            UUID doctorId = authenticatedDoctor.doctorId();
            AppointmentModel appointment = service.rescheduleAppointment(
                    appointmentId,
                    doctorId,
                    request.getNewDate(),
                    request.getReason());
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentServiceResponse
                            (appointment, "SUCCESS", "Appointment rescheduled successfully"));
            log.info("Appointment rescheduled successfully");
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void rejectAppointment
            (AppointmentActionRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            DoctorModel authenticatedDoctor=doctorService.getDoctorByEmail(email);
            if(!authenticatedDoctor.doctorId().toString().equals(request.getDoctorId())){
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("Doctor with id not matched")
                                .asRuntimeException()
                );
                return;
            }
            UUID appointmentId = UUID.fromString(request.getAppointmentServiceId());
            UUID doctorId = authenticatedDoctor.doctorId();
            AppointmentModel model = service.rejectAppointment(appointmentId, doctorId);
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentResponse(model, "SUCCESS", "Appointment rejected successfully")
            );
            log.info("Appointment {} rejected successfully", appointmentId);
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    //get appointment by id
    @Override
    public void getAppointmentById
    (GetAppointmentByIdRequest request, StreamObserver<AppointmentServiceResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password=BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            if(email==null || password==null){
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Missing authorization header")
                                .asRuntimeException()
                );
                return;
            }
            UUID appointmentId = UUID.fromString(request.getAppointmentServiceId());
            AppointmentModel appointment = service.getAppointmentById(appointmentId);
            responseObserver.onNext(
                    AppointmentGrpcHelper.toAppointmentServiceResponse
                            (appointment, "SUCCESS", "Appointment found successfully"));
            log.info("Appointment {} found successfully", appointmentId);
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    //get patient appointments
    @Override
    public void getPatientAppointments
    (GetByPatientIdRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password=BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            if(email==null || password==null){
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Missing authorization header")
                                .asRuntimeException()
                );
                return;
            }
            UUID patientId = UUID.fromString(request.getPatientId());
            List<AppointmentModel> appointments = service.getPatientAppointments(patientId);
            responseObserver.onNext
                    (AppointmentGrpcHelper.toAppointmentListResponse
                            (appointments, "SUCCESS", "Appointment list successfully retrieved"));
            log.info("patients Appointment list successfully retrieved");
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void getDoctorAppointments
            (GetByDoctorIdRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        try {
            String email=BasicAuthInterceptor.EMAIL_CONTEXT_KEY.get();
            String password=BasicAuthInterceptor.PASSWORD_CONTEXT_KEY.get();
            if(email==null || password==null){
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Missing authorization header")
                                .asRuntimeException()
                );
                return;
            }
            UUID DoctorId = UUID.fromString(request.getDoctorId());
            List<AppointmentModel> appointments = service.getDoctorAppointments(DoctorId, request.getDate());
            responseObserver.onNext
                    (AppointmentGrpcHelper.toAppointmentListResponse
                            (appointments, "SUCCESS", "Appointment list successfully retrieved"));
            log.info("Doctor appointments  list successfully retrieved");
            responseObserver.onCompleted();
        } catch (AppointmentNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (UnauthorizedAccessException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }


}
