package com.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EnrollmentDTO {

    private Long id;
    private Long studentId;
    private String studentNom;
    private String studentPrenom;
    private Long courseId;
    private String courseTitre;
    private LocalDateTime dateInscription;
    private int progression;
    private String statut;
}
