package com.elearning.repository;

import com.elearning.model.LiveSession;
import com.elearning.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    List<LiveSession> findByCourseId(Long courseId);

    List<LiveSession> findByTeacherId(Long teacherId);

    List<LiveSession> findByStatus(SessionStatus status);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'PLANNED' AND ls.startTime >= :now ORDER BY ls.startTime ASC")
    List<LiveSession> findUpcomingSessions(@Param("now") LocalDateTime now);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'LIVE'")
    List<LiveSession> findLiveSessions();

    @Query("SELECT COUNT(ls) FROM LiveSession ls WHERE ls.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.course.id IN :courseIds AND ls.status IN ('PLANNED', 'LIVE') ORDER BY ls.startTime ASC")
    List<LiveSession> findActiveSessionsByCourseIds(@Param("courseIds") List<Long> courseIds);
}
