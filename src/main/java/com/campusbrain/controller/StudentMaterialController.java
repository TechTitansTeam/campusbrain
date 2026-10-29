package com.campusbrain.controller;

import com.campusbrain.dto.MaterialResponse;
import com.campusbrain.entity.AcademicMaterial;
import com.campusbrain.service.AcademicMaterialService;
import com.campusbrain.service.FileStorageService;
import com.campusbrain.service.SseBroadcastService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
public class StudentMaterialController {

    private final AcademicMaterialService materialService;
    private final FileStorageService fileStorageService;
    private final SseBroadcastService sseBroadcastService;

    public StudentMaterialController(AcademicMaterialService materialService,
                                     FileStorageService fileStorageService,
                                     SseBroadcastService sseBroadcastService) {
        this.materialService = materialService;
        this.fileStorageService = fileStorageService;
        this.sseBroadcastService = sseBroadcastService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> getMaterials(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String fileFormat) {

        List<MaterialResponse> list = materialService.listMaterials(
                search, category, academicYear, semester, subject, fileFormat, true);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long id) {
        AcademicMaterial material = materialService.getPublishedMaterial(id);
        return ResponseEntity.ok(MaterialResponse.fromEntity(material, ""));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewMaterial(@PathVariable Long id) throws IOException {
        AcademicMaterial material = materialService.getPublishedMaterial(id);
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
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) throws IOException {
        AcademicMaterial material = materialService.getPublishedMaterial(id);
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

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategoryCounts() {
        return ResponseEntity.ok(materialService.getCategoryCounts());
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<String>>> getFilterOptions() {
        return ResponseEntity.ok(materialService.getFilterOptions());
    }

    @GetMapping(value = "/sync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToSyncStream() {
        return sseBroadcastService.createConnection();
    }
}
