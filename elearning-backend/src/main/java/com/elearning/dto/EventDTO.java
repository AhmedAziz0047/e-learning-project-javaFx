package com.elearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventDTO {

    private Long id;
    private Long courseId;
    private String courseTitre;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotBlank(message = "Le type d'événement est obligatoire")
    private String eventType;

    @NotNull(message = "La date de l'événement est obligatoire")
    private LocalDateTime eventDate;

    private Integer durationMinutes;
    private Long createdById;
    private String createdByNom;
    private LocalDateTime createdAt;
    private java.util.List<Long> targetGroupIds;
}
