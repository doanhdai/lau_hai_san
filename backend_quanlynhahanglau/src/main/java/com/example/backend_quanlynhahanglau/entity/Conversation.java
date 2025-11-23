package com.example.backend_quanlynhahanglau.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User trong cuộc trò chuyện (mỗi user có 1 conversation với system)

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer; // Customer liên kết (nếu có, để lấy thông tin chi tiết)

    @Column(nullable = false, length = 100)
    private String userName; // Tên user (lưu để hiển thị nhanh)
    
    @Column(length = 100)
    private String customerName; // Tên customer (nếu có, để hiển thị)

    @Column(columnDefinition = "bit default 0")
    @Builder.Default
    private Boolean isActive = true; // Cuộc trò chuyện còn hoạt động không

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt; // Thời gian tin nhắn cuối cùng

    @Column(name = "unread_count")
    @Builder.Default
    private Long unreadCount = 0L; // Số tin nhắn chưa đọc (từ phía system/staff)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

