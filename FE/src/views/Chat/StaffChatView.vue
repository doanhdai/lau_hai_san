<template>
  <div class="h-screen flex bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 text-gray-900 dark:text-white">
    <!-- Left Panel - Chat List -->
    <div class="w-96 border-r border-gray-200 dark:border-gray-700 flex flex-col bg-white dark:bg-gray-800 shadow-lg">
      <!-- Header -->
      <div class="p-5 border-b border-gray-200 dark:border-gray-700 bg-gradient-to-r from-blue-500 to-blue-600 dark:from-blue-600 dark:to-blue-700">
        <h2 class="text-2xl font-bold mb-4 text-white flex items-center gap-2">
          <i class="fas fa-comments"></i>
          <span>Tin nhắn khách hàng</span>
        </h2>
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
      <div class="flex border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
        <button
          @click="activeTab = 'all'"
          class="flex-1 px-4 py-3.5 text-sm font-semibold transition-all relative group"
          :class="activeTab === 'all' 
            ? 'text-blue-600 dark:text-blue-400 bg-white dark:bg-gray-800' 
            : 'text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400'"
        >
          <span class="flex items-center justify-center gap-2">
            <i class="fas fa-comments"></i>
            <span>Tất cả</span>
          </span>
          <span v-if="activeTab === 'all'" class="absolute bottom-0 left-0 right-0 h-1 bg-blue-600 dark:bg-blue-400 rounded-t-full"></span>
        </button>
        <button
          @click="activeTab = 'unread'"
          class="flex-1 px-4 py-3.5 text-sm font-semibold transition-all relative group"
          :class="activeTab === 'unread' 
            ? 'text-blue-600 dark:text-blue-400 bg-white dark:bg-gray-800' 
            : 'text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400'"
        >
          <span class="flex items-center justify-center gap-2">
            <i class="fas fa-envelope"></i>
            <span>Chưa đọc</span>
            <span v-if="unreadCount > 0" class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full font-bold min-w-[24px] text-center">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </span>
          <span v-if="activeTab === 'unread'" class="absolute bottom-0 left-0 right-0 h-1 bg-blue-600 dark:bg-blue-400 rounded-t-full"></span>
        </button>
      </div>

      <!-- Conversations List -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="loading" class="flex flex-col items-center justify-center py-12">
          <i class="fas fa-spinner fa-spin text-3xl text-blue-500 mb-3"></i>
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
            :key="conv.id || conv.conversationId"
            @click.stop.prevent="handleConversationClick(conv)"
            class="w-full p-4 hover:bg-blue-50 dark:hover:bg-gray-700/50 transition-all cursor-pointer group relative"
            :class="{ 
              'bg-blue-50 dark:bg-gray-700/70 border-l-4 border-blue-500': 
                selectedConversation?.id === conv.id || selectedConversation?.conversationId === conv.conversationId 
            }"
          >
            <div class="flex items-start gap-3">
              <!-- Avatar -->
              <div class="relative flex-shrink-0">
                <div class="w-14 h-14 rounded-full bg-gradient-to-br from-blue-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg ring-2 ring-white dark:ring-gray-800"
                     :class="{ 'ring-blue-400 ring-4': selectedConversation?.id === conv.id || selectedConversation?.conversationId === conv.conversationId }">
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
                    <span v-if="conv.lastMessage.senderType === 'SYSTEM'" class="text-blue-600 dark:text-blue-400 font-medium">Bạn: </span>
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
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between bg-gradient-to-r from-blue-500 to-blue-600 dark:from-blue-600 dark:to-blue-700 shadow-md">
        <div class="flex items-center gap-4">
          <!-- Avatar -->
          <div class="w-12 h-12 rounded-full bg-white dark:bg-gray-800 flex items-center justify-center shadow-lg ring-2 ring-white/50">
            <span class="text-base font-bold text-blue-600 dark:text-blue-400">
              {{ getInitials(selectedConversation.userName || selectedConversation.customerName || 'Người dùng') }}
            </span>
          </div>
          <div>
            <p class="font-bold text-white text-base">
              {{ selectedConversation.userName || selectedConversation.customerName || 'Người dùng' }}
            </p>
            <div class="flex items-center gap-3 mt-0.5">
              <p v-if="selectedConversation.userPhone || selectedConversation.customerPhone" class="text-xs text-blue-100 flex items-center gap-1">
                <i class="fas fa-phone text-xs"></i>
                {{ selectedConversation.userPhone || selectedConversation.customerPhone }}
              </p>
              <p v-if="selectedConversation.userEmail || selectedConversation.customerEmail" class="text-xs text-blue-100 flex items-center gap-1">
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
      <div class="flex-1 overflow-y-auto p-6 bg-gray-50 dark:bg-gray-900 space-y-4" ref="messagesContainer">
        <div v-if="loadingMessages" class="flex flex-col items-center justify-center py-12">
          <i class="fas fa-spinner fa-spin text-3xl text-blue-500 mb-3"></i>
          <p class="text-sm text-gray-500 dark:text-gray-400">Đang tải tin nhắn...</p>
        </div>
        <div v-else-if="selectedConversation && selectedConversation.messages && selectedConversation.messages.length > 0">
          <template v-for="(message, index) in selectedConversation.messages" :key="message.id || `msg-${index}`">
            <!-- Timestamp separator -->
            <div v-if="shouldShowTimestamp(message, selectedConversation.messages[index - 1])" 
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
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0 shadow-md">
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
                  <span class="text-xs font-semibold text-blue-600 dark:text-blue-400 mb-1 px-1">
                    {{ message.senderName || authStore.user?.fullName || 'Bạn' }}
                  </span>
                  <div class="px-4 py-3 rounded-2xl rounded-tr-sm bg-gradient-to-br from-blue-500 to-blue-600 text-white shadow-md">
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
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-green-500 to-teal-600 flex items-center justify-center flex-shrink-0 shadow-md">
                  <span class="text-xs font-bold text-white">
                    {{ getInitials(message.senderName || authStore.user?.fullName || 'AD') }}
                  </span>
                </div>
              </template>
            </div>
          </template>
        </div>
        <div v-else class="flex flex-col items-center justify-center py-16 px-4">
          <div class="w-20 h-20 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mb-4">
            <i class="fas fa-comment-dots text-3xl text-gray-400"></i>
          </div>
          <p class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Chưa có tin nhắn nào</p>
          <p class="text-xs text-gray-500 dark:text-gray-500">Hãy bắt đầu cuộc trò chuyện với khách hàng!</p>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-4 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shadow-lg">
        <form @submit.prevent="sendMessage" class="flex items-end gap-3">
          <button type="button" class="p-3 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition-colors text-gray-600 dark:text-gray-400" title="Gửi file">
            <i class="fas fa-paperclip text-lg"></i>
          </button>
          <div class="flex-1 flex items-center gap-2 bg-gray-100 dark:bg-gray-700 rounded-2xl px-4 py-3 border-2 border-transparent focus-within:border-blue-500 dark:focus-within:border-blue-400 transition-colors">
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
            class="p-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-xl hover:from-blue-600 hover:to-blue-700 transition-all shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:from-blue-500 disabled:hover:to-blue-600 disabled:hover:shadow-md"
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
        <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-gradient-to-br from-blue-100 to-purple-100 dark:from-blue-900 dark:to-purple-900 flex items-center justify-center">
          <i class="fas fa-comments text-4xl text-blue-500 dark:text-blue-400"></i>
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
const messageInput = ref('')
const searchQuery = ref('')
const activeTab = ref('all')
const loading = ref(false)
const loadingMessages = ref(false)
const messagesContainer = ref(null)

// Debug: Watch conversations
watch(conversations, (newVal) => {
  console.log('=== Conversations changed ===')
  console.log('Count:', newVal.length)
  console.log('Data:', newVal)
}, { deep: true })

// Debug: Watch selectedConversation
watch(selectedConversation, (newVal) => {
  console.log('=== Selected Conversation changed ===')
  console.log('Data:', newVal)
  if (newVal && newVal.messages) {
    console.log('Messages count:', newVal.messages.length)
  }
}, { deep: true })

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
  
  // Show timestamp if messages are more than 5 minutes apart
  return diffMinutes > 5
}

function shouldShowSenderName(currentMessage, previousMessage) {
  // Hiển thị tên nếu:
  // 1. Không có tin nhắn trước đó (tin nhắn đầu tiên)
  // 2. Tin nhắn trước đó từ người khác (senderType khác)
  // 3. Tin nhắn trước đó cách hơn 5 phút
  if (!previousMessage) return true
  
  if (currentMessage.senderType !== previousMessage.senderType) return true
  
  const currentTime = new Date(currentMessage.createdAt).getTime()
  const previousTime = new Date(previousMessage.createdAt).getTime()
  const diffMinutes = (currentTime - previousTime) / (1000 * 60)
  
  // Hiển thị tên nếu cách nhau hơn 5 phút
  return diffMinutes > 5
}

async function loadConversations() {
  console.log('=== loadConversations called ===')
  loading.value = true
  try {
    console.log('Calling chatService.getConversations()...')
    const response = await chatService.getConversations()
    console.log('Response from getConversations:', response)
    
    if (response && response.success) {
      console.log('Response data:', response.data)
      console.log('Response data length:', (response.data || []).length)
      
      // Chỉ lấy thông tin cần thiết: tên, lastMessage, unreadCount, thời gian
      conversations.value = (response.data || []).map(conv => ({
        conversationId: conv.conversationId,
        id: conv.conversationId, // Map conversationId to id for backward compatibility
        userName: conv.userName || conv.customerName || 'Người dùng',
        customerName: conv.customerName || conv.userName || 'Người dùng',
        userPhone: conv.userPhone,
        userEmail: conv.userEmail,
        customerPhone: conv.customerPhone,
        customerEmail: conv.customerEmail,
        // Chỉ lấy lastMessage (không lấy toàn bộ messages)
        lastMessage: conv.lastMessage ? {
          content: conv.lastMessage.content,
          senderType: conv.lastMessage.senderType,
          createdAt: conv.lastMessage.createdAt
        } : null,
        lastMessageTime: conv.lastMessageTime || conv.lastMessageAt || (conv.lastMessage?.createdAt),
        unreadCount: conv.unreadCount || 0,
        // Không lấy messages ở đây - sẽ load khi click vào conversation
        messages: undefined
      }))
      console.log('Loaded conversations list (without messages):', conversations.value)
      console.log('Conversations count:', conversations.value.length)
    } else {
      console.error('Invalid response format:', response)
    }
  } catch (error) {
    console.error('Error loading conversations:', error)
    console.error('Error details:', error.response || error.message)
  } finally {
    loading.value = false
    console.log('=== loadConversations completed ===')
  }
}

function handleConversationClick(conv) {
  console.log('!!! handleConversationClick triggered !!!')
  console.log('Conv data:', conv)
  console.log('Conv conversationId:', conv.conversationId)
  console.log('Conv id:', conv.id)
  if (!conv) {
    console.error('ERROR: conv is null or undefined!')
    return
  }
  selectConversation(conv)
}

async function selectConversation(conversation) {
  console.log('=== selectConversation called ===')
  console.log('Conversation clicked:', conversation)
  
  if (!conversation) {
    console.error('ERROR: conversation is null or undefined!')
    return
  }
  
  const conversationId = conversation.conversationId || conversation.id
  if (!conversationId) {
    console.error('ERROR: conversationId is null or undefined!')
    console.error('Conversation object:', conversation)
    return
  }
  
  console.log('Conversation ID resolved:', conversationId)
  
  // Set conversation cơ bản trước (để hiển thị header ngay)
  selectedConversation.value = {
    conversationId: conversationId,
    id: conversationId,
    userName: conversation.userName || conversation.customerName || 'Người dùng',
    customerName: conversation.customerName || conversation.userName,
    userPhone: conversation.userPhone || conversation.customerPhone,
    userEmail: conversation.userEmail || conversation.customerEmail,
    customerPhone: conversation.customerPhone,
    customerEmail: conversation.customerEmail,
    messages: [], // Khởi tạo empty, sẽ load sau
    unreadCount: conversation.unreadCount || 0
  }
  
  console.log('Selected conversation set:', selectedConversation.value)
  loadingMessages.value = true
  console.log('Loading messages started...')
  
  try {
    console.log('Loading conversation:', conversationId)
    
    // API 1: Lấy thông tin conversation (không có messages)
    let conversationResponse = null
    try {
      console.log('Calling API: GET /api/chat/conversations/' + conversationId)
      conversationResponse = await chatService.getConversation(conversationId)
      console.log('Conversation info response:', conversationResponse)
    } catch (error) {
      console.error('Error loading conversation info:', error)
      console.error('Error response:', error.response)
      console.error('Error status:', error.response?.status)
      console.error('Error message:', error.response?.data)
      if (error.response?.status === 401 || error.response?.status === 403) {
        alert('Lỗi xác thực. Vui lòng đăng nhập lại.')
        return
      }
    }
    
    // API 2: Lấy messages riêng biệt
    let messages = []
    try {
      console.log('Calling API: GET /api/chat/conversations/' + conversationId + '/messages')
      const messagesResponse = await chatService.getConversationMessages(conversationId)
      console.log('Messages response:', messagesResponse)
      
      // Xử lý messages từ response
      if (messagesResponse) {
        if (messagesResponse.success && messagesResponse.data) {
          // Format: { success: true, data: [...] }
          messages = Array.isArray(messagesResponse.data) ? messagesResponse.data : []
        } else if (Array.isArray(messagesResponse)) {
          // Format: [...] (trực tiếp là array)
          messages = messagesResponse
        } else if (messagesResponse.data && Array.isArray(messagesResponse.data)) {
          // Format: { data: [...] }
          messages = messagesResponse.data
        }
      }
      
      console.log('Parsed messages:', messages)
      messages.forEach((msg, idx) => {
        console.log(`Message ${idx}:`, {
          id: msg.id,
          senderName: msg.senderName,
          senderType: msg.senderType,
          content: msg.content
        })
      })
    } catch (error) {
      console.error('Error loading messages:', error)
      console.error('Error response:', error.response)
      console.error('Error status:', error.response?.status)
      console.error('Error message:', error.response?.data)
      if (error.response?.status === 401 || error.response?.status === 403) {
        alert('Lỗi xác thực khi load messages. Vui lòng đăng nhập lại.')
        return
      }
      messages = []
    }
    
    // Cập nhật selectedConversation với thông tin từ 2 API
    console.log('=== Updating selectedConversation ===')
    console.log('Messages to set:', messages)
    console.log('Messages is array:', Array.isArray(messages))
    console.log('Messages length:', messages.length)
    
    // Đảm bảo conversationId luôn có giá trị
    const finalConversationId = conversationId
    
    if (conversationResponse && conversationResponse.success && conversationResponse.data) {
      console.log('Using conversationResponse data')
      selectedConversation.value = {
        ...selectedConversation.value,
        ...conversationResponse.data,
        conversationId: finalConversationId, // Đảm bảo conversationId không bị ghi đè
        id: finalConversationId, // Đảm bảo id không bị ghi đè
        userName: conversationResponse.data.userName || conversation.userName || conversation.customerName || 'Người dùng',
        userPhone: conversationResponse.data.userPhone || conversation.userPhone || conversation.customerPhone,
        userEmail: conversationResponse.data.userEmail || conversation.userEmail || conversation.customerEmail,
        customerName: conversationResponse.data.customerName || conversation.customerName,
        customerPhone: conversationResponse.data.customerPhone || conversation.customerPhone,
        customerEmail: conversationResponse.data.customerEmail || conversation.customerEmail,
        messages: messages, // Messages từ API riêng biệt
        lastMessage: conversationResponse.data.lastMessage,
        lastMessageTime: conversationResponse.data.lastMessageTime || conversationResponse.data.lastMessageAt,
        unreadCount: conversationResponse.data.unreadCount || 0
      }
    } else {
      console.log('Conversation API failed, setting messages only')
      // Nếu conversation API lỗi, vẫn cập nhật messages nếu có
      selectedConversation.value = {
        ...selectedConversation.value,
        messages: messages,
        conversationId: finalConversationId,
        id: finalConversationId
      }
    }
    
    console.log('=== After setting selectedConversation ===')
    console.log('selectedConversation.value:', selectedConversation.value)
    console.log('selectedConversation.value.messages:', selectedConversation.value.messages)
    console.log('selectedConversation.value.messages is array:', Array.isArray(selectedConversation.value.messages))
    console.log('selectedConversation.value.messages.length:', selectedConversation.value.messages?.length)
    console.log('Messages count:', messages.length)
    if (messages.length > 0) {
      console.log('First message:', messages[0])
    }
    
    // Đánh dấu đã đọc
    try {
      await markAsRead(conversationId)
    } catch (error) {
      console.error('Error marking as read:', error)
    }
  } catch (error) {
    console.error('Error loading conversation:', error)
    // Nếu lỗi, vẫn giữ conversation nhưng đảm bảo có messages array
    if (selectedConversation.value && !selectedConversation.value.messages) {
      selectedConversation.value.messages = []
    }
  } finally {
    loadingMessages.value = false
    // Đợi một chút để DOM cập nhật trước khi scroll
    await nextTick()
    scrollToBottom()
  }
}

async function markAsRead(conversationId) {
  try {
    await chatService.markAsRead(conversationId)
    // Update local state
    const conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
    if (conv) {
      conv.unreadCount = 0
    }
  } catch (error) {
    console.error('Error marking as read:', error)
  }
}

async function sendMessage() {
  console.log('=== sendMessage called ===')
  console.log('messageInput:', messageInput.value)
  console.log('selectedConversation:', selectedConversation.value)
  
  if (!messageInput.value.trim() || !selectedConversation.value) {
    console.log('Cannot send: empty message or no conversation selected')
    return
  }
  
  const messageText = messageInput.value.trim()
  const conversationId = selectedConversation.value.conversationId || selectedConversation.value.id
  
  if (!conversationId) {
    console.error('ERROR: conversationId is null or undefined!')
    alert('Lỗi: Không tìm thấy ID cuộc hội thoại. Vui lòng chọn lại cuộc hội thoại.')
    return
  }
  
  // Create optimistic message (temporary, will be replaced by real message)
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
  
  // Add optimistic message to UI immediately
  if (!selectedConversation.value.messages) {
    selectedConversation.value.messages = []
  }
  selectedConversation.value.messages.push(tempMessage)
  messageInput.value = '' // Clear input immediately
  scrollToBottom()
  
  // Update conversation list
  const conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
  if (conv) {
    conv.lastMessage = tempMessage
    conv.lastMessageAt = tempMessage.createdAt
    conv.lastMessageTime = tempMessage.createdAt
  }
  
  // Try WebSocket first
  console.log('Attempting to send via WebSocket...')
  const sentViaWebSocket = sendMessageViaWebSocket(messageText, conversationId)
  
  if (!sentViaWebSocket) {
    // Fallback to REST API
    console.log('Using REST API fallback...')
    try {
      const request = {
        content: messageText,
        conversationId: conversationId
      }
      
      const response = await chatService.sendMessage(request)
      console.log('Response from sendMessage:', response)
      
      if (response && response.success && response.data) {
        // Replace temp message with real message
        const tempIndex = selectedConversation.value.messages.findIndex(m => m.id === tempMessage.id)
        if (tempIndex !== -1) {
          selectedConversation.value.messages[tempIndex] = response.data
        } else {
          // If temp message not found, just add the real one
          selectedConversation.value.messages.push(response.data)
        }
        
        // Update last message
        selectedConversation.value.lastMessage = response.data
        selectedConversation.value.lastMessageAt = response.data.createdAt
        selectedConversation.value.lastMessageTime = response.data.createdAt
        
        // Update conversation in list
        if (conv) {
          conv.lastMessage = response.data
          conv.lastMessageAt = response.data.createdAt
          conv.lastMessageTime = response.data.createdAt
        }
        
        scrollToBottom()
      } else {
        // If API call failed, remove temp message and restore input
        const tempIndex = selectedConversation.value.messages.findIndex(m => m.id === tempMessage.id)
        if (tempIndex !== -1) {
          selectedConversation.value.messages.splice(tempIndex, 1)
        }
        messageInput.value = messageText
        alert('Không thể gửi tin nhắn. Vui lòng thử lại.')
      }
    } catch (error) {
      console.error('Error sending message:', error)
      // Remove temp message on error
      const tempIndex = selectedConversation.value.messages.findIndex(m => m.id === tempMessage.id)
      if (tempIndex !== -1) {
        selectedConversation.value.messages.splice(tempIndex, 1)
      }
      messageInput.value = messageText
      alert('Lỗi khi gửi tin nhắn: ' + (error.response?.data?.message || error.message))
    }
  }
  // If sent via WebSocket, the message will be added when received from server
  // The temp message will be replaced by the real one
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Watch for new messages
watch(() => selectedConversation.value?.messages, () => {
  scrollToBottom()
}, { deep: true })

// WebSocket connection
let stompClient = null
let subscription = null

function connectWebSocket() {
  const token = authStore.token
  
  if (!token) {
    console.warn('No token for WebSocket')
    return
  }

  try {
    const host = import.meta.env.VITE_API_URL?.replace(/^https?:\/\//, '') || 'localhost:8080'
    const wsUrl = `http://${host}/ws?token=${encodeURIComponent(token)}`
    
    const { Client } = require('@stomp/stompjs')
    const SockJS = require('sockjs-client')
    
    stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      onConnect: () => {
        console.log('Chat WebSocket connected')
        
        // Subscribe to system topic (for all staff/admin)
        subscription = stompClient.subscribe('/topic/chat/system', (message) => {
          try {
            const data = JSON.parse(message.body)
            handleIncomingMessage(data)
          } catch (error) {
            console.error('Error parsing message:', error)
          }
        })
      },
      onDisconnect: () => {
        console.log('Chat WebSocket disconnected')
      }
    })
    
    stompClient.activate()
  } catch (error) {
    console.error('Error connecting WebSocket:', error)
  }
}

function handleIncomingMessage(data) {
  console.log('=== handleIncomingMessage ===', data)
  
  if (data.type === 'MESSAGE_READ') {
    // Update unread count
    const conv = conversations.value.find(c => (c.id || c.conversationId) === data.conversationId)
    if (conv) {
      conv.unreadCount = 0
    }
    // Also update selected conversation if it's the same
    if (selectedConversation.value && 
        (selectedConversation.value.id === data.conversationId || 
         selectedConversation.value.conversationId === data.conversationId)) {
      selectedConversation.value.unreadCount = 0
    }
  } else if (data.id && data.conversationId) {
    // New message received
    const conversationId = data.conversationId
    console.log('Processing new message for conversation:', conversationId)
    
    // Find conversation in list
    let conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
    
    // If conversation doesn't exist in list, reload conversations
    if (!conv) {
      console.log('Conversation not found in list, reloading...')
      loadConversations()
      // After reload, find it again
      setTimeout(() => {
        conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
        if (conv) {
          updateConversationWithMessage(conv, data, conversationId)
        }
      }, 500)
    } else {
      updateConversationWithMessage(conv, data, conversationId)
    }
  }
}

function updateConversationWithMessage(conv, messageData, conversationId) {
  // Update last message in conversation list
  conv.lastMessage = {
    content: messageData.content,
    senderType: messageData.senderType,
    createdAt: messageData.createdAt
  }
  conv.lastMessageTime = messageData.createdAt
  conv.lastMessageAt = messageData.createdAt
  
  // Check if this conversation is currently selected
  const selectedId = selectedConversation.value?.id || selectedConversation.value?.conversationId
  const isSelected = selectedId === conversationId
  
  if (isSelected) {
    // Add message to selected conversation view
    if (!selectedConversation.value.messages) {
      selectedConversation.value.messages = []
    }
    
    // Check if this is a temp message that needs to be replaced
    const tempMessageIndex = selectedConversation.value.messages.findIndex(
      m => m.id && m.id.toString().startsWith('temp-') && 
      m.content === messageData.content && 
      m.senderType === messageData.senderType
    )
    
    if (tempMessageIndex !== -1) {
      // Replace temp message with real message
      console.log('Replacing temp message with real message:', messageData)
      selectedConversation.value.messages[tempMessageIndex] = messageData
    } else {
      // Check if message already exists (avoid duplicates)
      const exists = selectedConversation.value.messages.find(m => m.id === messageData.id)
      if (!exists) {
        console.log('Adding message to selected conversation:', messageData)
        selectedConversation.value.messages.push(messageData)
      }
    }
    
    // Update last message
    selectedConversation.value.lastMessage = messageData
    selectedConversation.value.lastMessageTime = messageData.createdAt
    scrollToBottom()
    
    // If message is from customer, mark as read (since admin is viewing)
    if (messageData.senderType === 'CUSTOMER') {
      conv.unreadCount = 0
      selectedConversation.value.unreadCount = 0
    }
  } else {
    // Conversation is not selected
    if (messageData.senderType === 'CUSTOMER') {
      // Increment unread count for customer messages
      conv.unreadCount = (conv.unreadCount || 0) + 1
    }
  }
  
  // Sort conversations by last message time
  conversations.value.sort((a, b) => {
    const timeA = (a.lastMessageAt || a.lastMessageTime) ? new Date(a.lastMessageAt || a.lastMessageTime).getTime() : 0
    const timeB = (b.lastMessageAt || b.lastMessageTime) ? new Date(b.lastMessageAt || b.lastMessageTime).getTime() : 0
    return timeB - timeA
  })
}

function sendMessageViaWebSocket(content, conversationId) {
  console.log('=== sendMessageViaWebSocket called ===')
  console.log('Arguments received:')
  console.log('  - content:', content)
  console.log('  - conversationId:', conversationId)
  console.log('  - conversationId type:', typeof conversationId)
  console.log('  - conversationId is null?', conversationId === null)
  console.log('  - conversationId is undefined?', conversationId === undefined)
  console.log('stompClient:', stompClient)
  console.log('stompClient.connected:', stompClient?.connected)
  
  if (!stompClient || !stompClient.connected) {
    console.log('WebSocket not connected')
    return false
  }

  const authStore = useAuthStore()
  const userInfo = authStore.user
  console.log('User info:', userInfo)
  console.log('User ID:', userInfo?.id)
  
  const payload = {
    senderType: 'SYSTEM',
    senderId: userInfo.id,
    conversationId: conversationId,
    content: content
  }
  console.log('=== WebSocket payload created ===')
  console.log('payload:', payload)
  console.log('payload.conversationId:', payload.conversationId)
  console.log('JSON.stringify(payload):', JSON.stringify(payload))

  try {
    const bodyString = JSON.stringify(payload)
    console.log('Sending to /app/chat.send:', bodyString)
    
    stompClient.publish({
      destination: '/app/chat.send',
      body: bodyString
    })
    console.log('Message published via WebSocket successfully')
    return true
  } catch (error) {
    console.error('Error sending via WebSocket:', error)
    return false
  }
}

onMounted(() => {
  loadConversations()
  connectWebSocket()
  
  // Refresh every 10 seconds as fallback
  const interval = setInterval(loadConversations, 10000)
  onUnmounted(() => {
    clearInterval(interval)
    if (subscription) {
      subscription.unsubscribe()
    }
    if (stompClient) {
      stompClient.deactivate()
    }
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


