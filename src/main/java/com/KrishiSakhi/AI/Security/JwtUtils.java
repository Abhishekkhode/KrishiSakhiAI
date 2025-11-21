package com.KrishiSakhi.AI.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")

    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Generates a short-lived Access Token using the username as the subject
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generates a new Access Token specifically for the Refresh endpoint
    public String generateTokenFromUsername(String username) {
        // Uses the same logic as the initial token, but called during renewal
        return generateJwtToken(username);
    }

    // Extracts the subject (username) from the token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Validates the token's signature and expiry
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // In a real application, you would log the exception here: logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }
}