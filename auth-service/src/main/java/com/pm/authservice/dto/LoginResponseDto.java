package com.pm.authservice.dto;

import lombok.Data;

/**
 * Data Transfer Object for authentication responses.
 * 
 * Encapsulates the generated JWT token to be returned to the client upon successful login.
 */
@Data
public class LoginResponseDto {
    private final String token;

    public LoginResponseDto(String token){
        this.token = token;
    }
}
