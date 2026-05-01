package com.provemeright.patient_service.service;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.exception.EmailAlreadyExistsException;
import com.provemeright.patient_service.exception.PatientNotFoundException;
import com.provemeright.patient_service.grpc.BillingServiceGrpcClient;
import com.provemeright.patient_service.kafka.KafkaProducer;
import com.provemeright.patient_service.mapper.PatientMapper;
import com.provemeright.patient_service.model.Patient;
import com.provemeright.patient_service.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Core business logic layer for Patient operations.
 * 
 * Orchestrates interactions between the database (PatientRepository), internal
 * mapping utilities, and external systems (Billing via gRPC, Analytics via Kafka).
 */
@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer){
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Retrieves all patients mapped to DTOs.
     *
     * @return List of all patients as response DTOs
     */
    public List<PatientResponseDto> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(PatientMapper::toDto).toList();
    }

    /**
     * Creates a new patient with cross-system integrations.
     * 
     * Workflow:
     * 1. Validates that the requested email is unique.
     * 2. Persists the patient to the primary DB.
     * 3. Syncs the patient with the Billing Service synchronously over gRPC.
     * 4. Publishes an asynchronous event to Kafka for the Analytics Service.
     *
     * @param patientRequestDto The request data.
     * @return The created patient as a response DTO.
     * @throws EmailAlreadyExistsException if a patient with this email already exists
     */
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto){
        if(patientRepository.existsByEmail(patientRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists: " +
                    patientRequestDto.getEmail());
        }

        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDto)
        );

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
        
        kafkaProducer.sendEvent(newPatient);
        return PatientMapper.toDto(newPatient);
    }

    /**
     * Updates an existing patient.
     * 
     * Ensures that the updated email is not conflicting with *another* user's email.
     *
     * @param id The ID of the patient to update
     * @param patientRequestDto The new data for the patient
     * @return The updated patient mapped as a Response DTO
     * @throws PatientNotFoundException if the ID doesn't exist
     * @throws EmailAlreadyExistsException if the new email belongs to another patient
     */
    public PatientResponseDto updatePatient(UUID id, PatientRequestDto patientRequestDto) {
        
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with id: " + id)
        );

        // Check if the email belongs to a different patient
        if(patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email " + patientRequestDto.getEmail() + " already exists!"
            );
        }

        patient.setName(patientRequestDto.getName());
        patient.setEmail(patientRequestDto.getEmail());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDto(updatedPatient);
    }

    /**
     * Deletes a patient from the database by ID.
     * 
     * Note: In production systems, this is typically implemented as a soft delete 
     * (updating a boolean flag) to comply with data retention policies.
     *
     * @param id The UUID of the patient to delete
     */
    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

}
