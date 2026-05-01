package com.pm.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the API Gateway (Reactive stack).
 * 
 * Captures exceptions thrown by WebClient during token validation (e.g., Auth Service
 * returning 401) and gracefully translates them into a reactive Mono<Void> response,
 * preventing the Gateway from throwing unhandled 500 Internal Server Errors.
 */
@RestControllerAdvice
public class JwtValidationException {
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<Void> handleUnauthorizedException(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}