package com.doctorappointment.appointment.service;

import com.doctorappointment.appointment.constant.AppointmentStatus;
import com.doctorappointment.appointment.dto.AppointmentModel;
import com.doctorappointment.appointment.dto.AppointmentRequest;
//import com.doctorappointment.appointment.exception.AppointmentAlreadyActiveException;
//import com.doctorappointment.appointment.exception.DuplicateAppointmentRequestException;
import com.doctorappointment.appointment.exception.DuplicateAppointmentRequestException;
import com.doctorappointment.appointment.exception.SameDateRescheduleNotAllowedException;
import com.doctorappointment.appointment.exception.UnauthorizedAccessException;
import com.doctorappointment.appointment.repository.AppointmentRepoInterface;
import com.doctorappointment.doctor.dto.DoctorModel;
import com.doctorappointment.doctor.exception.AppointmentNotFoundException;
import com.doctorappointment.doctor.exception.DoctorFullyBookedException;
import com.doctorappointment.doctor.repository.DoctorRepoInterface;
import com.doctorappointment.notification.AppointmentEvent;
import com.doctorappointment.notification.NotificationPublisher;
import com.doctorappointment.notification.NotificationSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Singleton
@Slf4j
public class AppointmentService {
    private final AppointmentRepoInterface appointmentRepo;
    private final DoctorRepoInterface doctorRepo;
    private final NotificationPublisher publisher;

    public AppointmentService
            (AppointmentRepoInterface appointmentRepo, DoctorRepoInterface doctorRepo, NotificationPublisher publisher) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.publisher = publisher;
    }

    //request appointment
    public AppointmentModel requestAppointment(AppointmentRequest request) {
        ValidateNewAppointment.validateNewAppointment(
                request.patientId(),
                request.doctorId(),
                request.appointment_date(),
                AppointmentStatus.PENDING);
        //validate date
        ValidateNewAppointment.validateDate(request.appointment_date());

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
        //add request appointment in appointment uniqueness table
        boolean inserted = appointmentRepo.countByPatientAndDoctor(
                appointment.patientId(),
                appointment.doctorId(),
                appointment.appointment_date(),
                appointment.appointmentId());
        if (!inserted) {
            throw new DuplicateAppointmentRequestException("Duplicate appointment request");
        }
        //save the appointment request in all appointment table
        AppointmentModel saved = appointmentRepo.saveAppointment(appointment);
        publisher.publish(NotificationSubject.DOCTOR_APPOINTMENT_REQUEST,
                AppointmentEvent.builder()
                        .appointmentId(saved.appointmentId().toString())
                        .patientId(saved.patientId().toString())
                        .doctorId(saved.doctorId().toString())
                        .date(saved.appointment_date())
                        .status(AppointmentStatus.PENDING)
                        .recipientType("DOCTOR")
                        .message("New appointment requested by patient for " + saved.appointment_date())
                        .build());
        log.info(" Appoint request by patient id {} with doctor {} on {} ",
                request.patientId(), request.doctorId(), request.appointment_date());
        return saved;
    }

    //confirm appointment
    public AppointmentModel confirmAppointment(UUID appointmentId, UUID doctorId) {
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if (!existingAppointment.doctorId().equals(doctorId)) {
            throw new UnauthorizedAccessException("Only the assigned doctor is allowed");
        }
        if (!existingAppointment.status().equals(AppointmentStatus.PENDING)) {
            throw new UnauthorizedAccessException
                    ("Only pending appointment is allowed. Current status is: " + existingAppointment.status());
        }
        appointmentRepo.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, "", "");
        publisher.publish(NotificationSubject.PATIENT_APPOINTMENT_CONFIRMED,
                AppointmentEvent.builder()
                        .appointmentId(appointmentId.toString())
                        .patientId(existingAppointment.patientId().toString())
                        .doctorId(existingAppointment.doctorId().toString())
                        .date(existingAppointment.appointment_date())
                        .status(AppointmentStatus.CONFIRMED)
                        .recipientType("PATIENT")
                        .message("your appointment on " + existingAppointment.appointment_date() + "has been confirmed")
                        .build());
        return existingAppointment.toBuilder().status(AppointmentStatus.CONFIRMED).build();

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

        //free the uniqueness slot so patient can rebook the same doctor/date
        appointmentRepo.deleteUniqueness(
                existingAppointment.patientId(),
                existingAppointment.doctorId(),
                existingAppointment.appointment_date());

        publisher.publish(NotificationSubject.PATIENT_APPOINTMENT_REJECTED,
                AppointmentEvent.builder()
                        .appointmentId(appointmentId.toString())
                        .patientId(existingAppointment.patientId().toString())
                        .doctorId(existingAppointment.doctorId().toString())
                        .date(existingAppointment.appointment_date())
                        .status(AppointmentStatus.REJECTED)
                        .recipientType("PATIENT")
                        .message("your appointment on " + existingAppointment.appointment_date() + "has been rejected")
                        .build());

        log.info("Appointment {} rejected by doctor {}", appointmentId, doctorId);
        return existingAppointment.toBuilder()
                .status(AppointmentStatus.REJECTED)
                .build();
    }

    //cancel appointment
    public AppointmentModel cancelAppointment(UUID appointmentId, UUID patientId, String reason) {
        //validate only authorized patient is allowed to cancel appointment
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if (!existingAppointment.patientId().equals(patientId)) {
            throw new UnauthorizedAccessException("Only the assigned patient is allowed");
        }
        //only pending and confirmed can be canceled
        if (existingAppointment.status().equals(AppointmentStatus.REJECTED)
                || existingAppointment.status().equals(AppointmentStatus.CANCELLED)) {
            throw new UnauthorizedAccessException("Cannot cancel the appointment with status" + existingAppointment.status());
        }
        appointmentRepo.updateStatus
                (appointmentId, AppointmentStatus.CANCELLED, reason != null ? reason : "", AppointmentStatus.PATIENT);
        /*
        delete appointment request
            from uniqueness table when
                patient cancel the request
          */
        appointmentRepo.deleteUniqueness(
                existingAppointment.patientId(),
                existingAppointment.doctorId(),
                existingAppointment.appointment_date());

        //notification
        publisher.publish(NotificationSubject.DOCTOR_APPOINTMENT_CANCELLED,
                AppointmentEvent.builder()
                        .appointmentId(appointmentId.toString())
                        .doctorId(existingAppointment.doctorId().toString())
                        .patientId(existingAppointment.patientId().toString())
                        .status(AppointmentStatus.CANCELLED)
                        .recipientType("DOCTOR")
                        .message("Appointment cancelled by patient on date" + existingAppointment.appointment_date())
                        .build());

        log.info("Appointment {} cancelled by patient {}", appointmentId, patientId);
        return existingAppointment.toBuilder()
                .status(AppointmentStatus.CANCELLED)
                .reason(reason != null ? reason : "")
                .cancelledBy(AppointmentStatus.PATIENT)
                .build();
    }

    //reschedule -doctor only
    public AppointmentModel rescheduleAppointment(UUID appointmentId, UUID doctorId, String newDate, String reason) {
        ValidateNewAppointment.validateDate(newDate);
        AppointmentModel existingAppointment = getExistingAppointment(appointmentId);
        if (!existingAppointment.doctorId().equals(doctorId)) {
            throw new UnauthorizedAccessException("Only the assigned doctor is allowed");
        }
        if (existingAppointment.status().equals(AppointmentStatus.REJECTED)
                || existingAppointment.status().equals(AppointmentStatus.CANCELLED)) {
            throw new UnauthorizedAccessException("Cannot reschedule appointment with status " + existingAppointment.status());
        }
        LocalDate parsedDate = LocalDate.parse(newDate);
        if (parsedDate.isEqual(LocalDate.parse(existingAppointment.appointment_date()))) {
            throw new SameDateRescheduleNotAllowedException("Can't reschedule appointment to the same data");
        }
        appointmentRepo.updateDateAndStatus(appointmentId,
                newDate,
                reason,
                AppointmentStatus.PENDING);
        //free the old date slot so the patient can re-book on it if they want
        appointmentRepo.deleteUniqueness(
                existingAppointment.patientId(),
                existingAppointment.doctorId(),
                existingAppointment.appointment_date());
        //notification rescheduled
        publisher.publish(NotificationSubject.PATIENT_APPOINTMENT_RESCHEDULED, AppointmentEvent.builder()
                .appointmentId(appointmentId.toString())
                .doctorId(existingAppointment.doctorId().toString())
                .patientId(existingAppointment.patientId().toString())
                .status(AppointmentStatus.RESCHEDULED)
                .recipientType("PATIENT")
                .message("Your appointment has been rescheduled " + newDate)
                .build());
        log.info("Appointment {} rescheduled by doctor {}", appointmentId, doctorId);
        return existingAppointment.toBuilder()
                .appointment_date(newDate)
                .status(AppointmentStatus.PENDING)
                .reason(reason != null ? reason : "")
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
        ValidateNewAppointment.validateDate(appointment_date);
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
