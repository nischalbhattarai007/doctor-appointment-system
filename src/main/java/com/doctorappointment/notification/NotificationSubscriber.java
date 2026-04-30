package com.doctorappointment.notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Context;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Singleton
@Context
public class NotificationSubscriber {
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    public NotificationSubscriber(Connection natsConnection, ObjectMapper objectMapper) {
        this.natsConnection = natsConnection;
        this.objectMapper = objectMapper;
    }
    @PostConstruct
    public void subscribe() {
        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {});
        //patient notification
        dispatcher.subscribe(NotificationSubject.PATIENT_APPOINTMENT_CONFIRMED, msg -> handle(msg.getData()));
        dispatcher.subscribe(NotificationSubject.PATIENT_APPOINTMENT_REJECTED, msg -> handle(msg.getData()));
        dispatcher.subscribe(NotificationSubject.PATIENT_APPOINTMENT_RESCHEDULED, msg -> handle(msg.getData()));
        //doctor notification
        dispatcher.subscribe(NotificationSubject.DOCTOR_APPOINTMENT_REQUEST, msg -> handle(msg.getData()));
        dispatcher.subscribe(NotificationSubject.DOCTOR_APPOINTMENT_CANCELLED, msg -> handle(msg.getData()));
    }

    private void handle(byte[] data) {
        try {
            AppointmentEvent event = objectMapper.readValue(data, AppointmentEvent.class);
            switch (event.status()) {
                case "REQUESTED" -> log.info("NOTIFY DOCTOR {} — new appointment request from patient {} on {}",
                        event.doctorId(), event.patientId(), event.date());
                case "CONFIRMED" -> log.info("NOTIFY PATIENT {} — appointment confirmed with doctor {} on {}",
                        event.patientId(), event.doctorId(), event.date());
                case "REJECTED" -> log.info("NOTIFY PATIENT {} — appointment rejected. Reason: {}",
                        event.patientId(), event.reason());
                case "RESCHEDULED" -> log.info("NOTIFY PATIENT {} — appointment rescheduled to {}",
                        event.patientId(), event.date());
                case "CANCELLED" -> log.info("NOTIFY DOCTOR {} — appointment cancelled by patient {}",
                        event.doctorId(), event.patientId());
                default -> log.warn("Unknown notification status {}", event.status());
            }
            log.info("Received appointment event  {}", event);
        }catch (Exception e){
            log.error("Error while processing appointment event",e);
        }
    }
}
