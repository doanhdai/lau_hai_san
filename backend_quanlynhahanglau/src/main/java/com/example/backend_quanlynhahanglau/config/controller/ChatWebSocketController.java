package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageRequest;
import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageResponse;
import com.example.backend_quanlynhahanglau.dto.chat.ConversationResponse;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import com.example.backend_quanlynhahanglau.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý tin nhắn chat qua WebSocket
     * Client gửi đến: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        try {
            log.info("=== WebSocket message received ===");
            log.info("Payload: {}", payload);
            log.info("Payload keys: {}", payload.keySet());
            log.info("conversationId in payload: {}", payload.get("conversationId"));
            log.info("conversationId type: {}", payload.get("conversationId") != null ? payload.get("conversationId").getClass() : "null");
            
            String senderType = (String) payload.get("senderType");
            Long senderId = payload.get("senderId") != null ? ((Number) payload.get("senderId")).longValue() : null;
            String content = (String) payload.get("content");
            Long conversationId = payload.get("conversationId") != null ? 
                    ((Number) payload.get("conversationId")).longValue() : null;

            log.info("senderType: {}, senderId: {}, conversationId: {}", senderType, senderId, conversationId);
            log.info("content: {}", content);
            
            if (conversationId == null) {
                log.error("ERROR: conversationId is NULL in payload!");
                log.error("Full payload again: {}", payload);
            }

            ChatMessageRequest request = new ChatMessageRequest();
            request.setContent(content);
            request.setConversationId(conversationId);

            ChatMessageResponse message;
            
            if ("CUSTOMER".equals(senderType)) {
                // User gửi tin nhắn đến system (senderId giờ là userId)
                // Lấy userId từ authentication context hoặc từ senderId
                Long userId = senderId; // senderId giờ là userId
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        userId = userDetails.getId();
                        log.debug("WebSocket: Using userId from authentication: {}", userId);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket: Could not get userId from authentication, using senderId: {}", e.getMessage());
                }
                
                // Dùng method mới với userId
                message = chatService.sendMessageFromCustomerByUserId(request, userId);
                
                // Gửi đến tất cả staff/admin đang online
                messagingTemplate.convertAndSend("/topic/chat/system", message);
                
                // Gửi lại cho customer để confirm
                messagingTemplate.convertAndSend("/topic/chat/customer/" + senderId, message);
                
            } else {
                // Staff/Admin gửi tin nhắn đến customer
                log.info("Processing SYSTEM message from staff/admin (id={})", senderId);
                message = chatService.sendMessageFromStaff(request, senderId);
                log.info("Message saved: {}", message);
                
                // Lấy conversation để biết user ID
                ConversationResponse conversation = chatService.getConversationById(conversationId);
                log.info("Conversation: userId={}, userName={}", conversation.getUserId(), conversation.getUserName());
                
                // Gửi đến user (userId)
                Long targetUserId = conversation.getUserId();
                if (targetUserId != null) {
                    log.info("Sending message to user via WebSocket: /topic/chat/customer/{}", targetUserId);
                    messagingTemplate.convertAndSend("/topic/chat/customer/" + targetUserId, message);
                }
                
                // Gửi đến tất cả staff/admin để cập nhật
                messagingTemplate.convertAndSend("/topic/chat/system", message);
            }

            log.info("Chat message sent from {}:{} in conversation:{}", senderType, senderId, conversationId);
        } catch (Exception e) {
            log.error("Error sending chat message via WebSocket", e);
        }
    }

    /**
     * Xử lý đánh dấu đã đọc qua WebSocket
     * Client gửi đến: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Map<String, Object> payload) {
        try {
            Long conversationId = ((Number) payload.get("conversationId")).longValue();
            
            chatService.markAsReadByStaff(conversationId);

            // Thông báo cho tất cả staff/admin
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "MESSAGE_READ");
            notification.put("conversationId", conversationId);

            messagingTemplate.convertAndSend("/topic/chat/system", notification);

            log.info("Chat conversation marked as read: {}", conversationId);
        } catch (Exception e) {
            log.error("Error marking chat message as read via WebSocket", e);
        }
    }
}

