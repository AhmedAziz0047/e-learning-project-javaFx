package com.elearning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CourseDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;
    private String categorie;
    private String niveau;
    private Integer dureeHeures;
    private String imageUrl;
    private Long enseignantId;
    private String enseignantNom;
    private boolean actif;
    private LocalDateTime createdAt;
    private java.util.List<Long> targetGroupIds;

    // Statistiques
    private long nombreInscrits;
    private long nombreRessources;
}
