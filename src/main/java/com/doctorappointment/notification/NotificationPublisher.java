package com.doctorappointment.notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class NotificationPublisher {
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    public NotificationPublisher(Connection natsConnection, ObjectMapper objectMapper) {
        this.natsConnection = natsConnection;
        this.objectMapper = objectMapper;
    }
    public void publish(String subject,AppointmentEvent event){
        try{
            byte[] data=objectMapper.writeValueAsBytes(event);
            natsConnection.publish(subject,data);
            log.info("Published appointment event {}",event);
        }catch (Exception e){
            log.error("Failed to publish notification to '{}' :'{}' ",subject,e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
