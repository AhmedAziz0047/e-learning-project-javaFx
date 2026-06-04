package com.elearning.repository;

import com.elearning.model.StudentGroup;
import com.elearning.model.StudyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByLevel(StudyLevel level);
}
