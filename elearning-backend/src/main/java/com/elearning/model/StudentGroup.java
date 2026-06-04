package com.elearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un groupe d'étudiants pour un niveau spécifique.
 */
@Entity
@Table(name = "student_groups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ex: "Niveau 1 - Groupe 1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyLevel level;

    @OneToMany(mappedBy = "studentGroup", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> students = new HashSet<>();

    @ManyToMany(mappedBy = "targetGroups", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Course> courses = new HashSet<>();
}
