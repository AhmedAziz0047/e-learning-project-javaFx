package com.elearning.controller;

import com.elearning.service.DiplomaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diploma")
@RequiredArgsConstructor
public class DiplomaController {

    private final DiplomaService diplomaService;

    @GetMapping("/download")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> downloadDiploma() {
        byte[] pdfBytes = diplomaService.generateDiploma();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diplome.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
