package com.learning.api.repo;

import com.learning.api.dto.DashboardDTO.PopularCourseDTO;
import com.learning.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DashboardRepo extends JpaRepository<User, Long> {

    // ── 人數統計 ──────────────────────────────────────────────────────

    /** 學生總人數 */
    @Query(value = "SELECT COUNT(*) FROM users WHERE role = 1", nativeQuery = true)
    Long countStudents();

    /** 已核准老師總人數 */
    @Query(value = "SELECT COUNT(*) FROM tutors WHERE status = 2", nativeQuery = true)
    Long countQualifiedTutors();

    /** 課程種類數（上架的科目代碼不重複） */
    @Query(value = "SELECT COUNT(DISTINCT subject) FROM courses WHERE is_active = 1", nativeQuery = true)
    Long countCourseTypes();

    // ── 熱門課程排行（前 5，依訂購總堂數降冪） ────────────────────────

    @Query(value = """
        SELECT
            c.id          AS courseId,
            c.name        AS courseName,
            u.name        AS tutorName,
            c.subject     AS subject,
            SUM(o.lesson_count) AS totalLessons
        FROM orders o
        JOIN courses c ON o.course_id = c.id
        JOIN users   u ON c.tutor_id  = u.id
        WHERE o.status IN (2, 3)
        GROUP BY c.id, c.name, u.name, c.subject
        ORDER BY totalLessons DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findTop5PopularCoursesRaw();

    // ── 本月新增註冊人數 ──────────────────────────────────────────────

    /** 本月新增學生數 */
    @Query(value = """
        SELECT COUNT(*) FROM users
        WHERE role = 1
          AND created_at >= :startOfMonth
        """, nativeQuery = true)
    Long countNewStudentsFrom(@Param("startOfMonth") Instant startOfMonth);

    /** 本月新增老師數 */
    @Query(value = """
        SELECT COUNT(*) FROM users
        WHERE role = 2
          AND created_at >= :startOfMonth
        """, nativeQuery = true)
    Long countNewTutorsFrom(@Param("startOfMonth") Instant startOfMonth);

    // ── 平台營收 ──────────────────────────────────────────────────────

    /**
     * 平台營收 = transaction_type=3（booking）且 amount 為負數的絕對值加總
     * 代表每堂課完成後從老師 wallet 扣掉流入平台的金額
     */
    @Query(value = """
        SELECT COALESCE(SUM(o.discount_price - w.amount), 0)
        FROM wallet_logs w
        LEFT JOIN bookings b ON w.related_id = b.id
        LEFT JOIN orders o   ON o.id = b.order_id
        WHERE w.transaction_type = 3
        AND w.amount > 0 and w.amount < 1000
        AND w.created_at >= :from
        AND w.created_at  < :to
        """, nativeQuery = true)
    Long sumPlatformRevenue(
        @Param("from") Instant from,
        @Param("to")   Instant to
    );
}
