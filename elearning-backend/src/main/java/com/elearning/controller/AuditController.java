package com.elearning.controller;

import com.elearning.dto.AuditLogDTO;
import com.elearning.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur de consultation des logs d'audit (admin uniquement).
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getAllAuditLogs(PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuditLogDTO>> searchAuditLogs(
            @RequestParam String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.searchAuditLogs(action, PageRequest.of(page, size)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogDTO>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getLogsByUser(userId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLogDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditService.getLogsByDateRange(start, end));
    }
}
