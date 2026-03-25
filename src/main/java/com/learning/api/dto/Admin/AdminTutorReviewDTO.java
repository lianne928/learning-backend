package com.learning.api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AdminTutorReviewDTO {

    // ── User 基本資料 ──────────────────────────────────────────────────
    private Long id;
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

    // 影片
    private String videoUrl1;
    private String videoUrl2;
}
