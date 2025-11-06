<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Back Button & Header -->
    <div class="flex items-center gap-4">
      <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-lg transition">
        <ArrowLeftIcon class="w-6 h-6 text-gray-600" />
      </button>
      <div class="flex-1">
        <h1 class="text-3xl font-bold text-gray-900">Chi tiết Khách hàng</h1>
        <p class="text-gray-600 mt-1">Thông tin và lịch sử đặt bàn</p>
      </div>
      <button @click="showEditModal = true" class="btn-primary flex items-center gap-2">
        <PencilIcon class="w-5 h-5" />
        Chỉnh sửa
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <div v-else-if="customer" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Customer Info Card -->
      <div class="lg:col-span-1 space-y-6">
        <!-- Profile Card -->
        <div class="card">
          <div class="text-center mb-6">
            <div class="w-24 h-24 mx-auto bg-gradient-to-br from-red-500 to-red-600 rounded-full flex items-center justify-center mb-4">
              <span class="text-4xl font-bold text-white">{{ customerInitial }}</span>
            </div>
            <h2 class="text-2xl font-bold text-gray-900">{{ customer.fullName }}</h2>
            <div class="flex items-center justify-center gap-2 mt-2">
              <span 
                v-if="customer.isVip" 
                class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800"
              >
                <StarIcon class="w-4 h-4 mr-1" />
                VIP
              </span>
              <span 
                v-if="customer.blocked"
                class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-red-100 text-red-800"
              >
                <NoSymbolIcon class="w-4 h-4 mr-1" />
                Đã chặn
              </span>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
              <PhoneIcon class="w-5 h-5 text-gray-600 mt-0.5 flex-shrink-0" />
              <div>
                <p class="text-xs text-gray-600">Số điện thoại</p>
                <p class="font-semibold text-gray-900">{{ customer.phone }}</p>
              </div>
            </div>

            <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
              <EnvelopeIcon class="w-5 h-5 text-gray-600 mt-0.5 flex-shrink-0" />
              <div>
                <p class="text-xs text-gray-600">Email</p>
                <p class="font-semibold text-gray-900">{{ customer.email || 'Chưa cập nhật' }}</p>
              </div>
            </div>

            <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
              <MapPinIcon class="w-5 h-5 text-gray-600 mt-0.5 flex-shrink-0" />
              <div>
                <p class="text-xs text-gray-600">Địa chỉ</p>
                <p class="font-semibold text-gray-900">{{ customer.address || 'Chưa cập nhật' }}</p>
              </div>
            </div>

            <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
              <CalendarIcon class="w-5 h-5 text-gray-600 mt-0.5 flex-shrink-0" />
              <div>
                <p class="text-xs text-gray-600">Ngày tạo</p>
                <p class="font-semibold text-gray-900">{{ formatDate(customer.createdAt) }}</p>
              </div>
            </div>
          </div>

          <!-- Notes -->
          <div v-if="customer.notes" class="mt-4 p-3 bg-yellow-50 border-l-4 border-yellow-400 rounded">
            <p class="text-sm text-yellow-700">
              <strong>Ghi chú:</strong> {{ customer.notes }}
            </p>
          </div>
        </div>

        <!-- Stats Card -->
        <div class="card">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Thống kê</h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
              <span class="text-gray-700 font-medium">Tổng đặt bàn</span>
              <span class="text-2xl font-bold text-blue-600">{{ reservations.length }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span class="text-gray-700 font-medium">Hoàn thành</span>
              <span class="text-2xl font-bold text-green-600">{{ completedCount }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-red-50 rounded-lg">
              <span class="text-gray-700 font-medium">Đã hủy</span>
              <span class="text-2xl font-bold text-red-600">{{ cancelledCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Reservation History -->
      <div class="lg:col-span-2">
        <div class="card">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-900">Lịch sử Đặt bàn</h3>
            <button @click="loadReservations" class="btn-secondary btn-sm flex items-center gap-2">
              <ArrowPathIcon class="w-4 h-4" />
              Làm mới
            </button>
          </div>

          <!-- Timeline -->
          <div v-if="reservations.length > 0" class="space-y-4">
            <div 
              v-for="reservation in reservations" 
              :key="reservation.id"
              class="relative pl-8 pb-6 border-l-2 border-gray-200 last:border-l-0 last:pb-0"
            >
              <!-- Timeline dot -->
              <div 
                class="absolute left-0 top-0 -translate-x-1/2 w-4 h-4 rounded-full border-2 border-white"
                :class="getStatusColor(reservation.status)"
              ></div>

              <!-- Content -->
              <div class="bg-white border rounded-lg p-4 hover:shadow-md transition-shadow">
                <div class="flex items-start justify-between mb-3">
                  <div>
                    <div class="flex items-center gap-2 mb-1">
                      <h4 class="font-bold text-gray-900">
                        {{ reservation.tableNumber ? `Bàn ${reservation.tableNumber}` : '' }}
                        {{ reservation.roomName ? `Phòng ${reservation.roomName}` : '' }}
                      </h4>
                      <span 
                        class="px-2 py-1 text-xs font-medium rounded-full"
                        :class="getStatusBadgeClass(reservation.status)"
                      >
                        {{ getStatusText(reservation.status) }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-600">
                      <ClockIcon class="w-4 h-4 inline mr-1" />
                      {{ formatDateTime(reservation.reservationTime) }}
                    </p>
                  </div>
                  <div class="text-right">
                    <p class="text-sm text-gray-600">Số khách</p>
                    <p class="text-lg font-bold text-gray-900 flex items-center gap-1">
                      <UsersIcon class="w-5 h-5" />
                      {{ reservation.numberOfGuests }}
                    </p>
                  </div>
                </div>

                <div v-if="reservation.specialRequests" class="mb-3 p-2 bg-gray-50 rounded text-sm text-gray-700">
                  <strong>Yêu cầu:</strong> {{ reservation.specialRequests }}
                </div>

                <div class="flex items-center justify-between text-xs text-gray-500">
                  <span>Tạo lúc: {{ formatDate(reservation.createdAt) }}</span>
                  <span v-if="reservation.confirmedByName">
                    Xác nhận bởi: {{ reservation.confirmedByName }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="text-center py-12">
            <CalendarDaysIcon class="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p class="text-gray-500 text-lg">Chưa có lịch sử đặt bàn</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else class="card text-center py-12">
      <ExclamationTriangleIcon class="w-16 h-16 text-red-500 mx-auto mb-4" />
      <p class="text-gray-500 text-lg">Không tìm thấy thông tin khách hàng</p>
      <button @click="$router.back()" class="btn-secondary mt-4">
        Quay lại
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  ArrowLeftIcon,
  PencilIcon,
  PhoneIcon,
  EnvelopeIcon,
  MapPinIcon,
  CalendarIcon,
  StarIcon,
  NoSymbolIcon,
  ClockIcon,
  UsersIcon,
  CalendarDaysIcon,
  ArrowPathIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'
import customerService from '@/services/customerService'
import { useNotificationStore } from '@/stores/notification'

const route = useRoute()
const notification = useNotificationStore()

const loading = ref(true)
const customer = ref(null)
const reservations = ref([])
const showEditModal = ref(false)

const customerInitial = computed(() => {
  return customer.value?.fullName?.charAt(0).toUpperCase() || 'K'
})

const completedCount = computed(() => {
  return reservations.value.filter(r => r.status === 'COMPLETED').length
})

const cancelledCount = computed(() => {
  return reservations.value.filter(r => r.status === 'CANCELLED').length
})

async function loadCustomer() {
  try {
    loading.value = true
    const customerId = route.params.id
    const response = await customerService.getById(customerId)
    customer.value = response.data.data
  } catch (error) {
    notification.error('Không thể tải thông tin khách hàng')
  } finally {
    loading.value = false
  }
}

async function loadReservations() {
  try {
    const customerId = route.params.id
    const response = await customerService.getReservations(customerId)
    reservations.value = response.data.data
  } catch (error) {
    notification.error('Không thể tải lịch sử đặt bàn')
  }
}

function getStatusColor(status) {
  const colors = {
    PENDING: 'bg-yellow-500',
    CONFIRMED: 'bg-green-500',
    COMPLETED: 'bg-blue-500',
    CANCELLED: 'bg-red-500'
  }
  return colors[status] || 'bg-gray-500'
}

function getStatusBadgeClass(status) {
  const classes = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    COMPLETED: 'bg-blue-100 text-blue-800',
    CANCELLED: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
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

function formatDate(dateString) {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatDateTime(dateString) {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadCustomer()
  loadReservations()
})
</script>