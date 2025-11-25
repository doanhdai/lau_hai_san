<template>
  <div v-if="showChat" class="fixed bottom-4 right-4 z-50 w-96 bg-white rounded-lg shadow-2xl flex flex-col"
       style="height: 600px; max-height: calc(100vh - 2rem);">
    <!-- Chat Header -->
    <div class="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between flex-shrink-0">
      <div class="flex items-center gap-2">
        <i class="fas fa-headset"></i>
        <span class="font-semibold">Hỗ trợ khách hàng</span>
      </div>
      <button @click="closeChat" class="text-white hover:text-gray-200">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- Messages List -->
    <div 
      class="flex-1 overflow-y-auto p-4 bg-gray-50 min-h-0" 
      ref="messagesContainer"
      @scroll="handleScroll"
    >
      <div v-if="loading" class="text-center py-8 text-gray-500">
        <i class="fas fa-spinner fa-spin text-2xl mb-2"></i>
        <p>Đang tải tin nhắn...</p>
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="(message, index) in messages"
          :key="message.id"
          class="flex flex-col"
          :class="message.senderType === 'CUSTOMER' ? 'items-end' : 'items-start'"
        >
          <!-- Timestamp separator for messages from different days or far apart -->
          <div 
            v-if="shouldShowTimestamp(message, messages[index - 1])" 
            class="w-full flex justify-center my-2"
          >
            <span class="text-xs text-gray-400 bg-gray-200 px-3 py-1 rounded-full">
              {{ formatDateSeparator(message.createdAt) }}
            </span>
          </div>
          
          <!-- Message bubble -->
          <div
            class="max-w-xs lg:max-w-md px-4 py-3 rounded-lg shadow-sm"
            :class="message.senderType === 'CUSTOMER' 
              ? 'bg-blue-600 text-white rounded-br-sm' 
              : 'bg-white text-gray-800 border rounded-bl-sm'"
          >
            <p class="text-sm leading-relaxed whitespace-pre-wrap break-words">{{ message.content }}</p>
            <p class="text-xs mt-2 opacity-70 flex items-center gap-1"
               :class="message.senderType === 'CUSTOMER' ? 'justify-end' : 'justify-start'">
              {{ formatTime(message.createdAt) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area - Fixed at bottom -->
    <div class="border-t p-4 bg-white flex-shrink-0">
      <form @submit.prevent="sendMessage" class="flex gap-2">
        <input
          v-model="messageInput"
          type="text"
          placeholder="Nhập tin nhắn..."
          class="flex-1 px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          :disabled="loading"
        />
        <button
          type="submit"
          :disabled="!messageInput.trim() || loading"
          class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <i class="fas fa-paper-plane"></i>
        </button>
      </form>
    </div>
  </div>

  <!-- Chat Button -->
  <button
    v-else
    @click="openChat"
    class="fixed bottom-4 right-4 z-50 bg-blue-600 text-white w-14 h-14 rounded-full shadow-lg hover:bg-blue-700 transition-colors flex items-center justify-center"
  >
    <i class="fas fa-comments text-xl"></i>
    <span
      v-if="unreadCount > 0"
      class="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-6 h-6 rounded-full flex items-center justify-center"
    >
      {{ unreadCount > 9 ? '9+' : unreadCount }}
    </span>
  </button>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { chatService } from '@/services/chatService'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const authStore = useAuthStore()

const showChat = ref(false)
const messageInput = ref('')
const messages = ref([])
const conversation = ref(null)
const loading = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)
const userScrolling = ref(false)
const scrollTimeout = ref(null)

let stompClient = null
let subscription = null

// Handle scroll event to detect user scrolling
function handleScroll() {
  userScrolling.value = true
  // Clear existing timeout
  if (scrollTimeout.value) {
    clearTimeout(scrollTimeout.value)
  }
  // Reset userScrolling after 2 seconds of no scrolling
  scrollTimeout.value = setTimeout(() => {
    userScrolling.value = false
  }, 2000)
}

function openChat() {
  showChat.value = true
  // Wait a bit for chat widget to render, then load conversation
  nextTick(() => {
    setTimeout(() => {
      loadConversation()
    }, 100)
  })
}

function closeChat() {
  showChat.value = false
}

async function loadConversation() {
  if (!authStore.user) return
  
  loading.value = true
  try {
    // Customer chỉ có 1 conversation với system
    const response = await chatService.getCustomerConversation(authStore.user.id)
    if (response && response.success) {
      conversation.value = response.data
      messages.value = response.data.messages || []
      unreadCount.value = 0 // Customer không có unread count
      
      // Wait for DOM to update and scroll to bottom
      await nextTick()
      setTimeout(() => {
        scrollToBottom(true) // Force scroll to bottom when first loading
      }, 100)
    }
  } catch (error) {
    console.error('Error loading conversation:', error)
  } finally {
    loading.value = false
    // Ensure scroll after loading completes
    await nextTick()
    setTimeout(() => {
      scrollToBottom(true)
    }, 150)
  }
}

async function sendMessage() {
  if (!messageInput.value.trim() || !authStore.user) return
  
  const messageText = messageInput.value.trim()
  messageInput.value = ''
  
  // Add optimistic message
  const tempMessage = {
    id: `temp-${Date.now()}`,
    conversationId: conversation.value?.conversationId,
    senderType: 'CUSTOMER',
    senderId: authStore.user.id,
    senderName: authStore.user.fullName,
    content: messageText,
    isRead: false,
    createdAt: new Date().toISOString()
  }
  messages.value.push(tempMessage)
  scrollToBottom(true) // Force scroll when user sends message
  
  // Try WebSocket first
  const sentViaWebSocket = sendMessageViaWebSocket(messageText)
  
  if (!sentViaWebSocket) {
    // Fallback to REST API
    try {
      const request = { content: messageText }
      const response = await chatService.sendMessageAsCustomer(request, authStore.user.id)
      
      if (response && response.success) {
        // Replace temp message with real one
        const index = messages.value.findIndex(m => m.id === tempMessage.id)
        if (index !== -1) {
          messages.value[index] = response.data
        }
      }
    } catch (error) {
      console.error('Error sending message:', error)
      // Remove temp message on error
      messages.value = messages.value.filter(m => m.id !== tempMessage.id)
      messageInput.value = messageText
    }
  }
}

function sendMessageViaWebSocket(content) {
  if (!stompClient || !stompClient.connected || !conversation.value) {
    return false
  }

  const payload = {
    senderType: 'CUSTOMER',
    senderId: authStore.user.id,
    conversationId: conversation.value.conversationId,
    content: content
  }

  try {
    stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload)
    })
    return true
  } catch (error) {
    console.error('Error sending via WebSocket:', error)
    return false
  }
}

function connectWebSocket() {
  if (!authStore.token || !authStore.user) {
    console.warn('[Customer WebSocket] No token or user available')
    return
  }

  // Disconnect existing connection if any
  if (stompClient && stompClient.connected) {
    console.log('[Customer WebSocket] Disconnecting existing connection...')
    stompClient.deactivate()
  }

  try {
    const host = import.meta.env.VITE_API_URL?.replace(/^https?:\/\//, '') || 'localhost:8080'
    const wsUrl = `http://${host}/ws?token=${encodeURIComponent(authStore.token)}`
    
    console.log('[Customer WebSocket] Connecting to:', wsUrl)
    
    stompClient = new Client({
      webSocketFactory: () => {
        console.log('[Customer WebSocket] Creating SockJS connection...')
        return new SockJS(wsUrl)
      },
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        'Authorization': `Bearer ${authStore.token}`
      },
      onConnect: (frame) => {
        console.log('[Customer WebSocket] ✅ Connected successfully!', frame)
        
        // Unsubscribe existing subscription if any
        if (subscription) {
          subscription.unsubscribe()
        }
        
        // Subscribe to customer's topic
        const topic = `/topic/chat/customer/${authStore.user.id}`
        console.log('[Customer WebSocket] Subscribing to:', topic)
        
        subscription = stompClient.subscribe(topic, (message) => {
          console.log('[Customer WebSocket] 📨 Received message on', topic, ':', message.body)
          try {
            const data = JSON.parse(message.body)
            console.log('[Customer WebSocket] Parsed message data:', data)
            handleIncomingMessage(data)
          } catch (error) {
            console.error('[Customer WebSocket] ❌ Error parsing message:', error)
            console.error('[Customer WebSocket] Raw message body:', message.body)
          }
        })
        
        console.log('[Customer WebSocket] ✅ Subscribed to', topic)
      },
      onDisconnect: () => {
        console.log('[Customer WebSocket] ❌ Disconnected')
        subscription = null
      },
      onStompError: (frame) => {
        console.error('[Customer WebSocket] ❌ STOMP error:', frame)
      },
      onWebSocketError: (event) => {
        console.error('[Customer WebSocket] ❌ WebSocket error:', event)
      }
    })
    
    stompClient.activate()
    console.log('[Customer WebSocket] Activation initiated...')
  } catch (error) {
    console.error('[Customer WebSocket] ❌ Error setting up WebSocket:', error)
  }
}

function handleIncomingMessage(data) {
  console.log('[handleIncomingMessage] 📥 Processing message:', data)
  
  if (!data || !data.id) {
    console.warn('[handleIncomingMessage] ⚠️ Invalid message data:', data)
    return
  }
  
  // If conversation is not loaded yet, try to load it
  if (!conversation.value || !conversation.value.conversationId) {
    console.log('[handleIncomingMessage] Conversation not loaded, loading now...')
    if (data.conversationId) {
      loadConversation().then(() => {
        // After loading, process the message again
        setTimeout(() => {
          handleIncomingMessage(data)
        }, 500)
      })
    }
    return
  }
  
  // Check if message belongs to current conversation
  if (data.conversationId && data.conversationId !== conversation.value.conversationId) {
    console.log('[handleIncomingMessage] Message belongs to different conversation, ignoring')
    return
  }
  
  // Check if message already exists
  const exists = messages.value.find(m => {
    if (m.id === data.id) return true
    if (m.id && m.id.toString().startsWith('temp-') && m.content === data.content && m.senderType === data.senderType) return true
    return false
  })
  
  const wasNearBottom = isNearBottom()
  const isMyMessage = data.senderType === 'CUSTOMER'
  
  console.log('[handleIncomingMessage] Message details:', {
    id: data.id,
    senderType: data.senderType,
    content: data.content,
    exists: !!exists,
    wasNearBottom,
    isMyMessage
  })
  
  if (exists) {
    // Replace temp message or update existing
    const index = messages.value.indexOf(exists)
    console.log('[handleIncomingMessage] Replacing message at index:', index)
    messages.value[index] = data
  } else {
    console.log('[handleIncomingMessage] Adding new message to array')
    messages.value.push(data)
  }
  
  // Only auto-scroll if:
  // 1. User sent the message (isMyMessage), OR
  // 2. User was already near bottom (wasNearBottom)
  if (isMyMessage || wasNearBottom) {
    console.log('[handleIncomingMessage] Auto-scrolling to bottom')
    scrollToBottom(true)
  }
  
  console.log('[handleIncomingMessage] ✅ Message processed, total messages:', messages.value.length)
}

// Check if user is near bottom of scroll
function isNearBottom() {
  if (!messagesContainer.value) return true
  const container = messagesContainer.value
  const threshold = 100 // pixels from bottom
  return container.scrollHeight - container.scrollTop - container.clientHeight < threshold
}

function scrollToBottom(force = false) {
  nextTick(() => {
    if (messagesContainer.value) {
      // Only scroll if:
      // 1. Force is true (user sent message or initial load), OR
      // 2. User is already near bottom AND not actively scrolling
      if (force || (isNearBottom() && !userScrolling.value)) {
        // Immediate scroll
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        
        // Additional scroll attempts to ensure it works
        setTimeout(() => {
          if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
          }
        }, 50)
        
        setTimeout(() => {
          if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
          }
        }, 150)
      }
    }
  })
}

function formatTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) return 'Vừa xong'
  if (diffMins < 60) return `${diffMins} phút trước`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)} giờ trước`
  
  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatDateSeparator(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (date.toDateString() === today.toDateString()) {
    return 'Hôm nay'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return 'Hôm qua'
  } else {
    return date.toLocaleDateString('vi-VN', {
      weekday: 'long',
      day: 'numeric',
      month: 'numeric'
    })
  }
}

function shouldShowTimestamp(currentMessage, previousMessage) {
  if (!previousMessage) return true
  
  const currentTime = new Date(currentMessage.createdAt).getTime()
  const previousTime = new Date(previousMessage.createdAt).getTime()
  const diffMinutes = (currentTime - previousTime) / (1000 * 60)
  
  // Show timestamp if messages are more than 5 minutes apart or different day
  if (diffMinutes > 5) {
    const currentDate = new Date(currentMessage.createdAt).toDateString()
    const previousDate = new Date(previousMessage.createdAt).toDateString()
    return currentDate !== previousDate
  }
  
  return false
}

// Remove auto-scroll watch - we'll handle scrolling manually
// Only scroll when user sends message or receives message while near bottom

onMounted(() => {
  console.log('[onMounted] CustomerChatWidget mounted')
  if (authStore.isAuthenticated && authStore.user) {
    console.log('[onMounted] User authenticated, connecting WebSocket...')
    connectWebSocket()
  } else {
    console.warn('[onMounted] User not authenticated')
  }
})

// Watch for chat opening to ensure WebSocket is connected
watch(() => showChat.value, (newVal) => {
  if (newVal && authStore.isAuthenticated && authStore.user) {
    console.log('[watch showChat] Chat opened, ensuring WebSocket connection...')
    // Ensure WebSocket is connected when chat opens
    if (!stompClient || !stompClient.connected) {
      console.log('[watch showChat] WebSocket not connected, connecting...')
      connectWebSocket()
    } else {
      console.log('[watch showChat] WebSocket already connected')
    }
  }
})

onUnmounted(() => {
  if (scrollTimeout.value) {
    clearTimeout(scrollTimeout.value)
  }
  if (subscription) {
    subscription.unsubscribe()
  }
  if (stompClient) {
    stompClient.deactivate()
  }
})
</script>

