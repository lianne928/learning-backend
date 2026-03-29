package com.learning.api.service.Chat;

import com.learning.api.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.dto.ChatRoom.ConversationDTO;
import com.learning.api.entity.ChatMessage;
import com.learning.api.entity.Order;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.Course;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.UserRepo;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.TutorRepo;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepo;
    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final TutorRepo tutorRepo;

    // ✅ 原有方法：查詢單一預約的訊息
    public List<ChatMessage> findByBookingId(Long bookingId) {
        return chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
    }

    // 🆕 新增：查詢多個預約的訊息（合併）
    public List<ChatMessage> findByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return chatMessageRepository.findByOrderIdsOrderByCreatedAtAsc(orderIds);
    }

    // ✅ 原有：老師查詢對話列表（按學生分組）
    public List<ConversationDTO> findConversationsByTutorId(Long tutorId) {
        List<Order> orders = orderRepo.findByTutorId(tutorId);

        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Order>> groupedByStudent = orders.stream()
                .collect(Collectors.groupingBy(Order::getUserId));

        List<ConversationDTO> conversations = new ArrayList<>();

        for (Map.Entry<Long, List<Order>> entry : groupedByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<Order> studentOrders = entry.getValue();

            List<Long> orderIds = studentOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            var studentOpt = userRepo.findById(studentId);
            if (studentOpt.isEmpty()) {
                continue;
            }

            var student = studentOpt.get();

            List<Long> courseIds = studentOrders.stream()
                    .map(Order::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> courseNames = new ArrayList<>();
            for (Long courseId : courseIds) {
                var courseOpt = courseRepo.findById(courseId);
                if (courseOpt.isPresent()) {
                    courseNames.add(courseOpt.get().getName());
                }
            }

            Optional<ChatMessage> lastMsg = chatMessageRepository.findLastMessageByOrderIds(orderIds);

            ConversationDTO conversation = ConversationDTO.builder()
                    .studentId(studentId)
                    .studentName(student.getName())
                    .studentAvatar("")
                    .orderIds(orderIds)
                    .courses(courseNames)
                    .lastMessage(lastMsg.map(ChatMessage::getMessage).orElse(""))
                    .lastMessageTime(lastMsg.map(ChatMessage::getCreatedAt).orElse(null))
                    .unreadCount(0)
                    .build();

            conversations.add(conversation);
        }

        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    // 🆕 新增：學生查詢對話列表（按老師分組）
    public List<StudentConversationDTO> findConversationsByStudentId(Long studentId) {
        // 1. 查詢該學生的所有訂單
        List<Order> orders = orderRepo.findByUserId(studentId);

        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 先建立 courseId → tutorId 的映射
        Map<Long, Long> courseTutorMap = new HashMap<>();
        Map<Long, String> courseNameMap = new HashMap<>();

        for (Order order : orders) {
            Long courseId = order.getCourseId();
            if (!courseTutorMap.containsKey(courseId)) {
                var courseOpt = courseRepo.findById(courseId);
                if (courseOpt.isPresent()) {
                    Course course = courseOpt.get();
                    courseTutorMap.put(courseId, course.getTutor().getId());
                    courseNameMap.put(courseId, course.getName());
                }
            }
        }

        // 3. 按 tutorId 分組訂單（關鍵修正！）
        Map<Long, List<Order>> groupedByTutor = new HashMap<>();
        for (Order order : orders) {
            Long tutorId = courseTutorMap.get(order.getCourseId());
            if (tutorId != null) {
                groupedByTutor.computeIfAbsent(tutorId, k -> new ArrayList<>()).add(order);
            }
        }

        // 4. 對每個老師，組裝 StudentConversationDTO
        List<StudentConversationDTO> conversations = new ArrayList<>();

        for (Map.Entry<Long, List<Order>> entry : groupedByTutor.entrySet()) {
            Long tutorId = entry.getKey();
            List<Order> tutorOrders = entry.getValue();

            // 取得該老師的所有 orderId
            List<Long> orderIds = tutorOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            // 查詢老師資訊
            var tutorOpt = tutorRepo.findById(tutorId);
            if (tutorOpt.isEmpty()) {
                continue;
            }

            Tutor tutor = tutorOpt.get();

            // 取得課程名稱（取第一個課程，或合併多個課程名稱）
            List<String> courseNames = tutorOrders.stream()
                    .map(order -> courseNameMap.get(order.getCourseId()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            String subject = courseNames.isEmpty() ? "課程" :
                    courseNames.size() == 1 ? courseNames.get(0) :
                            String.join("、", courseNames);

            // 查詢最後一則訊息
            Optional<ChatMessage> lastMsg = chatMessageRepository.findLastMessageByOrderIds(orderIds);

            StudentConversationDTO conversation = StudentConversationDTO.builder()
                    .orderId(orderIds.get(0))
                    .bookingRecordId(0L)
                    .bookingIds(orderIds)
                    .bookingRecordIds(List.of())
                    .participantId(tutorId)
                    .participantName(tutor.getUser().getName())
                    .avatar(tutor.getAvatar() != null ? tutor.getAvatar() : "")
                    .subject(subject)  // 如果有多個課程，會顯示「英文會話、英文寫作」
                    .lastMessage(lastMsg.map(ChatMessage::getMessage).orElse(""))
                    .lastMessageTime(lastMsg.map(ChatMessage::getCreatedAt).orElse(null))
                    .unread(0)
                    .build();

            conversations.add(conversation);
        }

        // 5. 依最後訊息時間排序（最新的在前）
        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    public ChatMessage save(Long bookingId, Integer role, Integer messageTypeValue, String message, String mediaUrl) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID 不能為空");
        }

        // ❌ 移除這段檢查（或改成只檢查訂單存在）
        // orderRepo.findById(bookingId)
        //     .orElseThrow(() -> new NoSuchElementException("Booking ID: " + bookingId + " 不存在"));

        // ✅ 改成：檢查訂單存在即可，不管狀態
        Order order = orderRepo.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Order ID: " + bookingId + " 不存在"));

        // 不檢查 status，允許所有狀態的訂單發送訊息

        MessageType type = MessageType.fromValue(messageTypeValue != null ? messageTypeValue : MessageType.TEXT.getValue());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setOrderId(bookingId);
        chatMessage.setRole(role);
        chatMessage.setMessageType(type.getValue());

        if (type.isMedia()) {
            chatMessage.setMediaUrl(mediaUrl);
            if (message != null && !message.isBlank()) {
                chatMessage.setMessage(message);
            }
        } else {
            chatMessage.setMessage(message);
        }

        return chatMessageRepository.save(chatMessage);
    }

    // ✅ 原有方法：更新訊息
    public Optional<ChatMessage> update(Long id, String message) {
        return chatMessageRepository.findById(id).map(existing -> {
            existing.setMessage(message);
            return chatMessageRepository.save(existing);
        });
    }

    // ✅ 原有方法：刪除訊息
    public boolean deleteById(Long id) {
        if (chatMessageRepository.existsById(id)) {
            chatMessageRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 🆕 內部 DTO：學生端對話列表回傳格式
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StudentConversationDTO {
        private Long orderId;
        private Long bookingRecordId;
        private List<Long> bookingIds;
        private List<Long> bookingRecordIds;
        private Long participantId;
        private String participantName;
        private String avatar;
        private String subject;
        private String lastMessage;
        private Instant lastMessageTime;
        private Integer unread;
    }
}