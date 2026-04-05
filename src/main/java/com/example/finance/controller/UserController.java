package com.example.finance.controller;


import com.example.finance.dto.UserCreateRequest;
import com.example.finance.dto.UserStatusUpdateRequest;
import com.example.finance.model.Role;
import com.example.finance.model.User;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable String userId,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.validateUserAccess(userId, currentUser.getId(), Role.VIEWER);
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.createUser(request, currentUser.getId()));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserStatus(@PathVariable String userId,
                                                 @Valid @RequestBody UserStatusUpdateRequest request,
                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request.getStatus(), currentUser.getId()));
    }
}