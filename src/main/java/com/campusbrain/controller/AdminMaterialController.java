package com.campusbrain.controller;

import com.campusbrain.dto.DashboardStatsResponse;
import com.campusbrain.dto.MaterialResponse;
import com.campusbrain.dto.MaterialUpdateRequest;
import com.campusbrain.entity.AcademicMaterial;
import com.campusbrain.entity.MaterialCategory;
import com.campusbrain.service.AcademicMaterialService;
import com.campusbrain.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/materials")
public class AdminMaterialController {

    private final AcademicMaterialService materialService;
    private final FileStorageService fileStorageService;

    public AdminMaterialController(AcademicMaterialService materialService,
                                   FileStorageService fileStorageService) {
        this.materialService = materialService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> getAllMaterials(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String fileFormat,
            @RequestParam(required = false, defaultValue = "false") boolean publishedOnly) {

        List<MaterialResponse> list = materialService.listMaterials(
                search, category, academicYear, semester, subject, fileFormat, publishedOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long id) {
        AcademicMaterial material = materialService.getMaterial(id);
        return ResponseEntity.ok(MaterialResponse.fromEntity(material, ""));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            @RequestParam("subject") String subject,
            @RequestParam("category") String categoryStr,
            @RequestParam(value = "published", defaultValue = "true") boolean published,
            @RequestParam("file") MultipartFile file) {

        MaterialCategory category = MaterialCategory.fromString(categoryStr);
        MaterialResponse response = materialService.uploadMaterial(
                title, description, academicYear, semester, subject, category, published, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponse> updateMaterialDetails(
            @PathVariable Long id,
            @RequestBody MaterialUpdateRequest request) {
        MaterialResponse response = materialService.updateMaterialDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/replace-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> replaceFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        MaterialResponse response = materialService.replaceFile(id, file);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<MaterialResponse> setPublishStatus(
            @PathVariable Long id,
            @RequestParam("published") boolean published) {
        MaterialResponse response = materialService.setPublicationStatus(id, published);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(Map.of("message", "Academic material deleted successfully", "id", id));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(materialService.getDashboardStats());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewMaterialAdmin(@PathVariable Long id) throws IOException {
        AcademicMaterial material = materialService.getMaterial(id);
        Resource resource = materialService.loadResource(material);

        String filename = material.getOriginalFilename();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        String contentType = material.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMaterialAdmin(@PathVariable Long id) throws IOException {
        AcademicMaterial material = materialService.getMaterial(id);
        Resource resource = materialService.loadResource(material);

        String filename = material.getOriginalFilename();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        String contentType = material.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
