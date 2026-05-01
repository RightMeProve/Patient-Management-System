package com.provemeright.patient_service.controller;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.dto.validators.CreatePatientValidationGroup;
import com.provemeright.patient_service.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API Controller for Patient operations.
 * 
 * Serves as the HTTP entry point for the Patient microservice. It handles incoming
 * requests, triggers DTO validation rules, and delegates core business logic to the 
 * PatientService.
 */
@RestController
@RequestMapping("/patients")
@Tag(name = "Patient",description = "API for managing patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    /**
     * Retrieves all patients.
     *
     * @return ResponseEntity containing a list of all patients mapped to DTOs.
     */
    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDto>> getPatients(){
        List<PatientResponseDto> patients = patientService.getPatients();
        return ResponseEntity.ok().body(patients);
    }

    /**
     * Creates a new patient.
     * 
     * Validates input using validation groups to ensure required fields are present
     * before delegating to the service layer for business rule checks (e.g., duplicate email).
     *
     * @param patientRequestDto The validated JSON request body.
     * @return ResponseEntity containing the created patient with its generated ID.
     */
    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientResponseDto> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDto patientRequestDto){
        
        PatientResponseDto patientResponseDto = patientService.createPatient(patientRequestDto);
        return ResponseEntity.ok().body(patientResponseDto);
    }

    /**
     * Updates an existing patient.
     * 
     * Uses PUT semantics, meaning a full replacement of the patient resource fields
     * is expected.
     *
     * @param id The UUID of the patient to update.
     * @param patientRequestDto The validated JSON request body containing updated data.
     * @return ResponseEntity with the updated patient data.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a Patient")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable UUID id,
            @Validated({Default.class}) @RequestBody PatientRequestDto patientRequestDto){
        
        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok().body(patientResponseDto);
    }

    /**
     * Deletes a patient.
     * 
     * Returns 204 No Content upon successful deletion, indicating the action succeeded
     * but there is no resource representation to return.
     *
     * @param id The UUID of the patient to delete.
     * @return ResponseEntity with 204 No Content status.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete the patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
