package com.example.finance.dto;


import com.example.finance.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String fullName;
    private Role role;
    private String tokenType = "Bearer";

    public AuthResponse(String token, String userId, String email, String fullName, Role role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public AuthResponse(String token, String userId, String email, String fullName, Role role, String tokenType) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.tokenType = tokenType;
    }
}
