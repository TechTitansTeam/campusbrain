package com.campusbrain.controller;

import com.campusbrain.dto.DashboardStatsResponse;
import com.campusbrain.service.AcademicMaterialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminStatsController {

    private final AcademicMaterialService materialService;

    public AdminStatsController(AcademicMaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(materialService.getDashboardStats());
    }
}
