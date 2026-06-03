package com.elearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LiveSessionDTO {

    private Long id;
    private Long courseId;
    private String courseTitre;
    private Long teacherId;
    private String teacherNom;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;
    private String meetingLink;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
}
