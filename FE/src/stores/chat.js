import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatService } from '@/services/chatService'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([]) // Tin nhắn trong cuộc trò chuyện hiện tại
  const conversations = ref([]) // Danh sách các cuộc trò chuyện
  const currentConversation = ref(null) // Cuộc trò chuyện đang mở
  const unreadCount = ref(0)
  const loading = ref(false)
  
  let stompClient = null
  let chatSubscription = null
  const authStore = useAuthStore()

  // Xác định loại user và ID
  const getUserType = () => {
    const user = authStore.user
    if (!user) return null
    
    if (user.roles?.includes('ROLE_ADMIN') || user.roles?.includes('ROLE_MANAGER') || user.roles?.includes('ROLE_STAFF')) {
      return { type: 'STAFF', id: user.id }
    }
    return { type: 'CUSTOMER', id: user.id }
  }

  // Kết nối WebSocket cho chat
  function connectChatWebSocket() {
    const userInfo = getUserType()
    if (!userInfo || !authStore.token) {
      console.warn('Cannot connect chat WebSocket: user not authenticated')
      return
    }

    if (stompClient && stompClient.connected) {
      return
    }

    try {
      const host = import.meta.env.VITE_API_URL?.replace(/^https?:\/\//, '') || 'localhost:8080'
      const wsUrl = `http://${host}/ws?token=${encodeURIComponent(authStore.token)}`
      
      stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 3000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        connectHeaders: {
          'Authorization': `Bearer ${authStore.token}`
        },
        onConnect: () => {
          console.log('Chat WebSocket connected')
          
          // Subscribe để nhận tin nhắn
          const topic = `/topic/chat/${userInfo.type}/${userInfo.id}`
          chatSubscription = stompClient.subscribe(topic, (message) => {
            try {
              const data = JSON.parse(message.body)
              handleIncomingMessage(data)
            } catch (error) {
              console.error('Error parsing chat message:', error)
            }
          })
        },
        onStompError: (frame) => {
          console.error('Chat WebSocket STOMP error:', frame)
        },
        onDisconnect: () => {
          console.log('Chat WebSocket disconnected')
        }
      })
      
      stompClient.activate()
    } catch (error) {
      console.error('Error connecting chat WebSocket:', error)
    }
  }

  // Xử lý tin nhắn đến
  function handleIncomingMessage(data) {
    const userInfo = getUserType()
    if (!userInfo) return
    
    if (data.type === 'MESSAGE_READ') {
      // Cập nhật trạng thái đã đọc
      messages.value = messages.value.map(msg => {
        if (msg.receiverType === data.receiverType && msg.receiverId === data.receiverId) {
          return { ...msg, isRead: true }
        }
        return msg
      })
    } else if (data.id) {
      // Tin nhắn mới
      if (currentConversation.value && 
          ((data.senderType === currentConversation.value.otherUserType && 
            data.senderId === currentConversation.value.otherUserId) ||
           (data.receiverType === currentConversation.value.otherUserType && 
            data.receiverId === currentConversation.value.otherUserId))) {
        // Kiểm tra xem đã có tin nhắn này chưa (theo ID hoặc theo content và time)
        const exists = messages.value.find(m => {
          // Nếu có ID thật từ server, so sánh theo ID
          if (m.id === data.id) return true
          // Nếu là tin nhắn tạm (temp-), thay thế bằng tin nhắn thật
          if (m.id && m.id.startsWith('temp-') && 
              m.senderType === data.senderType && 
              m.senderId === data.senderId &&
              m.content === data.content) {
            return true
          }
          return false
        })
        
        if (exists) {
          // Thay thế tin nhắn tạm bằng tin nhắn thật
          const index = messages.value.indexOf(exists)
          messages.value[index] = data
        } else {
          messages.value.push(data)
        }
      }
      
      // Cập nhật unread count nếu không phải tin nhắn của mình
      if (data.senderType !== userInfo.type || data.senderId !== userInfo.id) {
        loadUnreadCount()
        loadConversations() // Refresh conversations để cập nhật last message
      }
    }
  }

  // Gửi tin nhắn qua WebSocket hoặc REST API fallback
  async function sendMessage(content, receiverType, receiverId) {
    const userInfo = getUserType()
    if (!userInfo) {
      console.error('Cannot send message: user not authenticated')
      return
    }

    // Tạo tin nhắn tạm thời để hiển thị ngay (optimistic update)
    const tempMessage = {
      id: `temp-${Date.now()}`,
      senderType: userInfo.type,
      senderId: userInfo.id,
      senderName: authStore.user?.fullName || 'Bạn',
      receiverType,
      receiverId,
      content,
      isRead: false,
      createdAt: new Date().toISOString()
    }

    // Thêm vào danh sách messages ngay lập tức
    if (currentConversation.value && 
        currentConversation.value.otherUserType === receiverType &&
        currentConversation.value.otherUserId === receiverId) {
      messages.value.push(tempMessage)
    }

    // Gửi qua WebSocket nếu đã kết nối
    if (stompClient && stompClient.connected) {
      try {
        const payload = {
          senderType: userInfo.type,
          senderId: userInfo.id,
          receiverType,
          receiverId,
          content
        }

        stompClient.publish({
          destination: '/app/chat.send',
          body: JSON.stringify(payload)
        })
        console.log('Message sent via WebSocket')
      } catch (error) {
        console.error('Error sending via WebSocket, trying REST API:', error)
        // Fallback to REST API
        sendMessageViaRest(content, receiverType, receiverId, tempMessage)
      }
    } else {
      console.warn('WebSocket not connected, using REST API fallback')
      // Fallback to REST API
      sendMessageViaRest(content, receiverType, receiverId, tempMessage)
    }
  }

  // Gửi tin nhắn qua REST API (fallback)
  async function sendMessageViaRest(content, receiverType, receiverId, tempMessage) {
    const userInfo = getUserType()
    if (!userInfo) return

    try {
      let response
      if (userInfo.type === 'CUSTOMER') {
        // Customer gửi tin nhắn đến system
        const request = { content }
        response = await chatService.sendMessageAsCustomer(request)
      } else {
        // Staff gửi tin nhắn đến customer
        // Tìm conversation với customer có ID = receiverId
        const allConvsResponse = await chatService.getConversations()
        if (allConvsResponse && allConvsResponse.success) {
          const conv = (allConvsResponse.data || []).find(c => c.customerId === receiverId)
          if (conv) {
            const request = {
              content,
              conversationId: conv.id || conv.conversationId
            }
            response = await chatService.sendMessage(request)
          } else {
            throw new Error('Conversation not found')
          }
        }
      }

      if (response && response.success) {
        // Thay thế tin nhắn tạm bằng tin nhắn thật từ server
        const realMessage = response.data
        if (currentConversation.value) {
          const index = messages.value.findIndex(m => m.id === tempMessage.id)
          if (index !== -1) {
            messages.value[index] = realMessage
          } else {
            messages.value.push(realMessage)
          }
        }
        console.log('Message sent via REST API')
      }
    } catch (error) {
      console.error('Error sending message via REST API:', error)
      // Xóa tin nhắn tạm nếu gửi thất bại
      if (currentConversation.value) {
        messages.value = messages.value.filter(m => m.id !== tempMessage.id)
      }
      alert('Không thể gửi tin nhắn. Vui lòng thử lại.')
    }
  }

  // Đánh dấu đã đọc qua WebSocket
  function markAsReadWebSocket(senderType, senderId) {
    const userInfo = getUserType()
    if (!userInfo || !stompClient || !stompClient.connected) {
      return
    }

    const payload = {
      receiverType: userInfo.type,
      receiverId: userInfo.id,
      senderType,
      senderId
    }

    stompClient.publish({
      destination: '/app/chat.read',
      body: JSON.stringify(payload)
    })
  }

  // Load cuộc trò chuyện
  async function loadConversation(otherUserType, otherUserId) {
    const userInfo = getUserType()
    if (!userInfo) return

    loading.value = true
    try {
      let response
      if (userInfo.type === 'STAFF') {
        // Staff: tìm conversation với customer có ID = otherUserId
        // Tạm thời load tất cả conversations và tìm
        const allConvsResponse = await chatService.getConversations()
        if (allConvsResponse && allConvsResponse.success) {
          const conv = (allConvsResponse.data || []).find(c => c.customerId === otherUserId)
          if (conv) {
            response = await chatService.getConversation(conv.id || conv.conversationId)
          }
        }
      } else {
        // Customer: lấy conversation của chính họ (không cần customerId)
        response = await chatService.getCustomerConversation()
      }
      
      if (response && response.success) {
        messages.value = response.data?.messages || []
        currentConversation.value = {
          otherUserType,
          otherUserId,
          conversationId: response.data?.id || response.data?.conversationId
        }
        
        // Đánh dấu đã đọc
        if (response.data?.id || response.data?.conversationId) {
          await markAsRead(response.data.id || response.data.conversationId)
        }
      }
    } catch (error) {
      console.error('Error loading conversation:', error)
    } finally {
      loading.value = false
    }
  }

  // Load danh sách cuộc trò chuyện
  async function loadConversations() {
    const userInfo = getUserType()
    if (!userInfo) return

    loading.value = true
    try {
      // Staff dùng endpoint /conversations, customer không có danh sách conversations (chỉ có 1 conversation với system)
      let response
      if (userInfo.type === 'STAFF') {
        response = await chatService.getConversations() // Không truyền tham số
      } else {
        // Customer: lấy conversation của chính họ (không cần customerId)
        response = await chatService.getCustomerConversation()
        // Convert thành array format
        if (response && response.success && response.data) {
          conversations.value = [response.data]
          unreadCount.value = response.data.unreadCount || 0
          loading.value = false
          return
        }
        conversations.value = []
        unreadCount.value = 0
        loading.value = false
        return
      }
      
      if (response && response.success) {
        conversations.value = response.data || []
        // Tính tổng unread count
        unreadCount.value = conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
      }
    } catch (error) {
      console.error('Error loading conversations:', error)
      // Nếu lỗi 403 (Forbidden), có thể là customer đang cố truy cập endpoint staff
      if (error.response?.status === 403) {
        console.warn('Access denied - user may not have permission for this endpoint')
      }
    } finally {
      loading.value = false
    }
  }

  // Đánh dấu đã đọc
  async function markAsRead(conversationId) {
    const userInfo = getUserType()
    if (!userInfo) return

    try {
      if (userInfo.type === 'STAFF') {
        await chatService.markAsRead(conversationId)
      } else {
        // Customer: đánh dấu đã đọc tin nhắn từ system (tự động khi get conversation)
        await chatService.markCustomerMessagesAsRead()
      }
      
      // Cập nhật local state
      messages.value = messages.value.map(msg => ({ ...msg, isRead: true }))
      
      // Cập nhật conversations
      const conv = conversations.value.find(c => 
        (c.id || c.conversationId) === conversationId
      )
      if (conv) {
        conv.unreadCount = 0
      }
      
      loadUnreadCount()
    } catch (error) {
      console.error('Error marking as read:', error)
    }
  }

  // Load unread count
  async function loadUnreadCount() {
    const userInfo = getUserType()
    if (!userInfo) return

    try {
      if (userInfo.type === 'STAFF') {
        // Staff: lấy từ endpoint unread-count
        const response = await chatService.getUnreadConversationsCount()
        if (response && response.success) {
          unreadCount.value = response.data || 0
        }
      } else {
        // Customer: lấy unread count từ conversation của họ
        const response = await chatService.getCustomerUnreadCount()
        if (response && response.success) {
          unreadCount.value = response.data || 0
        }
      }
    } catch (error) {
      console.error('Error loading unread count:', error)
    }
  }

  // Disconnect WebSocket
  function disconnectChatWebSocket() {
    if (chatSubscription) {
      try {
        chatSubscription.unsubscribe()
      } catch (e) {
        // Ignore
      }
      chatSubscription = null
    }
    
    if (stompClient) {
      if (stompClient.connected) {
        stompClient.deactivate()
      }
      stompClient = null
    }
  }

  // Start chat system
  function start() {
    const userInfo = getUserType()
    if (!userInfo) {
      console.warn('Cannot start chat: user not authenticated')
      return
    }
    
    connectChatWebSocket()
    // Chỉ load conversations và unread count nếu user đã đăng nhập
    if (authStore.isAuthenticated && authStore.user) {
      loadConversations()
      loadUnreadCount()
    }
  }

  // Stop chat system
  function stop() {
    disconnectChatWebSocket()
    messages.value = []
    conversations.value = []
    currentConversation.value = null
    unreadCount.value = 0
  }

  return {
    messages,
    conversations,
    currentConversation,
    unreadCount,
    loading,
    sendMessage,
    loadConversation,
    loadConversations,
    markAsRead,
    loadUnreadCount,
    start,
    stop,
    isConnected: computed(() => stompClient && stompClient.connected)
  }
})

