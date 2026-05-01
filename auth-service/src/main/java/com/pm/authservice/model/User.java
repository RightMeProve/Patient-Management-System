package com.pm.authservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

/**
 * Domain model representing an authenticated user within the system.
 * 
 * Maps to the "users" table. This entity stores core identity and authorization 
 * details used to generate JWTs. Passwords stored here must always be cryptographically 
 * hashed (e.g., BCrypt).
 */
@Data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * Stores the user's role (e.g., "ROLE_USER", "ROLE_ADMIN").
     * Mapped as a simple string for this microservice, but could be expanded 
     * to a ManyToMany relationship for complex RBAC systems.
     */
    @Column(nullable = false)
    private String role;

}
