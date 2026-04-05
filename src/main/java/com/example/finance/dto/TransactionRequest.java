package com.example.finance.dto;

import com.example.finance.model.TransactionCategory;
import com.example.finance.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    @NotNull
    private TransactionCategory category;

    @Size(max = 500)
    private String description;

    @NotNull @PastOrPresent
    private LocalDateTime transactionDate;
}
