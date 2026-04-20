package com.doctorappointment.appointment.helper;

import com.doctorappointment.*;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;
import java.util.List;
import java.util.UUID;

public class AppointmentGrpcHelper {
    private AppointmentGrpcHelper() {
    }

    //appointment request
    public static AppointmentRequest fromAppointmentRequest(AppointmentServiceCreateRequest request) {
        return AppointmentRequest.builder()
                .patientId(UUID.fromString(request.getPatientId()))
                .doctorId(UUID.fromString(request.getDoctorId()))
                .appointment_date(request.getDate())
                .notes(request.getNotes())
                .build();
    }

    //appointment response
    public static AppointmentServiceResponse toAppointmentResponse(AppointmentModel appointment, String status, String message) {
        return AppointmentServiceResponse.newBuilder()
                .setAppointmentServiceId(appointment.appointmentId().toString())
                .setPatientId(appointment.patientId().toString())
                .setDoctorId(appointment.doctorId().toString())
                .setDate(appointment.appointment_date())
                .setNotes(appointment.notes())
                .setReason(appointment.reason())
                .setCancelledBy(appointment.cancelledBy())
                .setStatus(status)
                .setMessage(message)
                .build();
    }

    //confirm appointment response
    public static AppointmentServiceResponse  toAppointmentServiceResponse
            (AppointmentModel appointment, String status, String message) {
        return AppointmentServiceResponse.newBuilder()
                .setAppointmentServiceId(appointment.appointmentId().toString())
                .setPatientId(appointment.patientId().toString())
                .setDoctorId(appointment.doctorId().toString())
                .setDate(appointment.appointment_date())
                .setStatus(status)
                .setNotes(appointment.notes())
                .setReason(appointment.reason())
                .setCancelledBy(appointment.cancelledBy())
                .setStatusCode(appointment.status())
                .setMessage(message)
                .build();
    }
    public static AppointmentRequest fromCancelRequest(
            AppointmentServiceCancelRequest request) {
        return AppointmentRequest.builder()
                .appointmentId(UUID.fromString(request.getAppointmentServiceId()))
                .patientId(UUID.fromString(request.getPatientId()))
                .reason(request.getReason())
                .build();
    }

    //list response of doctor and patient
    public static AppointmentListResponse toAppointmentListResponse
    (List<AppointmentModel> appointments, String status, String message) {
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder()
                .setStatus(status)
                .setMessage(message);
        for (AppointmentModel appointment : appointments) {
            builder.addAppointments(summary(appointment));
        }
        return builder.build();
    }

    public static AppointmentSummary summary(AppointmentModel appointment) {
        return AppointmentSummary.newBuilder()
                .setAppointmentServiceId(appointment.appointmentId().toString())
                .setPatientId(appointment.patientId() != null ?
                        appointment.patientId().toString() : "")
                .setDoctorId(appointment.doctorId() != null ?
                        appointment.doctorId().toString() : "")
                .setDate(appointment.appointment_date())
                .setStatus(appointment.status())
                .setNotes(appointment.notes() != null ? appointment.notes() : "")
                .build();
    }
}
