package com.example.finance.repository;





import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionCategory;
import com.example.finance.model.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByCreatedByAndDeletedFalse(String userId);
    List<Transaction> findByCreatedByAndTransactionDateBetweenAndDeletedFalse(String userId, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByCreatedByAndCategoryAndDeletedFalse(String userId, TransactionCategory category);
    List<Transaction> findByCreatedByAndTypeAndDeletedFalse(String userId, TransactionType type);

    @Query("{ 'createdBy': ?0, 'deleted': false, 'transactionDate': { $gte: ?1, $lte: ?2 } }")
    List<Transaction> findUserTransactionsInDateRange(String userId, LocalDateTime start, LocalDateTime end);

    long countByCreatedByAndDeletedFalse(String userId);
}