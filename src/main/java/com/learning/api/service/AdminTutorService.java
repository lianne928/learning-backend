package com.learning.api.service;

import com.learning.api.dto.Admin.AdminTutorReviewDTO;
import com.learning.api.dto.Admin.TutorReviewCountDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.repo.TutorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminTutorService {

    @Autowired
    private TutorRepo tutorRepo;

    // ── 查：全部老師 ──────────────────────────────────────────────────
    public List<AdminTutorReviewDTO> getAllTutors() {
        return tutorRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：待審核（status = 1） ──────────────────────────────────────
    public List<AdminTutorReviewDTO> getPendingTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(1)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：已核准（status = 2） ──────────────────────────────────────
    public List<AdminTutorReviewDTO> getQualifiedTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(2)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：停權（status = 3） ────────────────────────────────────────
    public List<AdminTutorReviewDTO> getSuspendedTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(3)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：單一老師詳細資料 ──────────────────────────────────────────
    public AdminTutorReviewDTO getTutorReview(Long tutorId) {
        Tutor tutor = findOrThrow(tutorId);
        return toDTO(tutor);
    }

    // ── 修：審核動作 ──────────────────────────────────────────────────
    /**
     * 允許的狀態轉換：
     *   待審核(1) → 核准(2) 或 停權(3)
     *   已核准(2) → 停權(3)
     *   停權(3)   → 核准(2)
     */
    @Transactional
    public AdminTutorReviewDTO updateStatus(Long tutorId, Integer newStatus) {
        Tutor tutor = findOrThrow(tutorId);
        Integer currentStatus = tutor.getStatus();

        validateTransition(currentStatus, newStatus);

        tutor.setStatus(newStatus);
        tutorRepo.save(tutor);
        return toDTO(tutor);
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    private Tutor findOrThrow(Long tutorId) {
        return tutorRepo.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("找不到老師 id=" + tutorId));
    }

    /**
     * 驗證狀態轉換是否合法
     * 1=待審核 / 2=已核准 / 3=停權
     */
    private void validateTransition(Integer current, Integer next) {
        if (next == null) {
            throw new IllegalArgumentException("狀態不可為空");
        }
        boolean valid = switch (current) {
            case 1 -> next == 2 || next == 3; // 待審核 → 核准 or 停權
            case 2 -> next == 3;              // 已核准 → 停權
            case 3 -> next == 2;              // 停權   → 重新核准
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                "不允許的狀態轉換: " + statusLabel(current) + " → " + statusLabel(next)
            );
        }
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待審核";
            case 2 -> "已核准";
            case 3 -> "停權";
            default -> "未知(" + status + ")";
        };
    }

    private AdminTutorReviewDTO toDTO(Tutor tutor) {
        return new AdminTutorReviewDTO(
            tutor.getId(),
            tutor.getUser().getName(),
            tutor.getUser().getEmail(),
            tutor.getApplyDate(),
            tutor.getStatus(),
            tutor.getAvatar(),
            tutor.getTitle(),
            tutor.getIntro(),
            tutor.getExperience1(),
            tutor.getExperience2(),
            tutor.getCertificate1(),
            tutor.getCertificateName1(),
            tutor.getCertificate2(),
            tutor.getCertificateName2(),
            tutor.getEducation(),
            tutor.getVideoUrl1(),
            tutor.getVideoUrl2()
        );
    }

    // AdminTutorService.java 建議微調
    public TutorReviewCountDTO getCounts() {
        return tutorRepo.countTutorStatus();
    
    }
}
