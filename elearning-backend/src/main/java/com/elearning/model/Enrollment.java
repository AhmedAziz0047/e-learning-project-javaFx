package com.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Inscription — liaison entre un étudiant et un cours.
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "date_inscription", updatable = false)
    private LocalDateTime dateInscription;

    /** Progression en pourcentage (0 à 100). */
    @Column(nullable = false)
    private int progression = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus statut = EnrollmentStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        this.dateInscription = LocalDateTime.now();
    }
}
