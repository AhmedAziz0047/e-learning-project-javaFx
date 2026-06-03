package com.elearning.audit;

import com.elearning.model.AuditLog;
import com.elearning.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.elearning.model.User;
import com.elearning.repository.UserRepository;

/**
 * Aspect AOP pour l'audit automatique des actions.
 * Intercepte les méthodes annotées avec @Auditable.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String result = "SUCCESS";
        Object returnValue = null;

        try {
            returnValue = joinPoint.proceed();
        } catch (Throwable e) {
            result = "FAILURE";
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            try {
                AuditLog auditLog = AuditLog.builder()
                        .userId(getCurrentUserId())
                        .action(auditable.action())
                        .entityType(auditable.entityType())
                        .payload(buildPayload(joinPoint.getArgs()))
                        .result(result)
                        .ipAddress(getClientIpAddress())
                        .executionTimeMs(executionTime)
                        .build();

                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                log.error("Erreur lors de l'enregistrement de l'audit : {}", e.getMessage());
            }
        }

        return returnValue;
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                return userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'ID utilisateur pour l'audit");
        }
        return null;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'adresse IP");
        }
        return "unknown";
    }

    private String buildPayload(Object[] args) {
        try {
            if (args != null && args.length > 0) {
                // Filtrer les objets non sérialisables
                Object[] safeArgs = java.util.Arrays.stream(args)
                        .map(arg -> {
                            if (arg instanceof org.springframework.web.multipart.MultipartFile file) {
                                return "File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
                            } else if (arg instanceof jakarta.servlet.http.HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                                return "ServletObject";
                            }
                            return arg;
                        })
                        .toArray();

                String json = objectMapper.writeValueAsString(safeArgs);
                if (json.length() > 2000) {
                    json = json.substring(0, 2000) + "...";
                }
                return json;
            }
        } catch (Exception e) {
            log.debug("Impossible de sérialiser le payload de l'audit : {}", e.getMessage());
        }
        return "{}";
    }
}
