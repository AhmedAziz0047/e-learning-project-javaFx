package com.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO pour l'affichage d'un utilisateur (sans mot de passe).
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String avatar;
    private boolean actif;
    private LocalDateTime createdAt;
}
