<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Hero Section -->
    <section class="relative h-64 flex items-center justify-center bg-gradient-to-br from-sky-600 to-blue-700">
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-4xl md:text-5xl font-bold mb-2">Lịch Sử Đặt Bàn</h1>
        <p class="text-lg">Quản lý và theo dõi đơn đặt bàn của bạn</p>
      </div>
    </section>

    <!-- Main Content -->
    <section class="py-12 px-4">
      <div class="container mx-auto max-w-6xl">
        <!-- User not logged in -->
        <div v-if="!authStore.isAuthenticated" class="text-center py-20">
          <div class="text-6xl mb-6">🔐</div>
          <h2 class="text-3xl font-bold text-gray-900 mb-4">Vui lòng đăng nhập</h2>
          <p class="text-gray-600 mb-8">Bạn cần đăng nhập để xem lịch sử đặt bàn</p>
          <router-link to="/login" class="btn-primary px-8 py-3 inline-block">
            Đăng nhập ngay
          </router-link>
        </div>

        <!-- User logged in -->
        <div v-else>
          <!-- Loading State -->
          <div v-if="loading" class="text-center py-20">
            <div class="loading-spinner mx-auto"></div>
            <p class="mt-4 text-gray-600">Đang tải...</p>
          </div>

          <!-- No reservations -->
          <div v-else-if="reservations.length === 0" class="text-center py-20">
            <div class="text-6xl mb-6">📋</div>
            <h2 class="text-3xl font-bold text-gray-900 mb-4">Chưa có đặt bàn nào</h2>
            <p class="text-gray-600 mb-8">Bạn chưa có lịch sử đặt bàn. Hãy đặt bàn ngay!</p>
            <router-link to="/reservation" class="btn-primary px-8 py-3 inline-block">
              Đặt bàn ngay
            </router-link>
          </div>

          <!-- Reservations List -->
          <div v-else class="space-y-6">
            <div
              v-for="reservation in reservations"
              :key="reservation.id"
              class="card hover:shadow-2xl transition-all duration-300"
            >
              <div class="flex flex-col md:flex-row justify-between gap-6">
                <!-- Left Side: Info -->
                <div class="flex-1">
                  <div class="flex items-start justify-between mb-4">
                    <div>
                      <h3 class="text-xl font-bold text-gray-900 mb-1">
                        {{ reservation.customerName }}
                      </h3>
                      <p class="text-gray-600">{{ reservation.customerPhone }}</p>
                    </div>
                    <span
                      :class="getStatusBadgeClass(reservation.status)"
                      class="badge"
                    >
                      {{ getStatusText(reservation.status) }}
                    </span>
                  </div>

                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="flex items-center gap-3">
                      <span class="text-2xl">📅</span>
                      <div>
                        <p class="text-sm text-gray-500">Ngày đặt</p>
                        <p class="font-semibold text-gray-900">
                          {{ formatDate(reservation.reservationDateTime) }}
                        </p>
                      </div>
                    </div>

                    <div class="flex items-center gap-3">
                      <span class="text-2xl">🕐</span>
                      <div>
                        <p class="text-sm text-gray-500">Giờ đặt</p>
                        <p class="font-semibold text-gray-900">
                          {{ formatTime(reservation.reservationDateTime) }}
                        </p>
                      </div>
                    </div>

                    <div class="flex items-center gap-3">
                      <span class="text-2xl">👥</span>
                      <div>
                        <p class="text-sm text-gray-500">Số người</p>
                        <p class="font-semibold text-gray-900">
                          {{ reservation.numberOfGuests }} người
                        </p>
                      </div>
                    </div>

                    <div class="flex items-center gap-3">
                      <span class="text-2xl">🪑</span>
                      <div>
                        <p class="text-sm text-gray-500">Loại bàn</p>
                        <p class="font-semibold text-gray-900">
                          {{ reservation.tableType === 'vip' ? 'Phòng VIP' : 'Bàn thường' }}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div v-if="reservation.notes" class="mt-4 p-3 bg-gray-50 rounded-lg">
                    <p class="text-sm text-gray-700">
                      <span class="font-semibold">Ghi chú:</span> {{ reservation.notes }}
                    </p>
                  </div>
                </div>

                <!-- Right Side: Actions -->
                <div class="flex flex-col justify-between items-end gap-4 min-w-[200px]">
                  <div class="text-right">
                    <p class="text-sm text-gray-500">Mã đặt bàn</p>
                    <p class="font-mono text-sky-600 font-bold">
                      #{{ reservation.id }}
                    </p>
                  </div>

                  <!-- Rating Section -->
                  <div v-if="canRate(reservation)" class="w-full">
                    <button
                      v-if="!reservation.hasRated"
                      @click="openRatingModal(reservation)"
                      class="w-full btn-primary text-sm py-2"
                    >
                      ⭐ Đánh giá
                    </button>
                    <div v-else class="text-center">
                      <p class="text-sm text-green-600 font-semibold">✓ Đã đánh giá</p>
                      <div class="flex justify-center gap-1 mt-1">
                        <span v-for="n in 5" :key="n" class="text-yellow-400">
                          {{ n <= (reservation.rating || 0) ? '★' : '☆' }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <button
                    v-if="reservation.status === 'PENDING'"
                    @click="cancelReservation(reservation.id)"
                    class="w-full btn-secondary text-sm py-2 hover:bg-red-100 hover:text-red-700"
                  >
                    Hủy đặt bàn
                  </button>
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
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeRatingModal"
    >
      <div class="bg-white rounded-2xl max-w-md w-full p-8 animate-fade-in">
        <h3 class="text-2xl font-bold text-gray-900 mb-4 text-center">
          Đánh giá trải nghiệm
        </h3>

        <!-- Star Rating -->
        <div class="flex justify-center gap-2 mb-6">
          <button
            v-for="n in 5"
            :key="n"
            @click="ratingForm.rating = n"
            class="text-4xl transition-transform hover:scale-125"
          >
            {{ n <= ratingForm.rating ? '★' : '☆' }}
          </button>
        </div>

        <!-- Comment -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Bình luận của bạn
          </label>
          <textarea
            v-model="ratingForm.comment"
            rows="4"
            class="input-field"
            placeholder="Chia sẻ trải nghiệm của bạn về nhà hàng..."
          ></textarea>
        </div>

        <!-- Actions -->
        <div class="flex gap-3">
          <button
            @click="closeRatingModal"
            class="flex-1 btn-secondary"
          >
            Hủy
          </button>
          <button
            @click="submitRating"
            :disabled="ratingForm.rating === 0 || submittingRating"
            class="flex-1 btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
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
import { ref, onMounted } from 'vue'
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

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadReservations()
  }
})

async function loadReservations() {
  loading.value = true
  try {
    const response = await reservationService.getAll()
    if (response.success) {
      // Filter reservations by current user phone or email if available
      const userPhone = authStore.user?.phone
      const userEmail = authStore.user?.email
      
      reservations.value = response.data
        .filter(r => 
          r.customerPhone === userPhone || 
          r.customerEmail === userEmail
        )
        .sort((a, b) => new Date(b.reservationDateTime) - new Date(a.reservationDateTime))
    }
  } catch (error) {
    notification.error('Không thể tải lịch sử đặt bàn')
  } finally {
    loading.value = false
  }
}

function getStatusBadgeClass(status) {
  const classes = {
    PENDING: 'badge-warning',
    CONFIRMED: 'badge-success',
    COMPLETED: 'badge-info',
    CANCELLED: 'badge-danger'
  }
  return classes[status] || 'badge'
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

function formatDate(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatTime(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

function canRate(reservation) {
  // Can rate if status is COMPLETED and reservation date has passed
  if (reservation.status !== 'COMPLETED') return false
  const reservationDate = new Date(reservation.reservationDateTime)
  const now = new Date()
  return reservationDate < now
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

async function cancelReservation(id) {
  if (!confirm('Bạn có chắc muốn hủy đặt bàn này?')) return

  try {
    const response = await reservationService.update(id, { status: 'CANCELLED' })
    if (response.success) {
      notification.success('Đã hủy đặt bàn')
      loadReservations()
    }
  } catch (error) {
    notification.error('Không thể hủy đặt bàn')
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

.text-yellow-400 {
  color: #fbbf24;
}
</style>
