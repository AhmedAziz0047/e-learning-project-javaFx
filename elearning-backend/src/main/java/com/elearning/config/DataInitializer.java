package com.elearning.config;

import com.elearning.model.Role;
import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise la base de données avec un administrateur par défaut au premier lancement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer l'admin par défaut s'il n'existe pas
        if (!userRepository.existsByEmail("admin@elearning.com")) {
            User admin = User.builder()
                    .nom("Admin")
                    .prenom("Super")
                    .email("admin@elearning.com")
                    .motDePasse(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .actif(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Administrateur par défaut créé : admin@elearning.com / admin123");
        }

        // Créer un enseignant de test s'il n'existe pas
        if (!userRepository.existsByEmail("enseignant@elearning.com")) {
            User teacher = User.builder()
                    .nom("Dupont")
                    .prenom("Jean")
                    .email("enseignant@elearning.com")
                    .motDePasse(passwordEncoder.encode("teacher123"))
                    .role(Role.ROLE_TEACHER)
                    .actif(true)
                    .build();
            userRepository.save(teacher);
            log.info("✅ Enseignant de test créé : enseignant@elearning.com / teacher123");
        }

        // Créer un étudiant de test s'il n'existe pas
        if (!userRepository.existsByEmail("etudiant@elearning.com")) {
            User student = User.builder()
                    .nom("Martin")
                    .prenom("Marie")
                    .email("etudiant@elearning.com")
                    .motDePasse(passwordEncoder.encode("student123"))
                    .role(Role.ROLE_STUDENT)
                    .actif(true)
                    .build();
            userRepository.save(student);
            log.info("✅ Étudiant de test créé : etudiant@elearning.com / student123");
        }

        log.info("🎓 Plateforme E-Learning prête !");
    }
}
