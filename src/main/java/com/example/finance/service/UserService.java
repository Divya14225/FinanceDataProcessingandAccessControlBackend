package com.example.finance.service;



import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.LoginRequest;
import com.example.finance.dto.UserCreateRequest;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.exception.UnauthorizedAccessException;
import com.example.finance.model.Role;
import com.example.finance.model.User;
import com.example.finance.model.UserStatus;
import com.example.finance.repository.UserRepository;
import com.example.finance.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil tokenProvider;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        return AuthResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .tokenType("Bearer")
                .build();
    }

    public User createUser(UserCreateRequest request, String adminId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy(adminId);

        return userRepository.save(user);
    }

    public User createFirstUser(UserCreateRequest request) {
        // Only allow creation if no users exist
        if (userRepository.count() > 0) {
            throw new RuntimeException("Users already exist. Use admin account to create new users.");
        }

        // First user is always ADMIN
        request.setRole(Role.ADMIN);
        return createUser(request, "SYSTEM");
    }

    public User updateUserStatus(String userId, UserStatus status, String currentUserId) {
        User user = findById(userId);
        User currentUser = findById(currentUserId);

        // Prevent deactivating your own account
        if (userId.equals(currentUserId) && status != UserStatus.ACTIVE) {
            throw new RuntimeException("You cannot deactivate your own account");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .toList();
    }

    public void validateUserAccess(String userId, String currentUserId, Role requiredRole) {
        User currentUser = findById(currentUserId);

        // Check if user is active
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedAccessException("Your account is not active. Please contact administrator.");
        }

        // Check role-based access
        if (currentUser.getRole() != Role.ADMIN && !currentUserId.equals(userId)) {
            throw new UnauthorizedAccessException("You don't have permission to access this resource");
        }

        // Check if required role level is met
        if (currentUser.getRole().ordinal() < requiredRole.ordinal()) {
            throw new UnauthorizedAccessException("Insufficient permissions. Required role: " + requiredRole);
        }
    }

    public void deleteUser(String userId, String currentUserId) {
        User user = findById(userId);
        User currentUser = findById(currentUserId);

        // Prevent deleting your own account
        if (userId.equals(currentUserId)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        // Soft delete - just mark as deleted/inactive
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }
}