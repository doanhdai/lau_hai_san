<template>
  <div v-if="showChat" class="fixed bottom-4 right-4 z-50 w-96 bg-white rounded-lg shadow-2xl flex flex-col"
       style="height: 600px;">
    <!-- Chat Header -->
    <div class="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
      <div class="flex items-center gap-2">
        <i class="fas fa-headset"></i>
        <span class="font-semibold">Hỗ trợ khách hàng</span>
      </div>
      <button @click="closeChat" class="text-white hover:text-gray-200">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- Chat Messages -->
    <div class="flex-1 flex flex-col">
      <!-- Messages List -->
      <div class="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50" ref="messagesContainer">
        <div v-if="loading" class="text-center py-8 text-gray-500">
          <i class="fas fa-spinner fa-spin text-2xl mb-2"></i>
          <p>Đang tải tin nhắn...</p>
        </div>
        <div v-else>
          <div
            v-for="message in messages"
            :key="message.id"
            class="flex"
            :class="message.senderType === 'CUSTOMER' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg"
              :class="message.senderType === 'CUSTOMER' 
                ? 'bg-blue-600 text-white' 
                : 'bg-white text-gray-800 border'"
            >
              <p class="text-sm">{{ message.content }}</p>
              <p class="text-xs mt-1 opacity-70">
                {{ formatTime(message.createdAt) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="border-t p-4 bg-white">
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

let stompClient = null
let subscription = null

function openChat() {
  showChat.value = true
  loadConversation()
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
      scrollToBottom()
    }
  } catch (error) {
    console.error('Error loading conversation:', error)
  } finally {
    loading.value = false
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
  scrollToBottom()
  
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
  if (!authStore.token || !authStore.user) return

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
        console.log('Customer chat WebSocket connected')
        
        // Subscribe to customer's topic
        subscription = stompClient.subscribe(`/topic/chat/customer/${authStore.user.id}`, (message) => {
          try {
            const data = JSON.parse(message.body)
            handleIncomingMessage(data)
          } catch (error) {
            console.error('Error parsing message:', error)
          }
        })
      },
      onDisconnect: () => {
        console.log('Customer chat WebSocket disconnected')
      }
    })
    
    stompClient.activate()
  } catch (error) {
    console.error('Error connecting WebSocket:', error)
  }
}

function handleIncomingMessage(data) {
  if (data.id && data.conversationId === conversation.value?.conversationId) {
    // Check if message already exists
    const exists = messages.value.find(m => {
      if (m.id === data.id) return true
      if (m.id && m.id.startsWith('temp-') && m.content === data.content) return true
      return false
    })
    
    if (exists) {
      // Replace temp message
      const index = messages.value.indexOf(exists)
      messages.value[index] = data
    } else {
      messages.value.push(data)
    }
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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
  
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(() => messages.value, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  if (authStore.isAuthenticated && authStore.user) {
    connectWebSocket()
  }
})

onUnmounted(() => {
  if (subscription) {
    subscription.unsubscribe()
  }
  if (stompClient) {
    stompClient.deactivate()
  }
})
</script>

