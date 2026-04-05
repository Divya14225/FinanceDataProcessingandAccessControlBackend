package com.example.finance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret:5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expiration:86400000}")
    private int jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        System.out.println("Generating token for user: " + userPrincipal.getEmail());
        System.out.println("Token issued at: " + now);
        System.out.println("Token expires at: " + expiryDate);
        System.out.println("Token validity (ms): " + jwtExpiration);

        return Jwts.builder()
                .setSubject(userPrincipal.getId())
                .claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateToken(String userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        System.out.println("Generating token for user: " + email);
        System.out.println("Token expires at: " + expiryDate);

        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateTokenFromUserDetails(UserDetails userDetails) {
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userPrincipal.getId())
                .claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public Date extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        System.out.println("Token expiration date from token: " + expiration);
        System.out.println("Current time: " + new Date());
        return expiration;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        boolean isExpired = expiration.before(now);
        System.out.println("Token expired: " + isExpired);
        System.out.println("Expiration: " + expiration);
        System.out.println("Current: " + now);
        return isExpired;
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating token...");
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            boolean isExpired = isTokenExpired(token);
            if (isExpired) {
                System.out.println("Token is expired!");
                return false;
            }
            System.out.println("Token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return false;
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUserIdFromToken(String token) {
        return extractUserId(token);
    }

    public String getRoleFromToken(String token) {
        return extractRole(token);
    }

    public boolean hasRole(String token, String role) {
        String tokenRole = extractRole(token);
        return tokenRole != null && tokenRole.equals(role);
    }

    public String refreshToken(String token) {
        if (validateToken(token)) {
            Claims claims = extractAllClaims(token);
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);

            return Jwts.builder()
                    .setSubject(claims.getSubject())
                    .claim("email", claims.get("email"))
                    .claim("role", claims.get("role"))
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        }
        throw new RuntimeException("Invalid token");
    }

    public long getExpirationMs() {
        return jwtExpiration;
    }
}