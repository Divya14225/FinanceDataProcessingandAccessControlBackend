package com.example.finance.security;



import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String userId;
    private final String email;
    private final String role;

    public JwtAuthenticationToken(String userId, String email, String role,
                                  Object credentials,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(email, credentials, authorities);
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
