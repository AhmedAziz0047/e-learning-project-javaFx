package com.elearning.service;

import com.elearning.dto.TeacherDashboardDTO;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LiveSessionRepository sessionRepository;
    private final CourseResourceRepository resourceRepository;
    private final UserService userService;

    /**
     * Statistiques du tableau de bord enseignant.
     */
    public TeacherDashboardDTO getTeacherDashboard() {
        User teacher = userService.getCurrentUser();
        Long teacherId = teacher.getId();

        List<Course> courses = courseRepository.findByEnseignantId(teacherId);

        long totalEtudiants = enrollmentRepository.countStudentsByTeacherId(teacherId);
        Double avgProgression = enrollmentRepository.getAverageProgressionByTeacherId(teacherId);
        long completedCount = enrollmentRepository.countCompletedByTeacherId(teacherId);
        long totalEnrollments = totalEtudiants > 0 ? totalEtudiants : 1;
        double tauxCompletion = (double) completedCount / totalEnrollments * 100;

        long totalCours = courseRepository.countByEnseignantId(teacherId);
        long totalSessions = sessionRepository.countByTeacherId(teacherId);

        // Statistiques par cours
        Map<String, Long> inscritParCours = new HashMap<>();
        Map<String, Double> progressionParCours = new HashMap<>();
        long totalRessources = 0;

        for (Course course : courses) {
            String titre = course.getTitre();
            long inscrits = enrollmentRepository.countByCourseId(course.getId());
            inscritParCours.put(titre, inscrits);

            Double progress = enrollmentRepository.getAverageProgressionByCourseId(course.getId());
            progressionParCours.put(titre, progress != null ? progress : 0.0);

            totalRessources += resourceRepository.countByCourseId(course.getId());
        }

        return TeacherDashboardDTO.builder()
                .totalEtudiants(totalEtudiants)
                .tauxCompletion(Math.round(tauxCompletion * 100.0) / 100.0)
                .progressionMoyenne(avgProgression != null ? Math.round(avgProgression * 100.0) / 100.0 : 0.0)
                .totalCours(totalCours)
                .totalSeancesLive(totalSessions)
                .totalRessources(totalRessources)
                .inscritParCours(inscritParCours)
                .progressionParCours(progressionParCours)
                .participationLiveParCours(new HashMap<>()) // Peut être enrichi avec les logs d'audit
                .build();
    }
}
