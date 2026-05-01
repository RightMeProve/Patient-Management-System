package com.provemeright.patient_service.repository;

import com.provemeright.patient_service.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Data Access Layer for the Patient entity.
 * 
 * Extends JpaRepository to inherit standard CRUD operations. Custom derived query 
 * methods are defined here to support specific business rules, such as email uniqueness.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Checks if a patient with the given email already exists in the system.
     * 
     * Used primarily during patient creation to enforce the email uniqueness business rule.
     *
     * @param email The email address to check for existence
     * @return true if a patient with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if an email exists but EXCLUDING a specific patient ID.
     * 
     * Required during the update flow to verify that a patient's new email isn't 
     * already claimed by a *different* patient in the system.
     *
     * @param email The email address to check
     * @param id The ID to exclude from the check
     * @return true if the email is used by another patient, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
