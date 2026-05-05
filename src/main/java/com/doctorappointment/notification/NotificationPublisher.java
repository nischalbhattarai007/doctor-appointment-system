package com.doctorappointment.notification;

import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;

@NatsClient
public interface NotificationPublisher {
    @Subject(NotificationSubject.PATIENT_APPOINTMENT_CONFIRMED)
    void publishPatientAppointmentConfirmed(AppointmentEvent event);

    @Subject(NotificationSubject.DOCTOR_APPOINTMENT_CANCELLED)
    void publishDoctorAppointmentCancelled(AppointmentEvent event);

    @Subject(NotificationSubject.DOCTOR_APPOINTMENT_REQUEST)
    void publishDoctorAppointmentRequest(AppointmentEvent event);

    @Subject(NotificationSubject.PATIENT_APPOINTMENT_RESCHEDULED)
    void publishPatientAppointmentScheduled(AppointmentEvent event);

    @Subject(NotificationSubject.PATIENT_APPOINTMENT_REJECTED)
    void publishPatientAppointmentRejected(AppointmentEvent event);
}
