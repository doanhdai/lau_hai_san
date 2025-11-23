package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageRequest;
import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageResponse;
import com.example.backend_quanlynhahanglau.dto.chat.ConversationResponse;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import com.example.backend_quanlynhahanglau.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {
    private final ChatService chatService;

    // ========== API cho Customer ==========
    
    /**
     * Customer gửi tin nhắn đến system
     */
    @PostMapping("/public/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessageAsCustomer(
            @Valid @RequestBody ChatMessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        // Tìm Customer từ User ID (vì customer có thể đăng nhập với User account)
        ChatMessageResponse message = chatService.sendMessageFromCustomerByUserId(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi tin nhắn thành công", message));
    }

    /**
     * Customer lấy conversation của mình (chỉ có 1 conversation với system)
     */
    @GetMapping("/public/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConversationResponse>> getCustomerConversation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        // Tìm Customer từ User ID
        ConversationResponse conversation = chatService.getCustomerConversationByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    // ========== API cho Staff/Admin ==========
    
    /**
     * Staff/Admin lấy tất cả conversations (xem tất cả chat với customers)
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAllConversations() {
        List<ConversationResponse> conversations = chatService.getAllConversations();
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    /**
     * Staff/Admin lấy conversation theo ID (không bao gồm messages)
     */
    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationById(
            @PathVariable Long conversationId) {
        ConversationResponse conversation = chatService.getConversationById(conversationId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    /**
     * Staff/Admin lấy messages của conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversationMessages(
            @PathVariable Long conversationId) {
        List<ChatMessageResponse> messages = chatService.getConversationMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Staff/Admin gửi tin nhắn đến customer (trong conversation)
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessageFromStaff(
            @Valid @RequestBody ChatMessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long staffId = userDetails.getId();
        
        ChatMessageResponse message = chatService.sendMessageFromStaff(request, staffId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi tin nhắn thành công", message));
    }

    /**
     * Staff/Admin đánh dấu conversation đã đọc
     */
    @PutMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> markAsReadByStaff(
            @PathVariable Long conversationId) {
        chatService.markAsReadByStaff(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", null));
    }

    /**
     * Lấy số conversations chưa đọc (cho staff/admin)
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<Long>> getUnreadConversationsCount() {
        Long count = chatService.getUnreadConversationsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
