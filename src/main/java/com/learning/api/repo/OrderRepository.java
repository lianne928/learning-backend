// ==========================================
// OrderRepository.java 需要新增的方法
// 加到你現有的 OrderRepository.java 裡面
// ==========================================

package com.learning.api.repo;

import com.learning.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ... 你原有的方法 ...

    // 🆕 新增：查詢學生的所有訂單
    @Query("SELECT o FROM Order o WHERE o.userId = :studentId")
    List<Order> findByUserId(@Param("studentId") Long studentId);

    // 🆕 新增：查詢老師的所有訂單（透過 bookings 表關聯）
    @Query(value = "SELECT DISTINCT o.* FROM orders o " +
            "INNER JOIN bookings b ON b.order_id = o.id " +
            "WHERE b.tutor_id = :tutorId",
            nativeQuery = true)
    List<Order> findByTutorId(@Param("tutorId") Long tutorId);

    // 檢查學生是否已購買過體驗課
    boolean existsByUserIdAndIsExperiencedTrue(Long userId);
}