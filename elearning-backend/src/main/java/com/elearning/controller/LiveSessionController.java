package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.LiveSessionDTO;
import com.elearning.service.LiveSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur des séances en direct (classes virtuelles).
 */
@RestController
@RequestMapping("/api/live-sessions")
@RequiredArgsConstructor
public class LiveSessionController {

    private final LiveSessionService sessionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LiveSessionDTO> createSession(@Valid @RequestBody LiveSessionDTO dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveSessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(sessionService.getSessionsByCourse(courseId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<LiveSessionDTO>> getUpcomingSessions() {
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }

    @GetMapping("/live")
    public ResponseEntity<List<LiveSessionDTO>> getLiveSessions() {
        return ResponseEntity.ok(sessionService.getLiveSessions());
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(sessionService.getSessionsByTeacher(teacherId));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LiveSessionDTO> startSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.startSession(id));
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LiveSessionDTO> endSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.endSession(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LiveSessionDTO> updateSession(@PathVariable Long id, @RequestBody LiveSessionDTO dto) {
        return ResponseEntity.ok(sessionService.updateSession(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success("Séance supprimée avec succès"));
    }
}
