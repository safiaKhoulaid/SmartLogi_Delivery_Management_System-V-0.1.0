package com.smartlogi.sdms.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // Import nécessaire
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaire pour JWTService.
 */
class JWTServiceTest {

    private JWTService jwtService;
    private UserDetails userDetails;
    private String userEmail;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        userEmail = "test.user@smartlogi.com";
        userDetails = new User(
                userEmail,
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // ... (Les autres tests qui réussissent sont ici) ...

    @Test
    @DisplayName("isTokenValid devrait retourner FAUX pour un token expiré")
    void isTokenValid_ShouldReturnFalse_ForExpiredToken() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) // Émis il y a 10s
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) // Expiré il y a 5s
                .signWith(SignatureAlgorithm.HS256, jwtService.getSignInKey())
                .compact();

        // Act & Assert
        // Le service (corrigé) doit attraper l'exception et retourner false
        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));

        // isTokenExpired doit aussi attraper l'exception et retourner true
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    // --- CORRECTION DU TEST DÉFAILLANT ---
    @Test
    @DisplayName("extractUserEmail doit lever JwtException, mais isTokenValid doit retourner false")
    void isTokenValid_ShouldHandleMalformedToken() {
        // Arrange
        String malformedToken = "ceci.nest.pas.un.token";

        // 1. Tester la méthode qui DOIT lever l'exception (extractUserEmail)
        assertThrows(JwtException.class, () -> {
            jwtService.extractUserEmail(malformedToken);
        }, "extractUserEmail (qui n'a pas de try-catch) doit propager l'exception.");

        // 2. Tester la méthode qui DOIT gérer l'exception (isTokenValid)
        assertFalse(jwtService.isTokenValid(malformedToken, userDetails),
                "isTokenValid (qui a un try-catch) doit retourner false.");
    }
    // --- FIN CORRECTION ---

    @Test
    @DisplayName("generateToken (avec claims) devrait inclure les claims supplémentaires")
    void generateToken_ShouldIncludeExtraClaims() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", "user-uuid-123");
        extraClaims.put("role", "ADMIN");

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        Claims claims = jwtService.extractClaims(token);
        assertEquals(userEmail, claims.getSubject());
        assertEquals("user-uuid-123", claims.get("userId", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
    }
}