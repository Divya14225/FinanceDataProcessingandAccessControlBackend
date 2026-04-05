package com.example.finance.dto;


import com.example.finance.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;

    private String reason; // Optional reason for status change
}
