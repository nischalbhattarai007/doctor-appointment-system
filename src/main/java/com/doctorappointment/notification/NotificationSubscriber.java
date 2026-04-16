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
    public void subscribe(){
        Dispatcher dispatcher =natsConnection.createDispatcher(msg->{});
        dispatcher.subscribe(NotificationSubject.DOCTOR_APPOINTMENT_REQUEST,msg->handle(msg.getData()));
    }
    private void handle(byte[] data){
        try{
            AppointmentEvent event=objectMapper.readValue(data, AppointmentEvent.class);

            log.info("Received appointment event for {}",event.doctorId());
        }catch (Exception e){
            log.error("Error while processing appointment event",e);
        }
    }
}
