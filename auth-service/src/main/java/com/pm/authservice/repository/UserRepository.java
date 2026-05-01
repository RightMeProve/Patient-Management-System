package com.pm.authservice.repository;

import com.pm.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Layer for the User entity.
 * 
 * Abstracts database interactions. By extending JpaRepository, we leverage Spring Data's 
 * dynamic proxy creation to handle boilerplate CRUD without explicit SQL implementations.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Retrieves a User based on their unique email address.
     *
     * @param email The email to search for
     * @return An Optional containing the User if found, or Optional.empty() if not
     */
    Optional<User> findByEmail(String email);
}
