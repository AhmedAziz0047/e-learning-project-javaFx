package com.elearning.service;

import com.elearning.dto.AuditLogDTO;
import com.elearning.model.AuditLog;
import com.elearning.model.User;
import com.elearning.repository.AuditLogRepository;
import com.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Récupère tous les logs d'audit paginés.
     */
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDTO);
    }

    /**
     * Recherche de logs par action.
     */
    public Page<AuditLogDTO> searchAuditLogs(String action, Pageable pageable) {
        return auditLogRepository.findByActionContainingIgnoreCaseOrderByCreatedAtDesc(action, pageable)
                .map(this::toDTO);
    }

    /**
     * Logs par utilisateur.
     */
    public List<AuditLogDTO> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Logs par plage de dates.
     */
    public List<AuditLogDTO> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByDateRange(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Enregistre manuellement un log d'audit.
     */
    public void log(Long userId, String action, String entityType, Long entityId,
                    String payload, String result, String ipAddress, Long executionTimeMs) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .payload(payload)
                .result(result)
                .ipAddress(ipAddress)
                .executionTimeMs(executionTimeMs)
                .build();

        auditLogRepository.save(auditLog);
    }

    private AuditLogDTO toDTO(AuditLog log) {
        String userNom = null;
        if (log.getUserId() != null) {
            userNom = userRepository.findById(log.getUserId())
                    .map(u -> u.getPrenom() + " " + u.getNom())
                    .orElse("Inconnu");
        }

        return AuditLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userNom(userNom)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .payload(log.getPayload())
                .result(log.getResult())
                .ipAddress(log.getIpAddress())
                .executionTimeMs(log.getExecutionTimeMs())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
