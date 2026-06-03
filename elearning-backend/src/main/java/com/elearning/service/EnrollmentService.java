package com.elearning.service;

import com.elearning.dto.EnrollmentDTO;
import com.elearning.exception.BadRequestException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.*;
import com.elearning.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    /**
     * Inscrit l'utilisateur courant à un cours.
     */
    public EnrollmentDTO enrollCurrentUser(Long courseId) {
        User student = userService.getCurrentUser();
        Course course = courseService.getCourseEntityById(courseId);

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BadRequestException("Vous êtes déjà inscrit à ce cours");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .progression(0)
                .statut(EnrollmentStatus.ACTIVE)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return toDTO(enrollment);
    }

    /**
     * Désinscription de l'utilisateur courant.
     */
    public void unenrollCurrentUser(Long courseId) {
        User student = userService.getCurrentUser();
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée"));

        enrollment.setStatut(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    /**
     * Liste les inscriptions de l'utilisateur courant.
     */
    public List<EnrollmentDTO> getMyEnrollments() {
        User student = userService.getCurrentUser();
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les inscrits d'un cours.
     */
    public List<EnrollmentDTO> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour la progression.
     */
    public EnrollmentDTO updateProgression(Long enrollmentId, int progression) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée"));

        enrollment.setProgression(Math.min(100, Math.max(0, progression)));
        if (enrollment.getProgression() == 100) {
            enrollment.setStatut(EnrollmentStatus.COMPLETED);
        }

        enrollment = enrollmentRepository.save(enrollment);
        return toDTO(enrollment);
    }

    private EnrollmentDTO toDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentNom(enrollment.getStudent().getNom())
                .studentPrenom(enrollment.getStudent().getPrenom())
                .courseId(enrollment.getCourse().getId())
                .courseTitre(enrollment.getCourse().getTitre())
                .dateInscription(enrollment.getDateInscription())
                .progression(enrollment.getProgression())
                .statut(enrollment.getStatut().name())
                .build();
    }
}
