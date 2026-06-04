package com.elearning.controller;

import com.elearning.model.StudentGroup;
import com.elearning.model.StudyLevel;
import com.elearning.model.User;
import com.elearning.repository.StudentGroupRepository;
import com.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final StudentGroupRepository groupRepository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<Map<String, Object>>> getTeacherGroups() {
        User teacher = userService.getCurrentUser();
        StudyLevel level = teacher.getStudyLevel();
        if (level == null) return ResponseEntity.ok(List.of());

        List<StudentGroup> groups = groupRepository.findByLevel(level);
        List<Map<String, Object>> response = groups.stream().map(g -> 
                Map.<String, Object>of("id", g.getId(), "name", g.getName())
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
