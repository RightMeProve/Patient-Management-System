package com.provemeright.patient_service.mapper;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.model.Patient;

import java.time.LocalDate;

/**
 * Utility class for object mapping.
 * 
 * Isolates the logic for converting between internal Entity representations (Patient) 
 * and external API representations (DTOs). Implemented with pure static functions for 
 * thread safety and performance.
 */
public class PatientMapper {

    /**
     * Converts a Patient entity into a PatientResponseDto.
     *
     * @param patient The entity object from the database
     * @return A response DTO suitable for the API response
     */
    public static PatientResponseDto toDto(Patient patient){
        PatientResponseDto patientResponseDto = new PatientResponseDto();

        patientResponseDto.setId(patient.getId().toString());
        patientResponseDto.setAddress(patient.getAddress());
        patientResponseDto.setEmail(patient.getEmail());
        patientResponseDto.setName(patient.getName());
        patientResponseDto.setDateOfBirth(patient.getDateOfBirth().toString());

        return patientResponseDto;
    }

    /**
     * Converts a PatientRequestDto into a Patient entity.
     * 
     * Note: Does not set the ID field, as it is managed by Hibernate upon persistence.
     *
     * @param patientRequestDto The validated request DTO from the controller
     * @return A new Patient entity ready to be saved to the database
     */
    public static Patient toModel(PatientRequestDto patientRequestDto){
        Patient patient = new Patient();

        patient.setName(patientRequestDto.getName());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setEmail(patientRequestDto.getEmail());

        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientRequestDto.getRegisteredDate()));

        return patient;
    }
}
