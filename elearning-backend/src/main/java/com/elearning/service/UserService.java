package com.elearning.service;

import com.elearning.dto.*;
import com.elearning.exception.BadRequestException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Role;
import com.elearning.model.StudyLevel;
import com.elearning.model.StudentGroup;
import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import com.elearning.repository.StudentGroupRepository;
import com.elearning.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentGroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Inscription d'un nouvel utilisateur.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Un compte avec cet email existe déjà");
        }

        Role role = Role.ROLE_STUDENT;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rôle invalide : " + request.getRole());
            }
        }

        StudyLevel studyLevel = null;
        if (role == Role.ROLE_TEACHER && request.getStudyLevel() != null) {
            try {
                studyLevel = StudyLevel.valueOf(request.getStudyLevel());
            } catch (IllegalArgumentException ignored) {}
        } else if (role == Role.ROLE_STUDENT) {
            studyLevel = StudyLevel.LEVEL_1; // Par défaut
        }

        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(role)
                .studyLevel(studyLevel)
                .actif(true)
                .build();

        if (role == Role.ROLE_STUDENT) {
            assignToGroup(user, studyLevel);
        }

        user = userRepository.save(user);

        String token = jwtUtil.generateTokenFromEmail(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .studyLevel(user.getStudyLevel() != null ? user.getStudyLevel().name() : null)
                .groupId(user.getStudentGroup() != null ? user.getStudentGroup().getId() : null)
                .groupName(user.getStudentGroup() != null ? user.getStudentGroup().getName() : null)
                .build();
    }

    public void assignToGroup(User student, StudyLevel level) {
        java.util.List<StudentGroup> groups = groupRepository.findByLevel(level);
        StudentGroup targetGroup = null;

        for (StudentGroup group : groups) {
            if (group.getStudents().size() < 15) {
                targetGroup = group;
                break;
            }
        }

        if (targetGroup == null) {
            int nextNumber = groups.size() + 1;
            targetGroup = StudentGroup.builder()
                    .name("Niveau " + (level.ordinal() + 1) + " - Groupe " + nextNumber)
                    .level(level)
                    .build();
            targetGroup = groupRepository.save(targetGroup);
        }

        student.setStudentGroup(targetGroup);
    }

    /**
     * Connexion d'un utilisateur.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getMotDePasse()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .studyLevel(user.getStudyLevel() != null ? user.getStudyLevel().name() : null)
                .groupId(user.getStudentGroup() != null ? user.getStudentGroup().getId() : null)
                .groupName(user.getStudentGroup() != null ? user.getStudentGroup().getName() : null)
                .build();
    }

    /**
     * Récupère l'utilisateur connecté.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    /**
     * Récupère un utilisateur par ID.
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
        return toDTO(user);
    }

    /**
     * Liste tous les utilisateurs.
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Liste les utilisateurs par rôle.
     */
    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un utilisateur.
     */
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        user.setActif(dto.isActif());

        user = userRepository.save(user);
        return toDTO(user);
    }

    /**
     * Désactive un utilisateur.
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setActif(false);
        userRepository.save(user);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatar(user.getAvatar())
                .actif(user.isActif())
                .studyLevel(user.getStudyLevel() != null ? user.getStudyLevel().name() : null)
                .groupId(user.getStudentGroup() != null ? user.getStudentGroup().getId() : null)
                .groupName(user.getStudentGroup() != null ? user.getStudentGroup().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
