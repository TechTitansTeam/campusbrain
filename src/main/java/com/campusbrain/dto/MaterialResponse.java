package com.campusbrain.dto;

import com.campusbrain.entity.AcademicMaterial;
import com.campusbrain.entity.MaterialCategory;
import java.time.format.DateTimeFormatter;

public class MaterialResponse {
    private Long id;
    private String title;
    private String description;
    private String academicYear;
    private String semester;
    private String subject;
    private MaterialCategory category;
    private String categoryDisplayName;
    private String originalFilename;
    private String fileExtension;
    private String fileFormat;
    private Long fileSize;
    private String formattedFileSize;
    private boolean published;
    private String createdAt;
    private String updatedAt;
    private String viewUrl;
    private String downloadUrl;

    public MaterialResponse() {}

    public static MaterialResponse fromEntity(AcademicMaterial entity, String baseUrl) {
        MaterialResponse dto = new MaterialResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setSemester(entity.getSemester());
        dto.setSubject(entity.getSubject());
        dto.setCategory(entity.getCategory());
        dto.setCategoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : "");
        dto.setOriginalFilename(entity.getOriginalFilename());
        dto.setFileExtension(entity.getFileExtension());
        dto.setFileFormat(entity.getFileFormat());
        dto.setFileSize(entity.getFileSize());
        dto.setFormattedFileSize(formatFileSize(entity.getFileSize()));
        dto.setPublished(entity.isPublished());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(formatter));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(formatter));
        }

        String base = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : "";
        dto.setViewUrl(base + "/api/materials/" + entity.getId() + "/view");
        dto.setDownloadUrl(base + "/api/materials/" + entity.getId() + "/download");

        return dto;
    }

    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MaterialCategory getCategory() {
        return category;
    }

    public void setCategory(MaterialCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormattedFileSize() {
        return formattedFileSize;
    }

    public void setFormattedFileSize(String formattedFileSize) {
        this.formattedFileSize = formattedFileSize;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
