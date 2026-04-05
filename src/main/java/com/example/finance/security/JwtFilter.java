package com.example.finance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // List of public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/public/register",
            "/api/auth/public/",
            "/api/health",
            "/api/health/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        System.out.println("=== JWT Filter Debug ===");
        System.out.println("Request URI: " + requestURI);
        System.out.println("Method: " + request.getMethod());

        // Check if this is a public endpoint
        boolean isPublicEndpoint = isPublicEndpoint(requestURI);

        if (isPublicEndpoint) {
            System.out.println("Public endpoint - no authentication required");
            System.out.println("=== End JWT Filter Debug ===\n");
            filterChain.doFilter(request, response);
            return;
        }

        // Protected endpoint - require token
        System.out.println("Protected endpoint - checking authentication");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token present: " + token.substring(0, Math.min(token.length(), 30)) + "...");

            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    System.out.println("✓ Authentication successful for: " + username + " (Role: " + role + ")");

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✓ Security context updated");
                } else {
                    System.out.println("✗ Invalid token");
                    sendUnauthorizedResponse(response, "Invalid or expired token");
                    return;
                }
            } catch (Exception e) {
                System.out.println("✗ Token validation error: " + e.getMessage());
                sendUnauthorizedResponse(response, "Authentication failed: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("✗ No Bearer token found in Authorization header");
            sendUnauthorizedResponse(response, "Authentication required");
            return;
        }

        System.out.println("=== End JWT Filter Debug ===\n");
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\":\"%s\",\"timestamp\":%d}",
                message, System.currentTimeMillis()));
    }
}