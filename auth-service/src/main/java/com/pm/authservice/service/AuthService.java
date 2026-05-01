package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDto;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Core business logic for authentication operations.
 * 
 * Orchestrates the login process and token verification by bridging the HTTP controller,
 * the UserService (database access), and cryptography utilities.
 */
@Service
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and generates a JWT.
     * 
     * Uses a declarative, functional approach via Optionals to fetch the user, safely compare
     * the hashed password, and map the result to a JWT token.
     *
     * @param loginRequestDto Contains the raw email and password.
     * @return Optional containing the JWT string if successful, or empty if authentication fails.
     */
    public Optional<String> authenticate(LoginRequestDto loginRequestDto){
        return userService.findByEmail(loginRequestDto.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDto.getPassword(), u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));
    }

    /**
     * Validates a JWT against cryptographic tampering and expiration.
     * 
     * @param token The raw JWT string.
     * @return true if valid, false if tampered with or expired.
     */
    public boolean validateToken(String token){
        try{
            jwtUtil.validateToken(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }

}
