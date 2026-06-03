package com.elearning.repository;

import com.elearning.model.CourseResource;
import com.elearning.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {

    List<CourseResource> findByCourseId(Long courseId);

    List<CourseResource> findByCourseIdAndResourceType(Long courseId, ResourceType resourceType);

    List<CourseResource> findByUploadedById(Long userId);

    long countByCourseId(Long courseId);
}
