package com.campusbrain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_materials", indexes = {
    @Index(name = "idx_category_published", columnList = "category, published"),
    @Index(name = "idx_subject", columnList = "subject"),
    @Index(name = "idx_year_semester", columnList = "academic_year, semester")
})
public class AcademicMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear;

    @Column(nullable = false, length = 50)
    private String semester;

    @Column(nullable = false, length = 100)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MaterialCategory category;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "file_format", nullable = false, length = 100)
    private String fileFormat;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_filename", nullable = false, length = 255, unique = true)
    private String storageFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AcademicMaterial() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getStorageFilename() {
        return storageFilename;
    }

    public void setStorageFilename(String storageFilename) {
        this.storageFilename = storageFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
