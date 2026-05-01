package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for JSON Web Token (JWT) operations.
 * 
 * Handles the generation and cryptographic validation of JWTs (RFC 7519) using JJWT.
 * It provides stateless authentication capabilities by embedding user claims and 
 * verifying signatures via HMAC-SHA algorithms.
 */
@Component
public class JwtUtil {

    private final Key secretKey;

    /**
     * Initializes the secret key used for signing and verifying tokens.
     * 
     * The secret is injected from configuration and must be kept secure. A compromised
     * key would allow attackers to forge tokens with arbitrary claims.
     */
    public JwtUtil(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for an authenticated user.
     * 
     * Includes standard claims (sub, iat, exp) and custom claims (role).
     *
     * @param email The user's email, acting as the subject.
     * @param role The user's authorization role.
     * @return The signed JWT string.
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a provided token's signature and expiration.
     * 
     * JJWT recalculates the signature using the payload and secret key. If it matches,
     * the token is authentic. It automatically rejects expired tokens.
     *
     * @param token The raw token string.
     * @throws JwtException if the signature is invalid, altered, or expired.
     */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT");
        }
    }

}
