<template>
  <div class="h-screen flex bg-white dark:bg-gray-900 text-gray-900 dark:text-white overflow-hidden">
    <!-- Left Panel - Chat List -->
    <div class="w-96 border-r border-gray-200 dark:border-gray-700 flex flex-col bg-white dark:bg-gray-800 shadow-lg">
      <!-- Header -->
      <div class="p-5 border-b border-gray-200 dark:border-gray-700 bg-slate-900 dark:bg-slate-800">
        <!-- <div class="flex items-center justify-between mb-4">
          <h2 class="text-2xl font-bold text-white flex items-center gap-2">
            <i class="fas fa-comments"></i>
            <span>Tin nhắn khách hàng</span>
          </h2>
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 rounded-full" :class="wsConnected ? 'bg-green-400 animate-pulse' : 'bg-red-400'"></div>
            <span class="text-xs text-white/80">{{ wsConnected ? 'Đã kết nối' : 'Đang kết nối...' }}</span>
          </div>
        </div> -->
        <div class="relative">
          <i class="fas fa-search absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm cuộc trò chuyện..."
            class="w-full pl-11 pr-4 py-3 bg-white dark:bg-gray-700 border-0 rounded-xl text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 dark:focus:ring-blue-500 shadow-sm transition-all"
          />
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900">
        <button
          @click="activeTab = 'all'"
          class="flex-1 px-4 py-3.5 text-sm font-semibold transition-all relative group"
          :class="activeTab === 'all' 
            ? 'text-slate-900 dark:text-slate-300 bg-white dark:bg-gray-800' 
            : 'text-gray-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-slate-300'"
        >
          <span class="flex items-center justify-center gap-2">
            <i class="fas fa-comments"></i>
            <span>Tất cả</span>
          </span>
          <span v-if="activeTab === 'all'" class="absolute bottom-0 left-0 right-0 h-1 bg-slate-900 dark:bg-slate-700 rounded-t-full"></span>
        </button>
        <button
          @click="activeTab = 'unread'"
          class="flex-1 px-4 py-3.5 text-sm font-semibold transition-all relative group"
          :class="activeTab === 'unread' 
            ? 'text-slate-900 dark:text-slate-300 bg-white dark:bg-gray-800' 
            : 'text-gray-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-slate-300'"
        >
          <span class="flex items-center justify-center gap-2">
            <i class="fas fa-envelope"></i>
            <span>Chưa đọc</span>
            <span v-if="unreadCount > 0" class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full font-bold min-w-[24px] text-center">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </span>
          <span v-if="activeTab === 'unread'" class="absolute bottom-0 left-0 right-0 h-1 bg-slate-900 dark:bg-slate-700 rounded-t-full"></span>
        </button>
      </div>

      <!-- Conversations List -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="loading" class="flex flex-col items-center justify-center py-12">
          <i class="fas fa-spinner fa-spin text-3xl text-slate-700 dark:text-slate-400 mb-3"></i>
          <p class="text-sm text-gray-500 dark:text-gray-400">Đang tải...</p>
        </div>
        <div v-else-if="filteredConversations.length === 0" class="text-center py-12 px-4">
          <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
            <i class="fas fa-comments text-3xl text-gray-400"></i>
          </div>
          <p class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Không có cuộc trò chuyện nào</p>
          <p class="text-xs text-gray-500 dark:text-gray-500">Cuộc trò chuyện sẽ xuất hiện khi khách hàng gửi tin nhắn</p>
        </div>
        <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
          <div
            v-for="conv in filteredConversations"
            :key="conv.conversationId"
            @click="selectConversation(conv)"
            class="w-full p-4 hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-all cursor-pointer group relative"
            :class="{ 
              'bg-gray-100 dark:bg-gray-700/70 border-l-4 border-slate-900 dark:border-slate-700': 
                selectedConversation?.conversationId === conv.conversationId 
            }"
          >
            <div class="flex items-start gap-3">
              <!-- Avatar -->
              <div class="relative flex-shrink-0">
                <div class="w-12 h-12 rounded-full bg-slate-700 dark:bg-slate-600 flex items-center justify-center shadow-lg ring-2 ring-white dark:ring-gray-800"
                     :class="{ 'ring-slate-900 dark:ring-slate-700 ring-4': selectedConversation?.conversationId === conv.conversationId }">
                  <span class="text-base font-bold text-white">
                    {{ getInitials(conv.userName || conv.customerName || 'Người dùng') }}
                  </span>
                </div>
                <span v-if="conv.unreadCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 rounded-full flex items-center justify-center text-xs text-white font-bold ring-2 ring-white dark:ring-gray-800">
                  {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
                </span>
              </div>
              
              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1.5">
                  <p class="font-bold text-sm truncate text-gray-900 dark:text-white">
                    {{ conv.userName || conv.customerName || 'Người dùng' }}
                  </p>
                  <span v-if="conv.lastMessageAt || conv.lastMessageTime" class="text-xs text-gray-500 dark:text-gray-400 flex-shrink-0 ml-2 whitespace-nowrap">
                    {{ formatTime(conv.lastMessageAt || conv.lastMessageTime) }}
                  </span>
                </div>
                <div class="flex items-center gap-2">
                  <p v-if="conv.lastMessage && conv.lastMessage.content" 
                     class="text-xs text-gray-600 dark:text-gray-300 truncate flex-1"
                     :class="{ 'font-semibold text-gray-900 dark:text-white': conv.unreadCount > 0 }">
                    <span v-if="conv.lastMessage.senderType === 'SYSTEM'" class="text-slate-700 dark:text-slate-400 font-medium">Bạn: </span>
                    {{ conv.lastMessage.content }}
                  </p>
                  <p v-else class="text-xs text-gray-400 dark:text-gray-500 truncate flex-1 italic">
                    Chưa có tin nhắn
                  </p>
                </div>
                <!-- User info -->
                <div v-if="conv.userPhone || conv.userEmail" class="flex items-center gap-2 mt-1.5">
                  <i class="fas fa-phone text-xs text-gray-400"></i>
                  <span class="text-xs text-gray-500 dark:text-gray-400 truncate">
                    {{ conv.userPhone || conv.userEmail }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel - Chat Messages -->
    <div class="flex-1 flex flex-col bg-white dark:bg-gray-900" v-if="selectedConversation">
      <!-- Chat Header -->
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between bg-slate-900 dark:bg-slate-800 shadow-md">
        <div class="flex items-center gap-4">
          <!-- Avatar -->
          <div class="w-12 h-12 rounded-full bg-white dark:bg-gray-800 flex items-center justify-center shadow-lg ring-2 ring-white/50">
            <span class="text-base font-bold text-slate-700 dark:text-slate-300">
              {{ getInitials(selectedConversation.userName || selectedConversation.customerName || 'Người dùng') }}
            </span>
          </div>
          <div>
            <p class="font-bold text-white text-base">
              {{ selectedConversation.userName || selectedConversation.customerName || 'Người dùng' }}
            </p>
            <div class="flex items-center gap-3 mt-0.5">
              <p v-if="selectedConversation.userPhone || selectedConversation.customerPhone" class="text-xs text-gray-300 dark:text-gray-400 flex items-center gap-1">
                <i class="fas fa-phone text-xs"></i>
                {{ selectedConversation.userPhone || selectedConversation.customerPhone }}
              </p>
              <p v-if="selectedConversation.userEmail || selectedConversation.customerEmail" class="text-xs text-gray-300 dark:text-gray-400 flex items-center gap-1">
                <i class="fas fa-envelope text-xs"></i>
                {{ selectedConversation.userEmail || selectedConversation.customerEmail }}
              </p>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button class="p-2.5 hover:bg-white/20 rounded-lg transition-colors text-white" title="Gọi điện">
            <i class="fas fa-phone text-sm"></i>
          </button>
          <button class="p-2.5 hover:bg-white/20 rounded-lg transition-colors text-white" title="Thông tin">
            <i class="fas fa-info-circle text-sm"></i>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 overflow-y-auto p-6 bg-white dark:bg-gray-900 space-y-4 min-h-0" ref="messagesContainer">
        <div v-if="loadingMessages" class="flex flex-col items-center justify-center py-12">
          <i class="fas fa-spinner fa-spin text-3xl text-slate-700 dark:text-slate-400 mb-3"></i>
          <p class="text-sm text-gray-500 dark:text-gray-400">Đang tải tin nhắn...</p>
        </div>
        <div v-else-if="messages.length === 0" class="flex flex-col items-center justify-center py-16 px-4">
          <div class="w-20 h-20 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mb-4">
            <i class="fas fa-comment-dots text-3xl text-gray-400"></i>
          </div>
          <p class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Chưa có tin nhắn nào</p>
          <p class="text-xs text-gray-500 dark:text-gray-500">Hãy bắt đầu cuộc trò chuyện với khách hàng!</p>
        </div>
        <div v-else>
          <template v-for="(message, index) in messages" :key="message.id || `msg-${index}`">
            <!-- Timestamp separator -->
            <div v-if="shouldShowTimestamp(message, messages[index - 1])" 
                 class="flex justify-center my-6">
              <span class="text-xs font-medium text-gray-500 dark:text-gray-400 bg-white dark:bg-gray-800 px-4 py-1.5 rounded-full shadow-sm border border-gray-200 dark:border-gray-700">
                {{ formatDateSeparator(message.createdAt) }}
              </span>
            </div>
            
            <!-- Message -->
            <div
              class="flex items-start gap-3 mb-4 group"
              :class="message.senderType === 'SYSTEM' ? 'justify-end' : 'justify-start'"
            >
              <!-- Customer message layout -->
              <template v-if="message.senderType === 'CUSTOMER'">
                <!-- Avatar -->
                <div class="w-10 h-10 rounded-full bg-slate-700 dark:bg-slate-600 flex items-center justify-center flex-shrink-0 shadow-md">
                  <span class="text-xs font-bold text-white">
                    {{ getInitials(message.senderName || selectedConversation.userName || selectedConversation.customerName) }}
                  </span>
                </div>
                
                <!-- Message bubble -->
                <div class="flex flex-col items-start max-w-[70%]">
                  <span class="text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1 px-1">
                    {{ message.senderName || selectedConversation.userName || selectedConversation.customerName || 'Người dùng' }}
                  </span>
                  <div class="px-4 py-3 rounded-2xl rounded-tl-sm bg-white dark:bg-gray-800 shadow-md border border-gray-200 dark:border-gray-700">
                    <p class="text-sm leading-relaxed whitespace-pre-wrap break-words text-gray-900 dark:text-white">
                      {{ message.content }}
                    </p>
                    <div class="flex items-center justify-start gap-1 mt-2">
                      <span class="text-xs text-gray-500 dark:text-gray-400">
                        {{ formatMessageTime(message.createdAt) }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
              
              <!-- System message layout (Admin) -->
              <template v-else>
                <!-- Message bubble -->
                <div class="flex flex-col items-end max-w-[70%]">
                  <span class="text-xs font-semibold text-slate-700 dark:text-slate-400 mb-1 px-1">
                    {{ message.senderName || authStore.user?.fullName || 'Bạn' }}
                  </span>
                  <div class="px-4 py-3 rounded-2xl rounded-tr-sm bg-slate-900 dark:bg-slate-800 text-white shadow-md">
                    <p class="text-sm leading-relaxed whitespace-pre-wrap break-words">
                      {{ message.content }}
                    </p>
                    <div class="flex items-center justify-end gap-1.5 mt-2">
                      <span class="text-xs opacity-90">
                        {{ formatMessageTime(message.createdAt) }}
                      </span>
                      <span v-if="message.isRead" class="text-xs opacity-90">
                        <i class="fas fa-check-double"></i>
                      </span>
                      <span v-else class="text-xs opacity-90">
                        <i class="fas fa-check"></i>
                      </span>
                    </div>
                  </div>
                </div>
                
                <!-- Avatar -->
                <div class="w-10 h-10 rounded-full bg-slate-600 dark:bg-slate-700 flex items-center justify-center flex-shrink-0 shadow-md">
                  <span class="text-xs font-bold text-white">
                    {{ getInitials(message.senderName || authStore.user?.fullName || 'AD') }}
                  </span>
                </div>
              </template>
            </div>
          </template>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-4 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shadow-lg">
        <form @submit.prevent="sendMessage" class="flex items-end gap-3">
          <button type="button" class="p-3 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition-colors text-gray-600 dark:text-gray-400" title="Gửi file">
            <i class="fas fa-paperclip text-lg"></i>
          </button>
          <div class="flex-1 flex items-center gap-2 bg-gray-100 dark:bg-gray-700 rounded-2xl px-4 py-3 border-2 border-transparent focus-within:border-slate-900 dark:focus-within:border-slate-700 transition-colors">
            <input
              v-model="messageInput"
              type="text"
              placeholder="Nhập tin nhắn..."
              class="flex-1 bg-transparent border-0 text-gray-900 dark:text-white text-sm placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none"
              :disabled="loadingMessages"
            />
            <button type="button" class="p-1.5 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors text-gray-500 dark:text-gray-400" title="Emoji">
              <i class="fas fa-smile text-lg"></i>
            </button>
          </div>
          <button
            type="submit"
            :disabled="!messageInput.trim() || loadingMessages"
            class="p-3 bg-slate-900 dark:bg-slate-800 text-white rounded-xl hover:bg-slate-800 dark:hover:bg-slate-700 transition-all shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-slate-900 disabled:hover:shadow-md"
            title="Gửi tin nhắn"
          >
            <i class="fas fa-paper-plane text-sm"></i>
          </button>
        </form>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="flex-1 flex items-center justify-center bg-white dark:bg-gray-900">
      <div class="text-center px-4">
        <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
          <i class="fas fa-comments text-4xl text-slate-700 dark:text-slate-400"></i>
        </div>
        <p class="text-xl font-bold text-gray-900 dark:text-white mb-2">Chọn một cuộc trò chuyện để bắt đầu</p>
        <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto">
          Cuộc trò chuyện sẽ được tạo tự động khi khách hàng gửi tin nhắn. Bạn có thể xem và phản hồi tất cả tin nhắn từ khách hàng tại đây.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { chatService } from '@/services/chatService'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const authStore = useAuthStore()

const conversations = ref([])
const selectedConversation = ref(null)
const messages = ref([])
const messageInput = ref('')
const searchQuery = ref('')
const activeTab = ref('all')
const loading = ref(false)
const loadingMessages = ref(false)
const messagesContainer = ref(null)
const wsConnected = ref(false)

const unreadCount = computed(() => {
  return conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
})

const filteredConversations = computed(() => {
  let result = conversations.value

  // Filter by tab
  if (activeTab.value === 'unread') {
    result = result.filter(conv => (conv.unreadCount || 0) > 0)
  }

  // Filter by search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(conv =>
      (conv.userName || conv.customerName)?.toLowerCase().includes(query) ||
      (conv.userPhone || conv.customerPhone)?.includes(query) ||
      (conv.userEmail || conv.customerEmail)?.toLowerCase().includes(query) ||
      conv.lastMessage?.content?.toLowerCase().includes(query)
    )
  }

  // Sort by last message time
  return result.sort((a, b) => {
    const timeA = (a.lastMessageAt || a.lastMessageTime) ? new Date(a.lastMessageAt || a.lastMessageTime).getTime() : 0
    const timeB = (b.lastMessageAt || b.lastMessageTime) ? new Date(b.lastMessageAt || b.lastMessageTime).getTime() : 0
    return timeB - timeA
  })
})

function getInitials(name) {
  if (!name) return '?'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }
  return name[0].toUpperCase()
}

function formatTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) return 'Vừa xong'
  if (diffMins < 60) return `${diffMins} phút`
  if (diffMins < 1440) {
    const hours = Math.floor(diffMins / 60)
    return hours === 1 ? '1 giờ' : `${hours} giờ`
  }
  if (diffMins < 10080) {
    const days = Math.floor(diffMins / 1440)
    return days === 1 ? '1 ngày' : `${days} ngày`
  }
  
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit'
  })
}

function formatMessageTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
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
  
  return diffMinutes > 5
}

// Lấy danh sách conversations
async function loadConversations() {
  loading.value = true
  try {
    const response = await chatService.getConversations()
    if (response && response.success) {
      conversations.value = (response.data || []).map(conv => ({
        conversationId: conv.conversationId,
        userName: conv.userName || conv.customerName || 'Người dùng',
        customerName: conv.customerName || conv.userName,
        userPhone: conv.userPhone,
        userEmail: conv.userEmail,
        customerPhone: conv.customerPhone,
        customerEmail: conv.customerEmail,
        lastMessage: conv.lastMessage,
        lastMessageTime: conv.lastMessageTime || conv.lastMessageAt,
        unreadCount: conv.unreadCount || 0
      }))
    }
  } catch (error) {
    console.error('Error loading conversations:', error)
  } finally {
    loading.value = false
  }
}

// Chọn conversation và load messages
async function selectConversation(conv) {
  console.log('Select conversation:', conv.conversationId)
  selectedConversation.value = conv
  loadingMessages.value = true
  messages.value = []
  
  try {
    const response = await chatService.getConversationMessages(conv.conversationId)
    if (response && response.success) {
      messages.value = response.data || []
      console.log('Loaded messages:', messages.value.length)
      
      // Mark as read
      try {
        await chatService.markAsRead(conv.conversationId)
        // Update unread count
        const foundConv = conversations.value.find(c => c.conversationId === conv.conversationId)
        if (foundConv) {
          foundConv.unreadCount = 0
        }
      } catch (error) {
        console.error('Error marking as read:', error)
      }
      
      // Wait for DOM to update and then scroll
      await nextTick()
      // Use setTimeout to ensure messages are fully rendered
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  } catch (error) {
    console.error('Error loading messages:', error)
    messages.value = []
  } finally {
    loadingMessages.value = false
    // Ensure scroll after loading completes
    await nextTick()
    setTimeout(() => {
      scrollToBottom()
    }, 150)
  }
}

// Gửi tin nhắn
async function sendMessage() {
  if (!messageInput.value.trim() || !selectedConversation.value) return
  
  const messageText = messageInput.value.trim()
  const conversationId = selectedConversation.value.conversationId
  
  // Create optimistic message
  const tempMessage = {
    id: `temp-${Date.now()}`,
    conversationId: conversationId,
    senderType: 'SYSTEM',
    senderId: authStore.user?.id,
    senderName: authStore.user?.fullName || 'Bạn',
    content: messageText,
    isRead: false,
    createdAt: new Date().toISOString()
  }
  
  // Add optimistic message
  messages.value.push(tempMessage)
  messageInput.value = ''
  scrollToBottom()
  
  // Update conversation list
  const conv = conversations.value.find(c => c.conversationId === conversationId)
  if (conv) {
    conv.lastMessage = tempMessage
    conv.lastMessageAt = tempMessage.createdAt
    conv.lastMessageTime = tempMessage.createdAt
  }
  
  // Try WebSocket first
  const sentViaWebSocket = sendMessageViaWebSocket(messageText, conversationId)
  
  if (!sentViaWebSocket) {
    // Fallback to REST API
    try {
      const request = {
        content: messageText,
        conversationId: conversationId
      }
      
      const response = await chatService.sendMessage(request)
      if (response && response.success && response.data) {
        // Replace temp message
        const tempIndex = messages.value.findIndex(m => m.id === tempMessage.id)
        if (tempIndex !== -1) {
          messages.value[tempIndex] = response.data
        } else {
          messages.value.push(response.data)
        }
        
        // Update conversation
        if (conv) {
          conv.lastMessage = response.data
          conv.lastMessageAt = response.data.createdAt
          conv.lastMessageTime = response.data.createdAt
        }
        
        scrollToBottom()
      } else {
        // Remove temp message on error
        const tempIndex = messages.value.findIndex(m => m.id === tempMessage.id)
        if (tempIndex !== -1) {
          messages.value.splice(tempIndex, 1)
        }
        messageInput.value = messageText
        alert('Không thể gửi tin nhắn. Vui lòng thử lại.')
      }
    } catch (error) {
      console.error('Error sending message:', error)
      const tempIndex = messages.value.findIndex(m => m.id === tempMessage.id)
      if (tempIndex !== -1) {
        messages.value.splice(tempIndex, 1)
      }
      messageInput.value = messageText
      alert('Lỗi khi gửi tin nhắn: ' + (error.response?.data?.message || error.message))
    }
  }
}

function scrollToBottom(force = false) {
  // Use multiple attempts to ensure scroll works
  nextTick(() => {
    if (messagesContainer.value) {
      // Immediate scroll
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      
      // Additional scroll after a short delay to ensure it works
      setTimeout(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      }, 50)
      
      // One more attempt for safety
      setTimeout(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      }, 150)
    }
  })
}

// Watch for new messages and scroll to bottom
watch(() => messages.value, () => {
  // Only auto-scroll if user is near bottom or it's a new conversation
  if (messages.value.length > 0) {
    nextTick(() => {
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    })
  }
}, { deep: true })

// Watch for conversation selection to scroll to bottom
watch(() => selectedConversation.value?.conversationId, (newId) => {
  if (newId && messages.value.length > 0) {
    nextTick(() => {
      setTimeout(() => {
        scrollToBottom()
      }, 200)
    })
  }
})

// WebSocket connection
let stompClient = null
let subscription = null

function connectWebSocket() {
  const token = authStore.token
  
  if (!token) {
    console.warn('[WebSocket] No token available')
    return
  }

  // Disconnect existing connection if any
  if (stompClient && stompClient.connected) {
    console.log('[WebSocket] Disconnecting existing connection...')
    stompClient.deactivate()
  }

  try {
    const host = import.meta.env.VITE_API_URL?.replace(/^https?:\/\//, '') || 'localhost:8080'
    const wsUrl = `http://${host}/ws?token=${encodeURIComponent(token)}`
    
    console.log('[WebSocket] Connecting to:', wsUrl)
    
    stompClient = new Client({
      webSocketFactory: () => {
        console.log('[WebSocket] Creating SockJS connection...')
        return new SockJS(wsUrl)
      },
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      onConnect: (frame) => {
        console.log('[WebSocket] ✅ Connected successfully!', frame)
        wsConnected.value = true
        
        // Subscribe to system topic (for all staff/admin)
        if (subscription) {
          subscription.unsubscribe()
        }
        
        subscription = stompClient.subscribe('/topic/chat/system', (message) => {
          console.log('[WebSocket] 📨 Received message on /topic/chat/system:', message.body)
          try {
            const data = JSON.parse(message.body)
            console.log('[WebSocket] Parsed message data:', data)
            handleIncomingMessage(data)
          } catch (error) {
            console.error('[WebSocket] ❌ Error parsing message:', error)
            console.error('[WebSocket] Raw message body:', message.body)
          }
        })
        
        console.log('[WebSocket] ✅ Subscribed to /topic/chat/system')
      },
      onDisconnect: () => {
        console.log('[WebSocket] ❌ Disconnected')
        wsConnected.value = false
        subscription = null
      },
      onStompError: (frame) => {
        console.error('[WebSocket] ❌ STOMP error:', frame)
        wsConnected.value = false
      },
      onWebSocketError: (event) => {
        console.error('[WebSocket] ❌ WebSocket error:', event)
        wsConnected.value = false
      }
    })
    
    stompClient.activate()
    console.log('[WebSocket] Activation initiated...')
  } catch (error) {
    console.error('[WebSocket] ❌ Error setting up WebSocket:', error)
    wsConnected.value = false
  }
}

function handleIncomingMessage(data) {
  console.log('[handleIncomingMessage] 📥 Processing message:', data)
  
  if (!data) {
    console.warn('[handleIncomingMessage] ⚠️ Received null or undefined data')
    return
  }
  
  if (data.type === 'MESSAGE_READ') {
    console.log('[handleIncomingMessage] Marking conversation as read:', data.conversationId)
    const conv = conversations.value.find(c => c.conversationId === data.conversationId)
    if (conv) {
      conv.unreadCount = 0
    }
    // Also update selected conversation if it's the same
    if (selectedConversation.value?.conversationId === data.conversationId) {
      selectedConversation.value.unreadCount = 0
    }
  } else if (data.id && data.conversationId) {
    const conversationId = data.conversationId
    console.log('[handleIncomingMessage] 📨 New message for conversation:', conversationId)
    console.log('[handleIncomingMessage] Message details:', {
      id: data.id,
      senderType: data.senderType,
      senderName: data.senderName,
      content: data.content,
      conversationId: conversationId
    })
    
    // Find conversation in list
    let conv = conversations.value.find(c => c.conversationId === conversationId)
    
    if (!conv) {
      console.log('[handleIncomingMessage] Conversation not in list, reloading...')
      // Reload conversations if not found
      loadConversations().then(() => {
        setTimeout(() => {
          conv = conversations.value.find(c => c.conversationId === conversationId)
          if (conv) {
            console.log('[handleIncomingMessage] Found conversation after reload, updating...')
            updateConversationWithMessage(conv, data, conversationId)
          } else {
            console.warn('[handleIncomingMessage] ⚠️ Conversation still not found after reload:', conversationId)
          }
        }, 500)
      })
    } else {
      console.log('[handleIncomingMessage] Found conversation, updating...')
      updateConversationWithMessage(conv, data, conversationId)
    }
  } else {
    console.warn('[handleIncomingMessage] ⚠️ Invalid message format:', data)
  }
}

function updateConversationWithMessage(conv, messageData, conversationId) {
  console.log('[updateConversationWithMessage] Updating conversation:', conversationId)
  console.log('[updateConversationWithMessage] Message data:', messageData)
  
  // Update last message in conversation list
  conv.lastMessage = {
    content: messageData.content,
    senderType: messageData.senderType,
    createdAt: messageData.createdAt
  }
  conv.lastMessageTime = messageData.createdAt
  conv.lastMessageAt = messageData.createdAt
  
  // Check if this conversation is currently selected
  const isSelected = selectedConversation.value?.conversationId === conversationId
  console.log('[updateConversationWithMessage] Is conversation selected?', isSelected)
  
  if (isSelected) {
    console.log('[updateConversationWithMessage] Conversation is selected, adding message to view')
    // Check if this is a temp message that needs to be replaced
    const tempMessageIndex = messages.value.findIndex(
      m => m.id && m.id.toString().startsWith('temp-') && 
      m.content === messageData.content && 
      m.senderType === messageData.senderType
    )
    
    if (tempMessageIndex !== -1) {
      // Replace temp message with real message
      console.log('[updateConversationWithMessage] Replacing temp message at index:', tempMessageIndex)
      messages.value[tempMessageIndex] = messageData
    } else {
      // Check if message already exists
      const exists = messages.value.find(m => m.id === messageData.id)
      if (!exists) {
        console.log('[updateConversationWithMessage] Adding new message to messages array')
        messages.value.push(messageData)
        console.log('[updateConversationWithMessage] Messages count after add:', messages.value.length)
      } else {
        console.log('[updateConversationWithMessage] Message already exists, skipping')
      }
    }
    scrollToBottom()
    
    // If message is from customer, mark as read
    if (messageData.senderType === 'CUSTOMER') {
      conv.unreadCount = 0
      // Auto mark as read when viewing
      chatService.markAsRead(conversationId).catch(err => {
        console.error('[updateConversationWithMessage] Error marking as read:', err)
      })
    }
  } else {
    // Conversation is not selected
    console.log('[updateConversationWithMessage] Conversation not selected')
    if (messageData.senderType === 'CUSTOMER') {
      conv.unreadCount = (conv.unreadCount || 0) + 1
      console.log('[updateConversationWithMessage] Incremented unread count to:', conv.unreadCount)
    }
  }
  
  // Sort conversations
  conversations.value.sort((a, b) => {
    const timeA = (a.lastMessageAt || a.lastMessageTime) ? new Date(a.lastMessageAt || a.lastMessageTime).getTime() : 0
    const timeB = (b.lastMessageAt || b.lastMessageTime) ? new Date(b.lastMessageAt || b.lastMessageTime).getTime() : 0
    return timeB - timeA
  })
  
  console.log('[updateConversationWithMessage] ✅ Update complete')
}

function sendMessageViaWebSocket(content, conversationId) {
  if (!stompClient || !stompClient.connected) {
    return false
  }

  const payload = {
    senderType: 'SYSTEM',
    senderId: authStore.user?.id,
    conversationId: conversationId,
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

onMounted(() => {
  console.log('[onMounted] Initializing chat component...')
  loadConversations()
  connectWebSocket()
  
  // Refresh every 30 seconds as fallback
  const interval = setInterval(() => {
    console.log('[Auto-refresh] Reloading conversations...')
    loadConversations()
  }, 30000)
  
  // Try to reconnect WebSocket if disconnected
  const reconnectInterval = setInterval(() => {
    if (!wsConnected.value && authStore.token) {
      console.log('[Auto-reconnect] Attempting to reconnect WebSocket...')
      connectWebSocket()
    }
  }, 5000)
  
  onUnmounted(() => {
    console.log('[onUnmounted] Cleaning up...')
    clearInterval(interval)
    clearInterval(reconnectInterval)
    if (subscription) {
      subscription.unsubscribe()
      subscription = null
    }
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    wsConnected.value = false
  })
})
</script>

<style scoped>
/* Custom scrollbar */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.5);
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.7);
}

.dark .overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(75, 85, 99, 0.5);
}

.dark .overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(75, 85, 99, 0.7);
}

/* Smooth transitions */
* {
  transition-property: background-color, border-color, color, fill, stroke;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}

/* Message animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.group {
  animation: slideIn 0.2s ease-out;
}
</style>
