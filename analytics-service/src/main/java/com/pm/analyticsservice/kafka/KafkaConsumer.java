package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

/**
 * KafkaConsumer acts as the event listener for the Analytics Service.
 * It subscribes to the "patient" Kafka topic to consume events published by other microservices
 * (like the patient-service). 
 * 
 * Design Pattern: Observer Pattern (Publish-Subscribe)
 * By using Kafka, we decouple the producer of the event from the consumer. The analytics service
 * can process these events asynchronously without impacting the performance of the main patient service.
 */
@Service
public class KafkaConsumer {

    // Logger for structured logging, crucial for observability in microservices
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    /**
     * Listens to the specified Kafka topic.
     * 
     * @KafkaListener specifies that this method should be invoked whenever a new message 
     * is published to the "patient" topic. 
     * The groupId "analytics-service" ensures that if we scale out and have multiple instances 
     * of this service, each message is only processed by one instance within the group (Consumer Group concept).
     * 
     * @param event The raw byte array payload of the Kafka message. We use byte array because 
     *              Protocol Buffers (protobuf) serializes data into binary format for efficiency.
     */
    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event){
        try {
            // Deserializing the binary payload back into a Protobuf Java object.
            // This is computationally very fast and provides a strongly typed schema.
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            // TODO: perform any business related to analytics here
            // Example: update read-models for dashboards, feed data into a data warehouse, etc.
            
            // Logging the received event fields. 
            // NOTE: In a production environment, avoid logging sensitive PII data like Email!
            log.info("Received patient Event: [PatientID = {}, PatientName = {}, PatientEmail = {}]",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());

        } catch (InvalidProtocolBufferException e) {
            // If the schema of the incoming message doesn't match our Protobuf schema,
            // an InvalidProtocolBufferException is thrown. 
            // In a robust system, we would route these to a Dead Letter Queue (DLQ) for manual inspection.
            log.error("Error deserializing event {}", e.getMessage());
        }
    }
}
