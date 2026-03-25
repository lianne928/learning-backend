package com.learning.api.service;

import com.learning.api.dto.DashboardDTO;
import com.learning.api.dto.DashboardDTO.PopularCourseDTO;
import com.learning.api.repo.DashboardRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AdminPanelService {

    @Autowired
    private DashboardRepo dashboardRepo;

    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");

    public DashboardDTO getDashboard() {

        ZonedDateTime now        = ZonedDateTime.now(TAIPEI);
        Instant startOfToday     = now.toLocalDate().atStartOfDay(TAIPEI).toInstant();
        Instant startOfTomorrow  = startOfToday.plusSeconds(86400);
        Instant startOfMonth     = now.withDayOfMonth(1)
                                      .toLocalDate().atStartOfDay(TAIPEI).toInstant();
        Instant startOfNextMonth = now.plusMonths(1).withDayOfMonth(1)
                                      .toLocalDate().atStartOfDay(TAIPEI).toInstant();

        // ── 人數總覽 ──────────────────────────────────────────────────
        Long totalStudents    = dashboardRepo.countStudents();
        Long totalTutors      = dashboardRepo.countQualifiedTutors();
        Long totalCourseTypes = dashboardRepo.countCourseTypes();

        // ── 熱門課程排行（Object[] → DTO） ────────────────────────────
        List<PopularCourseDTO> popularCourses = dashboardRepo
                .findTop5PopularCoursesRaw()
                .stream()
                .map(this::toPopularCourseDTO)
                .toList();

        // ── 本月新增註冊 ──────────────────────────────────────────────
        Long newStudentsThisMonth = dashboardRepo.countNewStudentsFrom(startOfMonth);
        Long newTutorsThisMonth   = dashboardRepo.countNewTutorsFrom(startOfMonth);

        // ── 平台營收 ──────────────────────────────────────────────────
        Long revenueToday     = dashboardRepo.sumPlatformRevenue(startOfToday, startOfTomorrow);
        Long revenueThisMonth = dashboardRepo.sumPlatformRevenue(startOfMonth, startOfNextMonth);

        return new DashboardDTO(
                totalStudents,
                totalTutors,
                totalCourseTypes,
                popularCourses,
                newStudentsThisMonth,
                newTutorsThisMonth,
                revenueToday,
                revenueThisMonth
        );
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    /**
     * 原生 SQL 回傳 Object[]，欄位順序對應 SELECT：
     * [0] courseId, [1] courseName, [2] tutorName, [3] subject, [4] totalLessons
     */
    private PopularCourseDTO toPopularCourseDTO(Object[] row) {
        return new PopularCourseDTO(
            ((Number) row[0]).longValue(),    // courseId
            (String)  row[1],                 // courseName
            (String)  row[2],                 // tutorName
            ((Number) row[3]).intValue(),      // subject
            ((Number) row[4]).longValue()      // totalLessons
        );
    }
}
