package com.elearning.dto;

import lombok.*;
import java.util.Map;

/**
 * DTO pour les statistiques du tableau de bord enseignant.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherDashboardDTO {

    private long totalEtudiants;
    private double tauxCompletion;
    private double progressionMoyenne;
    private long totalCours;
    private long totalSeancesLive;
    private long totalRessources;

    /** Nombre d'inscrits par cours : clé = titre du cours */
    private Map<String, Long> inscritParCours;

    /** Progression moyenne par cours */
    private Map<String, Double> progressionParCours;

    /** Participation aux séances live par cours */
    private Map<String, Long> participationLiveParCours;
}
