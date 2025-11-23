package com.example.backend_quanlynhahanglau.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
    
    // Conversation ID sẽ được xác định từ customerId hoặc conversationId
    private Long conversationId; // Optional: nếu có thì dùng, không thì tìm từ customerId
}

