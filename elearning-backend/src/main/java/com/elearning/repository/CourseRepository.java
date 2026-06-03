package com.elearning.repository;

import com.elearning.model.Course;
import com.elearning.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByEnseignant(User enseignant);

    List<Course> findByEnseignantId(Long enseignantId);

    List<Course> findByActifTrue();

    Page<Course> findByActifTrue(Pageable pageable);

    List<Course> findByCategorie(String categorie);

    List<Course> findByNiveau(String niveau);

    @Query("SELECT c FROM Course c WHERE c.actif = true AND " +
           "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Course> searchCourses(@Param("search") String search);

    long countByEnseignantId(Long enseignantId);
}
