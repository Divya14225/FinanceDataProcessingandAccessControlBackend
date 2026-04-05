package com.example.finance.controller;


import com.example.finance.model.AuditLog;
import com.example.finance.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.getUserAuditLogs(userId));
    }

    @GetMapping("/entity")
    public ResponseEntity<List<AuditLog>> getEntityAuditLogs(@RequestParam String entityType,
                                                             @RequestParam String entityId) {
        return ResponseEntity.ok(auditService.getEntityAuditLogs(entityType, entityId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(start, end));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs(50));
    }
}
