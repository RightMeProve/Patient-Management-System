package com.pm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Auth Service.
 * 
 * Defines the HTTP security firewall rules. As this is the Authentication service,
 * it must allow unauthenticated access to its endpoints (like /login) so users
 * can authenticate. In a production scenario, internal endpoints would be tightly
 * secured, exposing only the necessary public paths.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the SecurityFilterChain.
     * 
     * - Allows all incoming requests without authentication (necessary for /login).
     * - Disables CSRF protection: Since our API is stateless and relies on JWTs 
     *   sent via the Authorization header rather than cookies, CSRF attacks are 
     *   structurally prevented. Disabling it removes unnecessary overhead.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Defines the cryptographic algorithm used to hash passwords.
     * 
     * BCrypt is chosen because it incorporates a random salt and is computationally
     * intensive, providing robust protection against rainbow table and brute-force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
