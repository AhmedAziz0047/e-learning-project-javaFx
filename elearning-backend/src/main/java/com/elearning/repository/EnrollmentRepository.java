package com.elearning.repository;

import com.elearning.model.Enrollment;
import com.elearning.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentIdAndStatut(Long studentId, EnrollmentStatus statut);

    long countByCourseId(Long courseId);

    @Query("SELECT AVG(e.progression) FROM Enrollment e WHERE e.course.id = :courseId")
    Double getAverageProgressionByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.enseignant.id = :teacherId")
    long countStudentsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT AVG(e.progression) FROM Enrollment e WHERE e.course.enseignant.id = :teacherId")
    Double getAverageProgressionByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.enseignant.id = :teacherId AND e.statut = 'COMPLETED'")
    long countCompletedByTeacherId(@Param("teacherId") Long teacherId);
}
