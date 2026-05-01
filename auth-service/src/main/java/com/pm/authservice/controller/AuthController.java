package com.pm.authservice.controller;

import com.pm.authservice.dto.LoginRequestDto;
import com.pm.authservice.dto.LoginResponseDto;
import com.pm.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Authentication.
 * 
 * Exposes endpoints for generating JWTs (login) and validating them. This service
 * acts as the centralized authority for identity. The API Gateway relies on the 
 * `/validate` endpoint to verify tokens before routing requests to downstream services.
 */
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Authenticates a user and returns a signed JWT.
     *
     * @param loginRequestDto Contains username and password.
     * @return 200 OK with the token, or 401 UNAUTHORIZED if credentials fail.
     */
    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto
            ){
        Optional<String> tokenOptional = authService.authenticate(loginRequestDto);

        if(tokenOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = tokenOptional.get();
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    /**
     * Validates a provided JWT.
     * 
     * Typically called by the API Gateway to ensure a request's token is authentic
     * and not expired before proceeding.
     *
     * @param authHeader The raw "Authorization: Bearer <token>" string.
     * @return 200 OK if valid, 401 UNAUTHORIZED if invalid/expired.
     */
    @Operation(summary = "Validate Token")
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader){
                
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return authService.validateToken(authHeader.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
