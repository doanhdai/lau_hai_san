<template>
  <div class="h-screen flex bg-[#18191a] text-white">
    <!-- Left Panel - Chat List -->
    <div class="w-80 border-r border-gray-800 flex flex-col bg-[#242526]">
      <!-- Header -->
      <div class="p-4 border-b border-gray-800">
        <h2 class="text-xl font-semibold mb-3 text-white">Đoạn chat</h2>
        <div class="relative">
          <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm trên Messenger"
            class="w-full pl-10 pr-4 py-2.5 bg-[#3a3b3c] border-0 rounded-full text-sm text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-gray-800">
        <button
          @click="activeTab = 'all'"
          class="flex-1 px-4 py-3 text-sm font-medium transition-colors relative"
          :class="activeTab === 'all' ? 'text-blue-400' : 'text-gray-400 hover:text-white'"
        >
          Tất cả
          <span v-if="activeTab === 'all'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-400"></span>
        </button>
        <button
          @click="activeTab = 'unread'"
          class="flex-1 px-4 py-3 text-sm font-medium transition-colors relative"
          :class="activeTab === 'unread' ? 'text-blue-400' : 'text-gray-400 hover:text-white'"
        >
          Chưa đọc
          <span v-if="activeTab === 'unread'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-400"></span>
          <span v-if="unreadCount > 0" class="ml-1 bg-red-500 text-white text-xs px-1.5 py-0.5 rounded-full">
            {{ unreadCount }}
          </span>
        </button>
      </div>

      <!-- Conversations List -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="loading" class="flex items-center justify-center py-8">
          <i class="fas fa-spinner fa-spin text-2xl text-gray-400"></i>
        </div>
        <div v-else-if="filteredConversations.length === 0" class="text-center py-8 text-gray-400">
          <i class="fas fa-comments text-4xl mb-2"></i>
          <p class="text-sm">Không có cuộc trò chuyện nào</p>
        </div>
        <div v-else>
          <div
            v-for="conv in filteredConversations"
            :key="conv.id || conv.conversationId"
            @click.stop.prevent="handleConversationClick(conv)"
            @mousedown="() => console.log('Mouse down on conversation:', conv.conversationId)"
            class="w-full p-3 hover:bg-[#3a3b3c] transition-colors text-left cursor-pointer"
            :class="{ 'bg-[#3a3b3c]': selectedConversation?.id === conv.id || selectedConversation?.conversationId === conv.conversationId }"
          >
            <div class="flex items-center gap-3">
              <!-- Avatar (mặc định) -->
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0 ring-2 ring-transparent"
                   :class="{ 'ring-blue-400': selectedConversation?.id === conv.id || selectedConversation?.conversationId === conv.conversationId }">
                <span class="text-sm font-semibold text-white">
                  {{ getInitials(conv.userName || conv.customerName || 'Người dùng') }}
                </span>
              </div>
              
              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <p class="font-semibold text-sm truncate text-white">{{ conv.userName || conv.customerName || 'Người dùng' }}</p>
                  <span v-if="conv.lastMessageAt || conv.lastMessageTime" class="text-xs text-gray-400 flex-shrink-0 ml-2">
                    {{ formatTime(conv.lastMessageAt || conv.lastMessageTime) }}
                  </span>
                </div>
                <div class="flex items-center justify-between gap-2">
                  <p v-if="conv.lastMessage && conv.lastMessage.content" class="text-xs text-gray-400 truncate flex-1">
                    <span v-if="conv.lastMessage.senderType === 'SYSTEM'" class="text-gray-500">Bạn: </span>
                    {{ conv.lastMessage.content }}
                  </p>
                  <p v-else class="text-xs text-gray-500 truncate flex-1 italic">
                    Chưa có tin nhắn
                  </p>
                  <span v-if="conv.unreadCount > 0" class="bg-blue-500 text-white text-xs px-2 py-0.5 rounded-full flex-shrink-0 min-w-[20px] text-center">
                    {{ conv.unreadCount }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel - Chat Messages -->
    <div class="flex-1 flex flex-col bg-[#18191a]" v-if="selectedConversation">
      <!-- Chat Header -->
      <div class="p-3 border-b border-gray-800 flex items-center justify-between bg-[#242526]">
        <div class="flex items-center gap-3">
          <!-- Avatar mặc định -->
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            <span class="text-sm font-semibold text-white">
              {{ getInitials(selectedConversation.userName || selectedConversation.customerName || 'Người dùng') }}
            </span>
          </div>
          <div>
            <p class="font-semibold text-white text-sm">{{ selectedConversation.userName || selectedConversation.customerName || 'Người dùng' }}</p>
            <p class="text-xs text-gray-400">{{ selectedConversation.userPhone || selectedConversation.customerPhone || selectedConversation.userEmail || selectedConversation.customerEmail }}</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-phone text-gray-300 text-sm"></i>
          </button>
          <button class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-video text-gray-300 text-sm"></i>
          </button>
          <button class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-info-circle text-gray-300 text-sm"></i>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 overflow-y-auto p-4 bg-[#18191a] space-y-1" ref="messagesContainer">
        <!-- Debug Info -->
        <div class="text-xs text-gray-500 p-2 bg-gray-800 rounded mb-2">
          <div>loadingMessages: {{ loadingMessages }}</div>
          <div>selectedConversation exists: {{ !!selectedConversation }}</div>
          <div>messages exists: {{ !!selectedConversation?.messages }}</div>
          <div>messages length: {{ selectedConversation?.messages?.length || 0 }}</div>
          <div>messages is array: {{ Array.isArray(selectedConversation?.messages) }}</div>
        </div>
        
        <div v-if="loadingMessages" class="flex items-center justify-center py-8">
          <i class="fas fa-spinner fa-spin text-2xl text-gray-400"></i>
        </div>
        <div v-else-if="selectedConversation && selectedConversation.messages && selectedConversation.messages.length > 0">
          <template v-for="(message, index) in selectedConversation.messages" :key="message.id || `msg-${index}`">
            <!-- Timestamp separator -->
            <div v-if="shouldShowTimestamp(message, selectedConversation.messages[index - 1])" 
                 class="flex justify-center my-4">
              <span class="text-xs text-gray-500 bg-[#242526] px-3 py-1 rounded-full">
                {{ formatDateSeparator(message.createdAt) }}
              </span>
            </div>
            
            <!-- Message -->
            <div
              class="flex items-end gap-2"
              :class="message.senderType === 'SYSTEM' ? 'justify-end' : 'justify-start'"
            >
              <!-- Customer message layout -->
              <div v-if="message.senderType === 'CUSTOMER'" class="flex flex-col items-start w-full mb-3">
                <!-- Sender name - luôn hiển thị -->
                <div class="mb-2 px-3 w-full">
                  <span class="text-sm text-white font-bold block">
                    {{ message.senderName || selectedConversation.userName || selectedConversation.customerName || 'Người dùng' }}
                  </span>
                </div>
                <div class="flex items-end gap-2">
                  <!-- Avatar for customer messages -->
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0">
                    <span class="text-xs font-semibold text-white">
                      {{ getInitials(message.senderName || selectedConversation.userName || selectedConversation.customerName) }}
                    </span>
                  </div>
                  
                  <div class="max-w-md px-4 py-2 rounded-2xl bg-[#3a3b3c] text-white rounded-tl-sm">
                    <p class="text-sm leading-relaxed whitespace-pre-wrap break-words">{{ message.content }}</p>
                    <div class="flex items-center justify-end gap-1 mt-1">
                      <span class="text-xs opacity-70">
                        {{ formatMessageTime(message.createdAt) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- System message layout -->
              <div v-else class="flex flex-col items-end w-full mb-3">
                <!-- Sender name - luôn hiển thị -->
                <div class="mb-2 px-3 w-full flex justify-end">
                  <span class="text-sm text-white font-bold block">
                    {{ message.senderName || authStore.user?.fullName || 'Bạn' }}
                  </span>
                </div>
                <div class="flex items-end gap-2">
                  <div class="max-w-md px-4 py-2 rounded-2xl bg-[#0084ff] text-white rounded-tr-sm">
                    <p class="text-sm leading-relaxed whitespace-pre-wrap break-words">{{ message.content }}</p>
                    <div class="flex items-center justify-end gap-1 mt-1">
                      <span class="text-xs opacity-70">
                        {{ formatMessageTime(message.createdAt) }}
                      </span>
                      <span v-if="message.isRead" class="text-xs opacity-70">
                        <i class="fas fa-check-double text-blue-200"></i>
                      </span>
                      <span v-else class="text-xs opacity-70">
                        <i class="fas fa-check text-blue-200"></i>
                      </span>
                    </div>
                  </div>
                  
                  <!-- Avatar for system messages -->
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-green-500 to-teal-600 flex items-center justify-center flex-shrink-0">
                    <span class="text-xs font-semibold text-white">
                      {{ getInitials(message.senderName || authStore.user?.fullName || 'SY') }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
        <div v-else class="flex items-center justify-center py-8 text-gray-400">
          <div class="text-center">
            <p class="text-sm">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</p>
            <div class="text-xs mt-2 text-red-400">
              Debug: messages = {{ JSON.stringify(selectedConversation?.messages) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-3 border-t border-gray-800 bg-[#242526]">
        <form @submit.prevent="sendMessage" class="flex items-end gap-2">
          <button type="button" class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-microphone text-gray-300 text-lg"></i>
          </button>
          <button type="button" class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-image text-gray-300 text-lg"></i>
          </button>
          <button type="button" class="p-2 hover:bg-[#3a3b3c] rounded-full transition-colors">
            <i class="fas fa-smile text-gray-300 text-lg"></i>
          </button>
          <div class="flex-1 flex items-center gap-2 bg-[#3a3b3c] rounded-full px-4 py-2">
            <input
              v-model="messageInput"
              type="text"
              placeholder="Nhập tin nhắn..."
              class="flex-1 bg-transparent border-0 text-white text-sm placeholder-gray-400 focus:outline-none"
              :disabled="loadingMessages"
            />
            <button type="button" class="p-1 hover:bg-[#4e4f50] rounded-full transition-colors">
              <i class="fas fa-font text-gray-300 text-sm"></i>
            </button>
          </div>
          <button
            type="submit"
            :disabled="!messageInput.trim() || loadingMessages"
            class="p-2 bg-[#0084ff] text-white rounded-full hover:bg-[#0066cc] transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-[#0084ff]"
          >
            <i class="fas fa-paper-plane text-sm"></i>
          </button>
        </form>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="flex-1 flex items-center justify-center bg-[#18191a]">
      <div class="text-center text-gray-400">
        <i class="fas fa-comments text-6xl mb-4"></i>
        <p class="text-lg">Chọn một cuộc trò chuyện để bắt đầu</p>
        <p class="text-sm mt-2 text-gray-500">Cuộc trò chuyện sẽ được tạo tự động khi khách hàng gửi tin nhắn</p>
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
  messageInput.value = ''
  
  const conversationId = selectedConversation.value.conversationId || selectedConversation.value.id
  console.log('selectedConversation.value:', selectedConversation.value)
  console.log('selectedConversation.value.conversationId:', selectedConversation.value.conversationId)
  console.log('selectedConversation.value.id:', selectedConversation.value.id)
  console.log('Resolved conversationId:', conversationId)
  
  if (!conversationId) {
    console.error('ERROR: conversationId is null or undefined!')
    console.error('selectedConversation:', selectedConversation.value)
    alert('Lỗi: Không tìm thấy ID cuộc hội thoại. Vui lòng chọn lại cuộc hội thoại.')
    messageInput.value = messageText // Restore message
    return
  }
  
  console.log('Sending message to conversation:', conversationId)
  console.log('Message text:', messageText)
  
  // Try WebSocket first
  console.log('Attempting to send via WebSocket...')
  const sentViaWebSocket = sendMessageViaWebSocket(messageText, conversationId)
  console.log('Sent via WebSocket:', sentViaWebSocket)
  
  if (!sentViaWebSocket) {
    // Fallback to REST API
    console.log('Using REST API fallback...')
    try {
      const request = {
        content: messageText,
        conversationId: conversationId
      }
      console.log('Request payload:', request)
      
      const response = await chatService.sendMessage(request)
      console.log('Response from sendMessage:', response)
      
      if (response && response.success) {
        // Add message to local state
        if (selectedConversation.value.messages) {
          selectedConversation.value.messages.push(response.data)
        } else {
          selectedConversation.value.messages = [response.data]
        }
        
        // Update last message
        selectedConversation.value.lastMessage = response.data
        selectedConversation.value.lastMessageAt = response.data.createdAt
        
        // Update conversation in list
        const conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
        if (conv) {
          conv.lastMessage = response.data
          conv.lastMessageAt = response.data.createdAt
        }
        
        scrollToBottom()
      }
    } catch (error) {
      console.error('Error sending message:', error)
      messageInput.value = messageText
    }
  } else {
    // Message sent via WebSocket, will be added when received
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
  if (data.type === 'MESSAGE_READ') {
    // Update unread count
    const conv = conversations.value.find(c => (c.id || c.conversationId) === data.conversationId)
    if (conv) {
      conv.unreadCount = 0
    }
  } else if (data.id && data.conversationId) {
    // New message
    const conversationId = data.conversationId
    const conv = conversations.value.find(c => (c.id || c.conversationId) === conversationId)
    if (conv) {
      // Update last message
      conv.lastMessage = data
      conv.lastMessageAt = data.createdAt
      
      // If this conversation is selected, add message to view
      const selectedId = selectedConversation.value?.id || selectedConversation.value?.conversationId
      if (selectedId === conversationId) {
        if (!selectedConversation.value.messages) {
          selectedConversation.value.messages = []
        }
        // Check if message already exists
        const exists = selectedConversation.value.messages.find(m => m.id === data.id)
        if (!exists) {
          selectedConversation.value.messages.push(data)
          scrollToBottom()
        }
      } else {
        // Increment unread count if not selected
        if (data.senderType === 'CUSTOMER') {
          conv.unreadCount = (conv.unreadCount || 0) + 1
        }
      }
    }
    
    // Reload conversations to update order
    loadConversations()
  }
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
  background: #4e4f50;
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #606162;
}
</style>

