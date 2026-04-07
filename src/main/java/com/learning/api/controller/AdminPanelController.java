package com.learning.api.controller;

import com.learning.api.dto.DashboardDTO;
import com.learning.api.service.AdminPanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminPanelController {

    @Autowired
    private AdminPanelService adminPanelService;

    /**
     * 取得儀表板所有統計數據
     * GET /api/admin/dashboard
     */
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminPanelService.getDashboard());
    }
}
