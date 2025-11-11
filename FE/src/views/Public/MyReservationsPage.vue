<template>
  <div class="min-h-screen bg-white">
    <!-- Hero Section -->
    <section class="relative h-64 flex items-center justify-center bg-slate-900">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative z-10 text-center text-white px-4 max-w-3xl mx-auto">
        <h1 class="text-3xl md:text-4xl font-bold mb-3 tracking-tight">Lịch Sử Đặt Bàn</h1>
        <p class="text-base md:text-lg text-slate-200">Quản lý và theo dõi đơn đặt bàn của bạn</p>
      </div>
    </section>

    <!-- Main Content -->
    <section class="py-12 px-4">
      <div class="container mx-auto max-w-6xl">
        <!-- User not logged in -->
        <div v-if="!authStore.isAuthenticated" class="text-center py-20">
          <i class="fas fa-lock text-6xl mb-6 text-slate-400"></i>
          <h2 class="text-2xl md:text-3xl font-bold text-slate-900 mb-3">Vui lòng đăng nhập</h2>
          <p class="text-slate-600 mb-8">Bạn cần đăng nhập để xem lịch sử đặt bàn</p>
          <router-link to="/login" class="bg-slate-900 hover:bg-slate-800 text-white px-8 py-3 rounded-lg font-semibold inline-block transition-colors">
            Đăng nhập ngay
          </router-link>
        </div>

        <!-- User logged in -->
        <div v-else>
          <!-- Loading State -->
          <div v-if="loading" class="text-center py-20">
            <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
            <p class="mt-4 text-slate-600 text-sm">Đang tải...</p>
          </div>

          <!-- No reservations -->
          <div v-else-if="reservations.length === 0" class="text-center py-20">
            <i class="fas fa-clipboard-list text-6xl mb-6 text-slate-300"></i>
            <h2 class="text-2xl md:text-3xl font-bold text-slate-900 mb-3">Chưa có đặt bàn nào</h2>
            <p class="text-slate-600 mb-8">Bạn chưa có lịch sử đặt bàn. Hãy đặt bàn ngay!</p>
            <router-link to="/reservation" class="bg-slate-900 hover:bg-slate-800 text-white px-8 py-3 rounded-lg font-semibold inline-block transition-colors">
              Đặt bàn ngay
            </router-link>
          </div>

          <!-- Reservations List -->
          <div v-else class="space-y-6">
            <div
              v-for="reservation in reservations"
              :key="reservation.id"
              class="bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden"
            >
              <!-- Card Header -->
              <div class="bg-gradient-to-r from-slate-50 to-gray-50 px-6 py-4 border-b border-gray-200">
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                  <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-slate-900 rounded-full flex items-center justify-center text-white font-bold text-lg">
                      {{ reservation.customerName?.charAt(0)?.toUpperCase() || 'U' }}
                    </div>
                    <div>
                      <h3 class="text-lg font-bold text-slate-900">
                        {{ reservation.customerName }}
                      </h3>
                      <p class="text-slate-600 text-sm flex items-center gap-1 mt-0.5">
                        <i class="fas fa-phone text-xs"></i>
                        {{ reservation.customerPhone }}
                      </p>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <div class="text-right hidden sm:block">
                      <p class="text-xs text-slate-500 mb-1">Mã đặt bàn</p>
                      <p class="font-mono text-slate-900 font-bold text-xl">
                        #{{ reservation.id }}
                      </p>
                    </div>
                    <span
                      :class="getStatusBadgeClass(reservation.status)"
                      class="px-4 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap"
                    >
                      {{ getStatusText(reservation.status) }}
                    </span>
                  </div>
                </div>
                <!-- Mobile: Reservation ID -->
                <div class="sm:hidden mt-3 pt-3 border-t border-gray-200 flex items-center gap-2">
                  <p class="text-xs text-slate-500 mb-1">Mã đặt bàn</p>
                  <p class="font-mono text-slate-900 font-bold text-lg">
                    #{{ reservation.id }}
                  </p>
                </div>
              </div>

              <!-- Card Body -->
              <div class="p-6">
                <!-- Main Info Grid -->
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                  <div class="flex items-start gap-3 p-4 bg-blue-50 rounded-lg border border-blue-100">
                    <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                      <i class="fas fa-calendar text-blue-600"></i>
                    </div>
                    <div class="min-w-0">
                      <p class="text-xs text-blue-600 font-medium mb-1">Ngày đặt</p>
                      <p class="font-bold text-slate-900 text-sm">
                        {{ formatDate(reservation.reservationTime || reservation.reservationDateTime) }}
                      </p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3 p-4 bg-purple-50 rounded-lg border border-purple-100">
                    <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                      <i class="fas fa-clock text-purple-600"></i>
                    </div>
                    <div class="min-w-0">
                      <p class="text-xs text-purple-600 font-medium mb-1">Giờ đặt</p>
                      <p class="font-bold text-slate-900 text-sm">
                        {{ formatTime(reservation.reservationTime || reservation.reservationDateTime) }}
                      </p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3 p-4 bg-green-50 rounded-lg border border-green-100">
                    <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                      <i class="fas fa-users text-green-600"></i>
                    </div>
                    <div class="min-w-0">
                      <p class="text-xs text-green-600 font-medium mb-1">Số người</p>
                      <p class="font-bold text-slate-900 text-sm">
                        {{ reservation.numberOfGuests }} người
                      </p>
                    </div>
                  </div>

                  <div class="flex items-start gap-3 p-4 bg-amber-50 rounded-lg border border-amber-100">
                    <div class="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center flex-shrink-0">
                      <i class="fas fa-chair text-amber-600"></i>
                    </div>
                    <div class="min-w-0">
                      <p class="text-xs text-amber-600 font-medium mb-1">Bàn</p>
                      <p class="font-bold text-slate-900 text-sm">
                        <span v-if="reservation.tableNumber">Bàn {{ reservation.tableNumber }}</span>
                        <span v-else-if="reservation.tableId && reservation.tableType === 'vip'">Phòng VIP</span>
                        <span v-else-if="reservation.tableId">Đã phân bàn</span>
                        <span v-else class="text-slate-500 italic">Chưa phân bàn</span>
                      </p>
                    </div>
                  </div>
                </div>

                <!-- Special Requests & Notes -->
                <div v-if="reservation.specialRequests || reservation.notes" class="mb-6">
                  <div v-if="reservation.specialRequests" class="p-4 bg-cyan-50 rounded-lg border border-cyan-100 mb-3">
                    <div class="flex items-start gap-2">
                      <i class="fas fa-star text-cyan-600 mt-0.5"></i>
                      <div class="flex-1">
                        <p class="text-xs text-cyan-600 font-semibold mb-1">Yêu cầu đặc biệt</p>
                        <p class="text-sm text-slate-700">{{ reservation.specialRequests }}</p>
                      </div>
                    </div>
                  </div>
                  <div v-if="reservation.notes && reservation.notes !== reservation.specialRequests" class="p-4 bg-slate-50 rounded-lg border border-gray-100">
                    <div class="flex items-start gap-2">
                      <i class="fas fa-sticky-note text-slate-600 mt-0.5"></i>
                      <div class="flex-1">
                        <p class="text-xs text-slate-600 font-semibold mb-1">Ghi chú</p>
                        <p class="text-sm text-slate-700">{{ reservation.notes }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Footer: Actions & Metadata -->
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pt-4 border-t border-gray-100">
                  <!-- Created At -->
                  <div class="text-sm text-slate-500 flex items-center gap-2">
                    <i class="fas fa-calendar-plus text-xs"></i>
                    <span>Đặt lúc: {{ formatDateTime(reservation.createdAt) }}</span>
                  </div>

                  <!-- Actions -->
                  <div class="flex flex-col sm:flex-row gap-3">
                    <!-- Rating Button -->
                    <button
                      v-if="canRate(reservation) && !reservation.hasRated"
                      @click="openRatingModal(reservation)"
                      class="px-4 py-2 bg-amber-500 hover:bg-amber-600 text-white text-sm rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
                    >
                      <i class="fas fa-star"></i>
                      <span>Đánh giá</span>
                    </button>
                    <div v-else-if="canRate(reservation) && reservation.hasRated" class="px-4 py-2 bg-green-50 border border-green-200 rounded-lg flex items-center justify-center gap-2">
                      <i class="fas fa-check-circle text-green-600"></i>
                      <span class="text-green-700 text-sm font-medium">Đã đánh giá</span>
                      <div class="flex gap-0.5 ml-1">
                        <i v-for="n in 5" :key="n" :class="['text-xs', n <= (reservation.rating || 0) ? 'fas fa-star text-amber-500' : 'far fa-star text-amber-300']"></i>
                      </div>
                    </div>

                    <!-- Cancel Button - Only show for PENDING status -->
                    <template v-if="reservation.status === 'PENDING'">
                      <button
                        @click="cancelReservation(reservation)"
                        class="px-4 py-2 bg-white border-2 border-red-300 text-red-600 hover:bg-red-50 text-sm rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
                      >
                        <i class="fas fa-times"></i>
                        <span>Hủy đặt bàn</span>
                      </button>
                    </template>
                    
                    <!-- Info message for non-cancellable reservations (only show if no rating button) -->
                    <template v-else-if="!canRate(reservation) && reservation.status !== 'CANCELLED' && reservation.status !== 'PENDING'">
                      <div class="px-4 py-2 bg-gray-50 border border-gray-200 text-gray-600 text-sm rounded-lg flex items-center justify-center gap-2">
                        <i class="fas fa-info-circle"></i>
                        <span>{{ getCancelMessage(reservation.status) }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Rating Modal -->
    <div
      v-if="showRatingModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      @click.self="closeRatingModal"
    >
      <div class="bg-white rounded-lg max-w-md w-full p-6 animate-fade-in border border-gray-200 shadow-xl">
        <h3 class="text-xl font-bold text-slate-900 mb-4 text-center">
          Đánh giá trải nghiệm
        </h3>

        <!-- Star Rating -->
        <div class="flex justify-center gap-3 mb-6">
          <button
            v-for="n in 5"
            :key="n"
            @click="ratingForm.rating = n"
            :class="['text-3xl transition-transform hover:scale-125', n <= ratingForm.rating ? 'text-amber-500' : 'text-gray-300']"
          >
            <i :class="n <= ratingForm.rating ? 'fas fa-star' : 'far fa-star'"></i>
          </button>
        </div>

        <!-- Comment -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-slate-700 mb-2">
            Bình luận của bạn
          </label>
          <textarea
            v-model="ratingForm.comment"
            rows="4"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
            placeholder="Chia sẻ trải nghiệm của bạn về nhà hàng..."
          ></textarea>
        </div>

        <!-- Actions -->
        <div class="flex gap-3">
          <button
            @click="closeRatingModal"
            class="flex-1 bg-gray-100 hover:bg-gray-200 text-slate-700 py-2.5 rounded-lg font-medium transition-colors"
          >
            Hủy
          </button>
          <button
            @click="submitRating"
            :disabled="ratingForm.rating === 0 || submittingRating"
            class="flex-1 bg-slate-900 hover:bg-slate-800 text-white py-2.5 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="!submittingRating">Gửi đánh giá</span>
            <span v-else>Đang gửi...</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { reservationService } from '@/services/reservationService'
import { feedbackService } from '@/services/feedbackService'
import { useNotificationStore } from '@/stores/notification'

const authStore = useAuthStore()
const notification = useNotificationStore()

const loading = ref(false)
const reservations = ref([])
const showRatingModal = ref(false)
const selectedReservation = ref(null)
const submittingRating = ref(false)

const ratingForm = ref({
  rating: 0,
  comment: ''
})

// Watch for authentication changes
watch(() => authStore.isAuthenticated, (isAuthenticated) => {
  if (isAuthenticated) {
    loadReservations()
  } else {
    reservations.value = []
  }
})

onMounted(() => {
  // Initialize auth store if needed
  if (!authStore.user && localStorage.getItem('token')) {
    authStore.initAuth()
  }
  
  if (authStore.isAuthenticated) {
    loadReservations()
  }
})

async function loadReservations() {
  loading.value = true
  try {
    // Get user ID from auth store - try multiple possible fields
    const user = authStore.user
    const userId = user?.id || user?.userId || user?.user?.id
    
    if (!userId) {
      console.error('User ID not found in auth store. User object:', user)
      notification.error('Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.')
      reservations.value = []
      return
    }

    console.log('Loading reservations for user ID:', userId)
    const response = await reservationService.getByUserId(userId)
    console.log('Reservations API response:', response)
    
    // Handle both wrapped and unwrapped responses
    let reservationsData = []
    if (response && response.success && response.data) {
      reservationsData = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      // If response is directly an array
      reservationsData = response
    } else if (response && response.data && Array.isArray(response.data)) {
      reservationsData = response.data
    } else if (response && Array.isArray(response)) {
      reservationsData = response
    }
    
    console.log('Parsed reservations data:', reservationsData)
    
    // Sort by reservation date (newest first)
    reservations.value = reservationsData.sort((a, b) => {
      const dateA = new Date(a.reservationTime || a.reservationDateTime || 0)
      const dateB = new Date(b.reservationTime || b.reservationDateTime || 0)
      return dateB - dateA
    })
    
    console.log('Final reservations count:', reservations.value.length)
  } catch (error) {
    console.error('Error loading reservations:', error)
    console.error('Error response:', error.response?.data)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể tải lịch sử đặt bàn'
    notification.error(errorMessage)
    reservations.value = []
  } finally {
    loading.value = false
  }
}

function getStatusBadgeClass(status) {
  const classes = {
    PENDING: 'bg-amber-100 text-amber-800 border border-amber-200',
    CONFIRMED: 'bg-green-100 text-green-800 border border-green-200',
    COMPLETED: 'bg-blue-100 text-blue-800 border border-blue-200',
    CANCELLED: 'bg-red-100 text-red-800 border border-red-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800 border border-gray-200'
}

function getStatusText(status) {
  const texts = {
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy'
  }
  return texts[status] || status
}

function getCancelMessage(status) {
  const messages = {
    CONFIRMED: 'Đã xác nhận, không thể hủy',
    COMPLETED: 'Đã hoàn thành, không thể hủy',
    CANCELLED: 'Đã hủy'
  }
  return messages[status] || 'Không thể hủy đặt bàn này'
}

function formatDate(dateTime) {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch (error) {
    return ''
  }
}

function formatTime(dateTime) {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return ''
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return ''
  }
}

function canRate(reservation) {
  // Can rate if status is COMPLETED and reservation date has passed
  if (reservation.status !== 'COMPLETED') return false
  try {
    const reservationDate = new Date(reservation.reservationTime || reservation.reservationDateTime)
    const now = new Date()
    return reservationDate < now
  } catch (error) {
    return false
  }
}

function openRatingModal(reservation) {
  selectedReservation.value = reservation
  ratingForm.value = {
    rating: 0,
    comment: ''
  }
  showRatingModal.value = true
}

function closeRatingModal() {
  showRatingModal.value = false
  selectedReservation.value = null
  ratingForm.value = {
    rating: 0,
    comment: ''
  }
}

async function submitRating() {
  if (ratingForm.value.rating === 0) {
    notification.error('Vui lòng chọn số sao')
    return
  }

  submittingRating.value = true
  try {
    const feedbackData = {
      reservationId: selectedReservation.value.id,
      rating: ratingForm.value.rating,
      comment: ratingForm.value.comment,
      customerName: selectedReservation.value.customerName
    }

    const response = await feedbackService.create(feedbackData)
    
    if (response.success) {
      notification.success('Cảm ơn bạn đã đánh giá!')
      
      // Update reservation in local state
      const index = reservations.value.findIndex(r => r.id === selectedReservation.value.id)
      if (index !== -1) {
        reservations.value[index].hasRated = true
        reservations.value[index].rating = ratingForm.value.rating
      }
      
      closeRatingModal()
    }
  } catch (error) {
    notification.error('Không thể gửi đánh giá, vui lòng thử lại')
  } finally {
    submittingRating.value = false
  }
}

async function cancelReservation(reservation) {
  // Validate: Only PENDING reservations can be cancelled
  if (!reservation) {
    notification.error('Không tìm thấy thông tin đặt bàn')
    return
  }

  if (reservation.status !== 'PENDING') {
    notification.error(`Không thể hủy đặt bàn với trạng thái "${getStatusText(reservation.status)}"`)
    return
  }

  // Get userId from authStore
  const user = authStore.user
  const userId = user?.id || user?.userId || user?.user?.id || reservation.customerId

  if (!userId) {
    notification.error('Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.')
    return
  }

  if (!confirm('Bạn có chắc muốn hủy đặt bàn này?')) return

  try {
    const response = await reservationService.cancelPublic(reservation.id, userId)
    if (response.success) {
      notification.success('Đã hủy đặt bàn thành công')
      loadReservations()
    } else {
      notification.error(response.message || 'Không thể hủy đặt bàn')
    }
  } catch (error) {
    console.error('Error canceling reservation:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể hủy đặt bàn'
    notification.error(errorMessage)
  }
}
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}
</style>
