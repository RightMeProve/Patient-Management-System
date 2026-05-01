package com.provemeright.patient_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain entity representing a Patient in the system.
 * 
 * Maps directly to the 'patient' table in the database. This class is meant solely 
 * for persistence and should not be exposed directly to API clients. DTOs are used 
 * instead for network transfers to ensure security and API contract decoupling.
 */
@Entity
public class Patient {

    /**
     * Primary key for the patient.
     * UUID is chosen over auto-incrementing integers to ensure global uniqueness,
     * security (unpredictability), and microservice-friendly distributed ID generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String name;

    /**
     * Unique email address for the patient.
     * 
     * The unique constraint here acts as the final, database-level defense against 
     * duplicates, backing up the application-level checks in PatientService.
     */
    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private String Address;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private LocalDate registeredDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

}
