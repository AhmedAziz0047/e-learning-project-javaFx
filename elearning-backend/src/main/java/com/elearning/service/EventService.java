package com.elearning.service;

import com.elearning.dto.EventDTO;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.*;
import com.elearning.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CourseService courseService;
    private final UserService userService;

    /**
     * Crée un nouvel événement.
     */
    public EventDTO createEvent(EventDTO dto) {
        User creator = userService.getCurrentUser();
        Course course = dto.getCourseId() != null ?
                courseService.getCourseEntityById(dto.getCourseId()) : null;

        Event event = Event.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventType(EventType.valueOf(dto.getEventType()))
                .eventDate(dto.getEventDate())
                .durationMinutes(dto.getDurationMinutes())
                .createdBy(creator)
                .build();

        event = eventRepository.save(event);
        return toDTO(event);
    }

    /**
     * Récupère un événement par ID.
     */
    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));
        return toDTO(event);
    }

    /**
     * Événements d'un jour donné.
     */
    public List<EventDTO> getEventsByDay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return eventRepository.findEventsBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Événements d'une semaine donnée (à partir d'une date).
     */
    public List<EventDTO> getEventsByWeek(LocalDate startDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusDays(7).atTime(LocalTime.MAX);
        return eventRepository.findEventsBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Événements d'un mois donné.
     */
    public List<EventDTO> getEventsByMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = lastDay.atTime(LocalTime.MAX);
        return eventRepository.findEventsBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Prochains événements.
     */
    public List<EventDTO> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un événement.
     */
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventType() != null) event.setEventType(EventType.valueOf(dto.getEventType()));
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getDurationMinutes() != null) event.setDurationMinutes(dto.getDurationMinutes());

        event = eventRepository.save(event);
        return toDTO(event);
    }

    /**
     * Supprime un événement.
     */
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Événement non trouvé");
        }
        eventRepository.deleteById(id);
    }

    private EventDTO toDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .courseId(event.getCourse() != null ? event.getCourse().getId() : null)
                .courseTitre(event.getCourse() != null ? event.getCourse().getTitre() : null)
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType().name())
                .eventDate(event.getEventDate())
                .durationMinutes(event.getDurationMinutes())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .createdByNom(event.getCreatedBy() != null ?
                        event.getCreatedBy().getPrenom() + " " + event.getCreatedBy().getNom() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
