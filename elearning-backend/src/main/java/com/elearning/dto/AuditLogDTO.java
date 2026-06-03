package com.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLogDTO {

    private Long id;
    private Long userId;
    private String userNom;
    private String action;
    private String entityType;
    private Long entityId;
    private String payload;
    private String result;
    private String ipAddress;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
}
