package com.learning.api.controller.ChatAndVideoController;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.learning.api.dto.ChatRoom.ChatMessageRequest;
import com.learning.api.dto.ChatRoom.ConversationDTO;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.Chat.ChatMessageService;
import com.learning.api.service.Chat.ChatMessageService.StudentConversationDTO;
import com.learning.api.service.Chat.FileStorageService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chatMessage")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    // ✅ 原有端點：查詢單一預約的訊息
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ChatMessage>> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(chatMessageService.findByBookingId(bookingId));
    }

    // 🆕 新增端點：查詢多個預約的訊息（合併）
    @GetMapping("/orders")
    public ResponseEntity<List<ChatMessage>> getByOrderIds(@RequestParam("ids") List<Long> orderIds) {
        return ResponseEntity.ok(chatMessageService.findByOrderIds(orderIds));
    }

    // 🆕 新增端點：學生查詢對話列表（按老師分組）
    @GetMapping("/conversations")
    public ResponseEntity<List<StudentConversationDTO>> getConversationsByStudent(
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long studentId = securityUser.getUser().getId();
        return ResponseEntity.ok(chatMessageService.findConversationsByStudentId(studentId));
    }

    // ✅ 原有端點：老師查詢對話列表（按學生分組）
    @GetMapping("/conversations/tutor/{tutorId}")
    public ResponseEntity<List<ConversationDTO>> getConversationsByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(chatMessageService.findConversationsByTutorId(tutorId));
    }

    // ✅ 原有端點：上傳檔案
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("role") String role,
            @RequestParam(value = "message", required = false) String message) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("檔案不能為空"));
            }
            String mediaUrl = fileStorageService.store(file);
            int messageType = fileStorageService.detectMessageType(file);
            String originalFileName = file.getOriginalFilename();
            String textMessage = (message != null && !message.isBlank()) ? message : originalFileName;
            Integer roleValue = parseRole(role);

            ChatMessage chatMessage = chatMessageService.save(
                    bookingId, roleValue, messageType, textMessage, mediaUrl);
            messagingTemplate.convertAndSend("/topic/room/" + bookingId + "/chat", chatMessage);
            return ResponseEntity.status(HttpStatus.CREATED).body(chatMessage);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("錯誤: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("上傳失敗: " + e.getMessage()));
        }
    }

    // ✅ 原有端點：下載檔案
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> download(
            @PathVariable String filename,
            @RequestParam(value = "name", required = false) String originalName) {
        try {
            Resource resource = fileStorageService.loadAsResource(filename);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String downloadName = (originalName != null && !originalName.isBlank())
                    ? originalName : filename;
            String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ 原有端點：建立訊息
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChatMessageRequest request) {
        try {
            String validationError = validateRequest(request);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: " + validationError));
            }

            Integer roleValue = parseRole(request.getRole());

            ChatMessage chatMessage = chatMessageService.save(
                    request.getBookingId(),
                    roleValue,
                    request.getMessageType(),
                    request.getMessage(),
                    request.getMediaUrl()
            );
            messagingTemplate.convertAndSend("/topic/room/" + request.getBookingId() + "/chat", chatMessage);
            return ResponseEntity.status(HttpStatus.CREATED).body(chatMessage);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("錯誤: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
        }
    }

    // ✅ 原有端點：更新訊息
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String message = body.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: 消息內容不能為空"));
            }
            return chatMessageService.update(id, message)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
        }
    }

    // ✅ 原有端點：刪除訊息
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return chatMessageService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ✅ 角色解析：支援字串 ("student"/"tutor") 和數字 ("1"/"2")
    private Integer parseRole(String role) {
        if (role == null) throw new IllegalArgumentException("Role 不能為空");
        String r = role.trim().toLowerCase();
        if ("student".equals(r) || "1".equals(r)) return 1;
        if ("tutor".equals(r)   || "2".equals(r)) return 2;
        throw new IllegalArgumentException("Role 必須為 student/tutor 或 1/2，收到: " + role);
    }

    // ✅ 原有方法：驗證請求
    private String validateRequest(ChatMessageRequest request) {
        if (request.getBookingId() == null) return "Booking ID 不能為空";
        if (request.getRole() == null) return "Role 不能為空";

        int typeValue = request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT.getValue();
        MessageType type;
        try {
            type = MessageType.fromValue(typeValue);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        if (type.isMedia()) {
            if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
                return typeName(type) + " URL 不能為空";
            }
        } else {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return "消息內容不能為空";
            }
        }

        return null;
    }

    // ✅ 原有方法：類型名稱
    private String typeName(MessageType type) {
        return switch (type) {
            case STICKER -> "貼圖";
            case VOICE -> "語音";
            case IMAGE -> "圖片";
            case VIDEO -> "影片";
            default -> "媒體";
        };
    }

    // ✅ 原有錯誤處理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
    }

    // ✅ 原有錯誤回應類別
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}