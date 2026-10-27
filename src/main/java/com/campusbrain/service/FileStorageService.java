package com.campusbrain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt"
    );

    private static final Map<String, String> FORMAT_DESCRIPTIONS = Map.of(
            "pdf", "PDF Document (.pdf)",
            "doc", "Word Document (.doc)",
            "docx", "Word Document (.docx)",
            "ppt", "PowerPoint Presentation (.ppt)",
            "pptx", "PowerPoint Presentation (.pptx)",
            "xls", "Excel Spreadsheet (.xls)",
            "xlsx", "Excel Spreadsheet (.xlsx)",
            "txt", "Text File (.txt)"
    );

    private static final Map<String, String> MIME_TYPES = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "ppt", "application/vnd.ms-powerpoint",
            "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "xls", "application/vnd.ms-excel",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "txt", "text/plain"
    );

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + uploadDir, e);
        }
    }

    public static class StoredFileInfo {
        private final String originalFilename;
        private final String fileExtension;
        private final String fileFormat;
        private final Long fileSize;
        private final String storageFilename;
        private final String contentType;

        public StoredFileInfo(String originalFilename, String fileExtension, String fileFormat, Long fileSize, String storageFilename, String contentType) {
            this.originalFilename = originalFilename;
            this.fileExtension = fileExtension;
            this.fileFormat = fileFormat;
            this.fileSize = fileSize;
            this.storageFilename = storageFilename;
            this.contentType = contentType;
        }

        public String getOriginalFilename() { return originalFilename; }
        public String getFileExtension() { return fileExtension; }
        public String getFileFormat() { return fileFormat; }
        public Long getFileSize() { return fileSize; }
        public String getStorageFilename() { return storageFilename; }
        public String getContentType() { return contentType; }
    }

    public StoredFileInfo storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename.");
        }

        // Clean filename and prevent path traversal
        String cleanedFilename = Paths.get(rawFilename).getFileName().toString();
        if (cleanedFilename.contains("..")) {
            throw new IllegalArgumentException("Path traversal attempt detected in filename.");
        }

        String extension = getExtension(cleanedFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file format: ." + extension + 
                ". Allowed formats: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT.");
        }

        // Generate safe unique storage filename
        String storageFilename = UUID.randomUUID().toString() + "." + extension;
        Path destination = this.rootLocation.resolve(storageFilename).normalize();

        if (!destination.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside target upload directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + cleanedFilename, e);
        }

        String format = FORMAT_DESCRIPTIONS.getOrDefault(extension, extension.toUpperCase() + " File");
        String contentType = MIME_TYPES.getOrDefault(extension, file.getContentType() != null ? file.getContentType() : "application/octet-stream");

        return new StoredFileInfo(
                cleanedFilename,
                extension,
                format,
                file.getSize(),
                storageFilename,
                contentType
        );
    }

    public Resource loadFileAsResource(String storageFilename) {
        try {
            Path filePath = this.rootLocation.resolve(storageFilename).normalize();
            if (!filePath.startsWith(this.rootLocation)) {
                throw new SecurityException("Unauthorized file access path detected.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + storageFilename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file: " + storageFilename, e);
        }
    }

    public void deleteFile(String storageFilename) {
        if (storageFilename == null || storageFilename.trim().isEmpty()) {
            return;
        }
        try {
            Path filePath = this.rootLocation.resolve(storageFilename).normalize();
            if (filePath.startsWith(this.rootLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not delete physical file: " + storageFilename + " - " + e.getMessage());
        }
    }

    public String detectFormatDescription(String filename) {
        String ext = getExtension(filename).toLowerCase();
        return FORMAT_DESCRIPTIONS.getOrDefault(ext, "Unknown File Format");
    }

    public String getMimeType(String extension) {
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
