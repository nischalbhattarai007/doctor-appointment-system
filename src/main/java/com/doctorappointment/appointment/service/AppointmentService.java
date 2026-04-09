package com.doctorappointment.appointment.service;

import com.doctorappointment.appointment.constant.AppointmentStatus;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;
import com.doctorappointment.appointment.exception.UnauthorizedAccessException;
import com.doctorappointment.appointment.repository.AppointmentRepoInterface;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.exception.AppointmentNotFoundException;
import com.doctorappointment.doctor.exception.DoctorFullyBookedException;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Singleton
@Slf4j
public class AppointmentService {
    private final AppointmentRepoInterface appointmentRepo;
    private final DoctorRepoInterface doctorRepo;

    public AppointmentService(AppointmentRepoInterface appointmentRepo, DoctorRepoInterface doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
    }


    //request appointment
    public AppointmentModel requestAppointment(AppointmentRequest request) {
        ValidateNewAppointment.validateNewAppointment(
                request.patientId(),
                request.doctorId(),
                request.appointment_date(),
                AppointmentStatus.PENDING);

        DoctorModel doctor = doctorRepo.getDoctorById(request.doctorId());
        if (doctor == null || doctor.isDeleted()) {
            throw new AppointmentNotFoundException("doctor not found");
        }
        long bookCount = appointmentRepo.countByDoctorAndDate(request.doctorId(), request.appointment_date());
        if (bookCount >= doctor.dailyLimit()) {
            throw new DoctorFullyBookedException
                    ("appointment limit reached for " + request.appointment_date() + " daily limit: " + doctor.dailyLimit());
        }
        AppointmentModel appointment = AppointmentModel.builder()
                .appointmentId(UUID.randomUUID())
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .appointment_date(request.appointment_date())
                .status(AppointmentStatus.PENDING)
                .notes(request.notes() != null ? request.notes() : "")
                .reason("")
                .cancelledBy("")
                .createdAt(Instant.now())
                .build();
        log.info(" Appoint request by patient id {} with doctor {} on {} ",
                request.patientId(), request.doctorId(), request.appointment_date());
        return appointmentRepo.saveAppointment(appointment);
    }

    //confirm appointment
    public AppointmentModel confirmAppointment(UUID appointmentId, UUID doctorId) {
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if (!existingAppointment.doctorId().equals(doctorId)) {
            throw new UnauthorizedAccessException("Only the assigned doctor is allowed");
        }
        if (!existingAppointment.status().equals(AppointmentStatus.PENDING)) {
            throw new UnauthorizedAccessException("Only pending appointment is allowed. Current status is: " + existingAppointment.status());
        }
        appointmentRepo.updateDateAndStatus(appointmentId, AppointmentStatus.CONFIRMED, "", "");
        log.info("Appointment {} confirmed by doctor {}", appointmentId, doctorId);
        return existingAppointment.toBuilder()
                .status(AppointmentStatus.CONFIRMED)
                .build();
    }

    //reject appointment
    public AppointmentModel rejectAppointment(UUID appointmentId, UUID doctorId) {
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if (!existingAppointment.doctorId().equals(doctorId)) {
            throw new UnauthorizedAccessException("Only the assigned doctor is allowed");
        }
        if (!existingAppointment.status().equals(AppointmentStatus.PENDING)) {
            throw new UnauthorizedAccessException("Only pending appointment is allowed. Current status is: " + existingAppointment.status());
        }
        appointmentRepo.updateStatus(appointmentId, AppointmentStatus.REJECTED, "", "");
        log.info("Appointment {} rejected by doctor {}", appointmentId, doctorId);
        return existingAppointment.toBuilder()
                .status(AppointmentStatus.REJECTED)
                .build();
    }

    //cancel appointment
    public AppointmentModel cancelAppointment(UUID appointmentId, UUID patientId, String reason) {
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if(!existingAppointment.patientId().equals(patientId)){
            throw new UnauthorizedAccessException("Only the assigned patient is allowed");
        }
        //only pending and confirmed can be canceled
        if(existingAppointment.status().equals(AppointmentStatus.REJECTED)
        || existingAppointment.status().equals(AppointmentStatus.CANCELLED)){
            throw new UnauthorizedAccessException("Cannot cancel the appointment with status"+   existingAppointment.status());
        }
        appointmentRepo.updateStatus
                (appointmentId, AppointmentStatus.CANCELLED, reason != null ? reason : "", AppointmentStatus.PATIENT);
        log.info("Appointment {} cancelled by patient {}", appointmentId, patientId);
        return existingAppointment.toBuilder()
                .status(AppointmentStatus.CANCELLED)
                .reason(reason != null ? reason : "")
                .cancelledBy(AppointmentStatus.PATIENT)
                .build();
    }

    //reschedule -doctor only
    public AppointmentModel rescheduleAppointment(UUID appointmentId, UUID doctorId,String newDate, String reason) {
        ValidateNewAppointment.validateDate(newDate);
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if(!existingAppointment.doctorId().equals(doctorId)){
            throw new UnauthorizedAccessException("Only the assigned doctor is allowed");
        }
        if (existingAppointment.status().equals(AppointmentStatus.REJECTED)
                || existingAppointment.status().equals(AppointmentStatus.CANCELLED)) {
            throw new UnauthorizedAccessException("Cannot cancel appointment with status " + existingAppointment.status());
        }
        appointmentRepo.updateDateAndStatus(appointmentId,
                newDate,
                AppointmentStatus.PENDING,
                reason!=null ? reason:"");
        log.info("Appointment {} rescheduled by doctor {}", appointmentId, doctorId);
        return existingAppointment.toBuilder()
                .appointment_date(newDate)
                .status(AppointmentStatus.PENDING)
                .reason(reason!=null? reason:"")
                .build();
    }

    //get appointment by ID
    public AppointmentModel getAppointmentById(UUID appointmentId) {
        AppointmentModel appointment = appointmentRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new AppointmentNotFoundException("appointment not found");
        }
        return appointment;
    }

    //get doctor appointment
    public List<AppointmentModel> getDoctorAppointments(UUID doctorId, String appointment_date) {
        if (doctorId == null) {
            throw new AppointmentNotFoundException("doctor id is required");
        }
        log.info("Getting doctor appointments for doctor {}", doctorId);
        return appointmentRepo.getAppointmentByDoctor(doctorId, appointment_date);
    }

    //get patient appointment
    public List<AppointmentModel> getPatientAppointments(UUID patientId) {
        if (patientId == null) {
            throw new AppointmentNotFoundException("patient id is required");
        }
        log.info("Getting patient appointments for patient {}", patientId);
        return appointmentRepo.getAppointmentByPatient(patientId);
    }

    //helper method
    private AppointmentModel getExistingAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            throw new AppointmentNotFoundException("Appointment id is required");
        }
        log.info("Getting appointment {}", appointmentId);
        AppointmentModel existing = appointmentRepo.getAppointmentById(appointmentId);
        log.info("Getting appointment {}", existing);
        if (existing == null) {
            throw new AppointmentNotFoundException("Appointment not found with id " + appointmentId);
        }
        return existing;
    }

}
