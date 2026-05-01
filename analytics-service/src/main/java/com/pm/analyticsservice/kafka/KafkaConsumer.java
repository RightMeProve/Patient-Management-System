package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

/**
 * KafkaConsumer acts as the event listener for the Analytics Service.
 * 
 * Implements the Observer Pattern (Publish-Subscribe). By consuming Kafka events, 
 * the analytics service processes domain state changes asynchronously, completely 
 * decoupled from the upstream operational services (e.g., patient-service).
 */
@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    /**
     * Consumes Patient events from the Kafka broker.
     * 
     * The groupId ensures that within a scaled-out cluster, each message is 
     * processed exactly once per group. Uses byte array payloads for efficient 
     * Protobuf deserialization.
     * 
     * @param event The raw binary payload of the Kafka message.
     */
    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event){
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            // TODO: Route event data to downstream analytics datastores or read-models.
            
            log.info("Received patient Event: [PatientID = {}, PatientName = {}, PatientEmail = {}]",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());

        } catch (InvalidProtocolBufferException e) {
            // In a production system, serialization failures should be routed 
            // to a Dead Letter Queue (DLQ) to prevent offset blocking.
            log.error("Error deserializing event {}", e.getMessage());
        }
    }
}
