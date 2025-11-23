package com.example.backend_quanlynhahanglau.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation; // Cuộc trò chuyện

    @Column(nullable = false, length = 20)
    private String senderType; // "SYSTEM" (staff/admin) hoặc "CUSTOMER"

    @Column(nullable = false)
    private Long senderId; // ID của User (nếu SYSTEM) hoặc Customer (nếu CUSTOMER)

    @Column(length = 100)
    private String senderName; // Tên người gửi (để hiển thị nhanh)

    @Column(nullable = false, length = 2000)
    private String content; // Nội dung tin nhắn

    @Column(columnDefinition = "bit default 0")
    @Builder.Default
    private Boolean isRead = false; // Đã đọc chưa

    @Column(name = "read_at")
    private LocalDateTime readAt; // Thời gian đọc

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Liên kết với User (luôn có giá trị)
    @ManyToOne
    @JoinColumn(name = "sender_user_id")
    private User senderUser;
}

