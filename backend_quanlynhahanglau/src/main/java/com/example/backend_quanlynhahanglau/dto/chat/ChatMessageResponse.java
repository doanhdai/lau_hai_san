package com.example.backend_quanlynhahanglau.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private String senderType; // "SYSTEM" (staff/admin) hoặc "CUSTOMER"
    private Long senderId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

