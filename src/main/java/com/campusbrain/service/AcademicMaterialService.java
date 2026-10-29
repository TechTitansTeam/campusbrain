package com.campusbrain.service;

import com.campusbrain.dto.DashboardStatsResponse;
import com.campusbrain.dto.MaterialResponse;
import com.campusbrain.dto.MaterialUpdateRequest;
import com.campusbrain.entity.AcademicMaterial;
import com.campusbrain.entity.MaterialCategory;
import com.campusbrain.repository.AcademicMaterialRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcademicMaterialService {

    private final AcademicMaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final SseBroadcastService sseBroadcastService;

    public AcademicMaterialService(AcademicMaterialRepository materialRepository,
                                   FileStorageService fileStorageService,
                                   SseBroadcastService sseBroadcastService) {
        this.materialRepository = materialRepository;
        this.fileStorageService = fileStorageService;
        this.sseBroadcastService = sseBroadcastService;
    }

    @Transactional
    public MaterialResponse uploadMaterial(String title, String description, String academicYear,
                                           String semester, String subject, MaterialCategory category,
                                           boolean published, MultipartFile file) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Material Title is required.");
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year is required.");
        }
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester is required.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Material Category is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File selection is required.");
        }

        // Store file physically
        FileStorageService.StoredFileInfo fileInfo = fileStorageService.storeFile(file);

        try {
            AcademicMaterial material = new AcademicMaterial();
            material.setTitle(title.trim());
            material.setDescription(description != null ? description.trim() : "");
            material.setAcademicYear(academicYear.trim());
            material.setSemester(semester.trim());
            material.setSubject(subject.trim());
            material.setCategory(category);
            material.setOriginalFilename(fileInfo.getOriginalFilename());
            material.setFileExtension(fileInfo.getFileExtension());
            material.setFileFormat(fileInfo.getFileFormat());
            material.setFileSize(fileInfo.getFileSize());
            material.setStorageFilename(fileInfo.getStorageFilename());
            material.setContentType(fileInfo.getContentType());
            material.setFileData(file.getBytes());
            material.setPublished(published);

            AcademicMaterial saved = materialRepository.save(material);

            MaterialResponse response = MaterialResponse.fromEntity(saved, "");

            // Broadcast SSE event
            if (saved.isPublished()) {
                sseBroadcastService.broadcast("MATERIAL_PUBLISHED", saved.getId(), saved.getCategory().name(),
                        "New material published: " + saved.getTitle(), response);
            } else {
                sseBroadcastService.broadcast("MATERIAL_CREATED", saved.getId(), saved.getCategory().name(),
                        "Material created as draft: " + saved.getTitle(), response);
            }

            return response;
        } catch (Exception e) {
            // Rollback stored file if DB save fails
            fileStorageService.deleteFile(fileInfo.getStorageFilename());
            throw new RuntimeException("Could not save academic material: " + e.getMessage(), e);
        }
    }

    @Transactional
    public MaterialResponse updateMaterialDetails(Long id, MaterialUpdateRequest request) {
        AcademicMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + id));

        boolean wasPublished = material.isPublished();

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            material.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            material.setDescription(request.getDescription().trim());
        }
        if (request.getAcademicYear() != null && !request.getAcademicYear().trim().isEmpty()) {
            material.setAcademicYear(request.getAcademicYear().trim());
        }
        if (request.getSemester() != null && !request.getSemester().trim().isEmpty()) {
            material.setSemester(request.getSemester().trim());
        }
        if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
            material.setSubject(request.getSubject().trim());
        }
        if (request.getCategory() != null) {
            material.setCategory(request.getCategory());
        }
        if (request.getPublished() != null) {
            material.setPublished(request.getPublished());
        }

        AcademicMaterial updated = materialRepository.save(material);
        MaterialResponse response = MaterialResponse.fromEntity(updated, "");

        if (!wasPublished && updated.isPublished()) {
            sseBroadcastService.broadcast("MATERIAL_PUBLISHED", updated.getId(), updated.getCategory().name(),
                    "Material published: " + updated.getTitle(), response);
        } else if (wasPublished && !updated.isPublished()) {
            sseBroadcastService.broadcast("MATERIAL_UNPUBLISHED", updated.getId(), updated.getCategory().name(),
                    "Material unpublished: " + updated.getTitle(), response);
        } else {
            sseBroadcastService.broadcast("MATERIAL_UPDATED", updated.getId(), updated.getCategory().name(),
                    "Material updated: " + updated.getTitle(), response);
        }

        return response;
    }

    @Transactional
    public MaterialResponse replaceFile(Long id, MultipartFile newFile) {
        AcademicMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + id));

        if (newFile == null || newFile.isEmpty()) {
            throw new IllegalArgumentException("Replacement file cannot be empty.");
        }

        String oldStorageFilename = material.getStorageFilename();

        // Store new file first
        FileStorageService.StoredFileInfo newFileInfo = fileStorageService.storeFile(newFile);

        try {
            material.setOriginalFilename(newFileInfo.getOriginalFilename());
            material.setFileExtension(newFileInfo.getFileExtension());
            material.setFileFormat(newFileInfo.getFileFormat());
            material.setFileSize(newFileInfo.getFileSize());
            material.setStorageFilename(newFileInfo.getStorageFilename());
            material.setContentType(newFileInfo.getContentType());
            material.setFileData(newFile.getBytes());

            AcademicMaterial updated = materialRepository.save(material);

            // Delete old file upon successful update
            fileStorageService.deleteFile(oldStorageFilename);

            MaterialResponse response = MaterialResponse.fromEntity(updated, "");

            sseBroadcastService.broadcast("MATERIAL_UPDATED", updated.getId(), updated.getCategory().name(),
                    "File replaced for material: " + updated.getTitle(), response);

            return response;
        } catch (Exception e) {
            // Delete newly uploaded file if database update fails
            fileStorageService.deleteFile(newFileInfo.getStorageFilename());
            throw new RuntimeException("Could not replace material file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public MaterialResponse setPublicationStatus(Long id, boolean published) {
        AcademicMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + id));

        material.setPublished(published);
        AcademicMaterial updated = materialRepository.save(material);
        MaterialResponse response = MaterialResponse.fromEntity(updated, "");

        String eventType = published ? "MATERIAL_PUBLISHED" : "MATERIAL_UNPUBLISHED";
        String message = published ? "Material published: " + updated.getTitle() : "Material unpublished: " + updated.getTitle();
        sseBroadcastService.broadcast(eventType, updated.getId(), updated.getCategory().name(), message, response);

        return response;
    }

    @Transactional
    public void deleteMaterial(Long id) {
        AcademicMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + id));

        String storageFilename = material.getStorageFilename();
        String title = material.getTitle();
        String category = material.getCategory().name();

        materialRepository.delete(material);
        fileStorageService.deleteFile(storageFilename);

        sseBroadcastService.broadcast("MATERIAL_DELETED", id, category, "Material deleted: " + title, Map.of("id", id));
    }

    public AcademicMaterial getMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Academic material not found with id: " + id));
    }

    public AcademicMaterial getPublishedMaterial(Long id) {
        AcademicMaterial material = getMaterial(id);
        if (!material.isPublished()) {
            throw new NoSuchElementException("Academic material is not available or has been unpublished.");
        }
        return material;
    }

    public org.springframework.core.io.Resource loadResource(AcademicMaterial material) throws java.io.IOException {
        if (material.getFileData() != null && material.getFileData().length > 0) {
            return new org.springframework.core.io.ByteArrayResource(material.getFileData());
        }
        return fileStorageService.loadFileAsResource(material.getStorageFilename());
    }

    public List<MaterialResponse> listMaterials(String search, String categoryStr, String academicYear,
                                                String semester, String subject, String fileFormat,
                                                boolean publishedOnly) {
        MaterialCategory category = null;
        if (categoryStr != null && !categoryStr.trim().isEmpty() && !categoryStr.equalsIgnoreCase("ALL")) {
            try {
                category = MaterialCategory.fromString(categoryStr);
            } catch (Exception ignored) {}
        }

        final MaterialCategory finalCategory = category;

        Specification<AcademicMaterial> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (publishedOnly) {
                predicates.add(cb.isTrue(root.get("published")));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate subjectMatch = cb.like(cb.lower(root.get("subject")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch, subjectMatch));
            }

            if (finalCategory != null) {
                predicates.add(cb.equal(root.get("category"), finalCategory));
            }

            if (academicYear != null && !academicYear.trim().isEmpty() && !academicYear.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(root.get("academicYear"), academicYear.trim()));
            }

            if (semester != null && !semester.trim().isEmpty() && !semester.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(root.get("semester"), semester.trim()));
            }

            if (subject != null && !subject.trim().isEmpty() && !subject.equalsIgnoreCase("ALL")) {
                String subjectPattern = "%" + subject.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("subject")), subjectPattern));
            }

            if (fileFormat != null && !fileFormat.trim().isEmpty() && !fileFormat.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(cb.lower(root.get("fileExtension")), fileFormat.trim().toLowerCase()));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return materialRepository.findAll(spec).stream()
                .map(m -> MaterialResponse.fromEntity(m, ""))
                .collect(Collectors.toList());
    }

    public DashboardStatsResponse getDashboardStats() {
        long totalMaterials = materialRepository.count();
        long totalNotes = materialRepository.countByCategory(MaterialCategory.NOTES);
        long totalLabManuals = materialRepository.countByCategory(MaterialCategory.LAB_MANUAL);
        long totalAssignments = materialRepository.countByCategory(MaterialCategory.ASSIGNMENT);
        long totalQuestionPapers = materialRepository.countByCategory(MaterialCategory.PREVIOUS_QUESTION_PAPER);
        long totalPublished = materialRepository.countByPublishedTrue();
        long totalUnpublished = materialRepository.countByPublishedFalse();

        return new DashboardStatsResponse(
                totalMaterials,
                totalNotes,
                totalLabManuals,
                totalAssignments,
                totalQuestionPapers,
                totalPublished,
                totalUnpublished
        );
    }

    public Map<String, Object> getCategoryCounts() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("NOTES", materialRepository.countByCategoryAndPublishedTrue(MaterialCategory.NOTES));
        counts.put("LAB_MANUAL", materialRepository.countByCategoryAndPublishedTrue(MaterialCategory.LAB_MANUAL));
        counts.put("ASSIGNMENT", materialRepository.countByCategoryAndPublishedTrue(MaterialCategory.ASSIGNMENT));
        counts.put("PREVIOUS_QUESTION_PAPER", materialRepository.countByCategoryAndPublishedTrue(MaterialCategory.PREVIOUS_QUESTION_PAPER));
        counts.put("ALL", materialRepository.countByPublishedTrue());
        return counts;
    }

    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("subjects", materialRepository.findDistinctSubjectsByPublishedTrue());
        filters.put("academicYears", materialRepository.findDistinctAcademicYearsByPublishedTrue());
        filters.put("semesters", materialRepository.findDistinctSemestersByPublishedTrue());
        filters.put("fileExtensions", materialRepository.findDistinctFileExtensionsByPublishedTrue());
        return filters;
    }
}
