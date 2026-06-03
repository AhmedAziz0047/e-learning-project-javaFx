package com.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CourseResourceDTO {

    private Long id;
    private Long courseId;
    private String courseTitre;
    private String resourceName;
    private String resourceType;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Long uploadedById;
    private String uploadedByNom;
    private int version;
    private LocalDateTime createdAt;
}
