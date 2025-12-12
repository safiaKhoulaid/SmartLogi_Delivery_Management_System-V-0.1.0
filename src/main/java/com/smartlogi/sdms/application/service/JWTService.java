package com.smartlogi.sdms.application.service;


import io.jsonwebtoken.Claims;
// --- AJOUTS IMPORTS ---
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
// --- FIN AJOUTS ---
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    private static final String SECRET_KEY = "e41cd8d26ff1ee82eb8d271b14f247f7c24ff9189aca42f6f3c841f4bb7c60da";


    public String extractUserEmail(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    // ... (generateToken methods are fine) ...
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(SignatureAlgorithm.HS256, getSignInKey())
                .compact();
    }
    public String generateToken(UserDetails userDetails) {
        return   generateToken(new HashMap<>() , userDetails);
    }
    // ...

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    public <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(jwt);
        return claimsResolver.apply(claims);
    }

    // --- CORRECTION 1 ---
    public boolean isTokenExpired(String jwt) {
        try {
            return extractExpiration(jwt).before(new Date());
        } catch (ExpiredJwtException e) {
            // Si le parser lève cette exception, c'est que le token EST expiré.
            return true;
        }
    }

    // --- CORRECTION 2 ---
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        try {
            final String userEmail = extractUserEmail(jwt);
            // Si extractUserEmail réussit (c-à-d n'a pas levé ExpiredJwtException),
            // nous savons que le token n'est pas expiré.
            // Il suffit donc de vérifier le nom d'utilisateur.
            return (userEmail.equals(userDetails.getUsername()));

        } catch (ExpiredJwtException e) {
            // Le token est expiré
            return false;
        } catch (JwtException e) {
            // Le token est malformé, a une signature invalide, etc.
            return false;
        }
    }

    public Claims extractClaims(String jwt) {
        return Jwts
                .parser()
                // Utilise la clé Key décodée
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(jwt) // C'est cette ligne qui lève l'exception
                .getBody();
    }

    public byte[] getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes).getEncoded();
    }
}