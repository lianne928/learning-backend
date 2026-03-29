package com.learning.api.dto.ChatRoom;

import java.time.Instant;

public class ConversationDTO {
    private Long orderId;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String lastMessage;
    private Instant lastMessageAt;

    public ConversationDTO(Long orderId, Long studentId, String studentName,
                           Long courseId, String courseName,
                           String lastMessage, Instant lastMessageAt) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getOrderId() { return orderId; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public Long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getLastMessage() { return lastMessage; }
    public Instant getLastMessageAt() { return lastMessageAt; }
}
