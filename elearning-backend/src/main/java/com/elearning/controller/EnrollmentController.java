package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.EnrollmentDTO;
import com.elearning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur de gestion des inscriptions.
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<EnrollmentDTO> enroll(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.enrollCurrentUser(courseId));
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse> unenroll(@PathVariable Long courseId) {
        enrollmentService.unenrollCurrentUser(courseId);
        return ResponseEntity.ok(ApiResponse.success("Désinscription effectuée avec succès"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @PutMapping("/{id}/progression")
    public ResponseEntity<EnrollmentDTO> updateProgression(
            @PathVariable Long id, @RequestParam int progression) {
        return ResponseEntity.ok(enrollmentService.updateProgression(id, progression));
    }
}
