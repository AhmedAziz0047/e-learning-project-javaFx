package com.elearning.service;

import com.elearning.dto.CourseDTO;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.CourseResourceRepository;
import com.elearning.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseResourceRepository resourceRepository;
    private final UserService userService;

    /**
     * Crée un nouveau cours.
     */
    public CourseDTO createCourse(CourseDTO dto) {
        User enseignant = userService.getCurrentUser();

        Course course = Course.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .categorie(dto.getCategorie())
                .niveau(dto.getNiveau())
                .dureeHeures(dto.getDureeHeures())
                .imageUrl(dto.getImageUrl())
                .enseignant(enseignant)
                .actif(true)
                .build();

        course = courseRepository.save(course);
        return toDTO(course);
    }

    /**
     * Récupère un cours par ID.
     */
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID : " + id));
        return toDTO(course);
    }

    /**
     * Liste tous les cours actifs.
     */
    public List<CourseDTO> getAllActiveCourses() {
        return courseRepository.findByActifTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les cours actifs avec pagination.
     */
    public Page<CourseDTO> getActiveCoursesPage(Pageable pageable) {
        return courseRepository.findByActifTrue(pageable).map(this::toDTO);
    }

    /**
     * Liste les cours d'un enseignant.
     */
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByEnseignantId(teacherId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche de cours.
     */
    public List<CourseDTO> searchCourses(String search) {
        return courseRepository.searchCourses(search).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un cours.
     */
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));

        if (dto.getTitre() != null) course.setTitre(dto.getTitre());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getCategorie() != null) course.setCategorie(dto.getCategorie());
        if (dto.getNiveau() != null) course.setNiveau(dto.getNiveau());
        if (dto.getDureeHeures() != null) course.setDureeHeures(dto.getDureeHeures());
        if (dto.getImageUrl() != null) course.setImageUrl(dto.getImageUrl());

        course = courseRepository.save(course);
        return toDTO(course);
    }

    /**
     * Supprime (désactive) un cours.
     */
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé"));
        course.setActif(false);
        courseRepository.save(course);
    }

    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID : " + id));
    }

    private CourseDTO toDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .titre(course.getTitre())
                .description(course.getDescription())
                .categorie(course.getCategorie())
                .niveau(course.getNiveau())
                .dureeHeures(course.getDureeHeures())
                .imageUrl(course.getImageUrl())
                .enseignantId(course.getEnseignant() != null ? course.getEnseignant().getId() : null)
                .enseignantNom(course.getEnseignant() != null ?
                        course.getEnseignant().getPrenom() + " " + course.getEnseignant().getNom() : null)
                .actif(course.isActif())
                .createdAt(course.getCreatedAt())
                .nombreInscrits(enrollmentRepository.countByCourseId(course.getId()))
                .nombreRessources(resourceRepository.countByCourseId(course.getId()))
                .build();
    }
}
