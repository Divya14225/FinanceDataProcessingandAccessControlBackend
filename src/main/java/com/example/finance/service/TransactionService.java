package com.example.finance.service;


import com.example.finance.dto.TransactionRequest;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.exception.UnauthorizedAccessException;
import com.example.finance.model.Role;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionCategory;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    public Transaction createTransaction(TransactionRequest request, String userId) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setCreatedBy(userId);

        Transaction saved = transactionRepository.save(transaction);
        auditService.logAction(userId, "CREATE_TRANSACTION", "Transaction", saved.getId(),
                "Created transaction of amount " + request.getAmount());
        return saved;
    }

    public Transaction updateTransaction(String transactionId, TransactionRequest request, String userId, Role userRole) {
        Transaction transaction = findById(transactionId);

        if (userRole != Role.ADMIN && !transaction.getCreatedBy().equals(userId)) {
            throw new UnauthorizedAccessException("You can only update your own transactions");
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setUpdatedBy(userId);

        Transaction updated = transactionRepository.save(transaction);
        auditService.logAction(userId, "UPDATE_TRANSACTION", "Transaction", transactionId,
                "Updated transaction details");
        return updated;
    }

    public void deleteTransaction(String transactionId, String userId, Role userRole) {
        Transaction transaction = findById(transactionId);

        if (userRole != Role.ADMIN && !transaction.getCreatedBy().equals(userId)) {
            throw new UnauthorizedAccessException("You can only delete your own transactions");
        }

        transaction.setDeleted(true);
        transactionRepository.save(transaction);
        auditService.logAction(userId, "DELETE_TRANSACTION", "Transaction", transactionId,
                "Soft deleted transaction");
    }

    public Transaction findById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
    }

    public List<Transaction> getUserTransactions(String userId, String currentUserId, Role currentUserRole) {
        if (currentUserRole == Role.ADMIN) {
            return transactionRepository.findByCreatedByAndDeletedFalse(userId);
        } else if (currentUserId.equals(userId)) {
            return transactionRepository.findByCreatedByAndDeletedFalse(userId);
        } else {
            throw new UnauthorizedAccessException("You can only view your own transactions");
        }
    }

    public List<Transaction> getUserTransactionsByDateRange(String userId, LocalDateTime start, LocalDateTime end,
                                                            String currentUserId, Role currentUserRole) {
        validateTransactionAccess(userId, currentUserId, currentUserRole);
        return transactionRepository.findUserTransactionsInDateRange(userId, start, end);
    }

    public List<Transaction> filterTransactions(String userId, TransactionType type, TransactionCategory category,
                                                String currentUserId, Role currentUserRole) {
        validateTransactionAccess(userId, currentUserId, currentUserRole);

        if (type != null && category != null) {
            return transactionRepository.findByCreatedByAndDeletedFalse(userId).stream()
                    .filter(t -> t.getType() == type && t.getCategory() == category)
                    .toList();
        } else if (type != null) {
            return transactionRepository.findByCreatedByAndTypeAndDeletedFalse(userId, type);
        } else if (category != null) {
            return transactionRepository.findByCreatedByAndCategoryAndDeletedFalse(userId, category);
        } else {
            return transactionRepository.findByCreatedByAndDeletedFalse(userId);
        }
    }

    private void validateTransactionAccess(String userId, String currentUserId, Role currentUserRole) {
        if (currentUserRole != Role.ADMIN && !currentUserId.equals(userId)) {
            throw new UnauthorizedAccessException("You can only access your own transactions");
        }
    }
}