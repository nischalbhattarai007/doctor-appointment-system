package com.doctorappointment.appointment.repository;

import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;

import java.util.List;
import java.util.UUID;

public interface AppointmentServiceInterface {
    // request appointment
    AppointmentModel requestAppointment(AppointmentRequest request);

    // confirm appointment
    AppointmentModel confirmAppointment(UUID appointmentId, UUID doctorId);

    // reject appointment
    AppointmentModel rejectAppointment(UUID appointmentId, UUID doctorId);

    // cancel appointment
    AppointmentModel cancelAppointment(UUID appointmentId, UUID patientId, String reason);

    // reschedule appointment
    AppointmentModel rescheduleAppointment(UUID appointmentId, UUID doctorId, String newDate, String reason);

    // get appointment by ID
    AppointmentModel getAppointmentById(UUID appointmentId);

    // get doctor appointments
    List<AppointmentModel> getDoctorAppointments(UUID doctorId, String appointment_date);

    // get patient appointments
    List<AppointmentModel> getPatientAppointments(UUID patientId);
}
