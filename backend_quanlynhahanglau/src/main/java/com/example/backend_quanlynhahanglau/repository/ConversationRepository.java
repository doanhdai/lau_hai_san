package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Conversation;
import com.example.backend_quanlynhahanglau.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // Tìm conversation của một user (mỗi user chỉ có 1 conversation với system)
    Optional<Conversation> findByUserId(Long userId);
    
    // Tìm conversation của một customer (backward compatibility)
    Optional<Conversation> findByCustomerId(Long customerId);
    
    // Tìm conversation của một customer
    Optional<Conversation> findByCustomer(Customer customer);
    
    // Lấy tất cả conversations (cho staff/admin xem) - chỉ lấy conversations có ít nhất 1 message từ CUSTOMER
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "WHERE c.isActive = true " +
           "AND EXISTS (SELECT 1 FROM ChatMessage m WHERE m.conversation.id = c.id AND m.senderType = 'CUSTOMER') " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<Conversation> findAllActiveConversations();
    
    // Đếm số conversations chưa đọc (từ phía system)
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.isActive = true AND c.unreadCount > 0")
    Long countUnreadConversations();
}

