package com.learning.api.dto.videoroom;

import lombok.Data;
import java.time.Instant;

/**
 * 視訊聊天室事件 DTO
 * type: "joined" | "left"
 */
@Data
public class RoomEvent {
    private String type;              // joined / left
    private String role;              // "student"/"tutor" 或 "1"/"2"
    private Long userId;              // 使用者 ID（由後端從 Principal 填入）
    private Instant timestamp = Instant.now();
}
