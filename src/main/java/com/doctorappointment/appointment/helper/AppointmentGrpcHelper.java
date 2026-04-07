package com.doctorappointment.appointment.helper;

import com.doctorappointment.AppointmentServiceCreateRequest;
import com.doctorappointment.AppointmentServiceResponse;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;

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
                .setStatus(appointment.status())
                .setNotes(appointment.notes())
                .setReason(appointment.reason())
                .setCancelledBy(appointment.cancelledBy())
                .setStatus(status)
                .setMessage(message)
                .build();
    }
}
