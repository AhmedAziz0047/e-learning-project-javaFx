package com.elearning.service;

import com.elearning.audit.Auditable;
import com.elearning.dto.CourseResourceDTO;
import com.elearning.exception.BadRequestException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.*;
import com.elearning.repository.CourseResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final CourseResourceRepository resourceRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Value("${app.upload.videos-dir}")
    private String videosDir;

    @Value("${app.upload.images-dir}")
    private String imagesDir;

    @Value("${app.upload.documents-dir}")
    private String documentsDir;

    // Types MIME autorisés
    private static final Map<String, ResourceType> MIME_TYPE_MAP = Map.ofEntries(
            Map.entry("application/pdf", ResourceType.DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ResourceType.DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", ResourceType.DOCUMENT),
            Map.entry("image/jpeg", ResourceType.IMAGE),
            Map.entry("image/jpg", ResourceType.IMAGE),
            Map.entry("image/png", ResourceType.IMAGE),
            Map.entry("video/mp4", ResourceType.VIDEO),
            Map.entry("video/x-msvideo", ResourceType.VIDEO),
            Map.entry("video/quicktime", ResourceType.VIDEO)
    );

    /**
     * Upload d'une ressource pédagogique.
     */
    @Auditable(action = "UPLOAD_RESOURCE", entityType = "CourseResource")
    public CourseResourceDTO uploadResource(Long courseId, MultipartFile file) throws IOException {
        Course course = courseService.getCourseEntityById(courseId);
        User uploader = userService.getCurrentUser();

        // Validation du type MIME
        String mimeType = file.getContentType();
        if (mimeType == null || !MIME_TYPE_MAP.containsKey(mimeType)) {
            throw new BadRequestException("Type de fichier non supporté : " + mimeType +
                    ". Types autorisés : PDF, DOCX, PPTX, JPG, PNG, MP4, AVI, MOV");
        }

        ResourceType resourceType = MIME_TYPE_MAP.get(mimeType);
        String targetDir = getDirectoryForType(resourceType);

        // Nom unique du fichier
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedFilename = UUID.randomUUID() + extension;

        // Sauvegarde du fichier
        Path targetPath = Paths.get(targetDir).resolve(storedFilename);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Enregistrement en base
        CourseResource resource = CourseResource.builder()
                .course(course)
                .resourceName(originalFilename)
                .resourceType(resourceType)
                .filePath(targetPath.toString())
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .uploadedBy(uploader)
                .version(1)
                .build();

        resource = resourceRepository.save(resource);
        return toDTO(resource);
    }

    /**
     * Met à jour une ressource (nouvelle version).
     */
    @Auditable(action = "UPLOAD_RESOURCE", entityType = "CourseResource")
    public CourseResourceDTO updateResource(Long resourceId, MultipartFile file) throws IOException {
        CourseResource existing = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));

        // Supprimer l'ancien fichier
        deleteFile(existing.getFilePath());

        // Même logique d'upload
        String mimeType = file.getContentType();
        if (mimeType == null || !MIME_TYPE_MAP.containsKey(mimeType)) {
            throw new BadRequestException("Type de fichier non supporté");
        }

        ResourceType resourceType = MIME_TYPE_MAP.get(mimeType);
        String targetDir = getDirectoryForType(resourceType);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedFilename = UUID.randomUUID() + extension;

        Path targetPath = Paths.get(targetDir).resolve(storedFilename);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        existing.setResourceName(originalFilename);
        existing.setResourceType(resourceType);
        existing.setFilePath(targetPath.toString());
        existing.setFileSize(file.getSize());
        existing.setMimeType(mimeType);
        existing.setVersion(existing.getVersion() + 1);

        existing = resourceRepository.save(existing);
        return toDTO(existing);
    }

    /**
     * Supprime une ressource.
     */
    @Auditable(action = "DELETE_RESOURCE", entityType = "CourseResource")
    public void deleteResource(Long resourceId) {
        CourseResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));

        deleteFile(resource.getFilePath());
        resourceRepository.delete(resource);
    }

    /**
     * Télécharge une ressource (retourne le fichier).
     */
    @Auditable(action = "DOWNLOAD_RESOURCE", entityType = "CourseResource")
    public Resource downloadResource(Long resourceId) {
        CourseResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));

        try {
            Path filePath = Paths.get(resource.getFilePath());
            Resource fileResource = new UrlResource(filePath.toUri());

            if (fileResource.exists() && fileResource.isReadable()) {
                return fileResource;
            } else {
                throw new ResourceNotFoundException("Fichier introuvable sur le serveur");
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Erreur d'accès au fichier");
        }
    }

    /**
     * Liste les ressources d'un cours.
     */
    public List<CourseResourceDTO> getResourcesByCourse(Long courseId) {
        return resourceRepository.findByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une ressource par ID.
     */
    @Auditable(action = "VIEW_RESOURCE", entityType = "CourseResource")
    public CourseResourceDTO getResourceById(Long resourceId) {
        CourseResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée"));
        return toDTO(resource);
    }

    private String getDirectoryForType(ResourceType type) {
        return switch (type) {
            case VIDEO -> videosDir;
            case IMAGE -> imagesDir;
            case DOCUMENT -> documentsDir;
        };
    }

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier : {}", filePath);
        }
    }

    private CourseResourceDTO toDTO(CourseResource resource) {
        return CourseResourceDTO.builder()
                .id(resource.getId())
                .courseId(resource.getCourse().getId())
                .courseTitre(resource.getCourse().getTitre())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType().name())
                .filePath(resource.getFilePath())
                .fileSize(resource.getFileSize())
                .mimeType(resource.getMimeType())
                .uploadedById(resource.getUploadedBy() != null ? resource.getUploadedBy().getId() : null)
                .uploadedByNom(resource.getUploadedBy() != null ?
                        resource.getUploadedBy().getPrenom() + " " + resource.getUploadedBy().getNom() : null)
                .version(resource.getVersion())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
