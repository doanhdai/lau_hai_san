<template>
  <div class="h-screen flex bg-[#18191a] text-white">
    <!-- Left Panel - Chat List -->
    <div class="w-80 border-r border-gray-800 flex flex-col bg-[#242526]">
      <div class="p-4 border-b border-gray-800">
        <h2 class="text-xl font-semibold text-white">Đoạn chat</h2>
      </div>

      <!-- Conversations List -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="loading" class="flex items-center justify-center py-8">
          <i class="fas fa-spinner fa-spin text-2xl text-gray-400"></i>
        </div>
        <div v-else-if="conversations.length === 0" class="text-center py-8 text-gray-400">
          <p class="text-sm">Không có cuộc trò chuyện nào</p>
        </div>
        <div v-else>
          <div
            v-for="conv in conversations"
            :key="conv.conversationId"
            @click="selectConversation(conv)"
            class="p-3 hover:bg-[#3a3b3c] cursor-pointer border-b border-gray-800"
            :class="{ 'bg-[#3a3b3c]': selectedConversation?.conversationId === conv.conversationId }"
          >
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                <span class="text-sm font-semibold text-white">{{ getInitials(conv.userName) }}</span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-sm text-white">{{ conv.userName }}</p>
                <p class="text-xs text-gray-400 truncate">{{ conv.lastMessage?.content || 'Chưa có tin nhắn' }}</p>
              </div>
              <span v-if="conv.unreadCount > 0" class="bg-blue-500 text-white text-xs px-2 py-0.5 rounded-full">
                {{ conv.unreadCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel - Chat Messages -->
    <div class="flex-1 flex flex-col bg-[#18191a]" v-if="selectedConversation">
      <!-- Chat Header -->
      <div class="p-3 border-b border-gray-800 bg-[#242526]">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            <span class="text-sm font-semibold text-white">{{ getInitials(selectedConversation.userName) }}</span>
          </div>
          <div>
            <p class="font-semibold text-white text-sm">{{ selectedConversation.userName }}</p>
            <p class="text-xs text-gray-400">{{ selectedConversation.userEmail }}</p>
          </div>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 overflow-y-auto p-4 bg-[#18191a]" ref="messagesContainer">
        <div v-if="loadingMessages" class="flex items-center justify-center py-8">
          <i class="fas fa-spinner fa-spin text-2xl text-gray-400"></i>
        </div>
        <div v-else-if="messages.length === 0" class="flex items-center justify-center py-8 text-gray-400">
          <p class="text-sm">Chưa có tin nhắn</p>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="message in messages"
            :key="message.id"
            class="flex"
            :class="message.senderType === 'SYSTEM' ? 'justify-end' : 'justify-start'"
          >
            <div class="max-w-md">
              <div class="mb-1">
                <span class="text-sm text-gray-400">{{ message.senderName }}</span>
              </div>
              <div
                class="px-4 py-2 rounded-2xl"
                :class="message.senderType === 'SYSTEM' ? 'bg-[#0084ff] text-white' : 'bg-[#3a3b3c] text-white'"
              >
                <p class="text-sm">{{ message.content }}</p>
                <p class="text-xs opacity-70 mt-1">{{ formatTime(message.createdAt) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="p-3 border-t border-gray-800 bg-[#242526]">
        <form @submit.prevent="sendMessage" class="flex gap-2">
          <input
            v-model="messageInput"
            type="text"
            placeholder="Nhập tin nhắn..."
            class="flex-1 px-4 py-2 bg-[#3a3b3c] border-0 rounded-full text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            :disabled="!messageInput.trim()"
            class="px-6 py-2 bg-blue-500 text-white rounded-full hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Gửi
          </button>
        </form>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="flex-1 flex items-center justify-center bg-[#18191a]">
      <div class="text-center text-gray-400">
        <i class="fas fa-comments text-6xl mb-4"></i>
        <p class="text-lg">Chọn một cuộc trò chuyện để bắt đầu</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatService } from '@/services/chatService'

const conversations = ref([])
const selectedConversation = ref(null)
const messages = ref([])
const messageInput = ref('')
const loading = ref(false)
const loadingMessages = ref(false)
const messagesContainer = ref(null)

// Lấy danh sách conversations
async function loadConversations() {
  loading.value = true
  try {
    const response = await chatService.getConversations()
    if (response && response.success) {
      conversations.value = response.data || []
      console.log('Loaded conversations:', conversations.value.length)
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
  
  try {
    const response = await chatService.getConversationMessages(conv.conversationId)
    if (response && response.success) {
      messages.value = response.data || []
      console.log('Loaded messages:', messages.value.length)
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('Error loading messages:', error)
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

// Gửi tin nhắn
async function sendMessage() {
  if (!messageInput.value.trim() || !selectedConversation.value) return
  
  const content = messageInput.value.trim()
  messageInput.value = ''
  
  try {
    const request = {
      content: content,
      conversationId: selectedConversation.value.conversationId
    }
    
    const response = await chatService.sendMessage(request)
    if (response && response.success) {
      messages.value.push(response.data)
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('Error sending message:', error)
    messageInput.value = content // Restore message
  }
}

// Helper functions
function getInitials(name) {
  if (!name) return '?'
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

function formatTime(datetime) {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadConversations()
  // Auto refresh every 10s
  setInterval(loadConversations, 10000)
})
</script>

