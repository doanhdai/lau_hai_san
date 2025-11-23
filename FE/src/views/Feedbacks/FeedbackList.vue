<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Phản hồi</h1>
        <p class="text-gray-600 mt-1">Đánh giá và phản hồi khách hàng</p>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-yellow-100 text-sm">Đánh giá trung bình</p>
            <p class="text-3xl font-bold mt-1">{{ averageRating.toFixed(1) }} ★</p>
          </div>
          <span class="text-4xl opacity-50">⭐</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Tích cực</p>
            <p class="text-3xl font-bold mt-1">{{ goodFeedbackCount }}</p>
          </div>
          <span class="text-4xl opacity-50">😊</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-red-500 to-red-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-red-100 text-sm">Tiêu cực</p>
            <p class="text-3xl font-bold mt-1">{{ badFeedbackCount }}</p>
          </div>
          <span class="text-4xl opacity-50">😞</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-blue-100 text-sm">Tổng phản hồi</p>
            <p class="text-3xl font-bold mt-1">{{ feedbacks.length }}</p>
          </div>
          <span class="text-4xl opacity-50">💬</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên khách hàng..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Đánh giá</label>
          <select v-model="filterRating" class="input-field">
            <option value="">Tất cả</option>
            <option value="5">5 sao</option>
            <option value="4">4 sao</option>
            <option value="3">3 sao</option>
            <option value="2">2 sao</option>
            <option value="1">1 sao</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
          <input
            v-model="filterDateFrom"
            type="date"
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
          <input
            v-model="filterDateTo"
            type="date"
            class="input-field"
          />
        </div>
        <div class="flex items-end">
          <button @click="loadFeedbacks" class="btn-secondary w-full flex items-center justify-center gap-2">
            <span class="text-lg">🔄</span>
            Làm mới
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Feedbacks List -->
    <div v-else class="space-y-4">
      <div 
        v-for="feedback in filteredFeedbacks" 
        :key="feedback.id"
        class="card hover:shadow-lg transition-all duration-300"
      >
        <div class="space-y-3">
          <!-- Header -->
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
                {{ feedback.customerName ? feedback.customerName.charAt(0) : 'K' }}
              </div>
              <div>
                <h3 class="font-bold text-gray-900">{{ feedback.customerName || 'Khách hàng' }}</h3>
                <p class="text-sm text-gray-500">{{ formatDate(feedback.createdAt || feedback.feedbackDate) }}</p>
              </div>
            </div>
            <div class="flex items-center gap-1">
              <StarIcon 
                v-for="i in 5" 
                :key="i"
                :class="i <= feedback.rating ? 'text-yellow-500 fill-yellow-500' : 'text-gray-300'"
                class="w-5 h-5"
              />
            </div>
          </div>

          <!-- Comment -->
          <div class="bg-gray-50 rounded-lg p-4">
            <p class="text-gray-700">{{ feedback.comment || 'Không có bình luận' }}</p>
          </div>

          <!-- Response -->
          <div v-if="feedback.response" class="bg-blue-50 rounded-lg p-4 ml-8">
            <p class="text-sm font-medium text-blue-900 mb-1">Đã phản hồi:</p>
            <p class="text-gray-700">{{ feedback.response }}</p>
          </div>

          <!-- Actions -->
          <div class="flex gap-2 pt-2 border-t">
            <button 
              @click="deleteFeedback(feedback)" 
              :disabled="deletingFeedbackId === feedback.id"
              class="text-sm text-red-600 hover:text-red-800 font-medium px-3 py-1.5 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 hover:bg-red-50"
            >
              <i v-if="deletingFeedbackId !== feedback.id" class="fas fa-trash text-xs"></i>
              <i v-else class="fas fa-spinner fa-spin text-xs"></i>
              <span>{{ deletingFeedbackId === feedback.id ? 'Đang xóa...' : 'Xóa' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredFeedbacks.length === 0" class="card text-center py-12">
      <span class="text-8xl text-gray-300 block mb-4">💬</span>
      <p class="text-gray-500 text-lg">Không tìm thấy phản hồi nào</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feedbackService } from '@/services/feedbackService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis

const notification = useNotificationStore()

const loading = ref(false)
const feedbacks = ref([])
const searchQuery = ref('')
const filterRating = ref('')
const filterDateFrom = ref('')
const filterDateTo = ref('')
const deletingFeedbackId = ref(null)

const averageRating = computed(() => {
  if (feedbacks.value.length === 0) return 0
  const sum = feedbacks.value.reduce((acc, f) => acc + f.rating, 0)
  return sum / feedbacks.value.length
})

const goodFeedbackCount = computed(() => feedbacks.value.filter(f => f.rating >= 4).length)
const badFeedbackCount = computed(() => feedbacks.value.filter(f => f.rating <= 2).length)

const filteredFeedbacks = computed(() => {
  let result = feedbacks.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(f => 
      (f.customerName && f.customerName.toLowerCase().includes(query)) ||
      (f.comment && f.comment.toLowerCase().includes(query))
    )
  }

  if (filterRating.value) {
    result = result.filter(f => f.rating === parseInt(filterRating.value))
  }

  // Lọc theo khoảng thời gian
  if (filterDateFrom.value || filterDateTo.value) {
    result = result.filter(f => {
      const feedbackDate = f.createdAt || f.feedbackDate
      if (!feedbackDate) return false
      
      try {
        const date = new Date(feedbackDate)
        if (isNaN(date.getTime())) return false
        
        // Chỉ lấy phần ngày (bỏ qua giờ)
        const feedbackDateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate())
        
        if (filterDateFrom.value) {
          const fromDate = new Date(filterDateFrom.value)
          fromDate.setHours(0, 0, 0, 0)
          if (feedbackDateOnly < fromDate) return false
        }
        
        if (filterDateTo.value) {
          const toDate = new Date(filterDateTo.value)
          toDate.setHours(23, 59, 59, 999)
          if (feedbackDateOnly > toDate) return false
        }
        
        return true
      } catch (error) {
        console.error('Error filtering by date:', error)
        return false
      }
    })
  }

  return result.sort((a, b) => {
    const dateA = new Date(a.createdAt || a.feedbackDate || 0)
    const dateB = new Date(b.createdAt || b.feedbackDate || 0)
    return dateB - dateA
  })
})

onMounted(() => {
  loadFeedbacks()
})

async function loadFeedbacks() {
  loading.value = true
  try {
    const response = await feedbackService.getAll()
    if (response.success) {
      feedbacks.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách phản hồi')
  } finally {
    loading.value = false
  }
}

async function respondToFeedback(feedback) {
  const response = prompt(`Phản hồi cho ${feedback.customerName}:`)
  if (response && response.trim()) {
    try {
      await feedbackService.respond(feedback.id, response)
      notification.success('Đã gửi phản hồi')
      loadFeedbacks()
    } catch (error) {
      notification.error('Không thể gửi phản hồi')
    }
  }
}

async function deleteFeedback(feedback) {
  if (!feedback || !feedback.id) {
    notification.error('Không tìm thấy thông tin phản hồi')
    return
  }

  // Confirmation dialog với thông tin chi tiết
  const confirmMessage = `Bạn có chắc muốn xóa phản hồi của "${feedback.customerName || 'Khách hàng'}"?\n\n` +
    `Đánh giá: ${feedback.rating} sao\n` +
    `Bình luận: ${feedback.comment ? (feedback.comment.length > 50 ? feedback.comment.substring(0, 50) + '...' : feedback.comment) : 'Không có'}\n\n` +
    `Hành động này không thể hoàn tác!`

  if (!confirm(confirmMessage)) {
    return
  }

  deletingFeedbackId.value = feedback.id
  try {
    console.log('Deleting feedback with ID:', feedback.id)
    const response = await feedbackService.delete(feedback.id)
    console.log('Delete response:', response)
    
    // Response từ feedbackService.delete() đã là response.data (ApiResponse)
    // Format: { success: true, message: "...", data: null }
    if (response && response.success !== false) {
      notification.success(response.message || 'Đã xóa phản hồi thành công')
      
      // Xóa khỏi danh sách ngay lập tức (optimistic update)
      const index = feedbacks.value.findIndex(f => f.id === feedback.id)
      if (index !== -1) {
        feedbacks.value.splice(index, 1)
      }
      
      // Reload để đảm bảo đồng bộ với server
      await loadFeedbacks()
    } else {
      notification.error(response?.message || 'Không thể xóa phản hồi')
    }
  } catch (error) {
    console.error('Error deleting feedback:', error)
    console.error('Error response:', error.response)
    console.error('Error response data:', error.response?.data)
    
    // Xử lý các loại lỗi khác nhau
    let errorMessage = 'Không thể xóa phản hồi. Vui lòng thử lại.'
    
    if (error.response) {
      // Lỗi từ server
      if (error.response.status === 403) {
        errorMessage = 'Bạn không có quyền xóa phản hồi. Chỉ ADMIN và MANAGER mới có thể xóa.'
      } else if (error.response.status === 404) {
        errorMessage = 'Không tìm thấy phản hồi cần xóa.'
      } else if (error.response.status === 401) {
        errorMessage = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
      } else {
        errorMessage = error.response?.data?.message || 
                      error.response?.data?.error || 
                      `Lỗi ${error.response.status}: ${error.response.statusText}`
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    notification.error(errorMessage)
  } finally {
    deletingFeedbackId.value = null
  }
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString('vi-VN')
}
</script>
