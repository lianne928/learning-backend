package com.learning.api.controller;

import com.learning.api.dto.Admin.AdminTutorReviewDTO;
import com.learning.api.dto.Admin.AdminTutorReviewReq;
import com.learning.api.dto.Admin.TutorReviewCountDTO;
import com.learning.api.service.AdminTutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tutors")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminTutorController {

    @Autowired
    private AdminTutorService adminTutorService;

    /**
     * 全部老師
     * GET /api/admin/tutors
     */
    @GetMapping
    public ResponseEntity<List<AdminTutorReviewDTO>> getAllTutors() {
        return ResponseEntity.ok(adminTutorService.getAllTutors());
    }

    /**
     * 待審核（status = 1）
     * GET /api/admin/tutors/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AdminTutorReviewDTO>> getPendingTutors() {
        return ResponseEntity.ok(adminTutorService.getPendingTutors());
    }

    /**
     * 已核准（status = 2）
     * GET /api/admin/tutors/qualified
     */
    @GetMapping("/qualified")
    public ResponseEntity<List<AdminTutorReviewDTO>> getQualifiedTutors() {
        return ResponseEntity.ok(adminTutorService.getQualifiedTutors());
    }

    /**
     * 停權（status = 3）
     * GET /api/admin/tutors/suspended
     */
    @GetMapping("/suspended")
    public ResponseEntity<List<AdminTutorReviewDTO>> getSuspendedTutors() {
        return ResponseEntity.ok(adminTutorService.getSuspendedTutors());
    }

    /**
     * 單一老師詳細資料
     * GET /api/admin/tutors/{tutorId}
     */
    @GetMapping("/{tutorId}")
    public ResponseEntity<AdminTutorReviewDTO> getTutorReview(@PathVariable Long tutorId) {
        return ResponseEntity.ok(adminTutorService.getTutorReview(tutorId));
    }

    /**
     * 執行審核動作
     * PATCH /api/admin/tutors/{tutorId}/status
     * Body: { "status": 2 }
     *
     * 允許的轉換：
     *   待審核(1) → 核准(2) 或 停權(3)
     *   已核准(2) → 停權(3)
     *   停權(3)   → 核准(2)
     */
    @PatchMapping("/{tutorId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long tutorId,
            @RequestBody AdminTutorReviewReq req) {
        try {
            return ResponseEntity.ok(adminTutorService.updateStatus(tutorId, req.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 獲取各狀態老師的數量統計
     * GET /api/admin/tutors/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<TutorReviewCountDTO> getCounts() {
        TutorReviewCountDTO counts = adminTutorService.getCounts();
        return ResponseEntity.ok(counts);
    }

}
