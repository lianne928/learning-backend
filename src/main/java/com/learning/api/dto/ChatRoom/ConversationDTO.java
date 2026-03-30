package com.learning.api.dto.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long studentId;
    private String studentName;
    private String studentAvatar;
    private List<Long> orderIds;           // 該學生所有預約的 order_id
    private List<String> courses;          // 該學生購買的課程名稱列表
    private String lastMessage;            // 最後一則訊息內容
    private Instant lastMessageTime;       // 最後訊息時間
    private Integer unreadCount;           // 未讀訊息數（目前先設為 0，之後可擴充）
}