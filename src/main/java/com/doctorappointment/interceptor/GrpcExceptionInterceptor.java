package com.doctorappointment.interceptor;

import com.doctorappointment.appointment.exception.*;
import com.doctorappointment.doctor.exception.EmailAlreadyExistsException;
import com.doctorappointment.doctor.exception.ValidationException;
import com.doctorappointment.patient.exception.PatientNotFoundException;
import io.grpc.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class GrpcExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall
            (ServerCall<ReqT, RespT> call,
             Metadata metadata,
             ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> listener = next.startCall(call, metadata);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    handleException(e, call);
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Exception e) {
                    handleException(e, call);
                }
            }
        };
    }

    private <ReqT, RespT> void handleException(
            Exception e, ServerCall<ReqT, RespT> call) {

        Status status = mapToStatus(e);
        log.error("gRPC call failed: {} - {}", status.getCode(), e.getMessage(), e);
        call.close(status, new Metadata());
    }

    private Status mapToStatus(Exception e) {
        // 404
        if (e instanceof AppointmentNotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }
        // 409 already exists
        if (e instanceof EmailAlreadyExistsException
                || e instanceof DuplicateAppointmentRequestException
                || e instanceof AppointmentAlreadyActiveException) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage());
        }
        // 403 permission
        if (e instanceof UnauthorizedAccessException) {
            return Status.PERMISSION_DENIED.withDescription(e.getMessage());
        }
        // 400 bad input
        if (e instanceof ValidationException
                || e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        // 412 precondition
        if (e instanceof DoctorFullyBookedException) {
            return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
        }
        if (e instanceof PatientNotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }

        if (e instanceof ExpiredJwtException) {
            return Status.UNAUTHENTICATED
                    .withDescription("Refresh token expired please login again");
        }

        if (e instanceof JwtException) {
            return Status.UNAUTHENTICATED
                    .withDescription("Invalid refresh token");
        }
        // 500 everything else
        log.error("Unhandled exception type: {}", e.getClass().getName(), e);
        return Status.INTERNAL
                .withDescription("An unexpected error occurred " + e.getMessage());
    }
}
