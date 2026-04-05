package com.example.finance.controller;


import com.example.finance.dto.TransactionRequest;
import com.example.finance.model.Role;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionCategory;
import com.example.finance.model.TransactionType;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, currentUser.getId()));
    }

    @PutMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable String transactionId,
                                                         @Valid @RequestBody TransactionRequest request,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(transactionService.updateTransaction(transactionId, request,
                currentUser.getId(), currentUser.getRole()));
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionId,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        transactionService.deleteTransaction(transactionId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<Transaction>> getUserTransactions(@RequestParam(required = false) String userId,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        String targetUserId = userId != null && currentUser.getRole() == Role.ADMIN ? userId : currentUser.getId();
        return ResponseEntity.ok(transactionService.getUserTransactions(targetUserId, currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<Transaction>> filterTransactions(@RequestParam(required = false) String userId,
                                                                @RequestParam(required = false) TransactionType type,
                                                                @RequestParam(required = false) TransactionCategory category,
                                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        String targetUserId = userId != null && currentUser.getRole() == Role.ADMIN ? userId : currentUser.getId();
        return ResponseEntity.ok(transactionService.filterTransactions(targetUserId, type, category,
                currentUser.getId(), currentUser.getRole()));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String targetUserId = userId != null && currentUser.getRole() == Role.ADMIN ? userId : currentUser.getId();
        return ResponseEntity.ok(transactionService.getUserTransactionsByDateRange(targetUserId, start, end,
                currentUser.getId(), currentUser.getRole()));
    }
}