package com.provemeright.patient_service.kafka;

import com.provemeright.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

/**
 * Apache Kafka Producer.
 * 
 * Facilitates asynchronous event publishing. Used to decouple the Patient Service 
 * from downstream consumers (e.g., Analytics Service) by broadcasting domain events.
 */
@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    
    private final KafkaTemplate<String,byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String,byte[]> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a "Patient Created" event to the Kafka broker.
     * 
     * Uses Protobuf serialization (byte[]) for efficient binary transfer.
     *
     * @param patient The newly created patient entity.
     */
    public void sendEvent(Patient patient){
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();

        try{
            kafkaTemplate.send("patient",event.toByteArray());
            log.info("Successfully sent PatientCreated event to Kafka");
        }catch (Exception e){
            log.error("Error sending PatientCreated event: {}",event);
        }
    }

}
