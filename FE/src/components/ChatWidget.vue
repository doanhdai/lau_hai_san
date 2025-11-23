<template>
  <div v-if="showChat" class="fixed bottom-4 right-4 z-50 w-96 bg-white rounded-lg shadow-2xl flex flex-col"
       style="height: 600px;">
    <!-- Chat Header -->
    <div class="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
      <div class="flex items-center gap-2">
        <button v-if="currentChat" @click="backToConversations" class="mr-2 hover:text-gray-200">
          <i class="fas fa-arrow-left"></i>
        </button>
        <i class="fas fa-comments"></i>
        <span class="font-semibold">{{ currentChat ? currentChat.otherUserName : 'Chat' }}</span>
        <span v-if="!currentChat && chatStore.unreadCount > 0" class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
          {{ chatStore.unreadCount }}
        </span>
      </div>
      <button @click="closeChat" class="text-white hover:text-gray-200">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- Conversations List -->
    <div v-if="!currentChat" class="flex-1 overflow-y-auto p-4 flex flex-col">
      <!-- Button to start new conversation -->
      <button
        v-if="!showNewConversation"
        @click="showNewConversation = true"
        class="mb-4 w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
      >
        <i class="fas fa-plus"></i>
        <span>Bắt đầu cuộc trò chuyện mới</span>
      </button>

      <!-- New Conversation Form -->
      <div v-if="showNewConversation" class="mb-4 p-3 bg-gray-50 rounded-lg">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-semibold">Chọn người để chat:</span>
          <button @click="showNewConversation = false" class="text-gray-500 hover:text-gray-700">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div v-if="getUserInfo()?.type === 'CUSTOMER'" class="space-y-2">
          <p class="text-xs text-gray-600 mb-2">Bạn sẽ chat với nhân viên hỗ trợ</p>
          <button
            @click="startChatWithStaff"
            class="w-full p-2 bg-white border rounded-lg hover:bg-gray-50 text-left"
          >
            <i class="fas fa-headset mr-2"></i>
            <span>Chat với nhân viên hỗ trợ</span>
          </button>
        </div>
        <div v-else class="space-y-2">
          <input
            v-model="searchUser"
            type="text"
            placeholder="Tìm kiếm khách hàng..."
            class="w-full px-3 py-2 border rounded-lg text-sm"
          />
          <div v-if="availableUsers.length > 0" class="max-h-40 overflow-y-auto space-y-1">
            <button
              v-for="user in availableUsers"
              :key="`${user.type}-${user.id}`"
              @click="startChatWithUser(user)"
              class="w-full p-2 bg-white border rounded-lg hover:bg-gray-50 text-left text-sm"
            >
              <i class="fas fa-user mr-2"></i>
              <span>{{ user.name }}</span>
            </button>
          </div>
          <p v-else class="text-xs text-gray-500 text-center py-2">
            {{ searchUser ? 'Không tìm thấy' : 'Nhập tên để tìm kiếm' }}
          </p>
        </div>
      </div>

      <!-- Existing Conversations -->
      <div v-if="chatStore.loading" class="text-center py-8 text-gray-500">
        <i class="fas fa-spinner fa-spin text-2xl mb-2"></i>
        <p>Đang tải...</p>
      </div>
      <div v-else-if="chatStore.conversations.length === 0" class="text-center py-8 text-gray-500">
        <i class="fas fa-comments text-4xl mb-2"></i>
        <p>Chưa có cuộc trò chuyện nào</p>
      </div>
      <div v-else class="space-y-2">
        <button
          v-for="conv in chatStore.conversations"
          :key="`${conv.otherUserType}-${conv.otherUserId}`"
          @click="openConversation(conv.otherUserType, conv.otherUserId, conv.otherUserName)"
          class="w-full p-3 rounded-lg border hover:bg-gray-50 transition-colors text-left"
          :class="{ 'bg-blue-50 border-blue-300': currentChat?.otherUserId === conv.otherUserId }"
        >
          <div class="flex items-center justify-between">
            <div class="flex-1 min-w-0">
              <p class="font-semibold text-sm truncate">{{ conv.otherUserName }}</p>
              <p v-if="conv.lastMessage" class="text-xs text-gray-500 truncate mt-1">
                {{ conv.lastMessage.content }}
              </p>
            </div>
            <div class="ml-2 flex flex-col items-end">
              <span v-if="conv.unreadCount > 0" class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full mb-1">
                {{ conv.unreadCount }}
              </span>
              <span v-if="conv.lastMessageTime" class="text-xs text-gray-400">
                {{ formatTime(conv.lastMessageTime) }}
              </span>
            </div>
          </div>
        </button>
      </div>
    </div>

    <!-- Chat Messages -->
    <div v-else class="flex-1 flex flex-col">
      <!-- Messages List -->
      <div class="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
        <div v-if="chatStore.loading" class="text-center py-8 text-gray-500">
          <i class="fas fa-spinner fa-spin text-2xl mb-2"></i>
          <p>Đang tải tin nhắn...</p>
        </div>
        <div v-else>
          <div
            v-for="message in chatStore.messages"
            :key="message.id"
            class="flex"
            :class="isMyMessage(message) ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg"
              :class="isMyMessage(message) 
                ? 'bg-blue-600 text-white' 
                : 'bg-white text-gray-800 border'"
            >
              <p class="text-sm">{{ message.content }}</p>
              <p class="text-xs mt-1 opacity-70">
                {{ formatTime(message.createdAt) }}
                <span v-if="isMyMessage(message) && message.isRead" class="ml-1">
                  <i class="fas fa-check-double"></i>
                </span>
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
            :disabled="chatStore.loading"
          />
          <button
            type="submit"
            :disabled="!messageInput.trim() || chatStore.loading"
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
      v-if="chatStore.unreadCount > 0"
      class="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-6 h-6 rounded-full flex items-center justify-center"
    >
      {{ chatStore.unreadCount > 9 ? '9+' : chatStore.unreadCount }}
    </span>
  </button>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/userService'
import { customerService } from '@/services/customerService'

const chatStore = useChatStore()
const authStore = useAuthStore()

const showChat = ref(false)
const currentChat = ref(null)
const messageInput = ref('')
const showNewConversation = ref(false)
const searchUser = ref('')
const availableUsers = ref([])
const loadingUsers = ref(false)

const isMyMessage = (message) => {
  const userInfo = getUserInfo()
  return message.senderType === userInfo?.type && message.senderId === userInfo?.id
}

const getUserInfo = () => {
  const user = authStore.user
  if (!user) return null
  
  if (user.roles?.includes('ROLE_ADMIN') || user.roles?.includes('ROLE_MANAGER') || user.roles?.includes('ROLE_STAFF')) {
    return { type: 'STAFF', id: user.id }
  }
  return { type: 'CUSTOMER', id: user.id }
}

function openChat() {
  showChat.value = true
  chatStore.loadConversations()
}

function closeChat() {
  showChat.value = false
  currentChat.value = null
}

function openConversation(otherUserType, otherUserId, otherUserName) {
  currentChat.value = { otherUserType, otherUserId, otherUserName }
  chatStore.loadConversation(otherUserType, otherUserId)
}

function backToConversations() {
  currentChat.value = null
  showNewConversation.value = false
  chatStore.loadConversations()
}

// Tìm kiếm users/customers để bắt đầu chat
watch(searchUser, async (newValue) => {
  if (!newValue || newValue.length < 2) {
    availableUsers.value = []
    return
  }

  loadingUsers.value = true
  try {
    const userInfo = getUserInfo()
    if (userInfo?.type === 'STAFF') {
      // Staff tìm kiếm customers
      const response = await customerService.getAll()
      if (response && response.success) {
        const customers = response.data || []
        availableUsers.value = customers
          .filter(c => 
            c.fullName?.toLowerCase().includes(newValue.toLowerCase()) ||
            c.phone?.includes(newValue) ||
            c.email?.toLowerCase().includes(newValue.toLowerCase())
          )
          .slice(0, 10)
          .map(c => ({
            type: 'CUSTOMER',
            id: c.id,
            name: c.fullName || c.phone || 'Khách hàng'
          }))
      }
    } else {
      // Customer có thể chat với staff đầu tiên
      availableUsers.value = []
    }
  } catch (error) {
    console.error('Error searching users:', error)
    availableUsers.value = []
  } finally {
    loadingUsers.value = false
  }
})

// Bắt đầu chat với staff (cho customer)
async function startChatWithStaff() {
  try {
    // Lấy staff đầu tiên (hoặc có thể lấy từ danh sách)
    const response = await userService.getAll({ page: 0, size: 1 })
    if (response && response.success) {
      const users = response.data?.content || response.data || []
      const staff = users.find(u => 
        u.roles?.some(r => r.name === 'ROLE_ADMIN' || r.name === 'ROLE_MANAGER' || r.name === 'ROLE_STAFF')
      ) || users[0]
      
      if (staff) {
        openConversation('STAFF', staff.id, staff.fullName || 'Nhân viên hỗ trợ')
        showNewConversation.value = false
      }
    }
  } catch (error) {
    console.error('Error starting chat with staff:', error)
    // Fallback: tạo conversation với ID mặc định (có thể là admin)
    openConversation('STAFF', 1, 'Nhân viên hỗ trợ')
    showNewConversation.value = false
  }
}

// Bắt đầu chat với user được chọn
function startChatWithUser(user) {
  openConversation(user.type, user.id, user.name)
  showNewConversation.value = false
  searchUser.value = ''
  availableUsers.value = []
}

async function sendMessage() {
  if (!messageInput.value.trim() || !currentChat.value) return
  
  const messageText = messageInput.value.trim()
  messageInput.value = '' // Clear input immediately for better UX
  
  try {
    await chatStore.sendMessage(
      messageText,
      currentChat.value.otherUserType,
      currentChat.value.otherUserId
    )
    
    // Scroll to bottom after sending
    setTimeout(() => {
      const messagesContainer = document.querySelector('.overflow-y-auto')
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight
      }
    }, 100)
  } catch (error) {
    console.error('Error sending message:', error)
    // Restore message text if sending failed
    messageInput.value = messageText
  }
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
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Watch for new messages and scroll to bottom
watch(() => chatStore.messages, () => {
  setTimeout(() => {
    const messagesContainer = document.querySelector('.overflow-y-auto')
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight
    }
  }, 100)
}, { deep: true })

onMounted(() => {
  // Chỉ start chat nếu user đã đăng nhập
  if (authStore.isAuthenticated && authStore.user) {
    chatStore.start()
  }
})

onUnmounted(() => {
  chatStore.stop()
})
</script>

