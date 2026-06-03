package com.elearning.controller;

import com.elearning.dto.TeacherDashboardDTO;
import com.elearning.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur du tableau de bord enseignant.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/teacher/stats")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<TeacherDashboardDTO> getTeacherDashboard() {
        return ResponseEntity.ok(dashboardService.getTeacherDashboard());
    }
}
