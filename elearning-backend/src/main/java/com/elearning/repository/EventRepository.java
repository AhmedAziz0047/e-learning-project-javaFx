package com.elearning.repository;

import com.elearning.model.Event;
import com.elearning.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCourseId(Long courseId);

    List<Event> findByEventType(EventType eventType);

    @Query("SELECT e FROM Event e WHERE e.eventDate BETWEEN :start AND :end ORDER BY e.eventDate ASC")
    List<Event> findEventsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Event e WHERE e.eventDate >= :now ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.course.id IN :courseIds AND e.eventDate BETWEEN :start AND :end ORDER BY e.eventDate ASC")
    List<Event> findEventsByCourseIdsAndDateBetween(
            @Param("courseIds") List<Long> courseIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Event> findByCreatedById(Long userId);
}
