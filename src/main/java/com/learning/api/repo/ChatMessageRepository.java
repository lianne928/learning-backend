package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.learning.api.entity.ChatMessage;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ✅ 原有方法：查詢單一 order 的訊息
    @Query("SELECT c FROM ChatMessage c WHERE c.orderId = :orderId ORDER BY c.createdAt ASC")
    List<ChatMessage> findByBookingIdOrderByCreatedAtAsc(@Param("orderId") Long orderId);

    // 🆕 新增：查詢多個 order 的訊息（合併）
    @Query("SELECT c FROM ChatMessage c WHERE c.orderId IN :orderIds ORDER BY c.createdAt ASC")
    List<ChatMessage> findByOrderIdsOrderByCreatedAtAsc(@Param("orderIds") List<Long> orderIds);

    // 🆕 新增：查詢多個 order 中最新的一則訊息
    @Query(value = "SELECT * FROM chat_messages WHERE order_id IN :orderIds ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<ChatMessage> findLastMessageByOrderIds(@Param("orderIds") List<Long> orderIds);
}