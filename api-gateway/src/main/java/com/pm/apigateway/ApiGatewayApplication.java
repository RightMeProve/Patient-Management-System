package com.pm.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the API Gateway microservice.
 * 
 * The API Gateway acts as a reverse proxy, routing all client requests to the appropriate 
 * downstream microservices. It abstracts the internal microservice architecture from the client,
 * providing a single point of entry.
 * 
 * Design Pattern: API Gateway Pattern
 * It simplifies the client by reducing the number of round trips and handles cross-cutting concerns 
 * like authentication, SSL termination, and rate limiting in a centralized place.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        // Bootstraps the API Gateway Spring context.
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
