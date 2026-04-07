package com.learning.api.service;

import com.learning.api.dto.Admin.AdminTutorReviewDTO;
import com.learning.api.dto.Admin.TutorReviewCountDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminTutorService {

    @Autowired
    private TutorRepo tutorRepo;

    @Autowired
    private UserRepo userRepo;

    /**
     * 取得所有老師（所有狀態）
     */
    public List<AdminTutorReviewDTO> getAllTutors() {
        return tutorRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得待審核老師 (status = 1)
     */
    public List<AdminTutorReviewDTO> getPendingTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(1).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得已核准老師 (status = 2)
     */
    public List<AdminTutorReviewDTO> getQualifiedTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(2).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得停權老師 (status = 3)
     */
    public List<AdminTutorReviewDTO> getSuspendedTutors() {
        return tutorRepo.findByStatusOrderByApplyDateAsc(3).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得單一老師詳細資料
     */
    public AdminTutorReviewDTO getTutorReview(Long tutorId) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該老師"));
        return toDTO(tutor);
    }

    /**
     * 更新老師審核狀態
     *
     * 狀態轉換規則：
     * - 待審核(1) → 核准(2) 或 停權(3)
     * - 已核准(2) → 停權(3)
     * - 停權(3) → 核准(2)
     *
     * ⚠️ 重點：審核通過時（status → 2），同時更新 User.role = TUTOR
     */
    @Transactional
    public Map<String, Object> updateStatus(Long tutorId, Integer newStatus) {
        // 1. 檢查 Tutor 是否存在
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該老師"));

        Integer currentStatus = tutor.getStatus();

        // 2. 驗證狀態轉換是否合法
        validateStatusTransition(currentStatus, newStatus);

        // 3. 更新 Tutor 的 status
        tutor.setStatus(newStatus);
        tutorRepo.save(tutor);

        // 4. ⚠️ 如果審核通過（status → 2），同時更新 User.role
        if (newStatus == 2) {
            User user = tutor.getUser();
            if (user != null && user.getRole() != UserRole.TUTOR) {
                user.setRole(UserRole.TUTOR);
                userRepo.save(user);
            }
        }

        // 5. ⚠️ 如果停權（status → 3），把 User.role 改回 STUDENT
        if (newStatus == 3) {
            User user = tutor.getUser();
            if (user != null) {
                user.setRole(UserRole.STUDENT);
                userRepo.save(user);
            }
        }

        return Map.of(
                "msg", "審核狀態更新成功",
                "tutorId", tutorId,
                "newStatus", newStatus,
                "statusName", getStatusName(newStatus)
        );
    }

    /**
     * 驗證狀態轉換是否合法
     */
    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        // 待審核(1) → 只能轉到 核准(2) 或 停權(3)
        if (currentStatus == 1 && (newStatus != 2 && newStatus != 3)) {
            throw new IllegalArgumentException("待審核狀態只能轉為核准或停權");
        }

        // 已核准(2) → 只能轉到 停權(3)
        if (currentStatus == 2 && newStatus != 3) {
            throw new IllegalArgumentException("已核准狀態只能轉為停權");
        }

        // 停權(3) → 只能轉回 核准(2)
        if (currentStatus == 3 && newStatus != 2) {
            throw new IllegalArgumentException("停權狀態只能轉回核准");
        }

        // 不能轉換成相同狀態
        if (currentStatus.equals(newStatus)) {
            throw new IllegalArgumentException("目前已經是該狀態");
        }
    }

    /**
     * ⚠️ 修正：直接使用 TutorRepo 的 countTutorStatus() 方法
     */
    public TutorReviewCountDTO getCounts() {
        return tutorRepo.countTutorStatus();
    }

    /**
     * 轉換為 DTO
     */
    private AdminTutorReviewDTO toDTO(Tutor tutor) {
        AdminTutorReviewDTO dto = new AdminTutorReviewDTO();
        dto.setTutorId(tutor.getId());
        dto.setName(tutor.getUser().getName());
        dto.setEmail(tutor.getUser().getEmail());
        dto.setTitle(tutor.getTitle());
        dto.setIntro(tutor.getIntro());
        dto.setEducation(tutor.getEducation());
        dto.setExperience1(tutor.getExperience1());
        dto.setExperience2(tutor.getExperience2());
        dto.setCertificateName1(tutor.getCertificateName1());
        dto.setCertificateName2(tutor.getCertificateName2());
        dto.setStatus(tutor.getStatus());
        dto.setApplyDate(tutor.getApplyDate());
        dto.setAvatar(tutor.getAvatar());
        dto.setCertificate1(tutor.getCertificate1());
        dto.setCertificate2(tutor.getCertificate2());
        dto.setVideoUrl1(tutor.getVideoUrl1());
        dto.setVideoUrl2(tutor.getVideoUrl2());
        return dto;
    }

    /**
     * 取得狀態名稱
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待審核";
            case 2 -> "已核准";
            case 3 -> "停權";
            default -> "未知";
        };
    }
}