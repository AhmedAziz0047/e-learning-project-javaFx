package com.elearning.service;

import com.elearning.dto.ApiResponse;
import com.elearning.exception.BadRequestException;
import com.elearning.model.StudyLevel;
import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final UserService userService;
    private final UserRepository userRepository;

    public ApiResponse submitExam(double score) {
        User student = userService.getCurrentUser();
        
        if (student.getStudyLevel() == StudyLevel.GRADUATED) {
            return ApiResponse.success("Vous êtes déjà diplômé !");
        }

        if (score >= 75.0) {
            StudyLevel currentLevel = student.getStudyLevel() != null ? student.getStudyLevel() : StudyLevel.LEVEL_1;
            StudyLevel nextLevel;

            switch (currentLevel) {
                case LEVEL_1:
                    nextLevel = StudyLevel.LEVEL_2;
                    break;
                case LEVEL_2:
                    nextLevel = StudyLevel.LEVEL_3;
                    break;
                case LEVEL_3:
                    nextLevel = StudyLevel.GRADUATED;
                    break;
                default:
                    nextLevel = StudyLevel.GRADUATED;
            }

            student.setStudyLevel(nextLevel);
            if (nextLevel != StudyLevel.GRADUATED) {
                userService.assignToGroup(student, nextLevel);
            } else {
                student.setStudentGroup(null);
            }
            
            userRepository.save(student);
            return ApiResponse.success("Félicitations ! Vous passez au niveau : " + nextLevel.name());
        }

        return ApiResponse.success("Désolé, votre score (" + score + "%) est insuffisant pour passer. Il faut 75%.");
    }
}
