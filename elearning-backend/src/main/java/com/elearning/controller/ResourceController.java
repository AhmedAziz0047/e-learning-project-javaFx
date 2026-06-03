package com.elearning.controller;

import com.elearning.dto.ApiResponse;
import com.elearning.dto.CourseResourceDTO;
import com.elearning.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur de gestion des ressources pédagogiques.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final FileStorageService fileStorageService;

    @PostMapping("/courses/{courseId}/resources")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseResourceDTO> uploadResource(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(fileStorageService.uploadResource(courseId, file));
    }

    @GetMapping("/courses/{courseId}/resources")
    public ResponseEntity<List<CourseResourceDTO>> getResourcesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(fileStorageService.getResourcesByCourse(courseId));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<CourseResourceDTO> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(fileStorageService.getResourceById(id));
    }

    @GetMapping("/resources/{id}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable Long id) {
        Resource resource = fileStorageService.downloadResource(id);
        CourseResourceDTO dto = fileStorageService.getResourceById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + dto.getResourceName() + "\"")
                .body(resource);
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseResourceDTO> updateResource(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(fileStorageService.updateResource(id, file));
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse> deleteResource(@PathVariable Long id) {
        fileStorageService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.success("Ressource supprimée avec succès"));
    }
}
