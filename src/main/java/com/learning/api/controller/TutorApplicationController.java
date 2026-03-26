package com.learning.api.controller;

import com.learning.api.dto.auth.BecomeTutorReq;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.TutorApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tutor")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorApplicationController {

    @Autowired
    private TutorApplicationService tutorApplicationService;

    /**
     * 申請成為老師
     * POST /api/tutor/become
     */
    @PostMapping("/become")
    public ResponseEntity<?> becomeTutor(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody BecomeTutorReq req) {

        try {
            Long userId = me.getUser().getId();
            tutorApplicationService.becomeTutor(userId, req);

            return ResponseEntity.ok(Map.of("msg", "申請已提交，請等待審核"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("msg", "申請失敗：" + e.getMessage()));
        }
    }

    /**
     * 查詢自己的申請狀態
     * GET /api/tutor/application/status
     */
    @GetMapping("/application/status")
    public ResponseEntity<?> getApplicationStatus(@AuthenticationPrincipal SecurityUser me) {
        try {
            Long userId = me.getUser().getId();
            Integer status = tutorApplicationService.getApplicationStatus(userId);

            if (status == null) {
                // 沒有申請記錄
                return ResponseEntity.status(404).body(Map.of("msg", "尚未申請"));
            }

            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("msg", "查詢失敗：" + e.getMessage()));
        }
    }
}