package com.learning.api.service.Chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.dto.ChatRoom.ConversationDTO;
import com.learning.api.entity.ChatMessage;
import com.learning.api.entity.Course;
import com.learning.api.entity.Order;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.ChatMessageRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepo;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;

    public List<ChatMessage> findByBookingId(Long bookingId) {
        return chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
    }

    public List<ChatMessage> findByOrderIds(List<Long> orderIds) {
        return chatMessageRepository.findByOrderIds(orderIds);
    }

    public List<ConversationDTO> findConversationsByTutorId(Long tutorId) {
        List<Long> courseIds = courseRepo.findByTutor_Id(tutorId)
                .stream().map(Course::getId).collect(Collectors.toList());

        if (courseIds.isEmpty()) return List.of();

        return orderRepo.findByCourseIdIn(courseIds).stream()
                .map(order -> {
                    String studentName = userRepo.findById(order.getUserId())
                            .map(u -> u.getName()).orElse("未知學生");
                    String courseName = courseRepo.findById(order.getCourseId())
                            .map(Course::getName).orElse("未知課程");
                    ChatMessage latest = chatMessageRepository
                            .findLatestByOrderId(order.getId()).orElse(null);
                    return new ConversationDTO(
                            order.getId(),
                            order.getUserId(),
                            studentName,
                            order.getCourseId(),
                            courseName,
                            latest != null ? latest.getMessage() : null,
                            latest != null ? latest.getCreatedAt() : null
                    );
                })
                .collect(Collectors.toList());
    }

    public ChatMessage save(Long bookingId, String role, Integer messageTypeValue, String message, String mediaUrl) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID 不能為空");
        }

        orderRepo.findById(bookingId)
            .orElseThrow(() -> new NoSuchElementException("Booking ID: " + bookingId + " 不存在"));

        MessageType type = MessageType.fromValue(messageTypeValue != null ? messageTypeValue : MessageType.TEXT.getValue());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setOrderId(bookingId);
        chatMessage.setRole(role);
        chatMessage.setMessageType(type.getValue());

        if (type.isMedia()) {
            chatMessage.setMediaUrl(mediaUrl);
            // 保存原始檔名到 message 欄位（用於下載時顯示正確檔名）
            if (message != null && !message.isBlank()) {
                chatMessage.setMessage(message);
            }
        } else {
            chatMessage.setMessage(message);
        }

        return chatMessageRepository.save(chatMessage);
    }

    public Optional<ChatMessage> update(Long id, String message) {
        return chatMessageRepository.findById(id).map(existing -> {
           /*  if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("消息內容不能為空");
            } */
            existing.setMessage(message);
            return chatMessageRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (chatMessageRepository.existsById(id)) {
            chatMessageRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 