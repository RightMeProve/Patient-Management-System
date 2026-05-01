package com.pm.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Custom JWT Validation Filter for the API Gateway.
 * 
 * Centralizes authentication logic at the gateway level. By validating JWTs here,
 * downstream microservices are protected from unauthorized requests, removing the need
 * for redundant security implementations in every service.
 * 
 * Extends AbstractGatewayFilterFactory to allow declarative configuration via application.yml.
 */
@Component
public class JwtValidationGatewayFilterFactory extends
        AbstractGatewayFilterFactory<Object> {

    /**
     * WebClient is used instead of RestTemplate because Spring Cloud Gateway operates
     * on a reactive, non-blocking stack (WebFlux/Netty). Blocking calls would exhaust
     * the event loop threads.
     */
    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if(token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            /**
             * Delegating token validation to the Auth Service via an HTTP call.
             * Trade-off: Validating the token locally at the Gateway using the shared secret
             * would be faster (zero network latency). However, delegating to the Auth Service
             * enables stateful checks, such as verifying if a token was revoked/blacklisted 
             * in the database.
             */
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()
                    .then(chain.filter(exchange));
        };
    }
}