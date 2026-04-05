package com.example.finance.controller;

import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.LoginRequest;
import com.example.finance.dto.UserCreateRequest;
import com.example.finance.model.User;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserCreateRequest request,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request, currentUser.getId()));
    }

    @PostMapping("/public/register")
    public ResponseEntity<User> registerFirstUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createFirstUser(request));
    }
}