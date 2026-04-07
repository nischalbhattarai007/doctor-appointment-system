package com.doctorappointment.appointment.repository;

import com.doctorappointment.appointment.constant.AppointmentStatus;
import com.doctorappointment.appointment.dto.AppointmentModel;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepoInterface {
    AppointmentModel saveAppointment(AppointmentModel appointmentModel);
    AppointmentModel getAppointmentById(UUID appointmentId);
    void updateStatus(UUID appointmentId, String status,String reason,String cancelledBy);
    List<AppointmentModel> getAppointmentByDoctor(UUID doctorId,String date);
    List<AppointmentModel> getAppointmentByPatient(UUID patientId);
    long countByDoctorAndDate(UUID doctorId,String date);
}
