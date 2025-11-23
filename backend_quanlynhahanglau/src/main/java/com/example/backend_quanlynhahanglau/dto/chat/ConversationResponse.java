package com.example.backend_quanlynhahanglau.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConversationResponse {
    private Long conversationId;
    private Long userId; // User ID (chính)
    private String userName; // Tên user
    private String userPhone; // Số điện thoại user
    private String userEmail; // Email user
    private Long customerId; // Customer ID (nếu có)
    private String customerName; // Tên customer (nếu có)
    private String customerPhone; // Số điện thoại customer (nếu có)
    private String customerEmail; // Email customer (nếu có)
    private Long unreadCount; // Số tin nhắn chưa đọc từ user (cho staff/admin)
    private ChatMessageResponse lastMessage;
    private LocalDateTime lastMessageTime;
    private List<ChatMessageResponse> messages;
    private Boolean isActive;
}

