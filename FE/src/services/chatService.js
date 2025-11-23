import api from './api'

export const chatService = {
  // ========== API cho Customer ==========
  
  // Customer gửi tin nhắn đến system
  async sendMessageAsCustomer(request, customerId) {
    // customerId không cần truyền nữa, lấy từ authentication
    const response = await api.post('/chat/public/send', request)
    return response.data
  },

  // Customer lấy conversation của mình
  async getCustomerConversation(customerId) {
    // customerId không cần truyền nữa, lấy từ authentication
    const response = await api.get('/chat/public/conversation')
    return response.data
  },

  // ========== API cho Staff/Admin ==========
  
  // Staff/Admin lấy tất cả conversations
  async getConversations() {
    const response = await api.get('/chat/conversations')
    return response.data
  },

  // Staff/Admin lấy conversation theo ID (không bao gồm messages)
  async getConversation(conversationId) {
    const response = await api.get(`/chat/conversations/${conversationId}`)
    return response.data
  },

  // Staff/Admin lấy messages của conversation
  async getConversationMessages(conversationId) {
    const response = await api.get(`/chat/conversations/${conversationId}/messages`)
    return response.data
  },

  // Staff/Admin gửi tin nhắn đến customer
  async sendMessage(request) {
    const response = await api.post('/chat/send', request)
    return response.data
  },

  // Staff/Admin đánh dấu conversation đã đọc
  async markAsRead(conversationId) {
    const response = await api.put(`/chat/conversations/${conversationId}/mark-read`)
    return response.data
  },

  // Backward compatibility
  async getAllConversations() {
    return this.getConversations()
  },

  async getConversationById(conversationId) {
    return this.getConversation(conversationId)
  },

  async sendMessageFromStaff(request, staffId) {
    return this.sendMessage(request)
  },

  async markAsReadByStaff(conversationId) {
    return this.markAsRead(conversationId)
  },

  // Lấy số conversations chưa đọc (cho staff/admin)
  async getUnreadConversationsCount() {
    const response = await api.get('/chat/unread-count')
    return response.data
  },

  // Customer: lấy unread count từ conversation của họ
  async getCustomerUnreadCount(customerId) {
    // customerId không cần truyền nữa, lấy từ authentication
    const response = await this.getCustomerConversation()
    if (response && response.success && response.data) {
      // Đếm số tin nhắn chưa đọc từ SYSTEM
      const unreadMessages = (response.data.messages || []).filter(
        msg => msg.senderType === 'SYSTEM' && !msg.isRead
      )
      return { success: true, data: unreadMessages.length }
    }
    return { success: true, data: 0 }
  },

  // Customer: đánh dấu tin nhắn đã đọc (không có endpoint riêng, tự động khi get conversation)
  async markCustomerMessagesAsRead(customerId) {
    // customerId không cần truyền nữa, lấy từ authentication
    // Khi customer get conversation, server tự động đánh dấu đã đọc
    // Nên chỉ cần gọi getCustomerConversation
    return this.getCustomerConversation()
  }
}

