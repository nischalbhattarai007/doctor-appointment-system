package com.doctorappointment.notification;

import io.micronaut.context.annotation.Context;
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NatsListener
@Context
public class NotificationSubscriber {
    @Subject(NotificationSubject.PATIENT_APPOINTMENT_CONFIRMED)
    public void onPatientConfirmed(AppointmentEvent event) {
        log.info("NOTIFY PATIENT {} — confirmed with doctor {} on {}",
                event.patientId(), event.doctorId(), event.date());
    }

    @Subject(NotificationSubject.PATIENT_APPOINTMENT_REJECTED)
    public void onPatientRejected(AppointmentEvent event) {
        log.info("NOTIFY PATIENT {} — rejected. Reason: {}",
                event.patientId(), event.reason());
    }

    @Subject(NotificationSubject.PATIENT_APPOINTMENT_RESCHEDULED)
    public void onPatientRescheduled(AppointmentEvent event) {
        log.info("NOTIFY PATIENT {} — rescheduled to {}",
                event.patientId(), event.date());
    }

    @Subject(NotificationSubject.DOCTOR_APPOINTMENT_REQUEST)
    public void onDoctorRequest(AppointmentEvent event) {
        log.info("NOTIFY DOCTOR {} — new request from patient {} on {}",
                event.doctorId(), event.patientId(), event.date());
    }

    @Subject(NotificationSubject.DOCTOR_APPOINTMENT_CANCELLED)
    public void onDoctorCancelled(AppointmentEvent event) {
        log.info("NOTIFY DOCTOR {} — cancelled by patient {}",
                event.doctorId(), event.patientId());
    }
}
