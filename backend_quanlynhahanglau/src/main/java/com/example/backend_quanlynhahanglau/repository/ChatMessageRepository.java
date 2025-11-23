package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.ChatMessage;
import com.example.backend_quanlynhahanglau.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Lấy tất cả tin nhắn trong một conversation
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation ORDER BY m.createdAt ASC")
    List<ChatMessage> findByConversation(@Param("conversation") Conversation conversation);
    
    // Lấy tin nhắn của một conversation theo ID
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    // Đếm số tin nhắn chưa đọc trong một conversation (từ phía system/staff)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.conversation = :conversation AND m.senderType = 'CUSTOMER' AND m.isRead = false")
    Long countUnreadMessagesInConversation(@Param("conversation") Conversation conversation);

    // Lấy tin nhắn chưa đọc sau một thời điểm
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "m.conversation = :conversation AND " +
           "m.senderType = 'CUSTOMER' AND m.isRead = false AND m.createdAt > :since")
    List<ChatMessage> findUnreadMessagesSince(
            @Param("conversation") Conversation conversation,
            @Param("since") LocalDateTime since);
    
    // Lấy tin nhắn cuối cùng của conversation
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation ORDER BY m.createdAt DESC")
    List<ChatMessage> findLastMessageByConversation(@Param("conversation") Conversation conversation);
}

