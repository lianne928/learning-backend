package com.learning.api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data  // ← 改用 @Data，包含 Getter 和 Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminTutorReviewDTO {

    // ── User 基本資料 ──────────────────────────────────────────────────
    private Long tutorId;  // ← 注意：AdminTutorService 使用 tutorId
    private String name;
    private String email;

    // ── Tutor 申請資料 ────────────────────────────────────────────────
    private LocalDate applyDate;
    private Integer status;      // 1=pending 2=qualified 3=停權
    private String avatar;
    private String title;
    private String intro;

    // 教學經歷
    private String experience1;
    private String experience2;

    // 證照
    private String certificate1;
    private String certificateName1;
    private String certificate2;
    private String certificateName2;

    // 學歷
    private String education;

    // 影片
    private String videoUrl1;
    private String videoUrl2;
}