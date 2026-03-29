package com.learning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardDTO {

    // ── 人數總覽 ──────────────────────────────────────────────────────
    private Long totalStudents;      // 總學生數
    private Long totalTutors;        // 總老師數（已核准）
    private Long totalCourseTypes;   // 課程種類數（科目代碼不重複）

    // ── 熱門課程排行（前 5） ──────────────────────────────────────────
    private List<PopularCourseDTO> popularCourses;

    // ── 本月新增註冊人數 ──────────────────────────────────────────────
    private Long newStudentsThisMonth;
    private Long newTutorsThisMonth;

    // ── 營收 ──────────────────────────────────────────────────────────
    private Long revenueToday;       // 今日營收（payment_amount 加總）
    private Long revenueThisMonth;   // 本月累積營收

    // ── 內部 DTO：熱門課程一筆 ────────────────────────────────────────
    @Getter
    @AllArgsConstructor
    public static class PopularCourseDTO {
        private Long courseId;
        private String courseName;
        private String tutorName;
        private Integer subject;
        private Long totalLessons;   // 訂購的總堂數（lesson_count 加總）
    }
}
