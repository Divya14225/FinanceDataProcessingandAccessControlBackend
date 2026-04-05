package com.example.finance.repository;



import com.example.finance.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // Find by user ID with descending timestamp
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);

    // Find by entity type and entity ID with descending timestamp
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    // Find by timestamp between with descending order
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    // Find top 10 by timestamp descending (using Spring Data MongoDB's built-in limit)
    List<AuditLog> findTop10ByOrderByTimestampDesc();

    // Alternative: Find first 10 by timestamp descending
    List<AuditLog> findFirst10ByOrderByTimestampDesc();

    // Find with pagination
    org.springframework.data.domain.Page<AuditLog> findByOrderByTimestampDesc(org.springframework.data.domain.Pageable pageable);

    // Custom query for actions since a specific time
    @Query("{ 'action': ?0, 'timestamp': { $gte: ?1 } }")
    List<AuditLog> findActionsSince(String action, LocalDateTime since);

    // Count by user ID and action
    long countByUserIdAndAction(String userId, String action);

    // Count user actions since a specific time
    @Query(value = "{ 'userId': ?0, 'timestamp': { $gte: ?1 } }", count = true)
    long countUserActionsSince(String userId, LocalDateTime since);

    // Find by action type
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    // Find by timestamp after
    List<AuditLog> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);

    // Find by timestamp before
    List<AuditLog> findByTimestampBeforeOrderByTimestampDesc(LocalDateTime timestamp);

    // Find by user ID and action
    List<AuditLog> findByUserIdAndActionOrderByTimestampDesc(String userId, String action);

    // Find by date range for a specific user
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(String userId, LocalDateTime start, LocalDateTime end);

    // Delete old logs (older than specified date)
    void deleteByTimestampBefore(LocalDateTime timestamp);

    // Count logs by entity type
    long countByEntityType(String entityType);

    // Find distinct actions for a user
    @Query(value = "{ 'userId': ?0 }", fields = "{ 'action': 1 }")
    List<String> findDistinctActionsByUserId(String userId);
}