package com.pm.authservice.service;

import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Domain service for User operations.
 * 
 * Separates simple CRUD and domain logic concerning the User entity from the 
 * complex authentication orchestration managed by AuthService, adhering to the 
 * Single Responsibility Principle (SRP).
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Fetches a user by their email address.
     * 
     * Returns an Optional to explicitly handle the possibility of a non-existent user
     * without resorting to null checks, providing a safer contract for callers.
     *
     * @param email The email address to look up
     * @return Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
