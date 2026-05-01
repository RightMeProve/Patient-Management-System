package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for login credentials.
 * 
 * Decouples the API contract from the internal User domain model. Utilizes Jakarta
 * Bean Validation to ensure incoming requests meet minimum format requirements before
 * hitting the service layer.
 */
@Data
public class LoginRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min=8,message = "Password should be atleat 8 character long")
    private String password;
}
