package com.provemeright.patient_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for the Patient Service REST API.
 * 
 * Intercepts specific application exceptions and validation errors, translating them 
 * into standard HTTP response codes and clean JSON payloads. This prevents stack 
 * traces from leaking to clients and ensures consistent API error structures.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles DTO validation failures (e.g., @NotBlank, @Email).
     *
     * @param ex The exception containing all validation errors
     * @return 400 Bad Request with a map of field -> error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles business rule violations regarding duplicate emails.
     *
     * @param ex The custom exception thrown by the service layer
     * @return 400 Bad Request with a JSON body
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        log.warn("Email address already exists: {}",ex.getMessage());

        Map<String,String> errors = new HashMap<>();
        errors.put("message","Email already exists!");

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles requests for non-existent resources.
     *
     * @param ex The exception thrown when the patient is not found
     * @return 404 Not Found response
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String,String>> handlePatientNotFoundException(PatientNotFoundException ex){
        log.warn("Patient not found: {}", ex.getMessage());
        
        Map<String,String> errors = new HashMap<>();
        errors.put("message", ex.getMessage() != null ? ex.getMessage() : "Patient not found");
        
        return ResponseEntity.status(404).body(errors);
    }
}
