package com.elearning.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration du stockage des fichiers — crée les dossiers d'upload au démarrage.
 */
@Configuration
public class FileStorageConfig {

    @Value("${app.upload.videos-dir}")
    private String videosDir;

    @Value("${app.upload.images-dir}")
    private String imagesDir;

    @Value("${app.upload.documents-dir}")
    private String documentsDir;

    @PostConstruct
    public void init() throws IOException {
        createDirectoryIfNotExists(Paths.get(videosDir));
        createDirectoryIfNotExists(Paths.get(imagesDir));
        createDirectoryIfNotExists(Paths.get(documentsDir));
    }

    private void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
