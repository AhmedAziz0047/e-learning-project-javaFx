package com.elearning.service;

import com.elearning.audit.Auditable;
import com.elearning.dto.LiveSessionDTO;
import com.elearning.exception.BadRequestException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.*;
import com.elearning.repository.LiveSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveSessionService {

    private final LiveSessionRepository sessionRepository;
    private final CourseService courseService;
    private final UserService userService;

    /**
     * Crée une nouvelle séance live.
     */
    @Auditable(action = "CREATE_SESSION", entityType = "LiveSession")
    public LiveSessionDTO createSession(LiveSessionDTO dto) {
        User teacher = userService.getCurrentUser();
        Course course = courseService.getCourseEntityById(dto.getCourseId());

        LiveSession session = LiveSession.builder()
                .course(course)
                .teacher(teacher)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .meetingLink(dto.getMeetingLink())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(SessionStatus.PLANNED)
                .build();

        session = sessionRepository.save(session);
        return toDTO(session);
    }

    /**
     * Démarre une séance live.
     */
    @Auditable(action = "START_SESSION", entityType = "LiveSession")
    public LiveSessionDTO startSession(Long sessionId) {
        LiveSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (session.getStatus() != SessionStatus.PLANNED) {
            throw new BadRequestException("La séance ne peut être démarrée que si elle est planifiée");
        }

        session.setStatus(SessionStatus.LIVE);
        session.setStartTime(LocalDateTime.now());
        session = sessionRepository.save(session);
        return toDTO(session);
    }

    /**
     * Termine une séance live.
     */
    @Auditable(action = "END_SESSION", entityType = "LiveSession")
    public LiveSessionDTO endSession(Long sessionId) {
        LiveSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (session.getStatus() != SessionStatus.LIVE) {
            throw new BadRequestException("La séance doit être en cours pour être terminée");
        }

        session.setStatus(SessionStatus.FINISHED);
        session.setEndTime(LocalDateTime.now());
        session = sessionRepository.save(session);
        return toDTO(session);
    }

    /**
     * Récupère une séance par ID.
     */
    public LiveSessionDTO getSessionById(Long id) {
        LiveSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        return toDTO(session);
    }

    /**
     * Liste les séances d'un cours.
     */
    public List<LiveSessionDTO> getSessionsByCourse(Long courseId) {
        return sessionRepository.findByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les séances à venir.
     */
    public List<LiveSessionDTO> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les séances en cours.
     */
    public List<LiveSessionDTO> getLiveSessions() {
        return sessionRepository.findLiveSessions().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les séances d'un enseignant.
     */
    public List<LiveSessionDTO> getSessionsByTeacher(Long teacherId) {
        return sessionRepository.findByTeacherId(teacherId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une séance.
     */
    public LiveSessionDTO updateSession(Long id, LiveSessionDTO dto) {
        LiveSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (dto.getTitle() != null) session.setTitle(dto.getTitle());
        if (dto.getDescription() != null) session.setDescription(dto.getDescription());
        if (dto.getMeetingLink() != null) session.setMeetingLink(dto.getMeetingLink());
        if (dto.getStartTime() != null) session.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) session.setEndTime(dto.getEndTime());

        session = sessionRepository.save(session);
        return toDTO(session);
    }

    /**
     * Supprime une séance.
     */
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Séance non trouvée");
        }
        sessionRepository.deleteById(id);
    }

    private LiveSessionDTO toDTO(LiveSession session) {
        return LiveSessionDTO.builder()
                .id(session.getId())
                .courseId(session.getCourse().getId())
                .courseTitre(session.getCourse().getTitre())
                .teacherId(session.getTeacher().getId())
                .teacherNom(session.getTeacher().getPrenom() + " " + session.getTeacher().getNom())
                .title(session.getTitle())
                .description(session.getDescription())
                .meetingLink(session.getMeetingLink())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus().name())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
