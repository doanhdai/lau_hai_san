package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageRequest;
import com.example.backend_quanlynhahanglau.dto.chat.ChatMessageResponse;
import com.example.backend_quanlynhahanglau.dto.chat.ConversationResponse;
import com.example.backend_quanlynhahanglau.entity.ChatMessage;
import com.example.backend_quanlynhahanglau.entity.Conversation;
import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.User;
import com.example.backend_quanlynhahanglau.repository.ChatMessageRepository;
import com.example.backend_quanlynhahanglau.repository.ConversationRepository;
import com.example.backend_quanlynhahanglau.repository.CustomerRepository;
import com.example.backend_quanlynhahanglau.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * User gửi tin nhắn đến system (mỗi user có 1 conversation với system/admin)
     */
    @Transactional
    public ChatMessageResponse sendMessageFromCustomerByUserId(ChatMessageRequest request, Long userId) {
        log.debug("sendMessageFromCustomerByUserId: userId = {}", userId);
        
        // Tìm hoặc tạo conversation cho user
        Conversation conversation = getOrCreateConversationByUserId(userId);
        
        // Lấy User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Tạo tin nhắn
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderType("CUSTOMER")
                .senderId(userId) // senderId là userId
                .senderName(user.getFullName())
                .content(request.getContent())
                .isRead(false)
                .build();
        
        // Set senderUser (bắt buộc)
        message.setSenderUser(user);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Cập nhật conversation
        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        // Đếm số tin nhắn chưa đọc từ user
        Long unreadCount = chatMessageRepository.countUnreadMessagesInConversation(conversation);
        conversation.setUnreadCount(unreadCount != null ? unreadCount : 0L);
        conversationRepository.save(conversation);
        
        log.info("Message sent from user (id={}) in conversation (id={})", userId, conversation.getId());
        
        return mapToResponse(savedMessage);
    }
    
    /**
     * Customer gửi tin nhắn đến system (từ Customer ID - backward compatibility)
     * @deprecated Sử dụng sendMessageFromCustomerByUserId thay thế
     */
    @Deprecated
    @Transactional
    public ChatMessageResponse sendMessageFromCustomer(ChatMessageRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        
        // Nếu customer có user, dùng userId
        if (customer.getUser() != null) {
            return sendMessageFromCustomerByUserId(request, customer.getUser().getId());
        }
        
        // Nếu không có user, tạo user từ customer hoặc throw error
        throw new RuntimeException("Customer (id=" + customerId + ") does not have a linked User account. Please use User account to send messages.");
    }

    /**
     * Staff/Admin gửi tin nhắn đến customer (trong conversation)
     */
    @Transactional
    public ChatMessageResponse sendMessageFromStaff(ChatMessageRequest request, Long staffId) {
        log.info("=== sendMessageFromStaff called ===");
        log.info("request: conversationId={}, content={}", request.getConversationId(), request.getContent());
        log.info("staffId: {}", staffId);
        
        if (request.getConversationId() == null) {
            throw new RuntimeException("Conversation ID is required for staff messages");
        }
        
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + request.getConversationId()));
        log.info("Found conversation: id={}, userId={}, userName={}", 
                conversation.getId(), conversation.getUser().getId(), conversation.getUserName());
        
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("User not found: " + staffId));
        log.info("Found staff: id={}, name={}", staff.getId(), staff.getFullName());
        
        // Tạo tin nhắn
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderType("SYSTEM")
                .senderId(staffId)
                .senderName(staff.getFullName())
                .content(request.getContent())
                .isRead(false)
                .build();
        
        message.setSenderUser(staff);
        log.info("Created message object: senderType={}, senderId={}, senderName={}", 
                message.getSenderType(), message.getSenderId(), message.getSenderName());
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Message saved to database: id={}", savedMessage.getId());
        
        // Cập nhật conversation
        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        conversationRepository.save(conversation);
        log.info("Updated conversation lastMessageAt: {}", savedMessage.getCreatedAt());
        
        ChatMessageResponse response = mapToResponse(savedMessage);
        log.info("Returning response: {}", response);
        return response;
    }

    /**
     * Lấy conversation của user (mỗi user có 1 conversation với system)
     */
    @Transactional
    public ConversationResponse getCustomerConversationByUserId(Long userId) {
        Conversation conversation = getOrCreateConversationByUserId(userId);
        return mapConversationToResponse(conversation);
    }
    
    /**
     * Lấy conversation của customer (backward compatibility)
     * @deprecated Sử dụng getCustomerConversationByUserId thay thế
     */
    @Deprecated
    @Transactional
    public ConversationResponse getCustomerConversation(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        
        if (customer.getUser() == null) {
            throw new RuntimeException("Customer (id=" + customerId + ") does not have a linked User account.");
        }
        
        return getCustomerConversationByUserId(customer.getUser().getId());
    }

    /**
     * Lấy tất cả conversations với CUSTOMER (cho staff/admin xem)
     * Chỉ trả về conversations có ít nhất 1 message từ CUSTOMER
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAllActiveConversations();
        return conversations.stream()
                .map(this::mapConversationToResponseWithoutMessages) // Không load messages trong danh sách
                .collect(Collectors.toList());
    }

    /**
     * Lấy conversation theo ID (cho staff/admin) - không bao gồm messages
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        return mapConversationToResponseWithoutMessages(conversation);
    }

    /**
     * Lấy tất cả messages của conversation với CUSTOMER (cho staff/admin)
     * Trả về tất cả messages (cả CUSTOMER và SYSTEM) trong conversation
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationMessages(Long conversationId) {
        // Kiểm tra conversation có tồn tại không
        if (!conversationRepository.existsById(conversationId)) {
            throw new RuntimeException("Conversation not found: " + conversationId);
        }
        
        // Lấy tất cả messages (cả CUSTOMER và SYSTEM) trong conversation
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đánh dấu tin nhắn đã đọc (từ phía staff/admin)
     */
    @Transactional
    public void markAsReadByStaff(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        
        // Đánh dấu tất cả tin nhắn từ customer là đã đọc
        List<ChatMessage> unreadMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(m -> "CUSTOMER".equals(m.getSenderType()) && !m.getIsRead())
                .collect(Collectors.toList());
        
        LocalDateTime now = LocalDateTime.now();
        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(now);
        }
        
        chatMessageRepository.saveAll(unreadMessages);
        
        // Cập nhật unread count
        conversation.setUnreadCount(0L);
        conversationRepository.save(conversation);
    }

    /**
     * Đếm số conversations chưa đọc (cho staff/admin)
     */
    @Transactional(readOnly = true)
    public Long getUnreadConversationsCount() {
        return conversationRepository.countUnreadConversations();
    }

    /**
     * Tìm hoặc tạo conversation cho user (mỗi user có 1 conversation với system/admin)
     */
    @Transactional
    private Conversation getOrCreateConversationByUserId(Long userId) {
        Optional<Conversation> existing = conversationRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Tìm Customer nếu có (để lưu thông tin)
        Customer customer = null;
        List<Customer> customers = customerRepository.findByUserId(userId);
        if (!customers.isEmpty()) {
            customer = customers.get(0);
        }
        
        Conversation conversation = Conversation.builder()
                .user(user)
                .userName(user.getFullName())
                .customer(customer)
                .customerName(customer != null ? customer.getFullName() : null)
                .isActive(true)
                .unreadCount(0L)
                .build();
        
        log.info("Created new conversation (id={}) for user (id={}, name={})", 
                conversation.getId(), userId, user.getFullName());
        
        return conversationRepository.save(conversation);
    }

    /**
     * Map Conversation entity sang DTO (không bao gồm messages)
     */
    private ConversationResponse mapConversationToResponseWithoutMessages(Conversation conversation) {
        // Chỉ lấy lastMessage, không load toàn bộ messages
        ChatMessageResponse lastMessage = null;
        List<ChatMessage> allMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        if (!allMessages.isEmpty()) {
            lastMessage = mapToResponse(allMessages.get(allMessages.size() - 1));
        }
        
        User user = conversation.getUser();
        Customer customer = conversation.getCustomer();
        
        // Đảm bảo userName luôn có giá trị
        String userName = conversation.getUserName();
        if (userName == null || userName.trim().isEmpty()) {
            userName = user != null ? user.getFullName() : "Người dùng";
        }
        
        // Đảm bảo customerName luôn có giá trị (nếu có customer)
        String customerName = null;
        if (customer != null) {
            customerName = customer.getFullName();
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = conversation.getCustomerName();
        }
        
        return ConversationResponse.builder()
                .conversationId(conversation.getId())
                .userId(user != null ? user.getId() : null)
                .userName(userName)
                .userPhone(user != null ? user.getPhone() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customerName)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .unreadCount(conversation.getUnreadCount())
                .lastMessage(lastMessage)
                .lastMessageTime(conversation.getLastMessageAt())
                .messages(null) // Không trả về messages
                .isActive(conversation.getIsActive())
                .build();
    }

    /**
     * Map Conversation entity sang DTO (bao gồm messages)
     */
    private ConversationResponse mapConversationToResponse(Conversation conversation) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        ChatMessageResponse lastMessage = messageResponses.isEmpty() ? null : 
                messageResponses.get(messageResponses.size() - 1);
        
        User user = conversation.getUser();
        Customer customer = conversation.getCustomer();
        
        // Đảm bảo userName luôn có giá trị
        String userName = conversation.getUserName();
        if (userName == null || userName.trim().isEmpty()) {
            userName = user != null ? user.getFullName() : "Người dùng";
        }
        
        // Đảm bảo customerName luôn có giá trị (nếu có customer)
        String customerName = null;
        if (customer != null) {
            customerName = customer.getFullName();
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = conversation.getCustomerName();
        }
        
        return ConversationResponse.builder()
                .conversationId(conversation.getId())
                .userId(user != null ? user.getId() : null)
                .userName(userName)
                .userPhone(user != null ? user.getPhone() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customerName)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .unreadCount(conversation.getUnreadCount())
                .lastMessage(lastMessage)
                .lastMessageTime(conversation.getLastMessageAt())
                .messages(messageResponses)
                .isActive(conversation.getIsActive())
                .build();
    }

    /**
     * Map ChatMessage entity sang DTO
     */
    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderType(message.getSenderType())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
